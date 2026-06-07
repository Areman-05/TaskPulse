package com.example.taskpulse.core

/**
 * Preferencias de rendimiento para móvil: animaciones decorativas solo donde aportan
 * (splash, onboarding) y fondos ligeros en el shell diario.
 */
object UiPerformance {
    /** Anillos, nebula animada y pulso en splash / mark hero. */
    const val decorativeMotionEnabled: Boolean = true

    /** Gradientes estáticos en Home, Calendario, Ajustes (sin nebula infinita). */
    const val useLightMainBackground: Boolean = true
}
