package com.example.taskpulse.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskpulse.MainActivity
import com.example.taskpulse.ui.splash.SPLASH_DURATION_MS
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskPulseNavigationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun showsBottomNavigationAfterSplash() {
        skipSplash()

        composeRule.onNavTab("Tareas").assertExists()
        composeRule.onNavTab("Calendario").assertExists()
        composeRule.onNavTab("Ajustes").assertExists()
    }

    @Test
    fun navigatesToSettingsAndShowsConfigurationHeading() {
        skipSplash()

        composeRule.onNavTab("Ajustes").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Configuración").assertExists()
        composeRule.onNodeWithText("Mantenimiento automático").assertExists()
    }

    @Test
    fun navigatesToCalendarTab() {
        skipSplash()

        composeRule.onNavTab("Calendario").performClick()
        composeRule.waitForIdle()

        composeRule.onNavTab("Calendario").assertIsSelected()
        composeRule.onNodeWithContentDescription("Mes anterior").assertExists()
        composeRule.onNodeWithContentDescription("Mes siguiente").assertExists()
    }

    private fun skipSplash() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy((SPLASH_DURATION_MS + 500).toLong())
        composeRule.waitForIdle()
    }

    /** Pestaña inferior (evita colisión con títulos de pantalla, p. ej. «Calendario»). */
    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, MainActivity>.onNavTab(
        label: String
    ) = onNode(hasText(label) and isTabRole())

    private fun isTabRole(): SemanticsMatcher = SemanticsMatcher(
        "${SemanticsProperties.Role.name} = Tab"
    ) { node ->
        node.config.getOrNull(SemanticsProperties.Role) == Role.Tab
    }
}
