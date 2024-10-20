package com.example.tobaccocellar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE items_new (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            brand TEXT NOT NULL,
            blend TEXT NOT NULL,
            type TEXT NOT NULL,
            quantity INTEGER NOT NULL,
            favorite INTEGER NOT NULL,
            disliked INTEGER NOT NULL,
            notes TEXT NOT NULL
            )
            """
        )

        db.execSQL("""
            INSERT INTO items_new (id, brand, blend, type, quantity, favorite, disliked, notes)
            SELECT id, brand, blend, type, quantity, favorite, disliked, notes
            FROM items
            """
        )

        db.execSQL("DROP TABLE items")

        db.execSQL("ALTER TABLE items_new RENAME TO items")

        db.execSQL("CREATE UNIQUE INDEX index_items_brand_blend ON items (brand, blend)")
    }
}

@Database(entities = [Items::class], version = 4, exportSchema = true)
abstract class TobaccoDatabase : RoomDatabase() {

    abstract fun itemsDao(): ItemsDao

    companion object {
        @Volatile
        private var Instance: TobaccoDatabase? = null

        fun getDatabase(context: Context): TobaccoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TobaccoDatabase::class.java, "tobacco_database")
                    .addMigrations(MIGRATION_3_4)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}