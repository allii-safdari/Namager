package com.example.data.remote

import com.example.core.error.AppException
import com.example.domain.model.Country
import com.example.domain.model.Genre
import com.example.domain.model.Media
import com.example.domain.model.Season
import com.example.domain.model.Source
import com.example.domain.model.Episode
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses the loosely-typed legacy API response. Parsing is defensive: a single
 * malformed item must not invalidate the whole page.
 */
@Singleton
class MediaJsonParser @Inject constructor() {

    fun parseMediaArray(json: String, defaultSeries: Boolean = false): List<Media> {
        return try {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val media = runCatching { parseMedia(item, defaultSeries) }.getOrNull() ?: continue
                    if (isValid(media)) add(media)
                }
            }
        } catch (error: Exception) {
            throw AppException.Parse(error)
        }
    }

    fun parseSearch(json: String): List<Media> {
        return try {
            val root = JSONObject(json)
            val posters = root.optJSONArray("posters") ?: JSONArray()
            buildList {
                for (index in 0 until posters.length()) {
                    val item = posters.optJSONObject(index) ?: continue
                    val media = runCatching { parseMedia(item) }.getOrNull() ?: continue
                    if (isValid(media)) add(media)
                }
            }
        } catch (error: Exception) {
            throw AppException.Parse(error)
        }
    }

    fun parseSeasons(json: String): List<Season> {
        return try {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val seasonObject = array.optJSONObject(index) ?: continue
                    add(
                        Season(
                            id = seasonObject.optInt("id", 0),
                            title = seasonObject.optString("title", "فصل"),
                            episodes = parseEpisodes(seasonObject.optJSONArray("episodes"))
                        )
                    )
                }
            }
        } catch (error: Exception) {
            throw AppException.Parse(error)
        }
    }

    fun parseGenres(json: String): List<Genre> {
        return try {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        Genre(
                            id = item.optInt("id", 0),
                            title = item.optString("title", "")
                        )
                    )
                }
            }.filter { it.id > 0 && it.title.isNotBlank() }
                .sortedBy { it.title }
        } catch (error: Exception) {
            throw AppException.Parse(error)
        }
    }

    fun parseMedia(jsonObject: JSONObject, defaultSeries: Boolean = false): Media {
        val rawType = jsonObject.optString("type", "")
        val type = rawType.ifBlank { if (defaultSeries) "serie" else "movie" }
        return Media(
            id = jsonObject.optInt("id", 0),
            type = type,
            title = jsonObject.optString("title", "بدون عنوان").trim(),
            description = jsonObject.optString("description", ""),
            year = jsonObject.optInt("year", 0),
            imdb = jsonObject.optDouble("imdb", 0.0).finiteOrZero(),
            rating = jsonObject.optDouble("rating", 0.0).finiteOrZero(),
            duration = safeDuration(jsonObject.optString("duration", "")),
            image = jsonObject.optString("image", ""),
            cover = jsonObject.optString("cover", ""),
            genres = parseGenresArray(jsonObject.optJSONArray("genres")),
            sources = parseSourcesArray(jsonObject.optJSONArray("sources")),
            country = parseCountriesArray(jsonObject.optJSONArray("country"))
        )
    }

    private fun parseEpisodes(array: JSONArray?): List<Episode> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    Episode(
                        id = item.optInt("id", 0),
                        title = item.optString("title", "قسمت"),
                        description = item.optString("description", ""),
                        duration = safeDuration(item.optString("duration", "")),
                        image = item.optString("image", ""),
                        sources = parseSourcesArray(item.optJSONArray("sources"))
                    )
                )
            }
        }
    }

    private fun parseGenresArray(array: JSONArray?): List<Genre> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    Genre(
                        id = item.optInt("id", 0),
                        title = item.optString("title", "")
                    )
                )
            }
        }
    }

    private fun parseSourcesArray(array: JSONArray?): List<Source> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val url = item.optString("url", "")
                if (url.isBlank()) continue
                add(
                    Source(
                        id = item.optInt("id", 0),
                        quality = item.optString("quality", "720p"),
                        type = item.optString("type", "mp4"),
                        url = url
                    )
                )
            }
        }
    }

    private fun parseCountriesArray(array: JSONArray?): List<Country> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    Country(
                        id = item.optInt("id", 0),
                        title = item.optString("title", ""),
                        image = item.optString("image", "")
                    )
                )
            }
        }
    }

    private fun isValid(media: Media): Boolean {
        if (media.id <= 0 || media.title.isBlank()) return false
        val title = media.title
        return UNWANTED_TITLES.none { unwanted -> title.contains(unwanted) }
    }

    private fun safeDuration(raw: String): String? {
        val clean = raw.trim()
        return clean.takeIf {
            it.isNotBlank() &&
                !it.equals("null", ignoreCase = true) &&
                !it.equals("N/A", ignoreCase = true)
        }
    }

    private fun Double.finiteOrZero(): Double = if (isFinite()) this else 0.0

    private companion object {
        val UNWANTED_TITLES = listOf(
            "هورتون",
            "ملودی باکس",
            "بروز رسانی نسخه ویندوز",
            "بروزرسانی نسخه ویندوز",
            "آموزش زبان به سبک مووی باکس"
        )
    }
}
