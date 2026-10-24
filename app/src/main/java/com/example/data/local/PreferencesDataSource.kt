package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.core.error.AppException
import com.example.domain.model.AppSettings
import com.example.domain.model.Country
import com.example.domain.model.FavoriteGroup
import com.example.domain.model.FavoriteItem
import com.example.domain.model.Genre
import com.example.domain.model.Media
import com.example.domain.model.Source
import com.example.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Local preferences data source. All multi-item reads/writes run on IO; only the
 * tiny settings snapshot is read eagerly so the theme can be applied without a
 * visible flash at launch.
 */
@Singleton
class PreferencesDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        val themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        }.getOrDefault(ThemeMode.DARK)

        return AppSettings(
            themeMode = themeMode,
            accentColorHex = prefs.getLong(KEY_ACCENT, 0xFFE50914),
            seekJumpSeconds = prefs.getInt(KEY_SEEK_SECONDS, 10),
            subtitleFontSizeSp = subtitlesSafeSize(prefs.getInt(KEY_SUBTITLE_SIZE, 18)),
            subtitleTextColorHex = prefs.getLong(KEY_SUBTITLE_COLOR, 0xFFFFFFFF),
            subtitleBackgroundColorHex = prefs.getLong(KEY_SUBTITLE_BG, 0x88000000)
        )
    }

    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        val saved = prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putLong(KEY_ACCENT, settings.accentColorHex)
            .putInt(KEY_SEEK_SECONDS, settings.seekJumpSeconds)
            .putInt(KEY_SUBTITLE_SIZE, settings.subtitleFontSizeSp)
            .putLong(KEY_SUBTITLE_COLOR, settings.subtitleTextColorHex)
            .putLong(KEY_SUBTITLE_BG, settings.subtitleBackgroundColorHex)
            .commit()
        if (!saved) throw AppException.Storage()
    }

    suspend fun loadGroups(): List<FavoriteGroup> = withContext(Dispatchers.IO) {
        val raw = prefs.getString(KEY_GROUPS, null) ?: return@withContext listOf(FavoriteGroup.Default)
        val parsed = runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        FavoriteGroup(
                            id = item.optString("id", ""),
                            name = item.optString("name", ""),
                            isDefault = item.optBoolean("isDefault", false),
                            createdAt = item.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }.filter { it.id.isNotBlank() && it.name.isNotBlank() }
        }.getOrDefault(emptyList())

        if (parsed.any { it.id == FavoriteGroup.DEFAULT_ID }) {
            parsed
        } else {
            listOf(FavoriteGroup.Default) + parsed
        }
    }

    suspend fun saveGroups(groups: List<FavoriteGroup>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        groups.forEach { group ->
            array.put(
                JSONObject()
                    .put("id", group.id)
                    .put("name", group.name)
                    .put("isDefault", group.isDefault)
                    .put("createdAt", group.createdAt)
            )
        }
        prefs.edit().putString(KEY_GROUPS, array.toString()).apply()
    }

    suspend fun loadFavoriteItems(): List<FavoriteItem> = withContext(Dispatchers.IO) {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return@withContext emptyList()
        runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val media = item.optJSONObject("media")?.toMedia() ?: continue
                    add(
                        FavoriteItem(
                            mediaId = item.optInt("mediaId", media.id),
                            groupId = item.optString("groupId", FavoriteGroup.DEFAULT_ID),
                            addedAt = item.optLong("addedAt", System.currentTimeMillis()),
                            media = media
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    suspend fun saveFavoriteItems(items: List<FavoriteItem>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("mediaId", item.mediaId)
                    .put("groupId", item.groupId)
                    .put("addedAt", item.addedAt)
                    .put("media", item.media.toJson())
            )
        }
        prefs.edit().putString(KEY_FAVORITES, array.toString()).apply()
    }

    suspend fun loadWatchedEpisodes(): Set<String> = withContext(Dispatchers.IO) {
        prefs.getStringSet(KEY_WATCHED_EPISODES, emptySet())?.toSet() ?: emptySet()
    }

    suspend fun saveWatchedEpisodes(episodes: Set<String>) = withContext(Dispatchers.IO) {
        prefs.edit().putStringSet(KEY_WATCHED_EPISODES, episodes).apply()
    }

    fun newGroupId(): String = UUID.randomUUID().toString()

    private fun Media.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("type", type)
        put("title", title)
        put("description", description)
        put("year", year)
        put("imdb", imdb)
        put("rating", rating)
        put("duration", duration ?: "")
        put("image", image)
        put("cover", cover)
        put("genres", genres.toJsonArray { genre ->
            JSONObject().put("id", genre.id).put("title", genre.title)
        })
        put("sources", sources.toJsonArray { source ->
            JSONObject()
                .put("id", source.id)
                .put("quality", source.quality)
                .put("type", source.type)
                .put("url", source.url)
        })
        put("country", country.toJsonArray { country ->
            JSONObject()
                .put("id", country.id)
                .put("title", country.title)
                .put("image", country.image)
        })
    }

    private fun JSONObject.toMedia(): Media {
        val durationRaw = optString("duration", "")
        return Media(
            id = optInt("id", 0),
            type = optString("type", "movie"),
            title = optString("title", ""),
            description = optString("description", ""),
            year = optInt("year", 0),
            imdb = optDouble("imdb", 0.0),
            rating = optDouble("rating", 0.0),
            duration = durationRaw.takeIf {
                it.isNotBlank() && !it.equals("null", true) && !it.equals("N/A", true)
            },
            image = optString("image", ""),
            cover = optString("cover", ""),
            genres = optJSONArray("genres").toGenres(),
            sources = optJSONArray("sources").toSources(),
            country = optJSONArray("country").toCountries()
        )
    }

    private fun JSONArray?.toGenres(): List<Genre> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(Genre(item.optInt("id", 0), item.optString("title", "")))
            }
        }
    }

    private fun JSONArray?.toSources(): List<Source> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    Source(
                        id = item.optInt("id", 0),
                        quality = item.optString("quality", "720p"),
                        type = item.optString("type", "mp4"),
                        url = item.optString("url", "")
                    )
                )
            }
        }
    }

    private fun JSONArray?.toCountries(): List<Country> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(
                    Country(
                        id = item.optInt("id", 0),
                        title = item.optString("title", ""),
                        image = item.optString("image", "")
                    )
                )
            }
        }
    }

    private inline fun <T> List<T>.toJsonArray(crossinline transform: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { item -> array.put(transform(item)) }
        return array
    }

    private fun subtitlesSafeSize(value: Int): Int = value.coerceIn(12, 32)

    private companion object {
        const val PREFERENCES_NAME = "cinematic_prefs"
        const val KEY_GROUPS = "pref_fav_groups"
        const val KEY_FAVORITES = "pref_fav_items"
        const val KEY_WATCHED_EPISODES = "pref_watched_episodes"
        const val KEY_THEME = "pref_theme_mode"
        const val KEY_ACCENT = "pref_accent_color"
        const val KEY_SEEK_SECONDS = "pref_seek_seconds"
        const val KEY_SUBTITLE_SIZE = "pref_sub_size"
        const val KEY_SUBTITLE_COLOR = "pref_sub_color"
        const val KEY_SUBTITLE_BG = "pref_sub_bg"
    }
}
