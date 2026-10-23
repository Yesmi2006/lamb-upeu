# S10 — Observabilidad de Matrícula y Pagos (Lamb UPeU)

Alcance: `lamb-matricula-ms` (8085) y `lamb-finanzas-ms` (8086, antes `lamb-finanzas-ms`: servicio de pagos/finanzas real del proyecto).
Estado de verificación: **los cambios no se compilaron ni ejecutaron en el entorno donde se prepararon**
(sin acceso a Maven Central ni a Docker). Todo está "implementado, pendiente de verificación"
hasta que ejecutes los pasos de la sección 3 en tu máquina.

## 0. Renombrado a lamb-*

Carpetas, `artifactId`, `spring.application.name`, archivos de `config-repo` y rutas del Gateway: `lamb-config`, `lamb-eureka`, `lamb-gateway`, `lamb-auth-ms`, `lamb-finanzas-ms` (antes `pagatu-*`). `pagatu-catalogo-ms` se quitó del proyecto.
Se conservaron a propósito: bases de datos y contenedores Docker (`pagatu_*_db`, `pagatu-kafka-dev`, ...), usuarios semilla (`admin@pagatu.com`, `cliente@pagatu.com`, cambiarlos rompería Flyway) y el `group-id` de Kafka `pagatu-pago-ms` (cambiarlo haría que Kafka releyera todos los eventos). Los paquetes Java (`pe.edu.upeu.pago`, `pe.edu.upeu.auth`, ...) no se movieron.
Los logs de finanzas ahora van a `logs/finanzas.log` y el job de Prometheus se llama `lamb-microservicios`.

## 1. Qué se cambió

| Archivo | Cambio | Motivo (guía S10) |
|---|---|---|
| `infra/lamb-config/config-repo/lamb-matricula-ms-dev.yml` / `-prod.yml` | `probes.enabled`, grupos `liveness` (`livenessState`) y `readiness` (`readinessState, db`) | 3.3 Health groups |
| `infra/lamb-config/config-repo/lamb-finanzas-ms-dev.yml` / `-prod.yml` | Mismo bloque de health groups | 3.3 |
| `infra/lamb-config/config-repo/lamb-matricula-ms-prod.yml` | Se agregó `jwk-set-uri` (`http://lamb-auth-ms:8080/...`), igual que `lamb-finanzas-ms-prod.yml`. Sin él, el servicio no arranca en PROD (no hay `JwtDecoder`). | Corrección S07 |
| `services/lamb-matricula-ms/.../config/TraceIdRelayInterceptor.java` (nuevo) | `RequestInterceptor` de Feign que reenvía `X-Trace-ID` (lee el MDC que deja `CorrelationIdFilter`) hacia `lamb-planificacion-ms` | 3.4 Propagación de traceId |
| `services/lamb-matricula-ms/.../service/MatriculaServiceImpl.java` | `log.info` al crear una matrícula (`matriculaId`, `idPersona`, `estado`, `totalCreditos`) | 2.4 logs útiles para diagnóstico |
| `services/lamb-matricula-ms/.../service/PlanificacionConsultaService.java` | `log.info` en cada consulta a planificación | 3.5 evidencia de la llamada saliente con traceId |
| `obs/compose-dev.yml`, `obs/compose.yml` | Volúmenes de logs de matrícula, planificación y pagos para Promtail | 3.2 Logs centralizados |
| `obs/promtail/promtail-config-dev.yml`, `promtail-config.yml` | Jobs `lamb-matricula-ms`, `lamb-planificacion-ms`, `lamb-finanzas-ms` (label `application`) | 3.2 |
| `obs/grafana/...` (datasources con `uid`, provisioning y `dashboards/s10-diagnostico-lamb.json`) | Tablero con 4 paneles: servicios arriba, tasa de peticiones, tasa de errores 5xx, logs filtrables por traceId | 3.6 Tablero |
| `infra/lamb-config/config-repo/lamb-planificacion-ms-dev.yml` | **Cambio fuera de tu alcance, mínimo y reversible:** `DispatcherServlet: DEBUG` para que planificación registre cada petición con su traceId. Quítalo al terminar. | 3.5 Evidencia del mismo traceId en dos servicios |

