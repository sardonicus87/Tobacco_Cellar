package com.sardonicus.tobaccocellar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//val MIGRATION_1_2 = object : Migration(1, 2) {
//    override fun migrate(db: SupportSQLiteDatabase) {
//
//    }
//}

@Database(entities = [Items::class], version = 1, exportSchema = true)
abstract class TobaccoDatabase : RoomDatabase() {

    abstract fun itemsDao(): ItemsDao

    companion object {
        @Volatile
        private var Instance: TobaccoDatabase? = null

        fun getDatabase(context: Context): TobaccoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TobaccoDatabase::class.java, "tobacco_database")
//                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }

        fun getDatabaseVersion(context: Context): Int {
            val database = getDatabase(context)
            return database.openHelper.readableDatabase.version
        }

    }
}