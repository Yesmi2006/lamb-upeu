package pe.edu.upeu.lamb.matricula.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMatriculaResponse {
    private Long idSeccion;
    private Integer creditos;
}
