package cat.company.qrreader.features.history.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.company.qrreader.events.SharedEvents
import cat.company.qrreader.features.history.presentation.HistoryViewModel
import cat.company.qrreader.features.history.presentation.ui.components.BarcodeCard
import cat.company.qrreader.features.history.presentation.ui.components.HistoryModalDrawerContent
import cat.company.qrreader.features.history.presentation.ui.components.SimpleBarcodeCard
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * History screen composable displaying saved barcode history with search and filtering.
 *
 * This is the main entry point for the History feature, providing:
 * - Navigation drawer for tag filtering
 * - Search functionality for finding barcodes
 * - List of saved barcodes with interaction capabilities
 * - Empty states for no results or no saved items
 *
 * The screen follows MVVM architecture with state management handled by [HistoryViewModel].
 * User interactions are tracked via [snackbarHostState] for feedback messages.
 *
 * @param snackbarHostState SnackbarHostState for displaying user feedback messages like
 *                          "Copied to clipboard" or "Barcode deleted"
 * @param viewModel HistoryViewModel instance managing the screen state. Defaults to
 *                  Koin-injected instance via [koinViewModel]
 *
 * @see HistoryViewModel for state management
 * @see HistoryContent for the main content implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(
    snackbarHostState: SnackbarHostState,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val drawerState = remember { mutableStateOf(DrawerValue.Closed) }
    val selectedTagId by viewModel.selectedTagId.collectAsStateWithLifecycle()

    HistoryDrawerSetup(drawerState)

    ModalNavigationDrawer(
        drawerState = DrawerState(drawerState.value),
        drawerContent = {
            HistoryModalDrawerContent(selectedTagId) {
                viewModel.onTagSelected(it?.id)
                drawerState.value = DrawerValue.Closed
            }
        }
    ) {
        HistoryContent(
            snackbarHostState = snackbarHostState,
            viewModel = viewModel,
            selectedTagId = selectedTagId
        )
    }
}

/**
 * Sets up drawer toggle behavior via SharedEvents.
 *
 * Configures the global [SharedEvents.openSideBar] callback to toggle the navigation drawer
 * between open and closed states. This allows the top app bar's menu button to control
 * the drawer state.
 *
 * @param drawerState Mutable state holding the current [DrawerValue] (Open or Closed).
 *                    This state is modified when the user triggers the drawer toggle.
 *
 * @see SharedEvents.openSideBar for the global event mechanism
 */
@Composable
private fun HistoryDrawerSetup(drawerState: androidx.compose.runtime.MutableState<DrawerValue>) {
    SharedEvents.openSideBar = {
        drawerState.value = when (drawerState.value) {
            DrawerValue.Closed -> DrawerValue.Open
            else -> DrawerValue.Closed
        }
    }
}

