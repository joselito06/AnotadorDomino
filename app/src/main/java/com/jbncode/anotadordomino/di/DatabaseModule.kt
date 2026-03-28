package com.jbncode.anotadordomino.di

import android.content.Context
import androidx.room.Room
import com.jbncode.anotadordomino.data.local.daos.DominoDao
import com.jbncode.anotadordomino.data.local.database.DominoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDominoDatabase(@ApplicationContext context: Context): DominoDatabase {
        return Room.databaseBuilder(
            context,
            DominoDatabase::class.java,
            "domino_db"
        )
            .fallbackToDestructiveMigration() // Útil en fase de desarrollo
            .build()
    }

    @Provides
    @Singleton
    fun provideDominoDao(database: DominoDatabase): DominoDao {
        return database.dominoDao()
    }
}