No se tocó: Gateway, Eureka, Config Server (código), autenticación, el código de planificación ni Prometheus
(ya descubre a ambos servicios por Eureka con `eureka_sd_configs`).

## 2. Qué no aplica a Lamb o queda fuera de tu responsabilidad

| Requisito de la guía | Estado |
|---|---|
| Keycloak como target de Prometheus y en Promtail | No aplica: el proyecto usa `lamb-auth-ms` (puerto 8090), que sí se registra en Eureka. S07b (Keycloak) no está en el repositorio. |
| `logback-spring.xml` del Gateway y su Promtail | Infraestructura compartida: responsable, quien administre `infra/lamb-gateway`. No modificado. |
| Health groups de `lamb-planificacion-ms` | Servicio de otro módulo; usa el mismo bloque YAML de la sección 1 si su responsable lo decide. |
| Propagación del traceId por Kafka | La guía lo declara fuera de alcance (el MDC es por hilo). |

## 3. Cómo aplicar y verificar (PowerShell, rutas de tu equipo)

Reinicia en este orden lo que cambió: `lamb-matricula-ms`, `lamb-finanzas-ms`, `lamb-planificacion-ms` (los tres leen el Config Server al arrancar).

### 3.1 Compilar matrícula

```powershell
cd C:\DA\lamb-upeu\services\lamb-matricula-ms
.\mvnw.cmd -q -DskipTests compile
```

Debe terminar sin errores. Luego `.\mvnw.cmd spring-boot:run`.

### 3.2 Health groups (esperado `{"status":"UP"}` en los cuatro)

```powershell
Invoke-RestMethod http://localhost:8085/actuator/health/liveness
Invoke-RestMethod http://localhost:8085/actuator/health/readiness
Invoke-RestMethod http://localhost:8086/actuator/health/liveness
Invoke-RestMethod http://localhost:8086/actuator/health/readiness
```

Para la evidencia de "dependencia real en readiness": detén el Postgres de matrícula (`docker stop` de su contenedor) y repite el `readiness` de 8085: debe dar 503 `DOWN` mientras `liveness` sigue `UP`. Vuelve a levantarlo después.

### 3.3 Logs y Grafana

La estructura del compose cambió, por eso aquí corresponde `up -d` (recrea Promtail y Grafana):

```powershell
cd C:\DA\lamb-upeu\obs
docker compose -f compose-dev.yml up -d
```

Abre `http://localhost:13000` (admin/admin) → carpeta **Lamb UPeU** → **S10 - Diagnóstico de Lamb UPeU**.
En Prometheus (`http://localhost:19090/targets`) deben aparecer `LAMB-MATRICULA-MS` y `LAMB-FINANZAS-MS` en UP.

### 3.4 Propagación del traceId (mismo valor en dos logs)

```powershell
$g = "http://localhost:18080"
$tokenCliente = (Invoke-RestMethod -Method Post -Uri "$g/api/v1/auth/login" -ContentType "application/json" -Body '{"email":"cliente@pagatu.com","password":"cliente123"}').access_token
$tid = "S10-LAMB-001"
Clear-Host
Write-Host ("=== TRAZA: mismo traceId en matricula y planificacion  |  " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss")) -ForegroundColor Cyan
Invoke-RestMethod -Method Post -Uri "$g/api/v1/matriculas" -Headers @{ Authorization = "Bearer $tokenCliente"; "X-Trace-ID" = $tid } -ContentType "application/json" -Body '{"periodo":"2026-4","detalles":[{"idSeccion":1}]}' | ConvertTo-Json -Depth 5
Select-String -Path "C:\DA\lamb-upeu\services\lamb-matricula-ms\logs\matricula.log","C:\DA\lamb-upeu\services\lamb-planificacion-ms\logs\planificacion.log" -Pattern $tid | ForEach-Object { $_.Filename + "  " + $_.Line }
```

