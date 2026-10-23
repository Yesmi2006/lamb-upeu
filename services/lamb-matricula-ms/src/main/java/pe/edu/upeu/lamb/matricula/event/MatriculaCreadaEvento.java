package pe.edu.upeu.lamb.matricula.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaCreadaEvento {

    private String tipoEvento;
    private Long matriculaId;
    private Long idPersona;
    private BigDecimal monto;
    private String metodoPago;
    private String origen;
    private Long timestamp;
}