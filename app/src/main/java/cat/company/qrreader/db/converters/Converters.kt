package cat.company.qrreader.db.converters

import androidx.room.TypeConverter
import java.sql.Date

class Converters {
    @TypeConverter
    fun timestampToDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}