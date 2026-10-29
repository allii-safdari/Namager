package com.example.data.repository

import com.example.data.remote.NamagerApi
import com.example.data.remote.MediaJsonParser
import com.example.domain.model.FilterOrder
import com.example.domain.model.Genre
import com.example.domain.model.Media
import com.example.domain.model.Season
import com.example.domain.repository.CatalogRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val api: NamagerApi,
    private val parser: MediaJsonParser
) : CatalogRepository {

    private val cache = ConcurrentHashMap<Int, Media>()

    override suspend fun getMovies(page: Int, genreId: Int, order: FilterOrder): List<Media> {
        val path = "/api/movie/by/filtres/$genreId/${order.value}/$page/${BuildConfigKeys.API_KEY}"
        val media = parser.parseMediaArray(api.get(path), defaultSeries = false)
        cache(media)
        return media
    }

    override suspend fun getSeries(page: Int, genreId: Int, order: FilterOrder): List<Media> {
        val path = "/api/serie/by/filtres/$genreId/${order.value}/$page/${BuildConfigKeys.API_KEY}"
        val media = parser.parseMediaArray(api.get(path), defaultSeries = true)
        cache(media)
        return media
    }

    override suspend fun search(query: String): List<Media> {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name()).replace("+", "%20")
        val path = "/api/search/$encoded/${BuildConfigKeys.API_KEY}/"
        val media = parser.parseSearch(api.get(path))
        cache(media)
        return media
    }

    override suspend fun getGenres(): List<Genre> {
        val path = "/api/genre/all/${BuildConfigKeys.API_KEY}"
        return parser.parseGenres(api.get(path))
    }

    override suspend fun getSeasons(seriesId: Int): List<Season> {
        val path = "/api/season/by/serie/$seriesId/${BuildConfigKeys.API_KEY}/"
        return parser.parseSeasons(api.get(path))
    }

    override fun findMedia(mediaId: Int): Media? = cache[mediaId]

    private fun cache(media: List<Media>) {
        media.forEach { item -> cache[item.id] = item }
    }
}

/**
 * Kept in the data layer so the domain and UI never read BuildConfig directly.
 */
private object BuildConfigKeys {
    val API_KEY: String = com.example.BuildConfig.API_KEY
}
