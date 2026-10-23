package pe.edu.upeu.lamb.matricula.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.lamb.matricula.event.MatriculaCreadaEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatriculaEventosProducer {

    private final KafkaTemplate<String, MatriculaCreadaEvento> kafkaTemplate;

    @Value("${app.kafka.topic.matriculas}")
    private String topicMatriculas;

    public void publicarTrasCommit(MatriculaCreadaEvento evento) {
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

    private void enviar(MatriculaCreadaEvento evento) {
        kafkaTemplate.send(topicMatriculas, String.valueOf(evento.getMatriculaId()), evento)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error("component=producer topic={} eventType={} matriculaId={} status=error error=\"{}\"",
                                topicMatriculas, evento.getTipoEvento(), evento.getMatriculaId(), ex.getMessage());
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