package com.example.domain.repository

import com.example.domain.model.FilterOrder
import com.example.domain.model.Genre
import com.example.domain.model.Media
import com.example.domain.model.Season

/**
 * Domain port for the remote catalogue. Implementations live in the data layer
 * and are responsible for HTTP, mirrors, parsing and cache details.
 */
interface CatalogRepository {
    suspend fun getMovies(page: Int, genreId: Int, order: FilterOrder): List<Media>

    suspend fun getSeries(page: Int, genreId: Int, order: FilterOrder): List<Media>

    suspend fun search(query: String): List<Media>

    suspend fun getGenres(): List<Genre>

    suspend fun getSeasons(seriesId: Int): List<Season>

    /** Best-effort lookup from the in-memory catalogue (used by the details route). */
    fun findMedia(mediaId: Int): Media?
}
