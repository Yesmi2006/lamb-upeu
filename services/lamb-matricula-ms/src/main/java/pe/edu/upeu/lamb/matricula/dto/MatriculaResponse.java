package pe.edu.upeu.lamb.matricula.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaResponse {
    private Long id;
    private Long idPersona;
    private String periodo;
    private LocalDateTime fecha;
    private String estado;
    private Integer totalCreditos;
    private List<DetalleMatriculaResponse> detalles;
}
