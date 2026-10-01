package pe.edu.upeu.lamb.matricula.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import pe.edu.upeu.lamb.matricula.client.CursoClient;
import pe.edu.upeu.lamb.matricula.client.SeccionClient;
import pe.edu.upeu.lamb.matricula.dto.CursoDto;
import pe.edu.upeu.lamb.matricula.dto.SeccionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bean separado a propósito: @CircuitBreaker solo se aplica cuando la llamada
 * cruza el proxy de otro bean (si estuviera en MatriculaServiceImpl se ignoraría).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanificacionConsultaService {

    private final SeccionClient seccionClient;
    private final CursoClient cursoClient;

    @CircuitBreaker(name = "planificacion", fallbackMethod = "fallbackSeccion")
    public SeccionDto consultarSeccion(Long idSeccion) {
        return seccionClient.findById(idSeccion);
    }

    @CircuitBreaker(name = "planificacion", fallbackMethod = "fallbackCurso")
    public CursoDto consultarCurso(Long idCurso) {
        return cursoClient.findById(idCurso);
    }

    public SeccionDto fallbackSeccion(Long idSeccion, Throwable ex) {
        log.warn("[PLANIFICACION] Fallback activado para idSeccion {}. Motivo: {}", idSeccion, ex.getMessage());
        return null;
    }

    public CursoDto fallbackCurso(Long idCurso, Throwable ex) {
        log.warn("[PLANIFICACION] Fallback activado para idCurso {}. Motivo: {}", idCurso, ex.getMessage());
        return null;
    }
}