/**
 * Main content of history screen with search and results.
 *
 * Orchestrates the main UI components including the search bar and results list.
 * Manages state collection from the ViewModel and delegates rendering to specialized composables.
 *
 * This composable:
 * - Collects barcode items and search query from ViewModel
 * - Renders search bar with all barcode items (for search suggestions)
 * - Renders filtered results list below search bar
 *
 * Note: All filtering logic (tag filtering, search queries, hideTaggedWhenNoTagSelected)
 * is handled at the database level via the ViewModel and repository.
 *
 * @param snackbarHostState SnackbarHostState for user feedback messages
 * @param viewModel HistoryViewModel managing barcode list state and search query
 * @param selectedTagId Currently selected tag ID for filtering, or null if no tag selected
 *
 * @see HistorySearchBar for the search interface
 * @see HistoryResults for the results display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryContent(
    snackbarHostState: SnackbarHostState,
    viewModel: HistoryViewModel,
    selectedTagId: Int?
) {
    val lazyListState = rememberLazyListState()
    val items by viewModel.savedBarcodes.collectAsStateWithLifecycle(initialValue = emptyList())
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsStateWithLifecycle()
    var searchActive by rememberSaveable { mutableStateOf(false) }


    Column(modifier = Modifier.fillMaxSize()) {
        val clipboard: Clipboard = LocalClipboard.current
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)

        HistorySearchBar(
            query = query,
            searchActive = searchActive,
            items = items,
            onQueryChange = viewModel::onQueryChange,
            onSearchActiveChange = { searchActive = it },
            sdf = sdf
        )

        FavoritesFilterBar(
            showOnlyFavorites = showOnlyFavorites,
            onToggle = viewModel::toggleFavoritesFilter
        )

        HistoryResults(
            searchActive = searchActive,
            displayData = BarcodeDisplayData(
                visibleItems = items,
                query = query,
                sdf = sdf
            ),
            interactionDeps = BarcodeInteractionDeps(
                clipboard = clipboard,
                snackbarHostState = snackbarHostState,
                viewModel = viewModel,
                lazyListState = lazyListState
            )
        )
    }
}


/**
 * Search bar component with integrated search functionality.
 *
 * Provides a Material3 SearchBar with:
 * - Expandable search interface
 * - Search icon that transforms to back button when expanded
 * - Clear button when query is not empty
 * - Live search suggestions showing all matching items
 * - Placeholder text guiding user input
 *
 * When expanded, displays a full-screen overlay with search results.
 * When collapsed, shows as a compact search bar at the top of the screen.
 *
 * @param query Current search query text entered by the user
 * @param searchActive Whether the search bar is currently expanded/active
 * @param items Complete list of barcode items for displaying search suggestions
 * @param onQueryChange Callback invoked when the search query text changes
 * @param onSearchActiveChange Callback invoked when search bar expand/collapse state changes
 * @param sdf SimpleDateFormat for formatting timestamps in search results
 *
 * @see SearchBarInputField for the input field implementation
 * @see SearchBarContent for the expanded search results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySearchBar(
    query: String,
    searchActive: Boolean,
    items: List<cat.company.qrreader.domain.model.BarcodeWithTagsModel>,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    sdf: SimpleDateFormat
) {
    val searchBarColors = SearchBarDefaults.colors()

    SearchBar(
        inputField = {
            SearchBarInputField(
                query = query,
                searchActive = searchActive,
                onQueryChange = onQueryChange,
                onSearchActiveChange = onSearchActiveChange,
                searchBarColors = searchBarColors
            )
        },
        expanded = searchActive,
        onExpandedChange = onSearchActiveChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = SearchBarDefaults.inputFieldShape,
        colors = searchBarColors,
        tonalElevation = SearchBarDefaults.TonalElevation,
        shadowElevation = SearchBarDefaults.ShadowElevation,
        windowInsets = WindowInsets(0.dp),
        content = {
            SearchBarContent(items = items, query = query, sdf = sdf, onDismissSearch = { onSearchActiveChange(false) })
        }
    )
}

/**
 * Input field for search bar with dynamic icons.
 *
 * Configures the Material3 SearchBar input field with:
 * - Query text handling
 * - Placeholder text for user guidance
 * - Leading icon (search/back button that changes based on state)
 * - Trailing icon (clear button, shown when query is not empty)
 * - Search submission handling
 *
 * The input field automatically focuses when the search bar expands and
 * provides a seamless typing experience with instant feedback.
 *
 * @param query Current search query text
 * @param searchActive Whether search is currently active/expanded
 * @param onQueryChange Callback invoked when query text changes
 * @param onSearchActiveChange Callback invoked when search state changes
 * @param searchBarColors Material3 SearchBarColors for consistent theming
 *
 * @see SearchBarLeadingIcon for the animated leading icon
 * @see SearchBarTrailingIcon for the clear button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarInputField(
    query: String,
    searchActive: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    searchBarColors: androidx.compose.material3.SearchBarColors
) {
    SearchBarDefaults.InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSearchActiveChange(false) },
        expanded = searchActive,
        onExpandedChange = onSearchActiveChange,
        placeholder = {
            Text(stringResource(R.string.search_placeholder), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingIcon = {
            SearchBarLeadingIcon(
                searchActive = searchActive,
                onQueryChange = onQueryChange,
                onSearchActiveChange = onSearchActiveChange
            )
        },
        trailingIcon = {
            SearchBarTrailingIcon(query = query, onClear = { onQueryChange("") })
        },
        colors = searchBarColors.inputFieldColors
    )
}

/**
 * Leading icon for search bar with animated state transition.
 *
 * Displays an animated icon that changes based on search state:
 * - **Collapsed state**: Search icon (magnifying glass) - tapping opens the search
 * - **Expanded state**: Back arrow icon - tapping closes search and clears query
 *
 * The transition between icons uses a Crossfade animation with 180ms duration
 * for smooth visual feedback.
 *
 * @param searchActive Whether search is currently active/expanded
 * @param onQueryChange Callback to clear the query text (called when closing search)
 * @param onSearchActiveChange Callback to toggle search active state
 *
 * @see Crossfade for the animation implementation
 */
@Composable
private fun SearchBarLeadingIcon(
    searchActive: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit
) {
    IconButton(
        onClick = {
            if (searchActive) {
                onQueryChange("")
                onSearchActiveChange(false)
            } else {
                onSearchActiveChange(true)
            }
        },
        modifier = Modifier.size(48.dp)
    ) {
        Crossfade(targetState = searchActive, animationSpec = tween(durationMillis = 180)) { expanded ->
            if (expanded) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel_search))
            } else {
                Icon(imageVector = Icons.Filled.Search, contentDescription = stringResource(R.string.open_search))
            }
        }
    }
}

