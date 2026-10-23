package pe.edu.upeu.lamb.matricula.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lamb.matricula.dto.*;
import pe.edu.upeu.lamb.matricula.entity.DetalleMatricula;
import pe.edu.upeu.lamb.matricula.entity.EstadoMatricula;
import pe.edu.upeu.lamb.matricula.entity.Matricula;
import pe.edu.upeu.lamb.matricula.event.MatriculaCreadaEvento;
import pe.edu.upeu.lamb.matricula.exception.ResourceNotFoundException;
import pe.edu.upeu.lamb.matricula.messaging.MatriculaEventosProducer;
import pe.edu.upeu.lamb.matricula.messaging.MatriculaPagadaProducer;
import pe.edu.upeu.lamb.matricula.event.MatriculaPagadaEvento;
import pe.edu.upeu.lamb.matricula.repository.MatriculaRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final PlanificacionConsultaService planificacionConsultaService;
    private final MatriculaEventosProducer producer;
    private final MatriculaPagadaProducer pagadaProducer;

    @Value("${app.matricula.tarifa-credito:50.00}")
    private BigDecimal tarifaCredito;

    @Value("${app.matricula.metodo-pago-defecto:TARJETA}")
    private String metodoPagoDefecto;

    @Override
    @Transactional
    public MatriculaResponse crear(MatriculaRequest request, Long idPersona) {
        Matricula matricula = Matricula.builder()
                .idPersona(idPersona)
                .periodo(request.getPeriodo())
                .build();

        List<DetalleMatricula> detalles = new ArrayList<>();

        for (DetalleMatriculaRequest item : request.getDetalles()) {
            SeccionDto seccion = planificacionConsultaService.consultarSeccion(item.getIdSeccion());
            CursoDto curso = (seccion == null)
                    ? null
                    : planificacionConsultaService.consultarCurso(seccion.getIdCurso());

            Integer creditos = (curso == null) ? null : curso.getCreditos();

            detalles.add(DetalleMatricula.builder()
                    .matricula(matricula)
                    .idSeccion(item.getIdSeccion())
                    .creditos(creditos)
                    .build());
        }

        matricula.setDetalles(detalles);
        // Estado inicial REGISTRADA: si alguna seccion no se pudo validar, no se puede cobrar todavia.
        Matricula guardada = matriculaRepository.save(matricula);
        MatriculaResponse respuesta = toResponse(guardada);
        log.info("component=service action=crear matriculaId={} idPersona={} periodo={} estado={} totalCreditos={}",
                guardada.getId(), guardada.getIdPersona(), guardada.getPeriodo(), respuesta.getEstado(), respuesta.getTotalCreditos());

        if (respuesta.getTotalCreditos() != null) {
            BigDecimal monto = tarifaCredito.multiply(BigDecimal.valueOf(respuesta.getTotalCreditos()));
            producer.publicarTrasCommit(MatriculaCreadaEvento.builder()
                    .tipoEvento("matricula.creada")
                    .matriculaId(guardada.getId())
                    .idPersona(guardada.getIdPersona())
                    .monto(monto)
                    .metodoPago(metodoPagoDefecto)
                    .origen("lamb-matricula-ms")
                    .timestamp(Instant.now().toEpochMilli())
                    .build());
        }
        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponse findById(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponse> listar() {
        return matriculaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MatriculaResponse confirmar(Long id) {
        Matricula matricula = buscar(id);
        if (matricula.getEstado() != EstadoMatricula.REGISTRADA) {
            throw new IllegalStateException("Solo se puede confirmar una matricula REGISTRADA");
        }
        boolean sinValidar = matricula.getDetalles().stream().anyMatch(d -> d.getCreditos() == null);
        if (sinValidar) {
            throw new IllegalStateException("No se puede confirmar: hay secciones sin validar en Planificacion");
        }
        matricula.setEstado(EstadoMatricula.CONFIRMADA);
        return toResponse(matriculaRepository.save(matricula));
    }

    @Override
    @Transactional
    public MatriculaResponse anular(Long id) {
        Matricula matricula = buscar(id);
        if (matricula.getEstado() != EstadoMatricula.REGISTRADA) {
            throw new IllegalStateException("Solo se puede anular una matricula REGISTRADA");
        }
        matricula.setEstado(EstadoMatricula.ANULADA);
        return toResponse(matriculaRepository.save(matricula));
    }

    @Override
    @Transactional
    public void marcarPagada(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId).orElse(null);
        if (matricula == null || matricula.getEstado() != EstadoMatricula.REGISTRADA) {
            log.warn("component=processor matriculaId={} status=ignored motivo=\"la matricula no existe o no esta REGISTRADA\"", matriculaId);
            return;
        }
        matricula.setEstado(EstadoMatricula.PAGADA);
        matriculaRepository.save(matricula);
        pagadaProducer.publicarTrasCommit(MatriculaPagadaEvento.builder()
                .tipoEvento("matricula.pagada")
                .matriculaId(matricula.getId())
                .idPersona(matricula.getIdPersona())
                .periodo(matricula.getPeriodo())
                .secciones(matricula.getDetalles().stream().map(d -> d.getIdSeccion()).toList())
                .origen("lamb-matricula-ms")
                .timestamp(java.time.Instant.now().toEpochMilli())
                .build());
        log.info("component=processor matriculaId={} estado={} status=processed", matriculaId, matricula.getEstado());
    }

    private Matricula buscar(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matricula no encontrada: " + id));
    }

    private MatriculaResponse toResponse(Matricula m) {
        List<DetalleMatriculaResponse> detalles = m.getDetalles().stream()
                .map(d -> DetalleMatriculaResponse.builder()
                        .idSeccion(d.getIdSeccion())
                        .creditos(d.getCreditos())
                        .build())
                .toList();

        boolean completa = m.getDetalles().stream().allMatch(d -> d.getCreditos() != null);
        Integer total = completa
                ? m.getDetalles().stream().mapToInt(DetalleMatricula::getCreditos).sum()
                : null;

        return MatriculaResponse.builder()
                .id(m.getId())
                .idPersona(m.getIdPersona())
                .periodo(m.getPeriodo())
                .fecha(m.getFecha())
                .estado(m.getEstado().name())
                .totalCreditos(total)
                .detalles(detalles)
                .build();
    }
}