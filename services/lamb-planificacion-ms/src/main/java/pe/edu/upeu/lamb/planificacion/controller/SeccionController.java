package pe.edu.upeu.lamb.planificacion.controller;

import pe.edu.upeu.lamb.planificacion.dto.SeccionRequest;
import pe.edu.upeu.lamb.planificacion.dto.SeccionResponse;
import pe.edu.upeu.lamb.planificacion.service.SeccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/secciones")
@RequiredArgsConstructor
public class SeccionController {

    private final SeccionService seccionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeccionResponse crear(@Valid @RequestBody SeccionRequest request) {
        return seccionService.crear(request);
    }

    @GetMapping
    public List<SeccionResponse> listar() {
        return seccionService.listar();
    }

    @GetMapping("/{id}")
    public SeccionResponse findById(@PathVariable Long id) {
        return seccionService.findById(id);
    }
}
