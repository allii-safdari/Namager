package com.example.domain.usecase

import com.example.domain.model.FilterOrder
import com.example.domain.model.Genre
import com.example.domain.model.Media
import com.example.domain.model.MediaKind
import com.example.domain.model.Season
import com.example.domain.repository.CatalogRepository
import com.example.domain.repository.LibraryRepository
import javax.inject.Inject

class GetCatalogPageUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(
        kind: MediaKind,
        page: Int,
        genreId: Int,
        order: FilterOrder
    ): List<Media> = when (kind) {
        MediaKind.MOVIE -> repository.getMovies(page, genreId, order)
        MediaKind.SERIES -> repository.getSeries(page, genreId, order)
    }
}

class GetFeaturedMediaUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(kind: MediaKind): List<Media> {
        val latest = when (kind) {
            MediaKind.MOVIE -> repository.getMovies(0, 0, FilterOrder.CREATED)
            MediaKind.SERIES -> repository.getSeries(0, 0, FilterOrder.CREATED)
        }
        return latest
            .distinctBy { it.id }
            .sortedByDescending { it.year }
            .take(5)
    }
}

class GetGenresUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): List<Genre> = repository.getGenres()
}

class SearchMediaUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(query: String): List<Media> {
        val cleanQuery = query.trim()
        return if (cleanQuery.length < 2) emptyList() else repository.search(cleanQuery)
    }
}

class GetSeasonsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(seriesId: Int): List<Season> = repository.getSeasons(seriesId)
}

class GetMediaByIdUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(mediaId: Int): Media? {
        return catalogRepository.findMedia(mediaId)
            ?: libraryRepository.items.value
                .firstOrNull { it.mediaId == mediaId }
                ?.media
    }
}
