package pe.edu.upeu.lamb.planificacion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_curso", nullable = false)
    private Long idCurso;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(length = 150)
    private String docente;

    @Column(length = 100)
    private String horario;

    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;
}
