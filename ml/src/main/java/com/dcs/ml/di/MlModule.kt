package com.dcs.ml.di

import com.dcs.domain.usecase.MessageAnalyzer
import com.dcs.ml.MessageAnalyzerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {

    /** Bind the ML pipeline implementation to the domain-layer interface. */
    @Binds
    @Singleton
    abstract fun bindMessageAnalyzer(impl: MessageAnalyzerImpl): MessageAnalyzer
}