/**
 * Trailing icon for search bar showing clear button when query is not empty.
 *
 * Displays a close (X) icon button that appears only when the user has entered
 * search text. Tapping this button clears the search query, allowing quick
 * reset without closing the search interface.
 *
 * @param query Current search query text - button only shows when not empty
 * @param onClear Callback invoked when user taps the clear button
 */
@Composable
private fun SearchBarTrailingIcon(
    query: String,
    onClear: () -> Unit
) {
    if (query.isNotEmpty()) {
        IconButton(onClick = onClear) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search))
        }
    }
}

/**
 * Content shown inside expanded search bar.
 *
 * Displays different content based on whether items are available:
 * - If items list is empty: Shows [EmptySearchState] with contextual message
 * - If items list has content: Shows [SearchResultsList] with matching barcodes
 *
 * This provides immediate feedback to users as they search through their barcode history.
 *
 * @param items List of barcode items to display as search results
 * @param query Current search query for empty state message context
 * @param sdf SimpleDateFormat for formatting barcode timestamps
 * @param onDismissSearch Callback to close the search interface when item is selected
 *
 * @see EmptySearchState for the empty results UI
 * @see SearchResultsList for the results list UI
 */
@Composable
private fun SearchBarContent(
    items: List<cat.company.qrreader.domain.model.BarcodeWithTagsModel>,
    query: String,
    sdf: SimpleDateFormat,
    onDismissSearch: () -> Unit
) {
    if (items.isEmpty()) {
        EmptySearchState(query = query)
    } else {
        SearchResultsList(items = items, sdf = sdf, onDismissSearch = onDismissSearch)
    }
}

/**
 * Empty state shown in search bar when no results are available.
 *
 * Displays a centered message guiding the user:
 * - If query is blank: "Start typing to search" - prompts user to enter text
 * - If query has text: "No results" - indicates no matches found
 *
 * This provides clear feedback and helps users understand the search state.
 *
 * @param query Current search query text to determine appropriate message
 */
@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = if (query.isBlank()) stringResource(R.string.start_typing_to_search) else stringResource(R.string.no_results),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * List of search results displayed in expanded search bar.
 *
 * Renders a scrollable list of barcode items matching the search query.
 * Uses [SimpleBarcodeCard] for a compact representation optimized for search results.
 * Each item can be tapped to dismiss the search interface and potentially navigate
 * to the full barcode details.
 *
 * @param items List of barcode items to display as search results
 * @param sdf SimpleDateFormat for formatting barcode timestamps
 * @param onDismissSearch Callback to close search interface when user selects an item
 *
 * @see SimpleBarcodeCard for the card implementation
 */
@Composable
private fun SearchResultsList(
    items: List<cat.company.qrreader.domain.model.BarcodeWithTagsModel>,
    sdf: SimpleDateFormat,
    onDismissSearch: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(items = items, key = { it.barcode.id }) { barcode ->
            SimpleBarcodeCard(
                barcode,
                sdf,
                onDismissSearch = onDismissSearch
            )
        }
    }
}

/**
 * Filter chip bar for showing only favorited barcodes.
 *
 * Displays a [FilterChip] that toggles the favorites-only filter.
 * When selected, only barcodes marked as favorites are shown in the results list.
 *
 * @param showOnlyFavorites Whether the favorites filter is currently active
 * @param onToggle Callback invoked when the user taps the chip to toggle the filter
 */
