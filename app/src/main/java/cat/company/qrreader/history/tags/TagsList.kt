package cat.company.qrreader.history.tags

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * List of tags
 */
@Composable
fun TagsList(db: BarcodesDb, viewModel: TagsViewModel = TagsViewModel(db), selectedTagId: Int?, selectTag: (Tag?) -> Unit) {
    viewModel.loadTags()
    val items by viewModel.tags.collectAsState(initial = emptyList())
    val ioCoroutine = CoroutineScope(Dispatchers.IO)
    val listState = rememberLazyListState()

    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                text = "No saved tags!",
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

