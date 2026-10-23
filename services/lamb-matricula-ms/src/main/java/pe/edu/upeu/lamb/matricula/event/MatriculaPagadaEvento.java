package pe.edu.upeu.lamb.matricula.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaPagadaEvento {

    private String tipoEvento;
    private Long matriculaId;
    private Long idPersona;
    private String periodo;
    private List<Long> secciones;
    private String origen;
    private Long timestamp;
}