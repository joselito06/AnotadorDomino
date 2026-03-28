package com.jbncode.anotadordomino.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1, // Si en el futuro agregas una columna, subes esto a 2
    exportSchema = false
)
abstract class DominoDatabase : RoomDatabase() {

    // Room autogenerará el código de esta función por detrás
    abstract fun dominoDao(): DominoDao

}