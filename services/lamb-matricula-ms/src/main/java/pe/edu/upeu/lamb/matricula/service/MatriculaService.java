package pe.edu.upeu.lamb.matricula.service;

import pe.edu.upeu.lamb.matricula.dto.MatriculaRequest;
import pe.edu.upeu.lamb.matricula.dto.MatriculaResponse;

import java.util.List;

public interface MatriculaService {
    MatriculaResponse crear(MatriculaRequest request);
    MatriculaResponse findById(Long id);
    List<MatriculaResponse> listar();
    MatriculaResponse confirmar(Long id);
    MatriculaResponse anular(Long id);
}
