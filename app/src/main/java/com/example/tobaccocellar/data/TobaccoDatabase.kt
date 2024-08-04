package com.example.tobaccocellar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//import androidx.room.migration.Migration
//import androidx.sqlite.db.SupportSQLiteDatabase

//val MIGRATION_1_2 = object : Migration(1, 2) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE items ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
//    }
//}

@Database(entities = [Items::class], version = 1, exportSchema = false)
abstract class TobaccoDatabase : RoomDatabase() {

    abstract fun itemsDao(): ItemsDao

    companion object {
        @Volatile
        private var Instance: TobaccoDatabase? = null

        fun getDatabase(context: Context): TobaccoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TobaccoDatabase::class.java, "tobacco_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}