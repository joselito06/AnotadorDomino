package com.jbncode.anotadordomino.di

import com.jbncode.anotadordomino.data.ml.YoloxPipCounter
import com.jbncode.anotadordomino.domain.ml.PipCounter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {
    @Binds
    @Singleton
    abstract fun bindPipCounter(impl: YoloxPipCounter): PipCounter
}