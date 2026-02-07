package cat.company.qrreader.events

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SharedEvents class
 */
class SharedEventsTest {

    @Before
    fun setup() {
        // Reset all callbacks before each test
        SharedEvents.openSideBar = null
        SharedEvents.onShareClick = null
        SharedEvents.onShareIsDisabled = null
        SharedEvents.onPrintClick = null
        SharedEvents.onPrintIsDisabled = null
    }

    @After
    fun tearDown() {
        // Clean up all callbacks after each test
        SharedEvents.openSideBar = null
        SharedEvents.onShareClick = null
        SharedEvents.onShareIsDisabled = null
        SharedEvents.onPrintClick = null
        SharedEvents.onPrintIsDisabled = null
    }

    // --- openSideBar tests ---

    /**
     * Test openSideBar is initially null
     */
    @Test
    fun openSideBar_initiallyNull() {
        assertNull(SharedEvents.openSideBar)
    }

    /**
     * Test openSideBar callback registration and invocation
     */
    @Test
    fun openSideBar_callbackRegistration_invokesCorrectly() {
        var invoked = false
        SharedEvents.openSideBar = {
            invoked = true
        }

        assertNotNull(SharedEvents.openSideBar)
        SharedEvents.openSideBar?.invoke()
        assertTrue(invoked)
    }

    /**
     * Test openSideBar can be reset to null
     */
    @Test
    fun openSideBar_canBeResetToNull() {
        SharedEvents.openSideBar = { }
        assertNotNull(SharedEvents.openSideBar)

        SharedEvents.openSideBar = null
        assertNull(SharedEvents.openSideBar)
    }

    // --- onShareClick tests ---

    /**
     * Test onShareClick is initially null
     */
    @Test
    fun onShareClick_initiallyNull() {
        assertNull(SharedEvents.onShareClick)
    }

    /**
     * Test onShareClick callback registration and invocation
     */
    @Test
    fun onShareClick_callbackRegistration_invokesCorrectly() {
        var invoked = false
        SharedEvents.onShareClick = {
            invoked = true
        }

        assertNotNull(SharedEvents.onShareClick)
        SharedEvents.onShareClick?.invoke()
        assertTrue(invoked)
    }

    /**
     * Test onShareClick multiple invocations
     */
    @Test
    fun onShareClick_multipleInvocations_countCorrectly() {
        var count = 0
        SharedEvents.onShareClick = {
            count++
        }

        SharedEvents.onShareClick?.invoke()
        SharedEvents.onShareClick?.invoke()
        SharedEvents.onShareClick?.invoke()

        assertEquals(3, count)
    }

    // --- onShareIsDisabled tests ---

    /**
     * Test onShareIsDisabled is initially null
     */
    @Test
    fun onShareIsDisabled_initiallyNull() {
        assertNull(SharedEvents.onShareIsDisabled)
    }

    /**
     * Test onShareIsDisabled receives true parameter
     */
    @Test
    fun onShareIsDisabled_trueParameter_receivesCorrectly() {
        var receivedValue: Boolean? = null
        SharedEvents.onShareIsDisabled = { disabled ->
            receivedValue = disabled
        }

        SharedEvents.onShareIsDisabled?.invoke(true)
        assertTrue(receivedValue == true)
    }

    /**
     * Test onShareIsDisabled receives false parameter
     */
    @Test
    fun onShareIsDisabled_falseParameter_receivesCorrectly() {
        var receivedValue: Boolean? = null
        SharedEvents.onShareIsDisabled = { disabled ->
            receivedValue = disabled
        }

        SharedEvents.onShareIsDisabled?.invoke(false)
        assertFalse(receivedValue == true)
    }

    /**
     * Test onShareIsDisabled toggle behavior
     */
    @Test
    fun onShareIsDisabled_toggle_updatesCorrectly() {
        var currentState = false
        SharedEvents.onShareIsDisabled = { disabled ->
            currentState = disabled
        }

        SharedEvents.onShareIsDisabled?.invoke(true)
        assertTrue(currentState)

        SharedEvents.onShareIsDisabled?.invoke(false)
        assertFalse(currentState)

        SharedEvents.onShareIsDisabled?.invoke(true)
        assertTrue(currentState)
    }

    // --- onPrintClick tests ---

