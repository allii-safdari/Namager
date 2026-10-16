package com.example.data.repository

import com.example.data.model.Genre
import com.example.domain.repository.IGenreRepository
import javax.inject.Inject

class GenreRepository @Inject constructor() : BaseRepository(), IGenreRepository {

    override suspend fun getGenres(): List<Genre> {
        val url = "$baseUrl/api/genre/all/$apiKey"
        val jsonData = executeRequest(url)
        return parseGenres(jsonData)
    }

    private fun parseGenres(jsonData: String): List<Genre> {
        val genres = mutableListOf<Genre>()
        val jsonArray = org.json.JSONArray(jsonData)

        for (i in 0 until jsonArray.length()) {
            try {
                val genreObj = jsonArray.getJSONObject(i)
                genres.add(
                    Genre(
                        id = genreObj.optInt("id", 0),
                        title = genreObj.optString("title", "ژانر")
                    )
                )
            } catch (_: Exception) {
                continue
            }
        }

        return genres.sortedBy { it.title }
    }
}
