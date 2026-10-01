package pe.edu.upeu.lamb.planificacion.controller;

import pe.edu.upeu.lamb.planificacion.dto.CursoRequest;
import pe.edu.upeu.lamb.planificacion.dto.CursoResponse;
import pe.edu.upeu.lamb.planificacion.service.CursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse crear(@Valid @RequestBody CursoRequest request) {
        return cursoService.crear(request);
    }

    @GetMapping
    public List<CursoResponse> listar() {
        return cursoService.listar();
    }

    @GetMapping("/{id}")
    public CursoResponse findById(@PathVariable Long id) {
        return cursoService.findById(id);
    }
}
