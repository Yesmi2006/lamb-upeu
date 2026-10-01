package pe.edu.upeu.lamb.planificacion.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionResponse {
    private Long id;
    private Long idCurso;
    private String periodo;
    private String docente;
    private String horario;
    private Integer cupoMaximo;
}
