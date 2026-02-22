package cat.company.qrreader.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cat.company.qrreader.db.converters.Converters
import cat.company.qrreader.db.daos.TagDao
import cat.company.qrreader.db.daos.SavedBarcodeDao
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.SavedBarcode

/**
 * Database for the barcodes
 */
@Database(entities = [SavedBarcode::class, Tag::class, BarcodeTagCrossRef::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BarcodesDb :RoomDatabase(){
    abstract fun savedBarcodeDao(): SavedBarcodeDao
    abstract fun tagDao(): TagDao
}