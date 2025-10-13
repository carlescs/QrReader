package cat.company.qrreader.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
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
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
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
                    .fillMaxWidth(),
                shape = SearchBarDefaults.inputFieldShape,
                colors = searchBarColors,
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets,
                content = { /* no suggestions */ },
            )

            if (items.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                    val msg = if (query.isBlank()) "No saved barcodes!" else "No results"
                    Text(text = msg, modifier = Modifier.align(CenterHorizontally))
                }
            } else {
                val clipboard: Clipboard = LocalClipboard.current
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
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
