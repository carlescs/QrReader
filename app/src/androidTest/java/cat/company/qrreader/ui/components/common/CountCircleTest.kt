package cat.company.qrreader.ui.components.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CountCircle] composable.
 */
@RunWith(AndroidJUnit4::class)
class CountCircleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** A count within 0–99 is displayed as its numeric string. */
    @Test
    fun countCircle_displaysCountWithinRange() {
        composeTestRule.setContent {
            CountCircle(count = 5)
        }
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    /** Count of zero is displayed as "0". */
    @Test
    fun countCircle_displaysZeroCount() {
        composeTestRule.setContent {
            CountCircle(count = 0)
        }
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    /** Count at the upper boundary (99) is displayed as "99", not "99+". */
    @Test
    fun countCircle_displaysCountAtBoundary() {
        composeTestRule.setContent {
            CountCircle(count = 99)
        }
        composeTestRule.onNodeWithText("99").assertIsDisplayed()
    }

    /** A count above 99 is capped and displayed as "99+". */
    @Test
    fun countCircle_cappedAbove99() {
        composeTestRule.setContent {
            CountCircle(count = 100)
        }
        composeTestRule.onNodeWithText("99+").assertIsDisplayed()
    }

    /** A count well above 99 is still capped at "99+". */
    @Test
    fun countCircle_largeCountCappedAt99Plus() {
        composeTestRule.setContent {
            CountCircle(count = 999)
        }
        composeTestRule.onNodeWithText("99+").assertIsDisplayed()
    }

    /** When countDescription is provided it is used as the accessibility content description. */
    @Test
    fun countCircle_withDescription_hasContentDescription() {
        composeTestRule.setContent {
            CountCircle(count = 3, countDescription = "3 barcodes")
        }
        composeTestRule.onNodeWithContentDescription("3 barcodes").assertIsDisplayed()
    }

    /** When countDescription is null the composable renders without crashing. */
    @Test
    fun countCircle_withoutDescription_rendersWithoutCrashing() {
        composeTestRule.setContent {
            CountCircle(count = 7)
        }
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
    }
}
