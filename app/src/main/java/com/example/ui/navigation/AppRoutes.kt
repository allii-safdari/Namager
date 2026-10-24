package com.example.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppRoutes {
    const val MOVIES = "movies"
    const val SERIES = "series"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"

    const val DETAILS = "details/{mediaId}"
    const val PLAYER =
        "player?url={url}&title={title}&subtitle={subtitle}&mediaId={mediaId}&seasonId={seasonId}&episodeId={episodeId}"

    val topLevelRoutes = setOf(MOVIES, SERIES, SEARCH, FAVORITES, SETTINGS)

    fun details(mediaId: Int): String = "details/$mediaId"

    fun player(
        url: String,
        title: String,
        subtitle: String?,
        mediaId: Int,
        seasonId: Int,
        episodeId: Int
    ): String = buildString {
        append("player?url=").append(encode(url))
        append("&title=").append(encode(title))
        append("&subtitle=").append(encode(subtitle.orEmpty()))
        append("&mediaId=").append(mediaId)
        append("&seasonId=").append(seasonId)
        append("&episodeId=").append(episodeId)
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
}
