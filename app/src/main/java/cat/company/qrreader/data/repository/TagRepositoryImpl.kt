package cat.company.qrreader.data.repository

import cat.company.qrreader.data.mapper.toDomainModel
import cat.company.qrreader.data.mapper.toEntity
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of TagRepository using Room database
 */
class TagRepositoryImpl(database: BarcodesDb) : TagRepository {

    private val tagDao = database.tagDao()

    override fun getAllTags(): Flow<List<TagModel>> {
        return tagDao.getAll().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun insertTags(vararg tags: TagModel) {
        tagDao.insertAll(*tags.map { it.toEntity() }.toTypedArray())
    }

    override fun updateTag(tag: TagModel) {
        tagDao.updateItem(tag.toEntity())
    }

    override fun deleteTag(tag: TagModel) {
        tagDao.delete(tag.toEntity())
    }
}

