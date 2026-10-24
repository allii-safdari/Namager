package com.example.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AppSettings
import com.example.domain.repository.SettingsRepository
import com.example.domain.usecase.FavoriteActionsUseCase
import com.example.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val url: String = "",
    val title: String = "",
    val subtitle: String? = null,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favoriteActions: FavoriteActionsUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    val uiState = PlayerUiState(
        url = savedStateHandle.get<String>(ARG_URL).orEmpty(),
        title = savedStateHandle.get<String>(ARG_TITLE).orEmpty(),
        subtitle = savedStateHandle.get<String>(ARG_SUBTITLE)?.takeIf { it.isNotBlank() },
        error = if (savedStateHandle.get<String>(ARG_URL).isNullOrBlank()) {
            "آدرس پخش معتبر نیست."
        } else {
            null
        }
    )

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        val mediaId = savedStateHandle.get<Int>(ARG_MEDIA_ID) ?: -1
        val seasonId = savedStateHandle.get<Int>(ARG_SEASON_ID) ?: -1
        val episodeId = savedStateHandle.get<Int>(ARG_EPISODE_ID) ?: -1
        if (mediaId > 0 && seasonId > 0 && episodeId > 0) {
            viewModelScope.launch {
                runCatching {
                    favoriteActions.markEpisodeWatched(mediaId, seasonId, episodeId)
                }.onFailure { _messages.tryEmit(it.toUserMessage()) }
            }
        }
    }

    companion object {
        const val ARG_URL = "url"
        const val ARG_TITLE = "title"
        const val ARG_SUBTITLE = "subtitle"
        const val ARG_MEDIA_ID = "mediaId"
        const val ARG_SEASON_ID = "seasonId"
        const val ARG_EPISODE_ID = "episodeId"
    }
}
