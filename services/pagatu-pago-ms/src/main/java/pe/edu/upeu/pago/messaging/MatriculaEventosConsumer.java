package pe.edu.upeu.pago.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.edu.upeu.pago.event.MatriculaCreadaEvento;
import pe.edu.upeu.pago.service.PagoService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatriculaEventosConsumer {

    private static final String MATRICULA_CREADA = "matricula.creada";

    private final PagoService pagoService;

    @KafkaListener(topics = "${app.kafka.topic.matriculas}")
    public void alRecibirMatricula(MatriculaCreadaEvento evento) {
        if (!MATRICULA_CREADA.equals(evento.getTipoEvento())) {
            log.warn("component=consumer eventType={} status=ignored", evento.getTipoEvento());
            return;
        }
        log.info("component=consumer eventType={} matriculaId={} status=consumed",
                evento.getTipoEvento(), evento.getMatriculaId());
        pagoService.procesar(evento);
    }
}