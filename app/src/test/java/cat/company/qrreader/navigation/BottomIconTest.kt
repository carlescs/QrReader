package cat.company.qrreader.navigation

import cat.company.qrreader.R
import cat.company.qrreader.ui.components.navigation.items
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for BottomIcon class and navigation items
 *
 * Note: BottomIcon creation tests with Composable lambdas require instrumentation tests.
 * These unit tests focus on testing the predefined items list and its properties.
 */
class BottomIconTest {

    /**
     * Test items list has correct size
     */
    @Test
    fun items_hasCorrectSize() {
        assertEquals(2, items.size)
    }

    /**
     * Test first item is History
     */
    @Test
    fun items_firstItem_isHistory() {
        val historyItem = items[0]

        assertEquals(R.string.history, historyItem.labelRes)
        assertEquals("history", historyItem.route)
        assertNotNull(historyItem.icon)
    }

    /**
     * Test second item is Code Creator
     */
    @Test
    fun items_secondItem_isCodeCreator() {
        val codeCreatorItem = items[1]

        assertEquals(R.string.code_creator, codeCreatorItem.labelRes)
        assertEquals("codeCreator", codeCreatorItem.route)
        assertNotNull(codeCreatorItem.icon)
    }

    /**
     * Test items list is an ArrayList
     */
    @Test
    fun items_isArrayList() {
        assertNotNull(items)
        assertEquals(ArrayList::class.java, items::class.java)
    }

    /**
     * Test each item has unique route
     */
    @Test
    fun items_allHaveUniqueRoutes() {
        val routes = items.map { it.route }
        val uniqueRoutes = routes.toSet()

        assertEquals(routes.size, uniqueRoutes.size)
    }

    /**
     * Test each item has non-empty label
     */
    @Test
    fun items_allHaveNonEmptyLabels() {
        items.forEach { item ->
            assertNotNull(item.labelRes)
            assert(item.labelRes != 0) { "Label resource should not be 0" }
        }
    }

    /**
     * Test each item has non-empty route
     */
    @Test
    fun items_allHaveNonEmptyRoutes() {
        items.forEach { item ->
            assertNotNull(item.route)
            assert(item.route.isNotEmpty()) { "Route should not be empty" }
        }
    }

    /**
     * Test each item has non-null icon
     */
    @Test
    fun items_allHaveNonNullIcons() {
        items.forEach { item ->
            assertNotNull(item.icon)
        }
    }

    /**
     * Test finding item by route
     */
    @Test
    fun items_findByRoute_findsCorrectItem() {
        val historyItem = items.find { it.route == "history" }
        val codeCreatorItem = items.find { it.route == "codeCreator" }

        assertNotNull(historyItem)
        assertNotNull(codeCreatorItem)
        assertEquals(R.string.history, historyItem?.labelRes)
        assertEquals(R.string.code_creator, codeCreatorItem?.labelRes)
    }

    /**
     * Test finding item by label
     */
    @Test
    fun items_findByLabel_findsCorrectItem() {
        val historyItem = items.find { it.labelRes == R.string.history }
        val codeCreatorItem = items.find { it.labelRes == R.string.code_creator }

        assertNotNull(historyItem)
        assertNotNull(codeCreatorItem)
        assertEquals("history", historyItem?.route)
        assertEquals("codeCreator", codeCreatorItem?.route)
    }

    /**
     * Test items order is stable
     */
    @Test
    fun items_orderIsStable() {
        // History should always be first
        assertEquals(R.string.history, items[0].labelRes)
        // Code Creator should always be second
        assertEquals(R.string.code_creator, items[1].labelRes)
    }

    /**
     * Test BottomIcon properties are accessible
     */
    @Test
    fun bottomIcon_propertiesAreAccessible() {
        val item = items[0]

        // These should not throw
        val icon = item.icon
        val labelRes = item.labelRes
        val route = item.route

        assertNotNull(icon)
        assertNotNull(labelRes)
        assertNotNull(route)
    }
}
