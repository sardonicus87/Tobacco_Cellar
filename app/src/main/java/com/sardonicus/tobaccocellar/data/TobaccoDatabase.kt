package com.sardonicus.tobaccocellar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sardonicus.tobaccocellar.data.multiDeviceSync.PendingSyncOperation
import com.sardonicus.tobaccocellar.data.multiDeviceSync.PendingSyncOperationDao

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS 'tins' (
                    'tinId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    'itemsId' INTEGER NOT NULL,
                    'tinLabel' TEXT NOT NULL DEFAULT '',
                    'container' TEXT NOT NULL DEFAULT '',
                    'tinQuantity' REAL NOT NULL,
                    'unit' TEXT NOT NULL DEFAULT '',
                    'manufactureDate' INTEGER,
                    'cellarDate' INTEGER,
                    'openDate' INTEGER,
                    FOREIGN KEY('itemsId') REFERENCES 'items' ('id')
                    ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """
        )
        db.execSQL(
            """
                CREATE UNIQUE INDEX IF NOT EXISTS
                'index_tins_itemsId_tinLabel'
                ON 'tins' ('itemsId', 'tinLabel')
            """
        )
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS 'components' (
                    'componentId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    'componentName' TEXT NOT NULL DEFAULT ''
                )
            """
        )
        db.execSQL(
            """
                CREATE UNIQUE INDEX IF NOT EXISTS
                'index_components_componentName'
                ON 'components' ('componentName')
            """
        )
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS 'items_components_cross_ref' (
                    'itemId' INTEGER NOT NULL,
                    'componentId' INTEGER NOT NULL,
                    PRIMARY KEY ('itemId', 'componentId'),
                    FOREIGN KEY('itemId') REFERENCES 'items' ('id')
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY('componentId') REFERENCES 'components' ('componentId')
                    ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """
        )
        db.execSQL(
            """
                CREATE INDEX IF NOT EXISTS
                'index_items_components_cross_ref_componentId'
                ON 'items_components_cross_ref' ('componentId')
            """
        )
        db.execSQL("ALTER TABLE 'items' ADD COLUMN 'subGenre' TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE 'items' ADD COLUMN 'cut' TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE 'items' ADD COLUMN 'inProduction' INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS 'flavoring' (
                    'flavoringId' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    'flavoringName' TEXT NOT NULL DEFAULT ''
                )
            """
        )
        db.execSQL(
            """
                CREATE UNIQUE INDEX IF NOT EXISTS
                'index_flavoring_flavoringName'
                ON 'flavoring' ('flavoringName')
            """
        )
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS 'items_flavoring_cross_ref' (
                    'itemId' INTEGER NOT NULL,
                    'flavoringId' INTEGER NOT NULL,
                    PRIMARY KEY ('itemId', 'flavoringId'),
                    FOREIGN KEY('itemId') REFERENCES 'items' ('id')
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY('flavoringId') REFERENCES 'flavoring' ('flavoringId')
                    ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """
        )
        db.execSQL(
            """
                CREATE INDEX IF NOT EXISTS
                'index_items_flavoring_cross_ref_flavoringId'
                ON 'items_flavoring_cross_ref' ('flavoringId')
            """
        )
        db.execSQL("ALTER TABLE 'tins' ADD COLUMN 'finished' INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'items' ADD COLUMN 'rating' REAL")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'items' ADD COLUMN 'syncTins' INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE 'items' ADD COLUMN 'lastModified' INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE 'tins' ADD COLUMN 'lastModified' INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS 'pending_sync_operations' (
                    'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    'operationType' TEXT NOT NULL,
                    'entityType' TEXT NOT NULL,
                    'entityId' TEXT NOT NULL,
                    'payload' TEXT NOT NULL,
                    'timestamp' INTEGER NOT NULL
                )
            """
        )
    }
}

@Database(
    entities = [
        Items::class,
        Tins::class,
        Components::class,
        ItemsComponentsCrossRef::class,
        Flavoring::class,
        ItemsFlavoringCrossRef::class,
        PendingSyncOperation::class
    ],
    version = 5,
    exportSchema = true
)
abstract class TobaccoDatabase : RoomDatabase() {

    abstract fun itemsDao(): ItemsDao
    abstract fun pendingSyncOperationDao(): PendingSyncOperationDao

    companion object {
        @Volatile
        private var Instance: TobaccoDatabase? = null

        fun getDatabase(context: Context): TobaccoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TobaccoDatabase::class.java, "tobacco_database")
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
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