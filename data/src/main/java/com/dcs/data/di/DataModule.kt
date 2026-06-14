package com.dcs.data.di

import android.content.Context
import androidx.room.Room
import com.dcs.data.local.dao.ThreatLogDao
import com.dcs.data.local.database.DcsDatabase
import com.dcs.data.repository.SettingsRepositoryImpl
import com.dcs.data.repository.ThreatLogRepositoryImpl
import com.dcs.domain.repository.SettingsRepository
import com.dcs.domain.repository.ThreatLogRepository
import com.dcs.security.EncryptedDatabaseFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindThreatLogRepository(impl: ThreatLogRepositoryImpl): ThreatLogRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
            encryptedDatabaseFactory: EncryptedDatabaseFactory
        ): DcsDatabase {
            return Room.databaseBuilder(
                context,
                DcsDatabase::class.java,
                "dcs_database"
            )
                .openHelperFactory(encryptedDatabaseFactory.createFactory())
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        @Singleton
        fun provideThreatLogDao(database: DcsDatabase): ThreatLogDao =
            database.threatLogDao()
    }
}
