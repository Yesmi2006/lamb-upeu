package pe.edu.upeu.lamb.matricula.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import pe.edu.upeu.lamb.matricula.filter.CorrelationIdFilter;

/**
 * S10: reenvia el X-Trace-ID de la peticion entrante hacia las llamadas Feign
 * (lamb-matricula-ms -> lamb-planificacion-ms), para que ambos servicios
 * registren el mismo traceId en sus logs.
 *
 * Lee el mismo valor que CorrelationIdFilter dejo en el MDC. El MDC es por hilo:
 * funciona porque @CircuitBreaker ejecuta la llamada en el hilo de la peticion HTTP.
 * No aplica a los eventos Kafka (otro hilo, sin traceId en el MDC).
 */
@Component
public class TraceIdRelayInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (traceId != null && !traceId.isBlank()) {
            template.header(CorrelationIdFilter.TRACE_ID_HEADER, traceId);
        }
    }
}
