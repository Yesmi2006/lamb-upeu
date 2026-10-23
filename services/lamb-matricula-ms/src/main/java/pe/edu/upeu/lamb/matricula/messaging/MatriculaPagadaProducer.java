package pe.edu.upeu.lamb.matricula.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.lamb.matricula.event.MatriculaPagadaEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatriculaPagadaProducer {

    private final KafkaTemplate<String, MatriculaPagadaEvento> kafkaTemplate;

    @Value("${app.kafka.topic.matriculas-pagadas}")
    private String topic;

    public void publicarTrasCommit(MatriculaPagadaEvento evento) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(evento);
                }
            });
        } else {
            enviar(evento);
        }
    }

    private void enviar(MatriculaPagadaEvento evento) {
        kafkaTemplate.send(topic, String.valueOf(evento.getMatriculaId()), evento)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error("component=producer topic={} eventType={} matriculaId={} status=error error=\"{}\"",
                                topic, evento.getTipoEvento(), evento.getMatriculaId(), ex.getMessage());
                        return;
                    }
                    log.info("component=producer topic={} partition={} offset={} eventType={} matriculaId={} status=published",
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(),
                            evento.getTipoEvento(), evento.getMatriculaId());
                });
    }
}