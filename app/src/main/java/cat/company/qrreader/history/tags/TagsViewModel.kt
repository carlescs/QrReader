package cat.company.qrreader.history.tags

import androidx.lifecycle.ViewModel
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.Tag
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel for the tags
 */
class TagsViewModel(val db: BarcodesDb):ViewModel(){
    lateinit var tags: Flow<List<Tag>>

    fun loadTags() {
        tags = db.tagDao().getAll()
    }

    fun deleteTag(tag: Tag) {
        db.tagDao().delete(tag)
    }
}