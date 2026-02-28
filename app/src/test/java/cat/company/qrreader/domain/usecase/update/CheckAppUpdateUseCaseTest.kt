package cat.company.qrreader.domain.usecase.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckAppUpdateUseCaseTest {

    private fun useCase(current: String = "1.0.0") = CheckAppUpdateUseCase(current)

    // isNewerVersion tests

    @Test
    fun `isNewerVersion returns true when latest major is higher`() {
        assertTrue(useCase("1.0.0").isNewerVersion("2.0.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns true when latest minor is higher`() {
        assertTrue(useCase("1.0.0").isNewerVersion("1.1.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns true when latest patch is higher`() {
        assertTrue(useCase("1.0.0").isNewerVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns false when versions are equal`() {
        assertFalse(useCase("1.2.3").isNewerVersion("1.2.3", "1.2.3"))
    }

    @Test
    fun `isNewerVersion returns false when installed version is newer`() {
        assertFalse(useCase("2.0.0").isNewerVersion("1.9.9", "2.0.0"))
    }

    @Test
    fun `isNewerVersion handles missing patch segment`() {
        // "1.1" should be treated as "1.1.0", so "1.1.0" == "1.1.0"
        assertFalse(useCase("1.1.0").isNewerVersion("1.1", "1.1.0"))
    }

    @Test
    fun `isNewerVersion handles missing segment in latest`() {
        // "2.0" vs "1.9.9" - 2 > 1
        assertTrue(useCase("1.9.9").isNewerVersion("2.0", "1.9.9"))
    }

    @Test
    fun `isNewerVersion compares multi-digit segment correctly`() {
        assertTrue(useCase("1.0.0").isNewerVersion("1.0.10", "1.0.9"))
    }

    @Test
    fun `isNewerVersion returns false for equal single segment versions`() {
        assertFalse(useCase("5.0").isNewerVersion("5", "5"))
    }
}
