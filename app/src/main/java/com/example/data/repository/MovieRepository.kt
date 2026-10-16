package com.example.data.repository

import com.example.data.model.FilterOrder
import com.example.data.model.Movie
import com.example.domain.repository.IMovieRepository
import javax.inject.Inject

class MovieRepository @Inject constructor() : BaseRepository(), IMovieRepository {

    override suspend fun getMovies(page: Int, genreId: Int, order: FilterOrder): List<Movie> {
        val url = "$baseUrl/api/movie/by/filtres/$genreId/${order.value}/$page/$apiKey"
        val jsonData = executeRequest(url)
        return parseMovies(jsonData)
    }

    private fun parseMovies(jsonData: String): List<Movie> {
        val movies = mutableListOf<Movie>()
        val jsonArray = org.json.JSONArray(jsonData)

        for (i in 0 until jsonArray.length()) {
            try {
                val movieObj = jsonArray.getJSONObject(i)
                movies.add(parseMovie(movieObj))
            } catch (_: Exception) {
                continue
            }
        }

        return movies
    }

    private fun parseMovie(movieObj: org.json.JSONObject): Movie {
        val durationRaw = movieObj.optString("duration", "")
        val safeDuration = if (durationRaw.isBlank() || durationRaw.equals("null", ignoreCase = true) || durationRaw.equals("N/A", ignoreCase = true)) {
            null
        } else {
            durationRaw
        }

        return Movie(
            id = movieObj.optInt("id", 0),
            type = movieObj.optString("type", "movie"),
            title = movieObj.optString("title", "بدون عنوان"),
            description = movieObj.optString("description", ""),
            year = movieObj.optInt("year", 0),
            imdb = movieObj.optDouble("imdb", 0.0),
            rating = movieObj.optDouble("rating", 0.0),
            duration = safeDuration,
            image = movieObj.optString("image", ""),
            cover = movieObj.optString("cover", ""),
            genres = parseGenres(movieObj.optJSONArray("genres")),
            sources = parseSources(movieObj.optJSONArray("sources")),
            country = parseCountries(movieObj.optJSONArray("country"))
        )
    }

    private fun parseGenres(genresArray: org.json.JSONArray?): List<com.example.data.model.Genre> {
        if (genresArray == null) return emptyList()
        val list = mutableListOf<com.example.data.model.Genre>()
        for (i in 0 until genresArray.length()) {
            try {
                val obj = genresArray.getJSONObject(i)
                list.add(
                    com.example.data.model.Genre(
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

    private fun parseSources(sourcesArray: org.json.JSONArray?): List<com.example.data.model.Source> {
        if (sourcesArray == null) return emptyList()
        val list = mutableListOf<com.example.data.model.Source>()
        for (i in 0 until sourcesArray.length()) {
            try {
                val obj = sourcesArray.getJSONObject(i)
                list.add(
                    com.example.data.model.Source(
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

    private fun parseCountries(countryArray: org.json.JSONArray?): List<com.example.data.model.Country> {
        if (countryArray == null) return emptyList()
        val list = mutableListOf<com.example.data.model.Country>()
        for (i in 0 until countryArray.length()) {
            try {
                val obj = countryArray.getJSONObject(i)
                list.add(
                    com.example.data.model.Country(
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
