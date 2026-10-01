package pe.edu.upeu.lamb.planificacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionRequest {

    @NotNull
    private Long idCurso;

    @NotBlank
    private String periodo;

    private String docente;

    private String horario;

    @NotNull
    @Positive
    private Integer cupoMaximo;
}
