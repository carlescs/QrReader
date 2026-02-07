package cat.company.qrreader.db.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

/**
 * Unit tests for Converters class (Room TypeConverters)
 */
class ConvertersTest {

    private val converters = Converters()

    // --- timestampToDate tests ---

    /**
     * Test that timestampToDate converts a valid timestamp to Date
     */
    @Test
    fun timestampToDate_validTimestamp_returnsDate() {
        val timestamp = 1609459200000L // 2021-01-01 00:00:00 UTC
        val result = converters.timestampToDate(timestamp)

        assertEquals(Date(timestamp), result)
    }

    /**
     * Test that timestampToDate returns null for null input
     */
    @Test
    fun timestampToDate_nullInput_returnsNull() {
        val result = converters.timestampToDate(null)

        assertNull(result)
    }

    /**
     * Test that timestampToDate handles zero timestamp
     */
    @Test
    fun timestampToDate_zeroTimestamp_returnsEpochDate() {
        val result = converters.timestampToDate(0L)

        assertEquals(Date(0L), result)
    }

    /**
     * Test that timestampToDate handles negative timestamp (before epoch)
     */
    @Test
    fun timestampToDate_negativeTimestamp_returnsDateBeforeEpoch() {
        val timestamp = -86400000L // One day before epoch
        val result = converters.timestampToDate(timestamp)

        assertEquals(Date(timestamp), result)
    }

    /**
     * Test that timestampToDate handles large timestamp (far future)
     */
    @Test
    fun timestampToDate_largeTimestamp_returnsDate() {
        val timestamp = 4102444800000L // 2100-01-01 00:00:00 UTC
        val result = converters.timestampToDate(timestamp)

        assertEquals(Date(timestamp), result)
    }

    // --- dateToTimestamp tests ---

    /**
     * Test that dateToTimestamp converts a valid Date to timestamp
     */
    @Test
    fun dateToTimestamp_validDate_returnsTimestamp() {
        val date = Date(1609459200000L)
        val result = converters.dateToTimestamp(date)

        assertEquals(1609459200000L, result)
    }

    /**
     * Test that dateToTimestamp returns null for null input
     */
    @Test
    fun dateToTimestamp_nullInput_returnsNull() {
        val result = converters.dateToTimestamp(null)

        assertNull(result)
    }

    /**
     * Test that dateToTimestamp handles epoch date
     */
    @Test
    fun dateToTimestamp_epochDate_returnsZero() {
        val date = Date(0L)
        val result = converters.dateToTimestamp(date)

        assertEquals(0L, result)
    }

    /**
     * Test that dateToTimestamp handles date before epoch
     */
    @Test
    fun dateToTimestamp_dateBeforeEpoch_returnsNegativeTimestamp() {
        val date = Date(-86400000L)
        val result = converters.dateToTimestamp(date)

        assertEquals(-86400000L, result)
    }

    // --- Roundtrip tests ---

    /**
     * Test that converting timestamp to Date and back preserves the value
     */
    @Test
    fun roundtrip_timestampToDateAndBack_preservesValue() {
        val originalTimestamp = 1672531199000L // 2022-12-31 23:59:59 UTC
        val date = converters.timestampToDate(originalTimestamp)
        val resultTimestamp = converters.dateToTimestamp(date)

        assertEquals(originalTimestamp, resultTimestamp)
    }

    /**
     * Test that converting Date to timestamp and back preserves the value
     */
    @Test
    fun roundtrip_dateToTimestampAndBack_preservesValue() {
        val originalDate = Date()
        val timestamp = converters.dateToTimestamp(originalDate)
        val resultDate = converters.timestampToDate(timestamp)

        assertEquals(originalDate, resultDate)
    }

    /**
     * Test roundtrip with null values
     */
    @Test
    fun roundtrip_nullValues_preservesNull() {
        val nullDate: Date? = null
        val nullTimestamp: Long? = null

        assertNull(converters.dateToTimestamp(nullDate))
        assertNull(converters.timestampToDate(nullTimestamp))
    }
}
