package cat.company.qrreader.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migrations {
    companion object{
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE saved_barcodes ADD format Int NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2,3){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE saved_barcodes ADD title VARCHAR(100)")
                database.execSQL("ALTER TABLE saved_barcodes ADD description VARCHAR(200)")
            }
        }
        val MIGRATION_3_4 = object : Migration(3,4){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE tags(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, color VARCHAR(9) NOT NULL)")
                database.execSQL("CREATE TABLE barcode_tag_cross_ref(barcodeId INTEGER NOT NULL, tagId INTEGER NOT NULL, PRIMARY KEY(barcodeId, tagId), FOREIGN KEY(barcodeId) REFERENCES saved_barcodes(id) ON DELETE CASCADE, FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE)")
            }
        }
    }
}