package com.example.domain.model

/**
 * The two catalogue types supported by the app. Keeping the kind in the domain
 * layer lets presentation code stay independent from API route names.
 */
enum class MediaKind(val titleFa: String) {
    MOVIE("فیلم"),
    SERIES("سریال")
}

enum class FilterOrder(val value: String, val titleFa: String) {
    CREATED("created", "جدیدترین"),
    YEAR("year", "سال ساخت"),
    IMDB("imdb", "امتیاز IMDb");

    companion object {
        fun fromValue(value: String): FilterOrder =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: CREATED
    }
}

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

/**
 * Single presentation-friendly media model used by movies, series, search and
 * favourites. The old app had four near-identical DTO/UI models which caused
 * duplicated mapping code and inconsistent behaviour.
 */
data class Media(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val year: Int,
    val imdb: Double,
    val rating: Double,
    val duration: String?,
    val image: String,
    val cover: String,
    val genres: List<Genre> = emptyList(),
    val sources: List<Source> = emptyList(),
    val country: List<Country> = emptyList()
) {
    val isSeries: Boolean
        get() = type.contains("serie", ignoreCase = true)

    val kind: MediaKind
        get() = if (isSeries) MediaKind.SERIES else MediaKind.MOVIE

    val heroImage: String
        get() = cover.ifBlank { image }
}
