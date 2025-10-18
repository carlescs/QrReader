package cat.company.qrreader.history

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.events.SharedEvents
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * History screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(
    db: BarcodesDb,
    snackbarHostState: SnackbarHostState,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(db))
) {
    val drawerState = remember { mutableStateOf(DrawerValue.Closed) }
    val selectedTagId by viewModel.selectedTagId.collectAsStateWithLifecycle()

    SharedEvents.openSideBar = {
        when (drawerState.value) {
            DrawerValue.Closed -> drawerState.value = DrawerValue.Open
            else -> drawerState.value = DrawerValue.Closed
        }
    }
    ModalNavigationDrawer(
        drawerState = DrawerState(drawerState.value),
        drawerContent = {
            HistoryModalDrawerContent(db, selectedTagId) {
                viewModel.onTagSelected(it?.id)
                drawerState.value = DrawerValue.Closed
            }
        }) {
        val lazyListState = rememberLazyListState()
        val items by viewModel.savedBarcodes.collectAsStateWithLifecycle(initialValue = emptyList())
        val query by viewModel.searchQuery.collectAsStateWithLifecycle()
        var searchActive by rememberSaveable { mutableStateOf(false) }

        // Top-level layout with search bar and results
        Column(modifier = Modifier.fillMaxSize()) {
            val onActiveChange: (Boolean) -> Unit = { expanded -> searchActive = expanded }
            val searchBarColors = SearchBarDefaults.colors()
            val clipboard: Clipboard = LocalClipboard.current
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)

            // Integrated SearchBar: when expanded, leading icon becomes a back/cancel icon (animated)
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { viewModel.onQueryChange(it) },
                        onSearch = { /*query*/ _ -> searchActive = false },
                        expanded = searchActive,
                        onExpandedChange = onActiveChange,
                        placeholder = { Text("Search barcodes, titles, descriptions") },
                        leadingIcon = {
                            // Keep consistent size for the leading icon's touch target to avoid layout jumps.
                            IconButton(
                                onClick = {
                                    if (searchActive) {
                                        viewModel.onQueryChange("")
                                        searchActive = false
                                    } else {
                                        onActiveChange(true)
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Crossfade(targetState = searchActive, animationSpec = tween(durationMillis = 180)) { expanded ->
                                    if (expanded) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel search")
                                    } else {
                                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Open search")
                                    }
                                }
                            }
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        colors = searchBarColors.inputFieldColors,
                    )
                },
                expanded = searchActive,
                onExpandedChange = onActiveChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = SearchBarDefaults.inputFieldShape,
                colors = searchBarColors,
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = WindowInsets(0.dp),
                content = {
                    // Show search suggestions/results
                    if (items.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = CenterHorizontally
                        ) {
                            Text(text = if (query.isBlank()) "Start typing to search" else "No results")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(items = items, key = { it.barcode.id }) { barcode ->
                                SimpleBarcodeCard(
                                    barcode,
                                    sdf,
                                    onDismissSearch = { searchActive = false }
                                )
                            }
                        }
                    }
                },
            )

            // Show results below search bar when not in search mode
            if (!searchActive) {
                if (items.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                        val msg = if (query.isBlank()) "No saved barcodes!" else "No results"
                        Text(text = msg, modifier = Modifier.align(CenterHorizontally))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp), state = lazyListState
                    ) {
                        items(items = items, key = { it.barcode.id }) { barcode ->
                            BarcodeCard(
                                clipboard,
                                barcode,
                                snackbarHostState,
                                sdf,
                                db,
                                viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
