package pe.edu.upeu.lamb.matricula.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicMatriculaPagadaConfig {

    @Bean
    public NewTopic matriculaPagadaEventos(@Value("${app.kafka.topic.matriculas-pagadas}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}