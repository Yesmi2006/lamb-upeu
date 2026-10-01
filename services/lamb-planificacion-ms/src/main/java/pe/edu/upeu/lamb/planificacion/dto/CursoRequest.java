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
public class CursoRequest {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    @NotNull
    @Positive
    private Integer creditos;
}
