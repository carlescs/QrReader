package cat.company.qrreader.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cat.company.qrreader.db.entities.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Dao for the tags
 */
@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<Tag>>

    @Insert
    suspend fun insertAll(vararg tags: Tag)

    @Update
    suspend fun updateItem(tag:Tag)

    @Delete
    suspend fun delete(tag:Tag)
}