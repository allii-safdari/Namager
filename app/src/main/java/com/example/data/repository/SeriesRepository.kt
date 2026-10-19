package com.example.data.repository

import com.example.data.model.FilterOrder
import com.example.data.model.Series
import com.example.domain.repository.ISeriesRepository
import javax.inject.Inject

class SeriesRepository @Inject constructor() : BaseRepository(), ISeriesRepository {

    override suspend fun getSeries(page: Int, genreId: Int, order: FilterOrder): List<Series> {
        val url = "$baseUrl/api/serie/by/filtres/$genreId/${order.value}/$page/$apiKey"
        val jsonData = executeRequest(url)
        return parseSeries(jsonData)
    }

    private fun parseSeries(jsonData: String): List<Series> {
        val seriesList = mutableListOf<Series>()
        val jsonArray = org.json.JSONArray(jsonData)

        for (i in 0 until jsonArray.length()) {
            try {
                val seriesObj = jsonArray.getJSONObject(i)
                val series = parseSeriesItem(seriesObj)
                val title = series.title
                val isUnwanted = title.contains("هورتون") ||
                        title.contains("ملودی باکس") ||
                        title.contains("بروز رسانی نسخه ویندوز") ||
                        title.contains("آموزش زبان به سبک مووی باکس")
                if (!isUnwanted) {
                    seriesList.add(series)
                }
            } catch (_: Exception) {
                continue
            }
        }

        return seriesList
    }

    private fun parseSeriesItem(seriesObj: org.json.JSONObject): Series {
        val durationRaw = seriesObj.optString("duration", "")
        val safeDuration = if (durationRaw.isBlank() || durationRaw.equals("null", ignoreCase = true) || durationRaw.equals("N/A", ignoreCase = true)) {
            null
        } else {
            durationRaw
        }

        return Series(
            id = seriesObj.optInt("id", 0),
            type = seriesObj.optString("type", "serie"),
            title = seriesObj.optString("title", "بدون عنوان"),
            description = seriesObj.optString("description", ""),
            year = seriesObj.optInt("year", 0),
            imdb = seriesObj.optDouble("imdb", 0.0),
            rating = seriesObj.optDouble("rating", 0.0),
            duration = safeDuration,
            image = seriesObj.optString("image", ""),
            cover = seriesObj.optString("cover", ""),
            genres = parseGenres(seriesObj.optJSONArray("genres")),
            country = parseCountries(seriesObj.optJSONArray("country"))
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
