package com.example.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FavoriteGroup
import com.example.domain.model.Media
import com.example.domain.model.Season
import com.example.domain.repository.LibraryRepository
import com.example.domain.usecase.FavoriteActionsUseCase
import com.example.domain.usecase.GetMediaByIdUseCase
import com.example.domain.usecase.GetSeasonsUseCase
import com.example.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val media: Media? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val seasons: List<Season> = emptyList(),
    val seasonsLoading: Boolean = false,
    val seasonsError: String? = null,
    val favoriteGroups: List<FavoriteGroup> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val watchedEpisodes: Set<String> = emptySet()
) {
    val isFavorite: Boolean
        get() = media?.let { favoriteIds.contains(it.id) } == true
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMediaById: GetMediaByIdUseCase,
    private val getSeasons: GetSeasonsUseCase,
    private val favoriteActions: FavoriteActionsUseCase,
    libraryRepository: LibraryRepository
) : ViewModel() {

    private val mediaId: Int = savedStateHandle.get<Int>(ARG_MEDIA_ID) ?: -1

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        libraryRepository.groups
            .onEach { groups -> _uiState.update { it.copy(favoriteGroups = groups) } }
            .launchIn(viewModelScope)
        libraryRepository.items
            .onEach { items ->
                _uiState.update { state ->
                    state.copy(favoriteIds = items.mapTo(LinkedHashSet()) { it.mediaId })
                }
            }
            .launchIn(viewModelScope)
        libraryRepository.watchedEpisodes
            .onEach { watched -> _uiState.update { it.copy(watchedEpisodes = watched) } }
            .launchIn(viewModelScope)
        loadMedia()
    }

    fun retry() = loadMedia()

    fun retrySeasons() {
        val media = _uiState.value.media ?: return
        if (media.isSeries) loadSeasons(media)
    }

    fun addToGroup(groupId: String) {
        val media = _uiState.value.media ?: return
        viewModelScope.launch {
            try {
                favoriteActions.add(media, groupId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _messages.tryEmit(error.toUserMessage())
            }
        }
    }

    fun removeFromFavorites() {
        val media = _uiState.value.media ?: return
        viewModelScope.launch {
            try {
                favoriteActions.remove(media.id)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _messages.tryEmit(error.toUserMessage())
            }
        }
    }

    private fun loadMedia() {
        if (mediaId <= 0) {
            _uiState.update {
                it.copy(isLoading = false, error = "شناسه محتوا معتبر نیست.")
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val media = getMediaById(mediaId)
                if (media == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "اطلاعات این عنوان در دسترس نیست. به فهرست بازگردید."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            media = media,
                            isLoading = false,
                            error = null,
                            seasons = emptyList(),
                            seasonsError = null
                        )
                    }
                    if (media.isSeries) loadSeasons(media)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = error.toUserMessage())
                }
            }
        }
    }

    private fun loadSeasons(media: Media) {
        _uiState.update { it.copy(seasonsLoading = true, seasonsError = null) }
        viewModelScope.launch {
            try {
                val seasons = getSeasons(media.id)
                _uiState.update {
                    it.copy(seasons = seasons, seasonsLoading = false, seasonsError = null)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(seasonsLoading = false, seasonsError = error.toUserMessage())
                }
            }
        }
    }

    companion object {
        const val ARG_MEDIA_ID = "mediaId"
    }
}
