package cat.company.qrreader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for [GlobalExceptionHandler].
 */
@RunWith(RobolectricTestRunner::class)
class GlobalExceptionHandlerTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun uncaughtException_forwardsThreadAndThrowableToDefaultHandler() {
        val recordedThreads = mutableListOf<Thread>()
        val recordedThrowables = mutableListOf<Throwable>()

        val fakeDefault = Thread.UncaughtExceptionHandler { t, e ->
            recordedThreads += t
            recordedThrowables += e
        }

        val handler = GlobalExceptionHandler(context, fakeDefault)
        val thread = Thread.currentThread()
        val throwable = RuntimeException("test crash")

        handler.uncaughtException(thread, throwable)

        assertEquals(1, recordedThreads.size)
        assertEquals(thread, recordedThreads[0])
        assertEquals(throwable, recordedThrowables[0])
    }

    @Test
    fun uncaughtException_defaultHandlerCalledEvenIfRestartFails() {
        var defaultHandlerCalled = false

        val fakeDefault = Thread.UncaughtExceptionHandler { _, _ ->
            defaultHandlerCalled = true
        }

        val handler = GlobalExceptionHandler(context, fakeDefault)
        handler.uncaughtException(Thread.currentThread(), RuntimeException("boom"))

        assertTrue(defaultHandlerCalled)
    }
}
