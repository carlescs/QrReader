package cat.company.qrreader.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for the tag
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: String
)
