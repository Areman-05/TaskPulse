# Checklist de pruebas — TaskPulse (Android Studio)

Lista ordenada para validar la app **a mano** en emulador o dispositivo. Marca cada ítem al probarlo.

**Versión de referencia:** 1.0.5  
**Tiempo orientativo:** 45–60 min (completo) · 15 min (recorrido rápido en negrita)

---

## Antes de empezar

- [ ] Proyecto abierto en Android Studio; `./gradlew :app:assembleDebug` termina sin errores.
- [ ] Emulador **API 26+** o dispositivo físico con depuración USB.
- [ ] (Recomendado) Ejecutar tests automáticos:
  - [ ] `./gradlew testDebugUnitTest`
  - [ ] `./gradlew connectedDebugAndroidTest` (con dispositivo conectado)
- [ ] Instalar la app en limpio (desinstalar versión anterior si quieres probar primera ejecución).
- [ ] Aceptar permiso de **notificaciones** si el sistema lo pide (Android 13+).

---

## 1. Arranque y navegación

- [ ] **Splash** (~3 s): aparece marca TaskPulse y luego entra la app sin bloqueos.
- [ ] Barra inferior visible: **Tareas**, **Calendario**, **Ajustes**.
- [ ] Cambiar de pestaña varias veces: cada pantalla carga sin crash.
- [ ] **Tareas** → **Calendario** → **Ajustes** → volver a **Tareas**: el estado de la lista se mantiene razonablemente.

---

## 2. Inicio (Tareas)

### Lista vacía y creación rápida

- [ ] Sin entradas: mensaje de lista vacía y FAB (lápiz) abajo a la derecha.
- [ ] Pulsar FAB → pantalla **Crear** → guardar una **tarea** con título → vuelve a Inicio y se ve en sección **Tareas**.

### Notas y secciones

- [ ] Crear una **nota** → en Inicio aparece en sección **Notas**, debajo de las tareas (y divisor si hay tareas).
- [ ] Las notas no muestran acciones de “completar” como las tareas.

### Búsqueda y orden

- [ ] Campo de búsqueda: filtra por título/descripción; vaciar restaura la lista.
- [ ] Menú (⋮) → **Ordenar por** prioridad / edición / creación / título; el orden cambia de forma coherente.
- [ ] Menú → **Ver como galería** / **Ver como lista**: cambia el layout sin perder datos.

### Prioridad y colores

- [ ] Crear tareas con prioridades distintas (crítica, alta, media, baja): colores/etiquetas distinguibles.
- [ ] Con varias tareas, el orden por **prioridad** (más urgente arriba) es correcto.

### Gestos y selección

- [ ] Deslizar tarea pendiente → **completar**: pasa a completada (estilo atenuado).
- [ ] Deslizar → **eliminar**: desaparece de la lista.
- [ ] Menú → **Seleccionar tareas** → marcar varias → **Completar** / **Prioridad** / **Eliminar** funcionan y salen del modo selección.

### FAB y detalle

- [ ] Pulsar una fila (sin modo selección) → **Detalle** de la entrada.
- [ ] Desde detalle, **volver**: regresa a Inicio.

---

## 3. Crear entrada

- [ ] Alternar **Tarea** / **Nota**: campos coherentes (prioridad y recordatorio en tarea).
- [ ] Elegir **fecha** en calendario → guardar → la entrada aparece en el día correcto en pestaña Calendario.
- [ ] **Recordatorio** activado (p. ej. 1 h antes) en tarea con fecha → guardar sin error.
- [ ] Crear sin fecha → aparece en Inicio; en calendario solo si asignas día después en edición.

---

## 4. Calendario

- [ ] Mes actual visible; flechas **mes anterior** / **mes siguiente** cambian el mes.
- [ ] Días con entradas marcados/visualmente distinguibles.
- [ ] Pulsar un día → lista del día con secciones **Tareas** y **Notas**.
- [ ] Día sin nada: mensaje de día vacío.
- [ ] Lápiz en barra superior (con día seleccionado) → **Crear** con fecha del día preseleccionada.
- [ ] Pulsar entrada del día → **Detalle**.

---

## 5. Detalle y edición

