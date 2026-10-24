package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Media
import com.example.domain.usecase.FavoriteActionsUseCase
import com.example.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.domain.usecase.SearchMediaUseCase
import com.example.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class SearchUiState(
    val query: String = "",
    val results: List<Media> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null,
    val favoriteIds: Set<Int> = emptySet()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMedia: SearchMediaUseCase,
    private val favoriteActions: FavoriteActionsUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var searchJob: Job? = null

    init {
        observeFavoriteIds
            .invoke()
            .onEach { ids -> _uiState.update { it.copy(favoriteIds = ids) } }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        if (query == _uiState.value.query) return
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                query = query,
                error = null,
                isLoading = false,
                results = if (query.isBlank()) emptyList() else it.results,
                hasSearched = if (query.isBlank()) false else it.hasSearched
            )
        }

        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return
        if (cleanQuery.length < 2) {
            _uiState.update { it.copy(results = emptyList(), hasSearched = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(cleanQuery)
        }
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.length < 2) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    hasSearched = query.isNotBlank(),
                    isLoading = false,
                    error = null
                )
            }
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch { performSearch(query) }
    }

    fun searchQuickTag(tag: String) {
        _uiState.update { it.copy(query = tag) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch { performSearch(tag.trim()) }
    }

    fun retry() = submitSearch()

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            SearchUiState(favoriteIds = it.favoriteIds)
        }
    }

    fun toggleFavorite(media: Media) {
        viewModelScope.launch {
            try {
                favoriteActions.toggle(media)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _messages.tryEmit(error.toUserMessage())
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, hasSearched = true, error = null) }
        try {
            val results = searchMedia(query)
            if (_uiState.value.query.trim() != query) return
            _uiState.update {
                it.copy(results = results, isLoading = false, error = null)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (_uiState.value.query.trim() != query) return
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isLoading = false,
                    error = error.toUserMessage()
                )
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
