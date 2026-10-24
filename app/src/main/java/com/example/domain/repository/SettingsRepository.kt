package com.example.domain.repository

import com.example.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    suspend fun update(settings: AppSettings)

    suspend fun reset()
}
