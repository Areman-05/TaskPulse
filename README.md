# TaskPulse

**Organiza tareas y notas, mídelas por día y sigue tu pulso con calendario, recordatorios y archivo automático.**

TaskPulse es una app Android para quienes quieren un sitio claro para lo pendiente: ver **tareas de hoy** en un panel legible, crear entradas con prioridad y fecha, apuntar notas al lado, revisar el mes en calendario y dejar que la app archive lo antiguo sin limpiar a mano cada semana. No es otra lista infinita sin contexto ni un bloc de notas desconectado: une splash de carga real, inicio, calendario, archivo y ajustes en un flujo coherente, con interfaz **Stitch** (bronce, glass cards, Hanken Grotesk) pensada para el uso diario en móvil.

## La idea en una frase

Llevar el pulso de lo que importa en un solo sitio: priorizar hoy, recordar a tiempo, revisar por fechas y recuperar lo archivado cuando haga falta — sin mezclar apps de notas, calendario y recordatorios por separado.

## Para quién es

- **Quien quiere claridad al abrir la app:** sección *Tareas de hoy*, búsqueda, métricas de productividad y próximo vencimiento en el inicio.
- **Quien mezcla tareas y notas:** mismos datos, distinta presentación; notas en carrusel bajo las tareas activas.
- **Quien entrena constancia sin microgestión:** archivado automático, barrido en segundo plano y archivo consultable (hasta 50 entradas).
- **Quien cuida sus datos locales:** exportación JSON, CSV y copia de base de datos desde Ajustes; todo en Room en el dispositivo.
- **Quien alterna claro y oscuro:** tema Stitch coherente en Home, Calendario, Crear, Archivo y Ajustes.

## Qué hace la app (en lenguaje humano)

### Splash

Arranque con mark animado (pulso bronce), carga real de categorías, tareas, notas, reglas, exportaciones y workers, y barra de progreso minimalista integrada en el fondo. Tiempo mínimo visible para que la transición se lea con calma.

### Inicio (Tareas)

- **Tareas de hoy:** solo pendientes relevantes (vencen hoy o antes, o sin fecha). Las completadas desaparecen de la lista; las de días futuros no aparecen hasta su fecha.
- Búsqueda por título o descripción; orden por prioridad, edición, creación o título; vista lista o galería (notas).
- Gestos: deslizar para completar o eliminar; selección múltiple para completar, cambiar prioridad o borrar.
- **Bento:** productividad del día y tarjeta de próximo evento con recordatorio.

### Calendario

Navega por meses, elige un día y ve tareas y notas de esa fecha. Desde el lápiz creas una entrada ya ligada al día seleccionado.

### Crear y editar

Elige **tarea** o **nota**, prioridad, fecha en calendario y recordatorio opcional (30 min, 1 h, 1 día…). El detalle permite editar, completar tareas o borrar entradas.

### Archivo

Entradas completadas o descartadas automáticamente. Desliza para restaurar o eliminar permanentemente. Fechas relativas (*Hoy*, *Ayer*…).

### Ciclo de vida automático

- Tareas con fecha **pasada** pueden marcarse completadas en el mantenimiento.
- Tras **2 días** desde el vencimiento (o **14 días** sin fecha), pasan al **archivo** y salen de Home y calendario activo.
- Hasta **50** entradas archivadas; al superar el límite se elimina la más antigua.

### Ajustes

Modo claro/oscuro, acceso al archivo, mantenimiento automático (frecuencia, Wi‑Fi/carga, historial de barridos) y exportación de datos.

### Recordatorios y widget

Notificaciones con acciones (completar, posponer, abrir) y reglas de automatización en segundo plano. **Widget** con contador de pendientes.

## Por qué TaskPulse y no “otra app de tareas”

