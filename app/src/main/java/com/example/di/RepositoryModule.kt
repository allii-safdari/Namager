package com.example.di

import com.example.data.repository.LibraryRepositoryImpl
import com.example.data.repository.MediaRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.domain.repository.CatalogRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: MediaRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
