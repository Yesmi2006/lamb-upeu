package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.MatriculaCreadaEvento;
import pe.edu.upeu.pago.event.PagoValidadoEvento;
import pe.edu.upeu.pago.exception.ResourceNotFoundException;
import pe.edu.upeu.pago.messaging.PagoEventosProducer;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final String PAGO_VALIDADO = "pago.validado";

    private final PagoRepository pagoRepository;
    private final PagoEventosProducer producer;

    @Value("${spring.application.name}")
    private String nombreServicio;

    @Override
    @Transactional
    public void procesar(MatriculaCreadaEvento matricula) {
        Pago pago = pagoRepository.save(Pago.builder()
                .matriculaId(matricula.getMatriculaId())
                .monto(matricula.getMonto())
                .metodoPago(matricula.getMetodoPago())
                .estado(EstadoPago.VALIDADO)
                .fechaPago(LocalDateTime.now())
                .build());

        producer.publicarTrasCommit(PagoValidadoEvento.builder()
                .tipoEvento(PAGO_VALIDADO)
                .matriculaId(pago.getMatriculaId())
                .monto(pago.getMonto())
                .estado(pago.getEstado().name())
                .origen(nombreServicio)
                .timestamp(Instant.now().toEpochMilli())
                .build());

        log.info("component=processor matriculaId={} estado={} status=processed", pago.getMatriculaId(), pago.getEstado());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return toResponse(pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id)));
    }

    private PagoResponse toResponse(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .matriculaId(p.getMatriculaId())
                .monto(p.getMonto())
                .metodoPago(p.getMetodoPago())
                .estado(p.getEstado().name())
                .fechaPago(p.getFechaPago())
                .build();
    }
}