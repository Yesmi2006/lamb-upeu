package pe.edu.upeu.lamb.matricula.service;

import pe.edu.upeu.lamb.matricula.dto.*;
import pe.edu.upeu.lamb.matricula.entity.DetalleMatricula;
import pe.edu.upeu.lamb.matricula.entity.EstadoMatricula;
import pe.edu.upeu.lamb.matricula.entity.Matricula;
import pe.edu.upeu.lamb.matricula.exception.ResourceNotFoundException;
import pe.edu.upeu.lamb.matricula.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final PlanificacionConsultaService planificacionConsultaService;

    @Override
    @Transactional
    public MatriculaResponse crear(MatriculaRequest request) {
        Matricula matricula = Matricula.builder()
                .idPersona(request.getIdPersona())
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
        // Estado inicial REGISTRADA: si alguna sección no se pudo validar, no se puede confirmar todavía.
        return toResponse(matriculaRepository.save(matricula));
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
            throw new IllegalStateException("Solo se puede confirmar una matrícula REGISTRADA");
        }
        boolean sinValidar = matricula.getDetalles().stream().anyMatch(d -> d.getCreditos() == null);
        if (sinValidar) {
            throw new IllegalStateException("No se puede confirmar: hay secciones sin validar en Planificación");
        }
        matricula.setEstado(EstadoMatricula.CONFIRMADA);
        return toResponse(matriculaRepository.save(matricula));
    }

    @Override
    @Transactional
    public MatriculaResponse anular(Long id) {
        Matricula matricula = buscar(id);
        if (matricula.getEstado() != EstadoMatricula.REGISTRADA) {
            throw new IllegalStateException("Solo se puede anular una matrícula REGISTRADA");
        }
        matricula.setEstado(EstadoMatricula.ANULADA);
        return toResponse(matriculaRepository.save(matricula));
    }

    private Matricula buscar(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matrícula no encontrada: " + id));
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
