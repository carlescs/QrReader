package cat.company.qrreader.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cat.company.qrreader.db.entities.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<Tag>>

    @Insert
    fun insertAll(vararg tags: Tag)

    @Update
    fun updateItem(tag:Tag)

    @Delete
    fun delete(tag:Tag)
}