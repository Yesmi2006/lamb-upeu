package pe.edu.upeu.lamb.planificacion.service;

import pe.edu.upeu.lamb.planificacion.dto.SeccionRequest;
import pe.edu.upeu.lamb.planificacion.dto.SeccionResponse;
import pe.edu.upeu.lamb.planificacion.entity.Seccion;
import pe.edu.upeu.lamb.planificacion.exception.ResourceNotFoundException;
import pe.edu.upeu.lamb.planificacion.repository.CursoRepository;
import pe.edu.upeu.lamb.planificacion.repository.SeccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final CursoRepository cursoRepository;

    @Transactional
    public SeccionResponse crear(SeccionRequest request) {
        if (!cursoRepository.existsById(request.getIdCurso())) {
            throw new ResourceNotFoundException("Curso no encontrado: " + request.getIdCurso());
        }
        Seccion seccion = Seccion.builder()
                .idCurso(request.getIdCurso())
                .periodo(request.getPeriodo())
                .docente(request.getDocente())
                .horario(request.getHorario())
                .cupoMaximo(request.getCupoMaximo())
                .build();
        return toResponse(seccionRepository.save(seccion));
    }

    @Transactional(readOnly = true)
    public List<SeccionResponse> listar() {
        return seccionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SeccionResponse findById(Long id) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sección no encontrada: " + id));
        return toResponse(seccion);
    }

    private SeccionResponse toResponse(Seccion s) {
        return SeccionResponse.builder()
                .id(s.getId())
                .idCurso(s.getIdCurso())
                .periodo(s.getPeriodo())
                .docente(s.getDocente())
                .horario(s.getHorario())
                .cupoMaximo(s.getCupoMaximo())
                .build();
    }
}
