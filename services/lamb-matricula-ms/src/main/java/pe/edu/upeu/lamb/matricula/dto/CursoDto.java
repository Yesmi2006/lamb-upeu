package pe.edu.upeu.lamb.matricula.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursoDto {
    private Long id;
    private String codigo;
    private String nombre;
    private Integer creditos;
}