@Composable
private fun FavoritesFilterBar(
    showOnlyFavorites: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        FilterChip(
            selected = showOnlyFavorites,
            onClick = onToggle,
            label = { Text(stringResource(R.string.favorites)) },
            leadingIcon = {
                Icon(
                    imageVector = if (showOnlyFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )
    }
}

/**
 * Display data for barcode results.
 *
 * This data class encapsulates all presentation-related data needed to render the barcode
 * results list. It follows the Separation of Concerns principle by grouping only the data
 * that determines **what** to display, separate from interaction dependencies.
 *
 * @property visibleItems The list of barcode items to display, already filtered based on
 *                        search query and tag selection. This list represents the current
 *                        view state after all filters have been applied.
 * @property query The current search query string entered by the user. Used to determine
 *                 the appropriate empty state message when no items are visible.
 * @property sdf SimpleDateFormat instance for formatting barcode timestamps consistently
 *               across all displayed cards. Format: "dd-MM-yyyy HH:mm:ss"
 *
 * @see BarcodeInteractionDeps for interaction-related dependencies
 * @see HistoryResults for usage example
 */
private data class BarcodeDisplayData(
    val visibleItems: List<cat.company.qrreader.domain.model.BarcodeWithTagsModel>,
    val query: String,
    val sdf: SimpleDateFormat
)

/**
 * Interaction dependencies for barcode card actions.
 *
 * This data class encapsulates all dependencies required for user interactions with barcode
 * cards. It follows the Separation of Concerns principle by grouping only the components
 * that enable **how** users interact with the displayed barcodes, separate from display data.
 *
 * These dependencies are typically stable across re-compositions and provide the infrastructure
 * for user actions like copying, deleting, updating, and scrolling through barcode items.
 *
 * @property clipboard Android system clipboard for copying barcode content. Used when user
 *                     taps the copy button on a barcode card to copy the barcode value to
 *                     the system clipboard.
 * @property snackbarHostState Material3 SnackbarHostState for displaying user feedback messages
 *                             (e.g., "Copied to clipboard", "Barcode deleted"). Provides a
 *                             consistent way to communicate action results to the user.
 * @property viewModel HistoryViewModel instance that manages the barcode list state and
 *                     provides actions for updating or deleting barcodes. Acts as the bridge
 *                     between UI interactions and business logic.
 * @property lazyListState LazyListState for managing the scroll position and state of the
 *                         barcode results list. Enables scroll position preservation across
 *                         configuration changes and smooth scrolling animations.
 *
 * @see BarcodeDisplayData for display-related data
 * @see HistoryResults for usage example
 */
private data class BarcodeInteractionDeps(
    val clipboard: Clipboard,
    val snackbarHostState: SnackbarHostState,
    val viewModel: HistoryViewModel,
    val lazyListState: androidx.compose.foundation.lazy.LazyListState
)

/**
 * Main results area shown below search bar.
 *
 * Displays the main content area with filtered barcode results when search is not active.
 * Delegates to either an empty state composable or the full results list based on
 * whether any items are visible after filtering.
 *
 * When search is active (expanded), this composable renders nothing, allowing the
 * search bar's expanded content to take full screen.
 *
 * @param searchActive Whether search is currently active - hides results when true
 * @param displayData Display-related data containing visible items, query, and formatter
 * @param interactionDeps Interaction dependencies for user actions on barcode cards
 *
 * @see EmptyResultsState for the empty state UI
 * @see BarcodeResultsList for the results list UI
 */
@Composable
private fun HistoryResults(
    searchActive: Boolean,
    displayData: BarcodeDisplayData,
    interactionDeps: BarcodeInteractionDeps
) {
    if (!searchActive) {
        if (displayData.visibleItems.isEmpty()) {
            EmptyResultsState(query = displayData.query)
        } else {
            BarcodeResultsList(
                visibleItems = displayData.visibleItems,
                clipboard = interactionDeps.clipboard,
                snackbarHostState = interactionDeps.snackbarHostState,
                sdf = displayData.sdf,
                viewModel = interactionDeps.viewModel,
                lazyListState = interactionDeps.lazyListState
            )
        }
    }
}

/**
 * Empty state for main results area when no barcodes are visible.
 *
 * Displays a centered message indicating why no results are shown:
 * - If query is blank: "No saved barcodes!" - user hasn't saved any barcodes yet
 * - If query has text: "No results" - search/filter returned no matches
 *
 * This helps users understand whether they need to scan barcodes or adjust their filters.
 *
 * @param query Current search query text to determine appropriate empty message
 */
@Composable
private fun EmptyResultsState(query: String) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        val msg = if (query.isBlank()) stringResource(R.string.no_saved_barcodes) else stringResource(R.string.no_results)
        Text(text = msg, modifier = Modifier.align(CenterHorizontally), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * List of barcode cards in main results area.
 *
 * Renders a scrollable list of all visible barcode items using [BarcodeCard] for
 * detailed representation with full interaction capabilities (copy, delete, tag management).
 * The list maintains scroll position across configuration changes via [lazyListState].
 *
 * Each card provides:
 * - Barcode content display with formatted timestamp
 * - Copy to clipboard functionality
 * - Delete action with confirmation
 * - Tag management
 * - Visual indication of barcode type
 *
 * @param visibleItems Filtered list of barcode items to display
 * @param clipboard Android system clipboard for copy operations
 * @param snackbarHostState For displaying user feedback messages
 * @param sdf SimpleDateFormat for consistent timestamp formatting
 * @param viewModel HistoryViewModel for barcode actions (update, delete)
 * @param lazyListState State for managing scroll position and animations
 *
 * @see BarcodeCard for the detailed card implementation
 */
@Composable
private fun BarcodeResultsList(
    visibleItems: List<cat.company.qrreader.domain.model.BarcodeWithTagsModel>,
    clipboard: Clipboard,
    snackbarHostState: SnackbarHostState,
    sdf: SimpleDateFormat,
    viewModel: HistoryViewModel,
    lazyListState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = lazyListState
    ) {
        items(items = visibleItems, key = { it.barcode.id }) { barcode ->
            BarcodeCard(
                clipboard,
                barcode,
                snackbarHostState,
                sdf,
                viewModel
            )
        }
    }
}


