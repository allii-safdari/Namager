package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.model.ScreenTab
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.CinemaViewModel
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApiParsingAndViewModelTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `test PersianUtils digit and duration formatting`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", PersianUtils.toPersianDigits("1234567890"))
        assertEquals("۸.۵", PersianUtils.formatRating(8.5))
        assertEquals("۱۴۰۲", PersianUtils.toPersianDigits(1402))

        // Resilience against null / "null" / "N/A"
        assertNull(PersianUtils.formatDuration(null))
        assertNull(PersianUtils.formatDuration("null"))
        assertNull(PersianUtils.formatDuration("N/A"))
        assertNull(PersianUtils.formatDuration("   "))

        assertEquals("۱۲۰ دقیقه", PersianUtils.formatDuration("120"))
        assertEquals("۹۵ دقیقه", PersianUtils.formatDuration("95 دقیقه"))
    }

    @Test
    fun `test JSON parsing of Movie with null duration and unknown fields`() {
        val jsonString = """
            {
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
                "unknown_extra_field_from_server": "should be safely ignored",
                "genres": [{"id": 1, "title": "درام"}],
                "sources": [
                    {"id": 1, "quality": "1080p", "type": "mkv", "url": "https://dl.sample/1080.mkv"},
                    {"id": 2, "quality": "720p", "type": "mp4", "url": "https://dl.sample/720.mp4"}
                ],
                "country": [{"id": 1, "title": "ایران", "image": "https://img.sample/flag.png"}]
            }
        """.trimIndent()

        val obj = JSONObject(jsonString)
        val durationRaw = obj.optString("duration")
        val safeDuration = if (durationRaw.isBlank() || durationRaw == "null" || durationRaw == "N/A") null else durationRaw

        val movie = Movie(
            id = obj.getInt("id"),
            type = obj.getString("type"),
            title = obj.getString("title"),
            description = obj.getString("description"),
            year = obj.getInt("year"),
            imdb = obj.getDouble("imdb"),
            rating = obj.getDouble("rating"),
            duration = safeDuration,
            image = obj.getString("image"),
            cover = obj.getString("cover")
        )

        assertEquals(101, movie.id)
        assertEquals("جدایی نادر از سیمین", movie.title)
        assertNull(movie.duration)
        assertEquals(8.3, movie.imdb, 0.01)

        val unified = movie.toUnified()
        assertFalse(unified.isSeries)
        assertEquals(101, unified.id)
    }

    @Test
    fun `test JSON parsing of Series and Seasons with episodes`() {
        val seriesJson = """
            {
                "id": 202,
                "type": "serie",
                "title": "پوست شیر",
                "description": "داستان نعیم و ماجرای ساحل",
                "year": 1401,
                "imdb": 8.5,
                "rating": 9.0,
                "duration": "N/A",
                "image": "https://img.sample/series.jpg"
            }
        """.trimIndent()

        val seriesObj = JSONObject(seriesJson)
        val durationRaw = seriesObj.optString("duration")
        val safeDuration = if (durationRaw == "null" || durationRaw == "N/A") null else durationRaw

        val series = Series(
            id = seriesObj.getInt("id"),
            type = seriesObj.getString("type"),
            title = seriesObj.getString("title"),
            description = seriesObj.getString("description"),
            year = seriesObj.getInt("year"),
            imdb = seriesObj.getDouble("imdb"),
            rating = seriesObj.getDouble("rating"),
            duration = safeDuration
        )

        val unified = series.toUnified()
        assertTrue(unified.isSeries)
        assertNull(unified.duration)

        // Test Season & Episodes parsing
        val seasonJson = """
            {
                "id": 1,
                "title": "فصل اول",
                "episodes": [
                    {
                        "id": 10,
                        "title": "قسمت ۱",
                        "description": "شروع ماجرا",
                        "duration": "50 دقیقه",
                        "image": "https://img.sample/e1.jpg",
                        "sources": [{"id": 1, "quality": "1080p", "type": "mkv", "url": "https://dl.sample/e1.mkv"}]
                    }
                ]
            }
        """.trimIndent()

        val sObj = JSONObject(seasonJson)
        val epArray = sObj.getJSONArray("episodes")
        assertEquals(1, epArray.length())
        val epObj = epArray.getJSONObject(0)
        assertEquals(10, epObj.getInt("id"))
        assertEquals("قسمت ۱", epObj.getString("title"))
    }

    @Test
    fun `test ViewModel navigation, favorites groups and watched status`() {
        val viewModel = CinemaViewModel(application)

        // 1. Navigation tabs
        assertEquals(ScreenTab.MOVIES, viewModel.currentTab.value)
        viewModel.setTab(ScreenTab.SERIES)
        assertEquals(ScreenTab.SERIES, viewModel.currentTab.value)
        viewModel.setTab(ScreenTab.SEARCH)
        assertEquals(ScreenTab.SEARCH, viewModel.currentTab.value)
        viewModel.setTab(ScreenTab.FAVORITES)
        assertEquals(ScreenTab.FAVORITES, viewModel.currentTab.value)
        viewModel.setTab(ScreenTab.SETTINGS)
        assertEquals(ScreenTab.SETTINGS, viewModel.currentTab.value)

        // 2. Custom Favorite Groups
        val initialGroups = viewModel.favoriteGroups.value
        assertTrue(initialGroups.any { it.isDefault })

        viewModel.createFavoriteGroup("فیلم‌های جشنواره‌ای")
        val updatedGroups = viewModel.favoriteGroups.value
        assertTrue(updatedGroups.any { it.name == "فیلم‌های جشنواره‌ای" })

        val customGroup = updatedGroups.first { it.name == "فیلم‌های جشنواره‌ای" }
        viewModel.renameFavoriteGroup(customGroup.id, "شاهکارهای سینما")
        assertTrue(viewModel.favoriteGroups.value.any { it.name == "شاهکارهای سینما" })

        // 3. Watched Episodes tracking
        assertFalse(viewModel.isEpisodeWatched(seriesId = 55, seasonId = 1, episodeId = 1))
        viewModel.markEpisodeWatched(seriesId = 55, seasonId = 1, episodeId = 1)
        assertTrue(viewModel.isEpisodeWatched(seriesId = 55, seasonId = 1, episodeId = 1))

        // 4. Settings update
        viewModel.updateTheme(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.settings.value.themeMode)

        viewModel.updateSeekJumpSeconds(15)
        assertEquals(15, viewModel.settings.value.seekJumpSeconds)

        viewModel.resetSettings()
        assertEquals(ThemeMode.DARK, viewModel.settings.value.themeMode)
        assertEquals(10, viewModel.settings.value.seekJumpSeconds)
    }
}
