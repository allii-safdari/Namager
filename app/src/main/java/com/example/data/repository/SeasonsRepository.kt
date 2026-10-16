package com.example.data.repository

import com.example.data.model.Episode
import com.example.data.model.Season
import com.example.data.model.Source
import com.example.domain.repository.ISeasonsRepository
import javax.inject.Inject

class SeasonsRepository @Inject constructor() : BaseRepository(), ISeasonsRepository {

    override suspend fun getSeasons(seriesId: Int): List<Season> {
        val url = "$baseUrl/api/season/by/serie/$seriesId/$apiKey/"
        val jsonData = executeRequest(url)
        return parseSeasons(jsonData)
    }

    private fun parseSeasons(jsonData: String): List<Season> {
        val seasons = mutableListOf<Season>()
        val jsonArray = org.json.JSONArray(jsonData)

        for (i in 0 until jsonArray.length()) {
            try {
                val seasonObj = jsonArray.getJSONObject(i)
                seasons.add(parseSeason(seasonObj))
            } catch (_: Exception) {
                continue
            }
        }

        return seasons
    }

    private fun parseSeason(seasonObj: org.json.JSONObject): Season {
        return Season(
            id = seasonObj.optInt("id", 0),
            title = seasonObj.optString("title", "فصل"),
            episodes = try {
                parseEpisodes(seasonObj.optJSONArray("episodes"))
            } catch (_: Exception) {
                emptyList()
            }
        )
    }

    private fun parseEpisodes(episodesArray: org.json.JSONArray?): List<Episode> {
        if (episodesArray == null) return emptyList()
        val episodes = mutableListOf<Episode>()

        for (i in 0 until episodesArray.length()) {
            try {
                val episodeObj = episodesArray.getJSONObject(i)
                val durationRaw = episodeObj.optString("duration", "")
                val safeDuration = if (durationRaw.isBlank() || durationRaw.equals("null", ignoreCase = true) || durationRaw.equals("N/A", ignoreCase = true)) {
                    null
                } else {
                    durationRaw
                }

                episodes.add(
                    Episode(
                        id = episodeObj.optInt("id", 0),
                        title = episodeObj.optString("title", "قسمت"),
                        description = episodeObj.optString("description", ""),
                        duration = safeDuration,
                        image = episodeObj.optString("image", ""),
                        sources = parseSources(episodeObj.optJSONArray("sources"))
                    )
                )
            } catch (_: Exception) {
                continue
            }
        }

        return episodes
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
}
