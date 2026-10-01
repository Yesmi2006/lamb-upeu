package pe.edu.upeu.lamb.planificacion.service;

import pe.edu.upeu.lamb.planificacion.dto.CursoRequest;
import pe.edu.upeu.lamb.planificacion.dto.CursoResponse;
import pe.edu.upeu.lamb.planificacion.entity.Curso;
import pe.edu.upeu.lamb.planificacion.exception.ResourceNotFoundException;
import pe.edu.upeu.lamb.planificacion.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    @Transactional
    public CursoResponse crear(CursoRequest request) {
        Curso curso = Curso.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .creditos(request.getCreditos())
                .build();
        return toResponse(cursoRepository.save(curso));
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse findById(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + id));
        return toResponse(curso);
    }

    private CursoResponse toResponse(Curso c) {
        return CursoResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .creditos(c.getCreditos())
                .build();
    }
}
