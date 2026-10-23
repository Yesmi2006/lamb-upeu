package pe.edu.upeu.lamb.planificacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.lamb.planificacion.entity.Seccion;
import pe.edu.upeu.lamb.planificacion.repository.SeccionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaCupoService {

    private final SeccionRepository seccionRepository;

    @Transactional
    public void reservar(Long matriculaId, List<Long> idsSeccion) {
        if (idsSeccion == null) {
            return;
        }
        for (Long idSeccion : idsSeccion) {
            Seccion seccion = seccionRepository.findById(idSeccion).orElse(null);
            if (seccion == null) {
                log.warn("component=processor matriculaId={} idSeccion={} status=ignored motivo=\"la seccion no existe\"", matriculaId, idSeccion);
                continue;
            }
            if (seccion.getCuposReservados() >= seccion.getCupoMaximo()) {
                log.warn("component=processor matriculaId={} idSeccion={} status=sin_cupo cupoMaximo={}", matriculaId, idSeccion, seccion.getCupoMaximo());
                continue;
            }
            seccion.setCuposReservados(seccion.getCuposReservados() + 1);
            seccionRepository.save(seccion);
            log.info("component=processor matriculaId={} idSeccion={} cuposReservados={} cupoMaximo={} status=processed",
                    matriculaId, idSeccion, seccion.getCuposReservados(), seccion.getCupoMaximo());
        }
    }
}