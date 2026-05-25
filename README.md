# Concurrencia en Java con Spring Boot

Proyecto de ejemplo que demuestra el uso de `CompletableFuture` para ejecutar tareas en paralelo en una API REST, reduciendo tiempos de respuesta en escenarios con múltiples llamadas independientes.

---

## ¿Qué problema resuelve?

Cuando un endpoint necesita consultar múltiples fuentes de datos independientes entre sí (cliente, historial de pagos, score crediticio), ejecutarlas en serie genera una latencia acumulada innecesaria.

Este proyecto compara ambos enfoques de forma medible:

| Enfoque | Tiempo de respuesta |
|---|---|
| En serie | ~910ms (300 + 400 + 200) |
| En paralelo | ~410ms (el más lento de los tres) |

---

## Arquitectura

El proyecto sigue una arquitectura por capas, clara y mantenible:

```
src/main/java/com/ejemplo/concurrencia/
├── controller/        # Capa de entrada — endpoints REST
├── service/           # Lógica de negocio y orquestación async
├── dto/               # Objetos de respuesta (desacoplados del modelo)
├── config/            # Configuración del ThreadPoolTaskExecutor
└── exception/         # Manejo global de errores con @RestControllerAdvice
```

---

## Decisiones técnicas

**¿Por qué `CompletableFuture` y no `@Async`?**
`@Async` de Spring es conveniente pero oculta el control del flujo. Con `CompletableFuture` se tiene control explícito sobre cuándo esperar, cómo encadenar tareas y cómo manejar errores individuales por tarea con `.exceptionally()`.

**¿Por qué configurar el `ThreadPoolTaskExecutor` manualmente?**
El executor por defecto de Java (`ForkJoinPool.commonPool`) no está pensado para I/O bloqueante. Al configurar `corePoolSize`, `maxPoolSize` y `queueCapacity` explícitamente, se evitan problemas de saturación bajo carga y se hace el pool visible en los logs mediante `threadNamePrefix`.

**¿Por qué `@RestControllerAdvice`?**
Para centralizar el manejo de errores y garantizar un formato de respuesta consistente en toda la API. Separa errores del cliente (4xx) de errores del servidor (5xx) sin repetir lógica en cada controller.

---

## Endpoints

```
GET /clientes/{id}/resumen-serie      # Consultas en serie — referencia de tiempo base
GET /clientes/{id}/resumen-paralelo   # Consultas en paralelo — versión optimizada
```

### Respuesta de ejemplo

```json
{
  "datos": "Cliente #1 - Juan Pérez",
  "historial": "Historial: 5 pagos al día",
  "score": 850,
  "tiempoMs": 412
}
```

El campo `tiempoMs` permite comparar el tiempo real de cada enfoque en tu entorno.

---

## Cómo correrlo

**Requisitos:** Java 21, Maven 3.8+

```bash
git clone https://github.com/christian-abril-dev/ejemplo-concurrencia.git
cd ejemplo-concurrencia
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

---

## Qué aprender de este proyecto

- Uso de `CompletableFuture.supplyAsync()` con executor personalizado
- Combinación de futuros con `CompletableFuture.allOf()`
- Manejo de errores por tarea con `.exceptionally()`
- Configuración de `ThreadPoolTaskExecutor` para I/O bloqueante
- Manejo global de excepciones con `@RestControllerAdvice`
- Separación por capas: controller → service → dto → config → exception

---

## Stack

- Java 21
- Spring Boot 3.2
- Maven
