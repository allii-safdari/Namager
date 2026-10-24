package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.AppException
import com.example.domain.model.FilterOrder
import com.example.domain.model.Genre
import com.example.domain.model.Media
import com.example.domain.model.MediaKind
import com.example.domain.usecase.FavoriteActionsUseCase
import com.example.domain.usecase.GetCatalogPageUseCase
import com.example.domain.usecase.GetFeaturedMediaUseCase
import com.example.domain.usecase.GetGenresUseCase
import com.example.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

data class CatalogFeedUiState(
    val kind: MediaKind,
    val items: List<Media> = emptyList(),
    val featured: List<Media> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val order: FilterOrder = FilterOrder.YEAR,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadMoreError: String? = null,
    val error: String? = null,
    val endReached: Boolean = false,
    val favoriteIds: Set<Int> = emptySet()
) {
    val hasContent: Boolean
        get() = items.isNotEmpty()
}

/**
 * Shared ViewModel for the movie and series feeds. It owns pagination, filters,
 * cancellation and error state so the two screens stay small and consistent.
 */
abstract class CatalogFeedViewModel(
    private val kind: MediaKind,
    private val getPage: GetCatalogPageUseCase,
    private val getFeatured: GetFeaturedMediaUseCase,
    private val getGenres: GetGenresUseCase,
    private val favoriteActions: FavoriteActionsUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogFeedUiState(kind = kind))
    val uiState: StateFlow<CatalogFeedUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var page = 0
    private var feedJob: Job? = null
    private var loadMoreJob: Job? = null
    private var genresJob: Job? = null

    init {
        observeFavoriteIds()
            .onEach { ids -> _uiState.update { it.copy(favoriteIds = ids) } }
            .launchIn(viewModelScope)
        loadGenres()
        loadFirstPage(showRefresh = false)
    }

    fun setGenre(genreId: Int?) {
        if (_uiState.value.selectedGenreId == genreId) return
        _uiState.update { it.copy(selectedGenreId = genreId) }
        loadFirstPage(showRefresh = false)
    }

    fun setOrder(order: FilterOrder) {
        if (_uiState.value.order == order) return
        _uiState.update { it.copy(order = order) }
        loadFirstPage(showRefresh = false)
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        loadFirstPage(showRefresh = true)
    }

    fun retry() {
        loadFirstPage(showRefresh = false)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingMore || state.endReached) return

        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
            try {
                val nextPage = getPage(
                    kind = kind,
                    page = page,
                    genreId = _uiState.value.selectedGenreId ?: 0,
                    order = _uiState.value.order
                )
                if (nextPage.isEmpty()) {
                    _uiState.update { it.copy(endReached = true) }
                } else {
                    page += 1
                    _uiState.update { current ->
                        val merged = (current.items + nextPage).distinctBy { it.id }
                        current.copy(items = merged, endReached = nextPage.isEmpty())
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update { it.copy(loadMoreError = error.toUserMessage()) }
            } finally {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun retryLoadMore() {
        _uiState.update { it.copy(loadMoreError = null, endReached = false) }
        loadMore()
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

    private fun loadGenres() {
        if (_uiState.value.genres.isNotEmpty()) return
        genresJob?.cancel()
        genresJob = viewModelScope.launch {
            try {
                val genres = getGenres()
                _uiState.update { it.copy(genres = genres) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Filters are secondary; the feed still works without them.
            }
        }
    }

    private fun loadFirstPage(showRefresh: Boolean) {
        feedJob?.cancel()
        loadMoreJob?.cancel()
        page = 0
        _uiState.update {
            it.copy(
                isInitialLoading = !showRefresh,
                isRefreshing = showRefresh,
                isLoadingMore = false,
                loadMoreError = null,
                error = null,
                endReached = false
            )
        }

        feedJob = viewModelScope.launch {
            try {
                val result = coroutineScope {
                    val feed = async {
                        getPage(
                            kind = kind,
                            page = 0,
                            genreId = _uiState.value.selectedGenreId ?: 0,
                            order = _uiState.value.order
                        )
                    }
                    val featured = async { runCatching { getFeatured(kind) }.getOrDefault(emptyList()) }
                    feed.await() to featured.await()
                }
                val (items, featured) = result
                page = 1
                _uiState.update { current ->
                    current.copy(
                        items = items.distinctBy { it.id },
                        featured = featured.ifEmpty { current.featured },
                        isInitialLoading = false,
                        isRefreshing = false,
                        error = null,
                        endReached = items.isEmpty()
                    )
                }
                if (_uiState.value.genres.isEmpty()) loadGenres()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val message = error.toUserMessage()
                val hasContent = _uiState.value.items.isNotEmpty()
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        error = if (hasContent) null else message
                    )
                }
                if (hasContent) _messages.tryEmit(message)
            }
        }
    }

    protected fun emitMessage(message: String) {
        _messages.tryEmit(message)
    }
}

@HiltViewModel
class MoviesViewModel @Inject constructor(
    getPage: GetCatalogPageUseCase,
    getFeatured: GetFeaturedMediaUseCase,
    getGenres: GetGenresUseCase,
    favoriteActions: FavoriteActionsUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase
) : CatalogFeedViewModel(
    kind = MediaKind.MOVIE,
    getPage = getPage,
    getFeatured = getFeatured,
    getGenres = getGenres,
    favoriteActions = favoriteActions,
    observeFavoriteIds = observeFavoriteIds
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    getPage: GetCatalogPageUseCase,
    getFeatured: GetFeaturedMediaUseCase,
    getGenres: GetGenresUseCase,
    favoriteActions: FavoriteActionsUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase
) : CatalogFeedViewModel(
    kind = MediaKind.SERIES,
    getPage = getPage,
    getFeatured = getFeatured,
    getGenres = getGenres,
    favoriteActions = favoriteActions,
    observeFavoriteIds = observeFavoriteIds
)