    /**
     * Test onPrintClick is initially null
     */
    @Test
    fun onPrintClick_initiallyNull() {
        assertNull(SharedEvents.onPrintClick)
    }

    /**
     * Test onPrintClick callback registration and invocation
     */
    @Test
    fun onPrintClick_callbackRegistration_invokesCorrectly() {
        var invoked = false
        SharedEvents.onPrintClick = {
            invoked = true
        }

        assertNotNull(SharedEvents.onPrintClick)
        SharedEvents.onPrintClick?.invoke()
        assertTrue(invoked)
    }

    /**
     * Test onPrintClick multiple invocations
     */
    @Test
    fun onPrintClick_multipleInvocations_countCorrectly() {
        var count = 0
        SharedEvents.onPrintClick = {
            count++
        }

        SharedEvents.onPrintClick?.invoke()
        SharedEvents.onPrintClick?.invoke()

        assertEquals(2, count)
    }

    // --- onPrintIsDisabled tests ---

    /**
     * Test onPrintIsDisabled is initially null
     */
    @Test
    fun onPrintIsDisabled_initiallyNull() {
        assertNull(SharedEvents.onPrintIsDisabled)
    }

    /**
     * Test onPrintIsDisabled receives true parameter
     */
    @Test
    fun onPrintIsDisabled_trueParameter_receivesCorrectly() {
        var receivedValue: Boolean? = null
        SharedEvents.onPrintIsDisabled = { disabled ->
            receivedValue = disabled
        }

        SharedEvents.onPrintIsDisabled?.invoke(true)
        assertTrue(receivedValue == true)
    }

    /**
     * Test onPrintIsDisabled receives false parameter
     */
    @Test
    fun onPrintIsDisabled_falseParameter_receivesCorrectly() {
        var receivedValue: Boolean? = null
        SharedEvents.onPrintIsDisabled = { disabled ->
            receivedValue = disabled
        }

        SharedEvents.onPrintIsDisabled?.invoke(false)
        assertFalse(receivedValue == true)
    }

    // --- Callback replacement tests ---

    /**
     * Test that replacing a callback works correctly
     */
    @Test
    fun callbackReplacement_newCallbackReceivesEvents() {
        var firstCallbackInvoked = false
        var secondCallbackInvoked = false

        SharedEvents.onShareClick = {
            firstCallbackInvoked = true
        }

        // Replace the callback
        SharedEvents.onShareClick = {
            secondCallbackInvoked = true
        }

        SharedEvents.onShareClick?.invoke()

        assertFalse(firstCallbackInvoked)
        assertTrue(secondCallbackInvoked)
    }

    // --- All callbacks can be set simultaneously ---

    /**
     * Test that all callbacks can be set and invoked independently
     */
    @Test
    fun allCallbacks_canBeSetAndInvokedIndependently() {
        var sideBarOpened = false
        var shareClicked = false
        var shareDisabled = false
        var printClicked = false
        var printDisabled = false

        SharedEvents.openSideBar = { sideBarOpened = true }
        SharedEvents.onShareClick = { shareClicked = true }
        SharedEvents.onShareIsDisabled = { shareDisabled = it }
        SharedEvents.onPrintClick = { printClicked = true }
        SharedEvents.onPrintIsDisabled = { printDisabled = it }

        SharedEvents.openSideBar?.invoke()
        SharedEvents.onShareClick?.invoke()
        SharedEvents.onShareIsDisabled?.invoke(true)
        SharedEvents.onPrintClick?.invoke()
        SharedEvents.onPrintIsDisabled?.invoke(true)

        assertTrue(sideBarOpened)
        assertTrue(shareClicked)
        assertTrue(shareDisabled)
        assertTrue(printClicked)
        assertTrue(printDisabled)
    }

    /**
     * Test safe invocation when callback is null
     */
    @Test
    fun safeInvocation_whenCallbackIsNull_doesNotCrash() {
        // All callbacks are null by default after setup
        // These should not throw any exception
        SharedEvents.openSideBar?.invoke()
        SharedEvents.onShareClick?.invoke()
        SharedEvents.onShareIsDisabled?.invoke(true)
        SharedEvents.onPrintClick?.invoke()
        SharedEvents.onPrintIsDisabled?.invoke(true)

        // If we reach here, the test passes
        assertTrue(true)
    }
}
