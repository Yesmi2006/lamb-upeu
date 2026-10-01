package pe.edu.upeu.lamb.matricula;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LambMatriculaMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LambMatriculaMsApplication.class, args);
    }
}
