package pe.edu.upeu.lamb.matricula.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.lamb.matricula.dto.MatriculaRequest;
import pe.edu.upeu.lamb.matricula.dto.MatriculaResponse;
import pe.edu.upeu.lamb.matricula.service.MatriculaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse crear(@Valid @RequestBody MatriculaRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        Number claimIdPersona = jwt.getClaim("idPersona");
        if (claimIdPersona == null) {
            throw new IllegalArgumentException("Token sin idPersona: solo un usuario CLIENTE puede registrar matriculas");
        }
        return matriculaService.crear(request, claimIdPersona.longValue());
    }

    @GetMapping
    public List<MatriculaResponse> listar() {
        return matriculaService.listar();
    }

    @GetMapping("/{id}")
    public MatriculaResponse findById(@PathVariable Long id) {
        return matriculaService.findById(id);
    }

    @PostMapping("/{id}/confirmar")
    public MatriculaResponse confirmar(@PathVariable Long id) {
        return matriculaService.confirmar(id);
    }

    @PostMapping("/{id}/anular")
    public MatriculaResponse anular(@PathVariable Long id) {
        return matriculaService.anular(id);
    }
}