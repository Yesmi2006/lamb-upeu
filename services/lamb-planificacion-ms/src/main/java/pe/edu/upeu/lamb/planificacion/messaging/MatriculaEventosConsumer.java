package pe.edu.upeu.lamb.planificacion.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.lamb.planificacion.event.MatriculaPagadaEvento;
import pe.edu.upeu.lamb.planificacion.service.ReservaCupoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatriculaEventosConsumer {

    private static final String MATRICULA_PAGADA = "matricula.pagada";

    private final ReservaCupoService reservaCupoService;

    @KafkaListener(topics = "${app.kafka.topic.matriculas-pagadas}")
    public void alRecibirMatriculaPagada(MatriculaPagadaEvento evento) {
        if (!MATRICULA_PAGADA.equals(evento.getTipoEvento())) {
            log.warn("component=consumer eventType={} status=ignored", evento.getTipoEvento());
            return;
        }
        log.info("component=consumer eventType={} matriculaId={} status=consumed",
                evento.getTipoEvento(), evento.getMatriculaId());
        reservaCupoService.reservar(evento.getMatriculaId(), evento.getSecciones());
    }
}