Esperado: líneas con `[S10-LAMB-001]` en **ambos** archivos. Si el Gateway no reenvía el header y matrícula genera un traceId propio, repite la llamada directo a `http://localhost:8085/api/v1/matriculas` con el mismo token. Si solo aparece en matrícula, revisa que reiniciaste planificación (para el `DEBUG`) y matrícula (para el interceptor).
En Grafana, escribe `S10-LAMB-001` en la variable **traceId** y el panel de logs filtra ambos servicios.

### 3.5 Falla simulada y diagnóstico (Matrícula → Pagos por Kafka)

1. Ctrl+C en la consola de `lamb-finanzas-ms`.
2. En el tablero: **Servicios arriba** muestra `LAMB-FINANZAS-MS` en 0 (o ausente) en menos de ~30 s.
3. Crea una matrícula con el bloque de 3.4 (otro periodo, por ejemplo `2026-5`). Queda `REGISTRADA`: el evento `matricula.creada` se publicó, pero nadie lo consume.
4. Levanta `lamb-finanzas-ms` (`.\mvnw.cmd spring-boot:run`). Con el `matriculaId` de la respuesta (ejemplo 40), en el panel de logs:

```
{application="lamb-finanzas-ms"} |= "matriculaId=40"
```

Debes ver `status=consumed` y `status=processed`; la matrícula pasa a `PAGADA`. No busques por traceId aquí: no viaja por Kafka.

Completa la Tabla 6 solo con lo que observes. Valores esperados, no evidencia: *Servicios arriba*: pago ausente/0, luego vuelve; *Tasa de peticiones*: plana en pagos; *Tasa de errores*: sin cambio (el fallo fue asíncrono); *Logs*: línea de consumo con `matriculaId`.

## 4. Discrepancias y riesgos encontrados (no se modificaron)

1. **Nombre del servicio financiero.** Decidido: `lamb-finanzas-ms` (nombre del informe de arquitectura). El informe aún dice puerto 8087, `/api/v1/pagos`, estados `PENDIENTE/APROBADO/REEMBOLSADO/RECHAZADO` y `POST /{id}/reembolsar`; el código tiene `GET /api/v1/pagos` y `GET /api/v1/pagos/{id}`, único estado `VALIDADO` y sin reembolsos. No se inventaron endpoints ni estados: actualiza el informe o implementa lo que falte con tu equipo.
2. **Choque de puertos.** El informe asigna 8086 a Evaluación y 8087 a Finanzas; `lamb-finanzas-ms` usa 8086 en DEV. Si Evaluación corre a la vez, uno no arranca. No se cambió para no invalidar tus evidencias de S08.
3. **Pagos sin Gateway ni autenticación.** El Gateway no tiene ruta `/api/v1/pagos/**` y `SecurityConfig` de pagos usa `permitAll()`, así que los GET de pagos son públicos en 8086.
4. **Idempotencia (S09).** Si llega dos veces `matricula.creada`, la restricción `uk_pagos_matricula` hace fallar el consumidor. La guía de S09 no se adjuntó, así que no se verificó ni se implementó.
5. **Estado `PAGADA`.** El informe documenta solo `REGISTRADA → CONFIRMADA/ANULADA`; el código agrega `PAGADA` (S08). Actualiza el informe.
6. **Topic `orden-eventos`.** `matricula.creada` viaja por un topic con nombre heredado de Pagatu. Se mantuvo porque es el contrato de tu informe S08.
7. **Contraseñas de DEV** (`lamb/lamb`, `pagatu/pagatu`) están en `config-repo`. Son valores de desarrollo local; en PROD se leen de variables de entorno.
