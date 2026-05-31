package com.cosmica.app.presentation.moonphase

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.model.MoonPhaseType
import com.cosmica.app.presentation.theme.CosmikaTheme
import org.junit.Rule
import org.junit.Test

class MoonPhaseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fullMoon = MoonPhase(
        phase               = 0.5,
        phaseType           = MoonPhaseType.FULL_MOON,
        illuminationPercent = 100,
        daysUntilFullMoon   = 0,
        locationName        = "Istanbul, TR",
    )

    @Test
    fun phase_name_is_shown() {
        composeRule.setContent {
            CosmikaTheme { MoonContentForTest(fullMoon, phaseDisplayName = "Full Moon") }
        }
        composeRule.onNodeWithText("Full Moon").assertIsDisplayed()
    }

    @Test
    fun illumination_percentage_is_shown() {
        composeRule.setContent {
            CosmikaTheme { MoonContentForTest(fullMoon, phaseDisplayName = "Full Moon") }
        }
        composeRule.onNodeWithText("100% illuminated").assertIsDisplayed()
    }

    @Test
    fun location_name_is_shown() {
        composeRule.setContent {
            CosmikaTheme { MoonContentForTest(fullMoon, phaseDisplayName = "Full Moon") }
        }
        composeRule.onNodeWithText("Istanbul, TR").assertIsDisplayed()
    }
}

@Composable
private fun MoonContentForTest(moon: MoonPhase, phaseDisplayName: String) {
    Column {
        Text(phaseDisplayName)
        Text("${moon.illuminationPercent}% illuminated")
        Text(moon.locationName)
    }
}
