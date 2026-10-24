package com.example

import com.example.data.remote.MediaJsonParser
import com.example.domain.model.FilterOrder
import com.example.domain.model.Media
import com.example.domain.usecase.GetFeaturedMediaUseCase
import com.example.domain.repository.CatalogRepository
import com.example.domain.model.Genre
import com.example.domain.model.Season
import com.example.ui.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class ApiParsingAndUseCaseTest {

    private val parser = MediaJsonParser()

    @Test
    fun `persian utils formats digits duration and rating`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", PersianUtils.toPersianDigits("1234567890"))
        assertEquals("۸.۵", PersianUtils.formatRating(8.5))
        assertEquals("۱۴۰۲", PersianUtils.toPersianDigits(1402))
        assertNull(PersianUtils.formatDuration(null))
        assertNull(PersianUtils.formatDuration("null"))
        assertNull(PersianUtils.formatDuration("N/A"))
        assertNull(PersianUtils.formatDuration("   "))
        assertEquals("۱۲۰ دقیقه", PersianUtils.formatDuration("120"))
        assertEquals("۹۵ دقیقه", PersianUtils.formatDuration("95 دقیقه"))
    }

    @Test
    fun `movie parser handles nullable duration and unknown fields`() {
        val json = """
            [{
                "id": 101,
                "type": "movie",
                "title": "جدایی نادر از سیمین",
                "description": "داستان یک طلاق پیچیده",
                "year": 1389,
                "imdb": 8.3,
                "rating": 9.1,
                "duration": "null",
                "image": "https://img.sample/poster.jpg",
                "cover": "https://img.sample/cover.jpg",
                "unknown_extra_field_from_server": "ignored",
                "genres": [{"id": 1, "title": "درام"}],
                "sources": [
                    {"id": 1, "quality": "1080p", "type": "mkv", "url": "https://dl.sample/1080.mkv"}
                ],
                "country": [{"id": 1, "title": "ایران", "image": ""}]
            }]
        """.trimIndent()

        val movies = parser.parseMediaArray(json)
        assertEquals(1, movies.size)
        val movie = movies.single()
        assertEquals(101, movie.id)
        assertEquals("جدایی نادر از سیمین", movie.title)
        assertNull(movie.duration)
        assertEquals(8.3, movie.imdb, 0.01)
        assertFalse(movie.isSeries)
        assertEquals("درام", movie.genres.single().title)
        assertEquals("1080p", movie.sources.single().quality)
    }

    @Test
    fun `series parser marks series and seasons include episodes`() {
        val seriesJson = """
            [{
                "id": 202,
                "type": "serie",
                "title": "پوست شیر",
                "description": "داستان نعیم",
                "year": 1401,
                "imdb": 8.5,
                "rating": 9.0,
                "duration": "N/A",
                "image": "https://img.sample/series.jpg"
            }]
        """.trimIndent()

        val series = parser.parseMediaArray(seriesJson).single()
        assertTrue(series.isSeries)
        assertNull(series.duration)

        val seasonsJson = """
            [{
                "id": 1,
                "title": "فصل اول",
                "episodes": [{
                    "id": 10,
                    "title": "قسمت اول",
                    "description": "شروع",
                    "duration": "50 دقیقه",
                    "image": "https://img.sample/e1.jpg",
                    "sources": [{
                        "id": 1,
                        "quality": "1080p",
                        "type": "mkv",
                        "url": "https://dl.sample/e1.mkv"
                    }]
                }]
            }]
        """.trimIndent()

        val seasons = parser.parseSeasons(seasonsJson)
        assertEquals(1, seasons.size)
        val episode = seasons.single().episodes.single()
        assertEquals(10, episode.id)
        assertEquals("1080p", episode.sources.single().quality)
    }

    @Test
    fun `unwanted advertisement titles are filtered`() {
        val json = """
            [
              {"id": 1, "title": "فیلم سالم", "type": "movie"},
              {"id": 2, "title": "آموزش زبان به سبک مووی باکس", "type": "movie"}
            ]
        """.trimIndent()

        val result = parser.parseMediaArray(json)
        assertEquals(1, result.size)
        assertEquals("فیلم سالم", result.single().title)
    }

    @Test
    fun `featured media returns five newest unique titles`() = runBlocking {
        val repository = FakeCatalogRepository(
            movies = (1..7).map { id -> media(id, year = 1400 + id) } +
                media(8, year = 1500)
        )
        val useCase = GetFeaturedMediaUseCase(repository)

        val featured = useCase(com.example.domain.model.MediaKind.MOVIE)

        assertEquals(5, featured.size)
        assertEquals(1500, featured.first().year)
        assertEquals(5, featured.map { it.id }.distinct().size)
    }

    private fun media(id: Int, year: Int): Media = Media(
        id = id,
        type = "movie",
        title = "فیلم $id",
        description = "",
        year = year,
        imdb = 7.0,
        rating = 7.0,
        duration = null,
        image = "",
        cover = ""
    )

    private class FakeCatalogRepository(
        private val movies: List<Media> = emptyList()
    ) : CatalogRepository {
        override suspend fun getMovies(page: Int, genreId: Int, order: FilterOrder): List<Media> =
            if (page == 0) movies else emptyList()

        override suspend fun getSeries(page: Int, genreId: Int, order: FilterOrder): List<Media> =
            emptyList()

        override suspend fun search(query: String): List<Media> = emptyList()

        override suspend fun getGenres(): List<Genre> = emptyList()

        override suspend fun getSeasons(seriesId: Int): List<Season> = emptyList()

        override fun findMedia(mediaId: Int): Media? = movies.firstOrNull { it.id == mediaId }
    }
}
