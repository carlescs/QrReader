package cat.company.qrreader.features.tags.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.koinViewModel

/**
 * Tag filter list for the history sidebar drawer.
 *
 * Renders tags as compact [TagFilterItem] rows using Material3 NavigationDrawerItem
 * styling: colored label icon, normal-color text, and built-in selection highlight.
 * Edit and delete actions are available inline on each item.
 */
@Composable
fun TagsFilterList(
    viewModel: TagsViewModel = koinViewModel(),
    selectedTagId: Int?,
    selectTag: (TagModel?) -> Unit
) {
    viewModel.loadTags()
    val items by viewModel.tags.collectAsState(initial = emptyList())
    val tagCounts by viewModel.tagBarcodeCounts.collectAsState(initial = emptyMap())
    val ioCoroutine = rememberCoroutineScope()
    val listState = rememberLazyListState()

    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.no_saved_tags),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(items = items) { tag ->
                TagFilterItem(
                    tag = tag,
                    isSelected = tag.id == selectedTagId,
                    onSelectTag = selectTag,
                    ioCoroutine = ioCoroutine,
                    viewModel = viewModel,
                    barcodeCount = tagCounts[tag.id] ?: 0
                )
            }
        }
    }
}

/**
 * List of tags
 */
@Composable
fun TagsList(viewModel: TagsViewModel = koinViewModel(), selectedTagId: Int?, selectTag: (TagModel?) -> Unit) {
    viewModel.loadTags()
    val items by viewModel.tags.collectAsState(initial = emptyList())
    val ioCoroutine = CoroutineScope(Dispatchers.IO)
    val listState = rememberLazyListState()

    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.no_saved_tags),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            state = listState
        ) {
            items(items = items) {
                TagCard(it, selectedTagId, selectTag, ioCoroutine, viewModel)
            }
        }
    }
}

