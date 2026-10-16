package com.example.di

import android.content.Context
import com.example.data.local.LocalPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalPreferencesManager(
        @ApplicationContext context: Context
    ): LocalPreferencesManager {
        return LocalPreferencesManager(context)
    }
}
