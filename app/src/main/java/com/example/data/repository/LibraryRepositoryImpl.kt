package com.example.data.repository

import com.example.data.local.PreferencesDataSource
import com.example.di.ApplicationScope
import com.example.domain.model.FavoriteGroup
import com.example.domain.model.FavoriteItem
import com.example.domain.model.Media
import com.example.domain.repository.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val preferences: PreferencesDataSource,
    @ApplicationScope scope: CoroutineScope
) : LibraryRepository {

    private val mutex = Mutex()

    private val _groups = MutableStateFlow(listOf(FavoriteGroup.Default))
    override val groups: StateFlow<List<FavoriteGroup>> = _groups.asStateFlow()

    private val _items = MutableStateFlow<List<FavoriteItem>>(emptyList())
    override val items: StateFlow<List<FavoriteItem>> = _items.asStateFlow()

    private val _watchedEpisodes = MutableStateFlow<Set<String>>(emptySet())
    override val watchedEpisodes: StateFlow<Set<String>> = _watchedEpisodes.asStateFlow()

    init {
        scope.launch {
            _groups.value = runCatching { preferences.loadGroups() }
                .getOrDefault(listOf(FavoriteGroup.Default))
            _items.value = runCatching { preferences.loadFavoriteItems() }
                .getOrDefault(emptyList())
            _watchedEpisodes.value = runCatching { preferences.loadWatchedEpisodes() }
                .getOrDefault(emptySet())
        }
    }

    override suspend fun addFavorite(media: Media, groupId: String) = mutex.withLock {
        if (_items.value.any { it.mediaId == media.id && it.groupId == groupId }) return@withLock
        val updated = _items.value + FavoriteItem(
            mediaId = media.id,
            groupId = groupId,
            media = media
        )
        preferences.saveFavoriteItems(updated)
        _items.value = updated
    }

    override suspend fun removeFavorite(mediaId: Int, groupId: String?) = mutex.withLock {
        val updated = _items.value.filterNot { item ->
            item.mediaId == mediaId && (groupId == null || item.groupId == groupId)
        }
        if (updated == _items.value) return@withLock
        preferences.saveFavoriteItems(updated)
        _items.value = updated
    }

    override suspend fun toggleFavorite(media: Media) = mutex.withLock {
        if (_items.value.any { it.mediaId == media.id }) {
            val updated = _items.value.filterNot { it.mediaId == media.id }
            preferences.saveFavoriteItems(updated)
            _items.value = updated
        } else {
            val updated = _items.value + FavoriteItem(
                mediaId = media.id,
                groupId = FavoriteGroup.DEFAULT_ID,
                media = media
            )
            preferences.saveFavoriteItems(updated)
            _items.value = updated
        }
    }

    override suspend fun createGroup(name: String): FavoriteGroup = mutex.withLock {
        val cleanName = name.trim().ifBlank { "پوشه جدید" }
        val group = FavoriteGroup(
            id = preferences.newGroupId(),
            name = cleanName,
            isDefault = false
        )
        val updated = _groups.value + group
        preferences.saveGroups(updated)
        _groups.value = updated
        group
    }

    override suspend fun renameGroup(groupId: String, name: String) = mutex.withLock {
        if (groupId == FavoriteGroup.DEFAULT_ID) return@withLock
        val cleanName = name.trim()
        if (cleanName.isBlank()) return@withLock
        val updated = _groups.value.map { group ->
            if (group.id == groupId) group.copy(name = cleanName) else group
        }
        preferences.saveGroups(updated)
        _groups.value = updated
    }

    override suspend fun deleteGroup(groupId: String) = mutex.withLock {
        if (groupId == FavoriteGroup.DEFAULT_ID) return@withLock
        val updatedGroups = _groups.value.filterNot { it.id == groupId }
        val updatedItems = _items.value.filterNot { it.groupId == groupId }
        preferences.saveGroups(updatedGroups)
        preferences.saveFavoriteItems(updatedItems)
        _groups.value = updatedGroups
        _items.value = updatedItems
    }

    override suspend fun clearFavorites() = mutex.withLock {
        preferences.saveFavoriteItems(emptyList())
        _items.value = emptyList()
    }

    override suspend fun markEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int) {
        val key = "$seriesId-$seasonId-$episodeId"
        if (key in _watchedEpisodes.value) return
        val updated = _watchedEpisodes.value + key
        preferences.saveWatchedEpisodes(updated)
        _watchedEpisodes.value = updated
    }
}
