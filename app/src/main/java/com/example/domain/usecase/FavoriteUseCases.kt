package com.example.domain.usecase

import com.example.domain.model.FavoriteGroup
import com.example.domain.model.Media
import com.example.domain.repository.LibraryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveFavoriteIdsUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    operator fun invoke(): Flow<Set<Int>> =
        repository.items
            .map { items -> items.mapTo(LinkedHashSet()) { it.mediaId } }
            .distinctUntilChanged()
}

/**
 * Small application-level facade for favourite mutations. Keeping it here means
 * ViewModels never orchestrate persistence details.
 */
class FavoriteActionsUseCase @Inject constructor(
    private val repository: LibraryRepository
) {
    suspend fun toggle(media: Media) = repository.toggleFavorite(media)

    suspend fun add(media: Media, groupId: String = FavoriteGroup.DEFAULT_ID) =
        repository.addFavorite(media, groupId)

    suspend fun remove(mediaId: Int, groupId: String? = null) =
        repository.removeFavorite(mediaId, groupId)

    suspend fun createGroup(name: String): FavoriteGroup = repository.createGroup(name)

    suspend fun renameGroup(groupId: String, name: String) = repository.renameGroup(groupId, name)

    suspend fun deleteGroup(groupId: String) = repository.deleteGroup(groupId)

    suspend fun clearAll() = repository.clearFavorites()

    suspend fun markEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int) =
        repository.markEpisodeWatched(seriesId, seasonId, episodeId)
}
