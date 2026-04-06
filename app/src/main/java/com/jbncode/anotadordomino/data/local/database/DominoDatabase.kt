package com.jbncode.anotadordomino.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jbncode.anotadordomino.data.local.daos.DominoDao
import com.jbncode.anotadordomino.data.local.entities.GameEntity
import com.jbncode.anotadordomino.data.local.entities.ParticipantEntity
import com.jbncode.anotadordomino.data.local.entities.RoundEntity

@Database(
    entities = [
        GameEntity::class,
        ParticipantEntity::class,
        RoundEntity::class
    ],
    version = 2, // Si en el futuro agregas una columna, subes esto a 2
    exportSchema = false
)
abstract class DominoDatabase : RoomDatabase() {

    // Room autogenerará el código de esta función por detrás
    abstract fun dominoDao(): DominoDao

    companion object {
        /**
         * Migración 1→2: agrega avatarType (TEXT NOT NULL DEFAULT 'PRESET_STAR')
         * y photoUri (TEXT nullable) a la tabla participants.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE participants ADD COLUMN avatarType TEXT NOT NULL DEFAULT 'PRESET_STAR'"
                )
                db.execSQL(
                    "ALTER TABLE participants ADD COLUMN photoUri TEXT"
                )
            }
        }
    }

}