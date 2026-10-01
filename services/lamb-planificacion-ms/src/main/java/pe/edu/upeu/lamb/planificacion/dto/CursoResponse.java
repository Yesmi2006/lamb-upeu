package pe.edu.upeu.lamb.planificacion.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private Integer creditos;
}
