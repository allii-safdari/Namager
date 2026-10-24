package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AppSettings
import com.example.domain.model.ThemeMode
import com.example.domain.repository.SettingsRepository
import com.example.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun setTheme(themeMode: ThemeMode) = update { it.copy(themeMode = themeMode) }

    fun setAccentColor(colorHex: Long) = update { it.copy(accentColorHex = colorHex) }

    fun setSeekJumpSeconds(seconds: Int) = update { it.copy(seekJumpSeconds = seconds) }

    fun setSubtitleSize(sizeSp: Int) = update { it.copy(subtitleFontSizeSp = sizeSp) }

    fun reset() {
        viewModelScope.launch {
            runCatching { repository.reset() }
                .onFailure { _messages.tryEmit(it.toUserMessage()) }
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(settings.value)
        if (updated == settings.value) return
        viewModelScope.launch {
            try {
                repository.update(updated)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _messages.tryEmit(error.toUserMessage())
            }
        }
    }
}
