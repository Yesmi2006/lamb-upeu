package pe.edu.upeu.lamb.matricula.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaRequest {

    @NotNull
    private Long idPersona;

    @NotBlank
    private String periodo;

    @NotEmpty
    @Valid
    private List<DetalleMatriculaRequest> detalles;
}
