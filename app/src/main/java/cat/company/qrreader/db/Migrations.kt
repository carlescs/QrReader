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
    }
}