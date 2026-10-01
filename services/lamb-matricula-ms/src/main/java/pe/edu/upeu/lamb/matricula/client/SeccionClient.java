package pe.edu.upeu.lamb.matricula.client;

import pe.edu.upeu.lamb.matricula.dto.SeccionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lamb-planificacion-ms", contextId = "seccionClient")
public interface SeccionClient {

    @GetMapping("/api/v1/secciones/{id}")
    SeccionDto findById(@PathVariable("id") Long id);
}
