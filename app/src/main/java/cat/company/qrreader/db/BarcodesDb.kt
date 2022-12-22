package cat.company.qrreader.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cat.company.qrreader.db.converters.Converters
import cat.company.qrreader.db.daos.SavedBarcodeDao
import cat.company.qrreader.db.entities.SavedBarcode

@Database(entities = [SavedBarcode::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BarcodesDb :RoomDatabase(){
    abstract fun savedBarcodeDao(): SavedBarcodeDao
}