package com.example.domain.repository

import com.example.domain.model.FavoriteGroup
import com.example.domain.model.FavoriteItem
import com.example.domain.model.Media
import kotlinx.coroutines.flow.StateFlow

/**
 * User-owned library: favourite folders, saved titles and watched episodes.
 * All write operations are suspend functions so disk I/O never blocks the UI.
 */
interface LibraryRepository {
    val groups: StateFlow<List<FavoriteGroup>>
    val items: StateFlow<List<FavoriteItem>>
    val watchedEpisodes: StateFlow<Set<String>>

    suspend fun addFavorite(media: Media, groupId: String = FavoriteGroup.DEFAULT_ID)

    suspend fun removeFavorite(mediaId: Int, groupId: String? = null)

    suspend fun toggleFavorite(media: Media)

    suspend fun createGroup(name: String): FavoriteGroup

    suspend fun renameGroup(groupId: String, name: String)

    suspend fun deleteGroup(groupId: String)

    suspend fun clearFavorites()

    suspend fun markEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int)
}
