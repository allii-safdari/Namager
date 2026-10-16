package com.example.data.repository

import com.example.data.model.Country
import com.example.data.model.Genre
import com.example.data.model.Poster
import com.example.data.model.SearchResult
import com.example.data.model.Source
import com.example.domain.repository.ISearchRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class SearchRepository @Inject constructor() : BaseRepository(), ISearchRepository {

    override suspend fun search(query: String): SearchResult {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        val url = "$baseUrl/api/search/$encodedQuery/$apiKey/"
        val jsonData = executeRequest(url)
        return parseSearchResult(jsonData)
    }

    private fun parseSearchResult(jsonData: String): SearchResult {
        val jsonObject = org.json.JSONObject(jsonData)
        val postersArray = jsonObject.optJSONArray("posters") ?: org.json.JSONArray()
        val posters = parsePosters(postersArray)
        return SearchResult(posters = posters)
    }

    private fun parsePosters(postersArray: org.json.JSONArray): List<Poster> {
        val posters = mutableListOf<Poster>()
        for (i in 0 until postersArray.length()) {
            try {
                val posterObj = postersArray.getJSONObject(i)
                posters.add(parsePoster(posterObj))
            } catch (_: Exception) {
                continue
            }
        }
        return posters
    }

    private fun parsePoster(posterObj: org.json.JSONObject): Poster {
        val durationRaw = posterObj.optString("duration", "")
        val safeDuration = if (durationRaw.isBlank() || durationRaw.equals("null", ignoreCase = true) || durationRaw.equals("N/A", ignoreCase = true)) {
            null
        } else {
            durationRaw
        }

        return Poster(
            id = posterObj.optInt("id", 0),
            title = posterObj.optString("title", "بدون عنوان"),
            type = posterObj.optString("type", "movie"),
            description = posterObj.optString("description", ""),
            year = posterObj.optInt("year", 0),
            imdb = posterObj.optDouble("imdb", 0.0),
            rating = posterObj.optDouble("rating", 0.0),
            duration = safeDuration,
            image = posterObj.optString("image", ""),
            cover = posterObj.optString("cover", ""),
            genres = parseGenres(posterObj.optJSONArray("genres")),
            sources = parseSources(posterObj.optJSONArray("sources")),
            country = parseCountries(posterObj.optJSONArray("country"))
        )
    }

    private fun parseGenres(genresArray: org.json.JSONArray?): List<Genre> {
        if (genresArray == null) return emptyList()
        val list = mutableListOf<Genre>()
        for (i in 0 until genresArray.length()) {
            try {
                val obj = genresArray.getJSONObject(i)
                list.add(
                    Genre(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", "")
                    )
                )
            } catch (_: Exception) {
                continue
            }
        }
        return list
    }

    private fun parseSources(sourcesArray: org.json.JSONArray?): List<Source> {
        if (sourcesArray == null) return emptyList()
        val list = mutableListOf<Source>()
        for (i in 0 until sourcesArray.length()) {
            try {
                val obj = sourcesArray.getJSONObject(i)
                list.add(
                    Source(
                        id = obj.optInt("id", 0),
                        quality = obj.optString("quality", "720p"),
                        type = obj.optString("type", "mp4"),
                        url = obj.optString("url", "")
                    )
                )
            } catch (_: Exception) {
                continue
            }
        }
        return list
    }

    private fun parseCountries(countryArray: org.json.JSONArray?): List<Country> {
        if (countryArray == null) return emptyList()
        val list = mutableListOf<Country>()
        for (i in 0 until countryArray.length()) {
            try {
                val obj = countryArray.getJSONObject(i)
                list.add(
                    Country(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", ""),
                        image = obj.optString("image", "")
                    )
                )
            } catch (_: Exception) {
                continue
            }
        }
        return list
    }
}
