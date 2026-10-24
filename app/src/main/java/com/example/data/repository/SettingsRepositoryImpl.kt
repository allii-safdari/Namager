package com.example.data.repository

import com.example.data.local.PreferencesDataSource
import com.example.domain.model.AppSettings
import com.example.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: PreferencesDataSource
) : SettingsRepository {

    private val _settings = MutableStateFlow(preferences.loadSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override suspend fun update(settings: AppSettings) {
        preferences.saveSettings(settings)
        _settings.value = settings
    }

    override suspend fun reset() {
        val defaults = AppSettings()
        preferences.saveSettings(defaults)
        _settings.value = defaults
    }
}
