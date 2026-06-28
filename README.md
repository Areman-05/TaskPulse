# TaskPulse

**Organiza tareas y notas con calendario, recordatorios y mantenimiento automático.**

TaskPulse es una app Android para quienes quieren un sitio claro para lo pendiente: crear tareas con prioridad y fecha, apuntar notas al lado, ver el mes en calendario y dejar que la app archive lo antiguo sin tener que limpiar a mano cada semana. No es otra lista infinita sin contexto: une lista activa, calendario, archivo y ajustes en un flujo coherente, con una interfaz moderna y legible.

## La idea en una frase

Llevar el pulso de lo que importa en un solo lugar: priorizar, recordar, revisar por fechas y recuperar lo archivado cuando haga falta—sin mezclar apps de notas, calendario y recordatorios por separado.

## Para quién es

- **Quien vive con tareas reales:** prioridades, fechas de vencimiento y completar con un gesto.
- **Quien mezcla notas y tareas:** las notas conviven en Home y en el calendario, separadas pero en la misma app.
- **Quien quiere orden sin microgestión:** archivado automático, barrido en segundo plano y un archivo consultable de hasta 50 entradas.
- **Quien cuida sus datos:** exportación local (JSON, CSV, copia de base de datos) desde Ajustes.

## Qué hace la app (en lenguaje humano)

### Inicio (Tareas)

Busca por título o descripción, ordena por prioridad, fecha de edición, creación o título, y alterna entre vista lista o galería. Las **tareas** van arriba (ordenadas por prioridad: crítica → baja); las **notas** debajo, con su propia sección. Puedes seleccionar varias tareas para completarlas, cambiar prioridad o borrarlas, o deslizar una fila para completar o eliminar.

### Calendario

Navega por meses, elige un día y ve qué tareas y notas caen ahí. Desde el lápiz de la barra superior creas una entrada ya ligada a ese día. La vista del día separa tareas y notas igual que en el inicio.

### Crear y editar

Al crear eliges **tarea** o **nota**, título, prioridad (en tareas), fecha en calendario y, si quieres, **recordatorio** (30 min, 1 h, 1 día antes del vencimiento, etc.). El detalle permite editar, completar tareas o borrar entradas.

### Ciclo de vida automático

- Las tareas con fecha **pasada** se marcan completadas en el mantenimiento.
- Tras **2 días** desde la fecha de vencimiento (o **14 días** sin fecha), las entradas pasan al **archivo** y desaparecen de Home y del calendario activo.
- El archivo guarda hasta **50** entradas; puedes **restaurar** o **eliminar** definitivamente.

### Ajustes

- **Apariencia:** modo claro u oscuro.
- **Archivo:** acceso directo a lo archivado.
- **Mantenimiento automático:** ejecutar ahora, frecuencia del barrido en horas, Wi‑Fi o solo cargando; historial de ejecuciones.
- **Exportar:** JSON, CSV o copia de la base de datos para compartir o respaldar.

### Recordatorios y widget

Notificaciones para recordatorios de tareas (con acciones completar, posponer o abrir) y alertas de reglas de automatización en segundo plano. Un **widget** muestra cuántas tareas siguen pendientes y abre la app al pulsar.

### Automatización (segundo plano)

Reglas predefinidas (por ejemplo, avisos de tareas vencidas o tareas estancadas) se evalúan en barridos periódicos vía WorkManager, junto con el mantenimiento del ciclo de vida. No hace falta configurarlas desde la app para que el motor trabaje.

## Por qué TaskPulse y no “otra app de tareas”

- **Un solo viaje de usuario:** de “apunto algo” a “lo veo en el calendario”, “me avisa” y “desaparece del día a día cuando toca” sin exportar a otra herramienta.
- **Tareas y notas juntas, pero ordenadas:** misma base de datos, distinta presentación; no es un bloc genérico ni un GTD complejo.
- **Mantenimiento con sentido:** archivar y completar vencidas no es un extra escondido; está en Ajustes y en el barrido programado.
- **Experiencia cuidada:** prioridades con color, scroll y secciones claras, splash y navegación por pestañas (Tareas · Calendario · Ajustes).

## Cómo probarla en tu máquina

No necesitas claves de API: todo es **local** (Room en el dispositivo, preferencias para tema y automatización, WorkManager para barridos).

Requisitos habituales: Android Studio reciente, JDK 11+, SDK con **API 26+** (minSdk 26).

```bash
./gradlew :app:assembleDebug
```

Instala el APK generado en `app/build/outputs/apk/debug/` o ejecuta desde Android Studio en emulador o dispositivo.

### Tests automatizados

```bash
# Unitarios (JVM, sin emulador)
./gradlew testDebugUnitTest

# Instrumentados (emulador o dispositivo conectado)
./gradlew connectedDebugAndroidTest
```

Los unitarios cubren ordenación, calendario, ciclo de vida, automatización y mappers; los instrumentados, Room, repositorio y navegación principal.


## Detalle técnico (opcional)

Si te interesa el cómo está hecha:

| Área | Tecnología |
|------|------------|
| UI | Kotlin, Jetpack Compose, Material 3 |
| Arquitectura | MVVM, casos de uso en `domain`, repositorios |
| Datos | Room (SQLite), SharedPreferences (tema, ajustes de barrido) |
| Segundo plano | WorkManager (barrido, recordatorios) |
| DI | `AppContainer` manual en `TaskPulseApp` (instancia única) |
| Tests | JUnit, MockK, coroutines-test, Compose UI Test, Room in-memory |

Estructura de paquetes resumida: `ui/` (pantallas y ViewModels), `domain/` (modelos, reglas, use cases), `data/` (Room, mappers, export), `worker/`, `notification/`, `widget/`, `core/`.

Versión actual de referencia: **1.0.5** (`versionCode` 6).

---

**TaskPulse: del pendiente al archivo, con calendario y recordatorios en un solo sitio.**
