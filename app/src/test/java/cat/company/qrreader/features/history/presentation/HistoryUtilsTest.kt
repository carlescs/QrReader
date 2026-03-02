package cat.company.qrreader.features.history.presentation

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.history.presentation.ui.components.getBarcodeIcon
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.features.history.presentation.ui.components.isIsbn
import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

/**
 * Unit tests for history Utils functions (getBarcodeIcon and getTitle)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class HistoryUtilsTest {

    // --- getBarcodeIcon tests ---

    /**
     * Test getBarcodeIcon returns Link icon for URL type
     */
    @Test
    fun getBarcodeIcon_urlType_returnsLinkIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_URL)
        assertEquals("Filled.Link", icon.name)
    }

    /**
     * Test getBarcodeIcon returns AccountBox icon for Contact type
     */
    @Test
    fun getBarcodeIcon_contactType_returnsAccountBoxIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_CONTACT_INFO)
        assertEquals("Filled.AccountBox", icon.name)
    }

    /**
     * Test getBarcodeIcon returns Email icon for Email type
     */
    @Test
    fun getBarcodeIcon_emailType_returnsEmailIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_EMAIL)
        assertEquals("Filled.Email", icon.name)
    }

    /**
     * Test getBarcodeIcon returns Phone icon for Phone type
     */
    @Test
    fun getBarcodeIcon_phoneType_returnsPhoneIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_PHONE)
        assertEquals("Filled.Phone", icon.name)
    }

    /**
     * Test getBarcodeIcon returns QrCode icon for unknown type
     */
    @Test
    fun getBarcodeIcon_unknownType_returnsQrCodeIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_UNKNOWN)
        assertEquals("Filled.QrCode", icon.name)
    }

    /**
     * Test getBarcodeIcon returns QrCode icon for text type
     */
    @Test
    fun getBarcodeIcon_textType_returnsQrCodeIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_TEXT)
        assertEquals("Filled.QrCode", icon.name)
    }

    /**
     * Test getBarcodeIcon returns QrCode icon for product type
     */
    @Test
    fun getBarcodeIcon_productType_returnsQrCodeIcon() {
        val icon = getBarcodeIcon(Barcode.TYPE_PRODUCT)
        assertEquals("Filled.QrCode", icon.name)
    }

    // --- getTitle tests ---

    /**
     * Test getTitle returns custom title when set
     */
    @Test
    fun getTitle_withCustomTitle_returnsCustomTitle() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_TEXT,
            format = Barcode.FORMAT_QR_CODE,
            title = "My Custom Title",
            barcode = "test"
        )

        assertEquals("My Custom Title", getTitle(barcode))
    }

    /**
     * Test getTitle returns custom title with leading/trailing spaces trimmed check
     */
    @Test
    fun getTitle_withWhitespaceOnlyTitle_returnsDefaultTitle() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_URL,
            format = Barcode.FORMAT_QR_CODE,
            title = "   ",
            barcode = "https://example.com"
        )

        assertEquals("URL", getTitle(barcode))
    }

    /**
     * Test getTitle returns "URL" for URL type without custom title
     */
    @Test
    fun getTitle_urlTypeNoTitle_returnsUrl() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_URL,
            format = Barcode.FORMAT_QR_CODE,
            title = null,
            barcode = "https://example.com"
        )

        assertEquals("URL", getTitle(barcode))
    }

    /**
     * Test getTitle returns "Contact" for contact type without custom title
     */
    @Test
    fun getTitle_contactTypeNoTitle_returnsContact() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_CONTACT_INFO,
            format = Barcode.FORMAT_QR_CODE,
            title = null,
            barcode = "contact info"
        )

        assertEquals("Contact", getTitle(barcode))
    }

    /**
     * Test getTitle returns "Email" for email type without custom title
     */
    @Test
    fun getTitle_emailTypeNoTitle_returnsEmail() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_EMAIL,
            format = Barcode.FORMAT_QR_CODE,
            title = null,
            barcode = "test@example.com"
        )

        assertEquals("Email", getTitle(barcode))
    }

    /**
     * Test getTitle returns "Phone" for phone type without custom title
     */
    @Test
    fun getTitle_phoneTypeNoTitle_returnsPhone() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PHONE,
            format = Barcode.FORMAT_QR_CODE,
            title = null,
            barcode = "+1234567890"
        )

        assertEquals("Phone", getTitle(barcode))
    }

    /**
     * Test getTitle returns "EAN13" for EAN-13 format without custom title
     */
    @Test
    fun getTitle_ean13FormatNoTitle_returnsEan13() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "1234567890123"
        )

        assertEquals("EAN13", getTitle(barcode))
    }

    /**
     * Test getTitle returns "EAN8" for EAN-8 format without custom title
     */
    @Test
    fun getTitle_ean8FormatNoTitle_returnsEan8() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_8,
            title = null,
            barcode = "12345678"
        )

        assertEquals("EAN8", getTitle(barcode))
    }

    /**
     * Test getTitle returns "UPC-A" for UPC-A format without custom title
     */
    @Test
    fun getTitle_upcaFormatNoTitle_returnsUpca() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_UPC_A,
            title = null,
            barcode = "123456789012"
        )

        assertEquals("UPC-A", getTitle(barcode))
    }

    /**
     * Test getTitle returns "UPC-E" for UPC-E format without custom title
     */
    @Test
    fun getTitle_upceFormatNoTitle_returnsUpce() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_UPC_E,
            title = null,
            barcode = "01234567"
        )

        assertEquals("UPC-E", getTitle(barcode))
    }

    /**
     * Test getTitle returns "Barcode" for unknown format without custom title
     */
    @Test
    fun getTitle_unknownFormatNoTitle_returnsBarcode() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_TEXT,
            format = Barcode.FORMAT_QR_CODE,
            title = null,
            barcode = "some text"
        )

        assertEquals("Barcode", getTitle(barcode))
    }

    /**
     * Test getTitle returns custom title even with empty string (if trimmed is not empty)
     */
    @Test
    fun getTitle_withValidTitleWithSpaces_returnsTrimmedTitle() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_TEXT,
            format = Barcode.FORMAT_QR_CODE,
            title = "  Valid Title  ",
            barcode = "test"
        )

        // The function returns the title as-is when not empty after trim
        assertEquals("  Valid Title  ", getTitle(barcode))
    }

    /**
     * Test getTitle with empty title string returns default
     */
    @Test
    fun getTitle_withEmptyTitle_returnsDefault() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_URL,
            format = Barcode.FORMAT_QR_CODE,
            title = "",
            barcode = "https://example.com"
        )

        assertEquals("URL", getTitle(barcode))
    }

    /**
     * Test getTitle returns "ISBN" for TYPE_ISBN barcodes
     */
    @Test
    fun getTitle_isbnTypeNoTitle_returnsIsbn() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_ISBN,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "9780743273565"
        )

        assertEquals("ISBN", getTitle(barcode))
    }

    /**
     * Test getTitle returns "ISBN" for EAN-13 with 978 prefix even when type is not TYPE_ISBN
     */
    @Test
    fun getTitle_ean13With978PrefixNoTitle_returnsIsbn() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "9780743273565"
        )

        assertEquals("ISBN", getTitle(barcode))
    }

    /**
     * Test getTitle returns "ISBN" for EAN-13 with 979 prefix
     */
    @Test
    fun getTitle_ean13With979PrefixNoTitle_returnsIsbn() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "9791032318881"
        )

        assertEquals("ISBN", getTitle(barcode))
    }

    /**
     * Test getTitle returns "EAN13" for EAN-13 without ISBN prefix
     */
    @Test
    fun getTitle_ean13WithoutIsbnPrefixNoTitle_returnsEan13() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "8412345678901"
        )

        assertEquals("EAN13", getTitle(barcode))
    }

    // --- isIsbn tests ---

    /**
     * Test isIsbn returns true for TYPE_ISBN barcodes
     */
    @Test
    fun isIsbn_isbnType_returnsTrue() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_ISBN,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "9780743273565"
        )

        assertTrue(isIsbn(barcode))
    }

    /**
     * Test isIsbn returns true for EAN-13 with 978 prefix
     */
    @Test
    fun isIsbn_ean13With978Prefix_returnsTrue() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "9780743273565"
        )

        assertTrue(isIsbn(barcode))
    }

    /**
     * Test isIsbn returns true for EAN-13 with 979 prefix
     */
    @Test
    fun isIsbn_ean13With979Prefix_returnsTrue() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "9791032318881"
        )

        assertTrue(isIsbn(barcode))
    }

    /**
     * Test isIsbn returns false for regular EAN-13
     */
    @Test
    fun isIsbn_regularEan13_returnsFalse() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "8412345678901"
        )

        assertFalse(isIsbn(barcode))
    }

    /**
     * Test isIsbn returns false for non-EAN-13 formats
     */
    @Test
    fun isIsbn_upcAFormat_returnsFalse() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_UPC_A,
            title = null,
            barcode = "012345678901"
        )

        assertFalse(isIsbn(barcode))
    }

    /**
     * Test isIsbn returns false for EAN-13 with ISBN prefix but non-digit content
     */
    @Test
    fun isIsbn_ean13With978PrefixNonDigit_returnsFalse() {
        val barcode = BarcodeModel(
            id = 1,
            date = Date(),
            type = Barcode.TYPE_PRODUCT,
            format = Barcode.FORMAT_EAN_13,
            title = null,
            barcode = "978074327356X"
        )

        assertFalse(isIsbn(barcode))
    }
}
