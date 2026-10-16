package com.example.data.repository

import com.example.data.model.Country
import com.example.data.model.FilterOrder
import com.example.data.model.Genre
import com.example.data.model.Poster
import com.example.data.model.Source
import com.example.domain.repository.ICountryPostersRepository
import javax.inject.Inject

class CountryPostersRepository @Inject constructor() : BaseRepository(), ICountryPostersRepository {

    override suspend fun getPostersByCountry(
        countryId: Int,
        page: Int,
        order: FilterOrder
    ): List<Poster> {
        val url = "$baseUrl/api/poster/by/filtres/0/$countryId/${order.value}/$page/$apiKey"
        val jsonData = executeRequest(url)
        return parsePosters(jsonData)
    }

    private fun parsePosters(jsonData: String): List<Poster> {
        val posters = mutableListOf<Poster>()
        val jsonArray = org.json.JSONArray(jsonData)

        for (i in 0 until jsonArray.length()) {
            try {
                val posterObj = jsonArray.getJSONObject(i)
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
