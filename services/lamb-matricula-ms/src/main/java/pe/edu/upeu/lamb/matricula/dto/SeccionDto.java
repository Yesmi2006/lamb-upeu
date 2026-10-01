package pe.edu.upeu.lamb.matricula.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionDto {
    private Long id;
    private Long idCurso;
    private String periodo;
    private Integer cupoMaximo;
}