- **Un solo viaje:** de la splash con carga real a “veo mi día”, “creo”, “me avisa” y “archiva solo”.
- **Tareas de hoy con criterio:** no es un volcado de todo el inbox; filtra por fecha y estado activo.
- **Tareas y notas juntas, pero ordenadas:** misma base Room, presentación distinta.
- **Mantenimiento con sentido:** archivar y completar vencidas está en el núcleo, no es un extra escondido.
- **Experiencia cuidada:** paleta bronce Stitch, tarjetas glass, splash con pulso, navegación por pestañas (Tareas · Calendario · Ajustes).

## UI y UX: diseño, flujo y rendimiento en móvil

### Principio rector: claridad antes que espectáculo

Las animaciones decorativas (nebula, anillos de pulso, trazo ECG) viven en la **splash**. El shell diario usa fondos **estáticos** ligeros para scroll fluido y menos trabajo de GPU.

### Flujo de navegación

```
Splash (carga) → Shell principal (3 tabs)
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
    Crear/Detalle  Calendario    Ajustes → Archivo
    (desde FAB     (día +        (tema, export,
     o calendario)  entradas)     mantenimiento)
```

- **Bottom bar** con tres destinos fijos; barra superior contextual por pantalla.
- **Home** con scroll único: tareas de hoy → bento → notas; FAB para crear.
- **Estados vacíos** con copy útil cuando no hay tareas o notas.

### Rendimiento (`UiPerformance`)

| Decisión | Efecto |
|----------|--------|
| `decorativeMotionEnabled` | Pulso y nebula animada solo en splash |
| `useLightMainBackground` | Gradientes estáticos en Home, Calendario, Ajustes |
| Carga en splash | Precalienta Room y workers antes del shell |
| Sin `LazyColumn` anidado en Home | Evita crashes de altura infinita; lista acotada por día |

## Cómo probarla en tu máquina

Todo funciona **sin backend**: cuentas no requeridas; datos en Room y SharedPreferences en el dispositivo.

**Requisitos:** Android Studio reciente, JDK 11+, SDK **API 26+** (minSdk 26).

```bash
# Windows
gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

No hay APK en el repositorio (`build/` no se sube a Git). Genera el instalable en tu máquina o ejecuta desde Android Studio.

| Build | Comando | Archivo |
|-------|---------|---------|
| Debug (pruebas) | `gradlew.bat :app:assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `gradlew.bat :app:assembleRelease` | `app/build/outputs/apk/release/app-release-unsigned.apk` |

Copia el `.apk` al móvil o usa **Run** en emulador/dispositivo. El release sale sin firmar: configura tu keystore en `app/build.gradle.kts` para distribuir fuera del IDE.

### Tests automatizados

```bash
# Unitarios (JVM, sin emulador)
gradlew.bat :app:testDebugUnitTest

# Instrumentados (emulador o dispositivo conectado)
gradlew.bat :app:connectedDebugAndroidTest
```

**Unitarios:** filtro *Tareas de hoy*, stats de productividad, ordenación Home, calendario, ciclo de vida, automatización, mappers y casos de uso (~18 clases).

**Instrumentados:** Room, repositorio offline, navegación por tabs, sección *Tareas de hoy* tras splash y pantalla de Ajustes.

## Detalle técnico (opcional)

| Área | Tecnología |
|------|------------|
| UI | Kotlin, Jetpack Compose, Material 3, diseño Stitch |
| Arquitectura | MVVM, casos de uso en `domain/`, repositorios |
| Datos | Room (SQLite), SharedPreferences (tema, barrido) |
| Segundo plano | WorkManager (barrido, recordatorios, automatización) |
| DI | `AppContainer` manual en `TaskPulseApp` |
| Splash | Bootstrap con progreso real (`RunAppBootstrapUseCase`) |
| Tests | JUnit, MockK, coroutines-test, Compose UI Test, Room in-memory |

**Paquetes:** `ui/` (pantallas, ViewModels, tema Stitch), `domain/` (modelos, reglas, use cases), `data/` (Room, mappers, export), `worker/`, `notification/`, `widget/`, `core/` (`UiPerformance`, `AppContainer`).

Versión de referencia: **1.0.5** (`versionCode` 6).

---

**TaskPulse: del pendiente de hoy al archivo, con calendario y recordatorios en un solo sitio.**
