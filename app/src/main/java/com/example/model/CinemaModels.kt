package com.example.model

import androidx.annotation.DrawableRes

enum class MediaType {
    MOVIE,
    SERIES
}

data class VideoQuality(
    val id: String,
    val label: String,
    val resolution: String,
    val fileSize: String,
    val bitrate: String,
    val description: String
)

enum class DownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    PAUSED,
    COMPLETED
}

data class DownloadItem(
    val movieId: String,
    val movie: MovieItem,
    val quality: VideoQuality,
    val progress: Float,
    val status: DownloadStatus,
    val downloadedSize: String,
    val totalSize: String
)

data class CastMember(
    val name: String,
    val role: String,
    val avatarInitial: String = name.take(1)
)

data class Episode(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val duration: String,
    val synopsis: String,
    @DrawableRes val thumbnailRes: Int? = null
)

data class Director(
    val name: String,
    val bio: String,
    val notableFilms: List<String>
)

data class Genre(
    val id: String,
    val name: String,
    val englishName: String
)

data class MovieItem(
    val id: String,
    val title: String,
    val englishTitle: String,
    val mediaType: MediaType,
    val director: String,
    val releaseYear: String,
    val duration: String, // e.g. "۱۲۵ دقیقه" or "۳ فصل"
    val imdbRating: Float,
    val ageRating: String, // e.g. "+۱۵"
    @DrawableRes val backdropRes: Int? = null,
    val genres: List<String>,
    val synopsis: String,
    val awards: List<String> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val episodes: List<Episode> = emptyList(),
    val isFeatured: Boolean = false,
    val isCriticChoice: Boolean = false,
    val isTrending: Boolean = false,
    val qualityBadge: String = "4K UHD"
)

enum class ScreenTab {
    MOVIES,
    SERIES,
    SEARCH,
    FAVORITES,
    SETTINGS;

    companion object {
        val HOME = MOVIES
        val EXPLORE = SEARCH
        val WATCHLIST = FAVORITES
    }
}
