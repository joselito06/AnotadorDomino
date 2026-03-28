package com.jbncode.anotadordomino.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val targetScore: Int,
    val modality: String, // Guardado como String (TEAM o INDIVIDUAL)
    val status: String
)

