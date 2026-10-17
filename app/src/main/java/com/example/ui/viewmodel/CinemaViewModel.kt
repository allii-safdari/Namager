package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CinemaSampleData
import com.example.data.local.LocalPreferencesManager
import com.example.data.model.*
import com.example.data.repository.*
import com.example.domain.repository.*
import com.example.model.ScreenTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

@HiltViewModel
class CinemaViewModel @Inject constructor(
    application: Application,
    private val movieRepository: IMovieRepository = MovieRepository(),
    private val seriesRepository: ISeriesRepository = SeriesRepository(),
    private val seasonsRepository: ISeasonsRepository = SeasonsRepository(),
    private val searchRepository: ISearchRepository = SearchRepository(),
    private val genreRepository: IGenreRepository = GenreRepository(),
    private val countryRepository: ICountryRepository = CountryRepository(),
    private val countryPostersRepository: ICountryPostersRepository = CountryPostersRepository(),
    private val localPrefs: LocalPreferencesManager = LocalPreferencesManager(application)
) : AndroidViewModel(application) {

    // Current Navigation tab
    private val _currentTab = MutableStateFlow(ScreenTab.MOVIES)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Filters for Movies & Series
    private val _selectedGenreId = MutableStateFlow(0)
    val selectedGenreId: StateFlow<Int> = _selectedGenreId.asStateFlow()

    private val _selectedOrder = MutableStateFlow(FilterOrder.CREATED)
    val selectedOrder: StateFlow<FilterOrder> = _selectedOrder.asStateFlow()

    // Compatibility alias
    val selectedFilterType: StateFlow<FilterOrder> = _selectedOrder.asStateFlow()

    // Genres & Countries
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    // Featured Hero Slider
    private val _featuredSlider = MutableStateFlow<List<UnifiedMedia>>(emptyList())
    val featuredSlider: StateFlow<List<UnifiedMedia>> = _featuredSlider.asStateFlow()

    // Movies list state & pagination
    private val _moviesState = MutableStateFlow<UiState<List<UnifiedMedia>>>(UiState.Loading)
    val moviesState: StateFlow<UiState<List<UnifiedMedia>>> = _moviesState.asStateFlow()
    private val currentMoviesList = mutableListOf<UnifiedMedia>()
    private var moviesPage = 0
    private var isMoviesLastPage = false
    private var isMoviesLoadingMore = false

    // Series list state & pagination
    private val _seriesState = MutableStateFlow<UiState<List<UnifiedMedia>>>(UiState.Loading)
    val seriesState: StateFlow<UiState<List<UnifiedMedia>>> = _seriesState.asStateFlow()
    private val currentSeriesList = mutableListOf<UnifiedMedia>()
    private var seriesPage = 0
    private var isSeriesLastPage = false
    private var isSeriesLoadingMore = false

    // Country page state
    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry.asStateFlow()

    private val _countryContentState = MutableStateFlow<UiState<List<UnifiedMedia>>>(UiState.Empty)
    val countryContentState: StateFlow<UiState<List<UnifiedMedia>>> = _countryContentState.asStateFlow()
    private val currentCountryList = mutableListOf<UnifiedMedia>()
    private var countryPage = 0
    private var isCountryLastPage = false

    // Search state (executed only on explicit button tap)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<UnifiedMedia>>>(UiState.Empty)
    val searchState: StateFlow<UiState<List<UnifiedMedia>>> = _searchState.asStateFlow()

    // Selected Media Item (for detail sheet/screen)
    private val _selectedMedia = MutableStateFlow<UnifiedMedia?>(null)
    val selectedMedia: StateFlow<UnifiedMedia?> = _selectedMedia.asStateFlow()

    // Seasons & Episodes for selected series
    private val _seasonsState = MutableStateFlow<UiState<List<Season>>>(UiState.Loading)
    val seasonsState: StateFlow<UiState<List<Season>>> = _seasonsState.asStateFlow()

    // Favorites & Groups
    private val _favoriteGroups = MutableStateFlow<List<FavoriteGroup>>(emptyList())
    val favoriteGroups: StateFlow<List<FavoriteGroup>> = _favoriteGroups.asStateFlow()

    private val _selectedFavoriteGroupId = MutableStateFlow(LocalPreferencesManager.DEFAULT_GROUP_ID)
    val selectedFavoriteGroupId: StateFlow<String> = _selectedFavoriteGroupId.asStateFlow()

    private val _favoriteItems = MutableStateFlow<List<FavoriteItem>>(emptyList())
    val favoriteItems: StateFlow<List<FavoriteItem>> = _favoriteItems.asStateFlow()

    // Watched episode keys: "$seriesId-$seasonId-$episodeId"
    private val _watchedEpisodes = MutableStateFlow<Set<String>>(emptySet())
    val watchedEpisodes: StateFlow<Set<String>> = _watchedEpisodes.asStateFlow()

    // App Settings
    private val _settings = MutableStateFlow(localPrefs.getAppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Player & Download Modals
    private val _activePlayingSource = MutableStateFlow<Source?>(null)
    val activePlayingSource: StateFlow<Source?> = _activePlayingSource.asStateFlow()

    private val _activePlayingTitle = MutableStateFlow("")
    val activePlayingTitle: StateFlow<String> = _activePlayingTitle.asStateFlow()

    private val _activePlayingSubtitle = MutableStateFlow<String?>(null)
    val activePlayingSubtitle: StateFlow<String?> = _activePlayingSubtitle.asStateFlow()

    private val _activeSourceForDownload = MutableStateFlow<Source?>(null)
    val activeSourceForDownload: StateFlow<Source?> = _activeSourceForDownload.asStateFlow()

    // Backward compatibility flows
    val movies: StateFlow<List<UnifiedMedia>> get() = MutableStateFlow(currentMoviesList.toList()).asStateFlow()
    val series: StateFlow<List<UnifiedMedia>> get() = MutableStateFlow(currentSeriesList.toList()).asStateFlow()
    val currentSeasons: StateFlow<List<Season>> get() {
        val s = (_seasonsState.value as? UiState.Success)?.data ?: emptyList()
        return MutableStateFlow(s).asStateFlow()
    }
    val isLoading: StateFlow<Boolean> get() = MutableStateFlow(_moviesState.value is UiState.Loading).asStateFlow()
    val watchlistIds: StateFlow<Set<Int>> get() = MutableStateFlow(_favoriteItems.value.map { it.mediaId }.toSet()).asStateFlow()
    val isPlaying: StateFlow<Boolean> get() = MutableStateFlow(_activePlayingSource.value != null).asStateFlow()

    init {
        loadInitialData()
        refreshFavorites()
    }

    private fun loadInitialData() {
        loadGenres()
        loadCountries()
        loadFeaturedSliderFromServer()
        loadMovies(isRefresh = true)
        loadSeries(isRefresh = true)
    }

    private fun loadFeaturedSliderFromServer() {
        viewModelScope.launch {
            try {
                val topMovies = movieRepository.getMovies(page = 0, genreId = 0, order = FilterOrder.IMDB)
                if (topMovies.isNotEmpty()) {
                    _featuredSlider.value = topMovies.take(5).map { it.toUnified() }
                }
            } catch (_: Exception) {}
        }
    }

    fun loadGenres() {
        viewModelScope.launch {
            try {
                val serverGenres = genreRepository.getGenres()
                if (serverGenres.isNotEmpty()) {
                    _genres.value = serverGenres
                }
            } catch (_: Exception) {}
        }
    }

    fun loadCountries() {
        viewModelScope.launch {
            try {
                val serverCountries = countryRepository.getAllCountries()
                if (serverCountries.isNotEmpty()) {
                    _countries.value = serverCountries
                }
            } catch (_: Exception) {}
        }
    }

    // --- Movies Pagination & Filtering ---
    fun loadMovies(isRefresh: Boolean = false) {
        if (isRefresh) {
            moviesPage = 0
            isMoviesLastPage = false
            currentMoviesList.clear()
            _moviesState.value = UiState.Loading
        }

        viewModelScope.launch {
            try {
                val result = movieRepository.getMovies(
                    page = moviesPage,
                    genreId = _selectedGenreId.value,
                    order = _selectedOrder.value
                )
                val unified = result.map { it.toUnified() }

                if (unified.isEmpty()) {
                    isMoviesLastPage = true
                    if (currentMoviesList.isEmpty()) {
                        _moviesState.value = UiState.Empty
                    }
                } else {
                    currentMoviesList.addAll(unified)
                    _moviesState.value = UiState.Success(currentMoviesList.distinctBy { it.id })
                    moviesPage++

                    // Update slider with top movies if available
                    if (_featuredSlider.value.size <= 2) {
                        _featuredSlider.value = currentMoviesList.take(5)
                    }
                }
            } catch (e: Exception) {
                if (currentMoviesList.isEmpty()) {
                    _moviesState.value = UiState.Error(e.message ?: "خطا در دریافت لیست فیلم‌ها")
                }
            }
        }
    }

    fun loadMoreMovies() {
        if (isMoviesLastPage || isMoviesLoadingMore || _moviesState.value is UiState.Loading) return
        isMoviesLoadingMore = true

        viewModelScope.launch {
            try {
                val result = movieRepository.getMovies(
                    page = moviesPage,
                    genreId = _selectedGenreId.value,
                    order = _selectedOrder.value
                )
                val unified = result.map { it.toUnified() }
                if (unified.isEmpty()) {
                    isMoviesLastPage = true
                } else {
                    currentMoviesList.addAll(unified)
                    _moviesState.value = UiState.Success(currentMoviesList.distinctBy { it.id })
                    moviesPage++
                }
            } catch (_: Exception) {
            } finally {
                isMoviesLoadingMore = false
            }
        }
    }

    // --- Series Pagination & Filtering ---
    fun loadSeries(isRefresh: Boolean = false) {
        if (isRefresh) {
            seriesPage = 0
            isSeriesLastPage = false
            currentSeriesList.clear()
            _seriesState.value = UiState.Loading
        }

        viewModelScope.launch {
            try {
                val result = seriesRepository.getSeries(
                    page = seriesPage,
                    genreId = _selectedGenreId.value,
                    order = _selectedOrder.value
                )
                val unified = result.map { it.toUnified() }

                if (unified.isEmpty()) {
                    isSeriesLastPage = true
                    if (currentSeriesList.isEmpty()) {
                        _seriesState.value = UiState.Empty
                    }
                } else {
                    currentSeriesList.addAll(unified)
                    _seriesState.value = UiState.Success(currentSeriesList.distinctBy { it.id })
                    seriesPage++
                }
            } catch (e: Exception) {
                if (currentSeriesList.isEmpty()) {
                    _seriesState.value = UiState.Error(e.message ?: "خطا در دریافت لیست سریال‌ها")
                }
            }
        }
    }

    fun loadMoreSeries() {
        if (isSeriesLastPage || isSeriesLoadingMore || _seriesState.value is UiState.Loading) return
        isSeriesLoadingMore = true

        viewModelScope.launch {
            try {
                val result = seriesRepository.getSeries(
                    page = seriesPage,
                    genreId = _selectedGenreId.value,
                    order = _selectedOrder.value
                )
                val unified = result.map { it.toUnified() }
                if (unified.isEmpty()) {
                    isSeriesLastPage = true
                } else {
                    currentSeriesList.addAll(unified)
                    _seriesState.value = UiState.Success(currentSeriesList.distinctBy { it.id })
                    seriesPage++
                }
            } catch (_: Exception) {
            } finally {
                isSeriesLoadingMore = false
            }
        }
    }

    fun setGenreFilter(genreId: Int) {
        _selectedGenreId.value = genreId
        loadMovies(isRefresh = true)
        loadSeries(isRefresh = true)
        _selectedCountry.value?.let { loadCountryContent(it, isRefresh = true) }
    }

    fun setSortOrder(order: FilterOrder) {
        _selectedOrder.value = order
        loadMovies(isRefresh = true)
        loadSeries(isRefresh = true)
        _selectedCountry.value?.let { loadCountryContent(it, isRefresh = true) }
    }

    // --- Search Feature (Executed ONLY on explicit action) ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun executeSearch() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _searchState.value = UiState.Empty
            return
        }

        _searchState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = searchRepository.search(query)
                val unified = result.posters.map { it.toUnified() }
                if (unified.isEmpty()) {
                    _searchState.value = UiState.Empty
                } else {
                    _searchState.value = UiState.Success(unified)
                }
            } catch (e: Exception) {
                _searchState.value = UiState.Error(e.message ?: "خطا در جستجو")
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = UiState.Empty
    }

    // --- Country Content (Mixed Movies & Series) ---
    fun selectCountry(country: Country?) {
        _selectedCountry.value = country
        if (country != null) {
            loadCountryContent(country, isRefresh = true)
        } else {
            _countryContentState.value = UiState.Empty
        }
    }

    fun loadCountryContent(country: Country, isRefresh: Boolean = false) {
        if (isRefresh) {
            countryPage = 0
            isCountryLastPage = false
            currentCountryList.clear()
            _countryContentState.value = UiState.Loading
        }

        viewModelScope.launch {
            try {
                val posters = countryPostersRepository.getPostersByCountry(
                    countryId = country.id,
                    page = countryPage,
                    order = _selectedOrder.value
                )
                val unified = posters.map { it.toUnified() }

                if (unified.isEmpty()) {
                    isCountryLastPage = true
                    if (currentCountryList.isEmpty()) {
                        _countryContentState.value = UiState.Empty
                    }
                } else {
                    currentCountryList.addAll(unified)
                    _countryContentState.value = UiState.Success(currentCountryList.distinctBy { it.id })
                    countryPage++
                }
            } catch (e: Exception) {
                if (currentCountryList.isEmpty()) {
                    _countryContentState.value = UiState.Error(e.message ?: "خطا در دریافت آثار کشور ${country.title}")
                }
            }
        }
    }

    fun loadMoreCountryContent() {
        val country = _selectedCountry.value ?: return
        if (isCountryLastPage || _countryContentState.value is UiState.Loading) return

        viewModelScope.launch {
            try {
                val posters = countryPostersRepository.getPostersByCountry(
                    countryId = country.id,
                    page = countryPage,
                    order = _selectedOrder.value
                )
                val unified = posters.map { it.toUnified() }
                if (unified.isEmpty()) {
                    isCountryLastPage = true
                } else {
                    currentCountryList.addAll(unified)
                    _countryContentState.value = UiState.Success(currentCountryList.distinctBy { it.id })
                    countryPage++
                }
            } catch (_: Exception) {}
        }
    }

    // --- Seasons & Episodes Detail ---
    fun selectMedia(media: UnifiedMedia?) {
        _selectedMedia.value = media
        if (media != null && media.isSeries) {
            loadSeasons(media.id)
        } else {
            _seasonsState.value = UiState.Empty
        }
    }

    fun loadSeasons(seriesId: Int) {
        _seasonsState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val seasons = seasonsRepository.getSeasons(seriesId)
                if (seasons.isEmpty()) {
                    _seasonsState.value = UiState.Empty
                } else {
                    _seasonsState.value = UiState.Success(seasons)
                }
            } catch (e: Exception) {
                _seasonsState.value = UiState.Error(e.message ?: "خطا در بارگذاری فصل‌ها")
            }
        }
    }

    // --- Favorites & Custom Groups ---
    fun refreshFavorites() {
        _favoriteGroups.value = localPrefs.getFavoriteGroups()
        _favoriteItems.value = localPrefs.getFavoriteItems()
    }

    fun selectFavoriteGroup(groupId: String) {
        _selectedFavoriteGroupId.value = groupId
    }

    fun createFavoriteGroup(name: String) {
        val group = localPrefs.createFavoriteGroup(name)
        refreshFavorites()
        _selectedFavoriteGroupId.value = group.id
    }

    fun renameFavoriteGroup(groupId: String, newName: String) {
        localPrefs.renameFavoriteGroup(groupId, newName)
        refreshFavorites()
    }

    fun deleteFavoriteGroup(groupId: String) {
        localPrefs.deleteFavoriteGroup(groupId)
        _selectedFavoriteGroupId.value = LocalPreferencesManager.DEFAULT_GROUP_ID
        refreshFavorites()
    }

    fun addToFavorites(media: UnifiedMedia, groupId: String = _selectedFavoriteGroupId.value) {
        localPrefs.addToFavorites(media, groupId)
        refreshFavorites()
    }

    fun removeFromFavorites(mediaId: Int, groupId: String? = null) {
        localPrefs.removeFromFavorites(mediaId, groupId)
        refreshFavorites()
    }

    fun toggleWatchlist(mediaId: Int) {
        if (localPrefs.isFavorite(mediaId)) {
            removeFromFavorites(mediaId)
        } else {
            val media = findMediaById(mediaId) ?: UnifiedMedia(
                id = mediaId,
                title = "عنوان $mediaId",
                type = "movie",
                description = "",
                year = 0,
                imdb = 0.0,
                rating = 0.0,
                duration = null,
                image = "",
                cover = "",
                genres = emptyList(),
                sources = emptyList(),
                country = emptyList()
            )
            addToFavorites(media)
        }
    }

    fun isFavorite(mediaId: Int): Boolean {
        return localPrefs.isFavorite(mediaId)
    }

    fun clearAllFavorites() {
        localPrefs.clearAllFavorites()
        refreshFavorites()
    }

    private fun findMediaById(mediaId: Int): UnifiedMedia? {
        return currentMoviesList.find { it.id == mediaId }
            ?: currentSeriesList.find { it.id == mediaId }
            ?: (_searchState.value as? UiState.Success)?.data?.find { it.id == mediaId }
            ?: currentCountryList.find { it.id == mediaId }
            ?: _featuredSlider.value.find { it.id == mediaId }
    }

    // --- Watched Episodes ---
    fun markEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int) {
        localPrefs.markEpisodeWatched(seriesId, seasonId, episodeId)
        _watchedEpisodes.update { it + "$seriesId-$seasonId-$episodeId" }
    }

    fun isEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int): Boolean {
        return _watchedEpisodes.value.contains("$seriesId-$seasonId-$episodeId") ||
                localPrefs.isEpisodeWatched(seriesId, seasonId, episodeId)
    }

    // --- Settings ---
    fun updateTheme(themeMode: ThemeMode) {
        val updated = _settings.value.copy(themeMode = themeMode)
        _settings.value = updated
        localPrefs.saveAppSettings(updated)
    }

    fun updateAccentColor(colorHex: Long) {
        val updated = _settings.value.copy(accentColorHex = colorHex)
        _settings.value = updated
        localPrefs.saveAppSettings(updated)
    }

    fun updateSeekJumpSeconds(seconds: Int) {
        val updated = _settings.value.copy(seekJumpSeconds = seconds)
        _settings.value = updated
        localPrefs.saveAppSettings(updated)
    }

    fun updateSubtitleSettings(sizeSp: Int, colorHex: Long, bgHex: Long) {
        val updated = _settings.value.copy(
            subtitleFontSizeSp = sizeSp,
            subtitleTextColorHex = colorHex,
            subtitleBackgroundColorHex = bgHex
        )
        _settings.value = updated
        localPrefs.saveAppSettings(updated)
    }

    fun resetSettings() {
        localPrefs.resetSettings()
        _settings.value = localPrefs.getAppSettings()
    }

    // --- Player & Download ---
    fun playMediaSource(source: Source, title: String, subtitle: String? = null, seriesId: Int? = null, seasonId: Int? = null, episodeId: Int? = null) {
        _activePlayingSource.value = source
        _activePlayingTitle.value = title
        _activePlayingSubtitle.value = subtitle

        if (seriesId != null && seasonId != null && episodeId != null) {
            markEpisodeWatched(seriesId, seasonId, episodeId)
        }
    }

    fun closePlayer() {
        _activePlayingSource.value = null
    }

    fun openDownloadOptions(source: Source) {
        _activeSourceForDownload.value = source
    }

    fun closeDownloadOptions() {
        _activeSourceForDownload.value = null
    }

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }
}