- [ ] **Tarea:** editar título, prioridad, fecha, recordatorio → guardar → cambios visibles en Inicio/Calendario.
- [ ] **Tarea:** botón completar (si aplica) → estado completado.
- [ ] **Nota:** editar contenido → guardar.
- [ ] Eliminar entrada desde detalle → ya no aparece en Inicio ni calendario activo.

---

## 6. Archivo y ciclo de vida

> Para probar archivado rápido puedes usar **Ajustes → Ejecutar mantenimiento ahora** o cambiar fechas del sistema (avanzado).

- [ ] **Ajustes → Ver archivo** → pantalla Archivo (vacía o con entradas).
- [ ] Tarea con fecha de vencimiento **hace varios días** + **Ejecutar mantenimiento ahora** → puede completarse y/o archivarse según reglas.
- [ ] Entrada archivada **no** aparece en Inicio ni en calendario del día activo.
- [ ] En **Archivo:** **Restaurar** → vuelve a Inicio.
- [ ] En **Archivo:** **Eliminar** → confirmación → desaparece del archivo.

---

## 7. Ajustes

### Apariencia

- [ ] **Modo claro** / **Modo oscuro**: la app cambia de tema de forma global.

### Mantenimiento

- [ ] **Ejecutar mantenimiento ahora** → termina sin bloqueo; **Historial de mantenimiento** muestra una línea nueva (o mensaje si aún vacío).
- [ ] Cambiar **frecuencia barrido (horas)** → **Guardar frecuencia** → mensaje de confirmación.
- [ ] Activar/desactivar **solo Wi‑Fi** y **solo cargando** (no tiene que verse efecto inmediato; no debe crashear).

### Exportar

- [ ] **Exportar JSON** → se abre selector para compartir archivo.
- [ ] **Exportar CSV** → igual.
- [ ] **Copia de base de datos** → igual.

---

## 8. Notificaciones (dispositivo o emulador con Google Play)

- [ ] Crear tarea con recordatorio **cercano** (p. ej. en 2–5 min si el flujo lo permite, o fecha hoy + recordatorio corto).
- [ ] Aviso en canal de recordatorios; acciones si aparecen:
  - [ ] **Completar** → tarea completada en app.
  - [ ] **Posponer** → notificación se gestiona sin crash.
- [ ] Denegar notificaciones al instalar → la app sigue usable; mensaje/aviso razonable si aplica.

---

## 9. Widget (pantalla de inicio del launcher)

- [ ] Añadir widget **TaskPulse** al escritorio.
- [ ] Muestra número de pendientes coherente con tareas no completadas en Inicio.
- [ ] Completar o borrar tareas → el número se actualiza (puede requerir volver al escritorio).
- [ ] Pulsar widget → abre la app.

---

## 10. Segundo plano y estabilidad

- [ ] Minimizar app 1–2 min → reabrir: sin crash, datos persistidos.
- [ ] Rotar pantalla (si el dispositivo lo permite) en Inicio y Calendario: sin crash grave.
- [ ] Crear ~10–15 entradas mezcladas → scroll fluido en Inicio y calendario.

---

## 11. Recorrido rápido (smoke test ~15 min)

Marca si todo este mini-flujo pasa de una vez:

- [ ] Instalar → splash → Inicio.
- [ ] Crear 1 tarea + 1 nota.
- [ ] Buscar y abrir detalle de la tarea.
- [ ] Calendario: ver el día de hoy.
- [ ] Ajustes: cambiar tema y abrir Archivo.
- [ ] Exportar JSON (selector de compartir).
- [ ] Volver a Inicio y completar una tarea.

---

## Registro de incidencias (opcional)

| # | Pantalla | Qué hiciste | Esperado | Obtenido |
|---|----------|-------------|----------|----------|
| 1 | | | | |
| 2 | | | | |

---

## Referencia rápida de reglas de negocio

| Comportamiento | Valor |
|----------------|--------|
| Archivar tras vencimiento | 2 días después de la fecha |
| Archivar sin fecha | 14 días desde creación |
| Máximo en archivo | 50 entradas |
| Auto-completar | Tareas con fecha de calendario ya pasada (mantenimiento) |

---

*Checklist alineado con TaskPulse 1.0.5. Para build y tests automáticos, ver [README.md](README.md).*
