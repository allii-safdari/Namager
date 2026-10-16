package com.example.data.model

enum class FilterOrder(val value: String, val titleFa: String) {
    CREATED("created", "جدیدترین"),
    YEAR("year", "سال ساخت"),
    IMDB("imdb", "امتیاز IMDb");

    companion object {
        fun fromValue(value: String): FilterOrder {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: CREATED
        }
    }
}

// Deprecated alias for compatibility
typealias FilterType = FilterOrder

data class Genre(
    val id: Int,
    val title: String
)

data class Country(
    val id: Int,
    val title: String,
    val image: String = ""
)

data class Source(
    val id: Int,
    val quality: String,
    val type: String,
    val url: String
)

data class Episode(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String? = null,
    val image: String = "",
    val sources: List<Source> = emptyList()
)

data class Season(
    val id: Int,
    val title: String,
    val episodes: List<Episode> = emptyList()
)

data class Movie(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String? = null,
    val image: String = "",
    val cover: String = "",
    val genres: List<Genre> = emptyList(),
    val sources: List<Source> = emptyList(),
    val country: List<Country> = emptyList()
)

data class Series(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String? = null,
    val image: String = "",
    val cover: String = "",
    val genres: List<Genre> = emptyList(),
    val country: List<Country> = emptyList()
)

data class Poster(
    val id: Int,
    val title: String,
    val type: String,
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String? = null,
    val image: String = "",
    val cover: String = "",
    val genres: List<Genre> = emptyList(),
    val sources: List<Source> = emptyList(),
    val country: List<Country> = emptyList()
)

data class SearchResult(
    val posters: List<Poster> = emptyList()
)

// Unified media item interface for clean Compose consumption
data class UnifiedMedia(
    val id: Int,
    val title: String,
    val type: String, // "movie", "serie", etc.
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String?,
    val image: String,
    val cover: String,
    val genres: List<Genre>,
    val sources: List<Source>,
    val country: List<Country>,
    val isSeries: Boolean = type.contains("serie", ignoreCase = true)
)

fun Movie.toUnified(): UnifiedMedia = UnifiedMedia(
    id = id,
    title = title,
    type = if (type.isEmpty()) "movie" else type,
    description = description,
    year = year,
    imdb = imdb,
    rating = rating,
    duration = duration,
    image = image,
    cover = cover,
    genres = genres,
    sources = sources,
    country = country,
    isSeries = false
)

fun Series.toUnified(): UnifiedMedia = UnifiedMedia(
    id = id,
    title = title,
    type = if (type.isEmpty()) "serie" else type,
    description = description,
    year = year,
    imdb = imdb,
    rating = rating,
    duration = duration,
    image = image,
    cover = cover,
    genres = genres,
    sources = emptyList(),
    country = country,
    isSeries = true
)

fun Poster.toUnified(): UnifiedMedia = UnifiedMedia(
    id = id,
    title = title,
    type = type,
    description = description,
    year = year,
    imdb = imdb,
    rating = rating,
    duration = duration,
    image = image,
    cover = cover,
    genres = genres,
    sources = sources,
    country = country,
    isSeries = type.contains("serie", ignoreCase = true)
)

// Local persistence models for Favorites & Settings
data class FavoriteGroup(
    val id: String,
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class FavoriteItem(
    val mediaId: Int,
    val groupId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val media: UnifiedMedia
)

data class WatchedEpisode(
    val seriesId: Int,
    val seasonId: Int,
    val episodeId: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

enum class ThemeMode(val titleFa: String) {
    SYSTEM("پیروی از سیستم"),
    DARK("حالت تیره"),
    LIGHT("حالت روشن")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColorHex: Long = 0xFFE50914, // Netflix Red
    val seekJumpSeconds: Int = 10,
    val subtitleFontSizeSp: Int = 18,
    val subtitleTextColorHex: Long = 0xFFFFFFFF,
    val subtitleBackgroundColorHex: Long = 0x88000000
)
