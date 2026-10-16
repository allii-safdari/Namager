package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppSettings
import com.example.data.model.FavoriteGroup
import com.example.data.model.FavoriteItem
import com.example.data.model.ThemeMode
import com.example.data.model.UnifiedMedia
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cinematic_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GROUPS = "pref_fav_groups"
        private const val KEY_FAVORITES = "pref_fav_items"
        private const val KEY_WATCHED_EPISODES = "pref_watched_episodes"
        private const val KEY_THEME = "pref_theme_mode"
        private const val KEY_ACCENT = "pref_accent_color"
        private const val KEY_SEEK_SECONDS = "pref_seek_seconds"
        private const val KEY_SUBTITLE_SIZE = "pref_sub_size"
        private const val KEY_SUBTITLE_COLOR = "pref_sub_color"
        private const val KEY_SUBTITLE_BG = "pref_sub_bg"

        const val DEFAULT_GROUP_ID = "default_group"
    }

    init {
        ensureDefaultGroup()
    }

    private fun ensureDefaultGroup() {
        val groups = getFavoriteGroups()
        if (groups.none { it.id == DEFAULT_GROUP_ID }) {
            val defaultGroup = FavoriteGroup(
                id = DEFAULT_GROUP_ID,
                name = "پیش‌فرض",
                isDefault = true
            )
            saveFavoriteGroups(listOf(defaultGroup) + groups)
        }
    }

    // --- Favorite Groups ---
    fun getFavoriteGroups(): List<FavoriteGroup> {
        val jsonStr = prefs.getString(KEY_GROUPS, null) ?: return listOf(
            FavoriteGroup(id = DEFAULT_GROUP_ID, name = "پیش‌فرض", isDefault = true)
        )
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FavoriteGroup>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FavoriteGroup(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        isDefault = obj.optBoolean("isDefault", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            if (list.none { it.id == DEFAULT_GROUP_ID }) {
                listOf(FavoriteGroup(id = DEFAULT_GROUP_ID, name = "پیش‌فرض", isDefault = true)) + list
            } else {
                list
            }
        } catch (_: Exception) {
            listOf(FavoriteGroup(id = DEFAULT_GROUP_ID, name = "پیش‌فرض", isDefault = true))
        }
    }

    private fun saveFavoriteGroups(groups: List<FavoriteGroup>) {
        val array = JSONArray()
        groups.forEach { group ->
            val obj = JSONObject().apply {
                put("id", group.id)
                put("name", group.name)
                put("isDefault", group.isDefault)
                put("createdAt", group.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_GROUPS, array.toString()).apply()
    }

    fun createFavoriteGroup(name: String): FavoriteGroup {
        val groups = getFavoriteGroups().toMutableList()
        val newGroup = FavoriteGroup(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "پوشه جدید" },
            isDefault = false
        )
        groups.add(newGroup)
        saveFavoriteGroups(groups)
        return newGroup
    }

    fun renameFavoriteGroup(groupId: String, newName: String) {
        if (groupId == DEFAULT_GROUP_ID) return
        val groups = getFavoriteGroups().map {
            if (it.id == groupId) it.copy(name = newName.trim().ifEmpty { it.name }) else it
        }
        saveFavoriteGroups(groups)
    }

    fun deleteFavoriteGroup(groupId: String) {
        if (groupId == DEFAULT_GROUP_ID) return
        val groups = getFavoriteGroups().filter { it.id != groupId }
        saveFavoriteGroups(groups)
        // Also remove items belonging to this group
        val favorites = getFavoriteItems().filter { it.groupId != groupId }
        saveFavoriteItems(favorites)
    }

    // --- Favorites Items ---
    fun getFavoriteItems(): List<FavoriteItem> {
        val jsonStr = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FavoriteItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val mediaObj = obj.getJSONObject("media")
                val media = UnifiedMedia(
                    id = mediaObj.getInt("id"),
                    title = mediaObj.getString("title"),
                    type = mediaObj.getString("type"),
                    description = mediaObj.optString("description", ""),
                    year = mediaObj.optInt("year", 0),
                    imdb = mediaObj.optDouble("imdb", 0.0),
                    rating = mediaObj.optDouble("rating", 0.0),
                    duration = mediaObj.optString("duration", null).takeIf { it != "null" && it != "N/A" },
                    image = mediaObj.optString("image", ""),
                    cover = mediaObj.optString("cover", ""),
                    genres = emptyList(),
                    sources = emptyList(),
                    country = emptyList(),
                    isSeries = mediaObj.optBoolean("isSeries", false)
                )
                list.add(
                    FavoriteItem(
                        mediaId = obj.getInt("mediaId"),
                        groupId = obj.getString("groupId"),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                        media = media
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveFavoriteItems(items: List<FavoriteItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("mediaId", item.mediaId)
                put("groupId", item.groupId)
                put("addedAt", item.addedAt)
                val mediaObj = JSONObject().apply {
                    put("id", item.media.id)
                    put("title", item.media.title)
                    put("type", item.media.type)
                    put("description", item.media.description)
                    put("year", item.media.year)
                    put("imdb", item.media.imdb)
                    put("rating", item.media.rating)
                    put("duration", item.media.duration ?: "")
                    put("image", item.media.image)
                    put("cover", item.media.cover)
                    put("isSeries", item.media.isSeries)
                }
                put("media", mediaObj)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FAVORITES, array.toString()).apply()
    }

    fun addToFavorites(media: UnifiedMedia, groupId: String = DEFAULT_GROUP_ID) {
        val current = getFavoriteItems().toMutableList()
        // If already in this group, do nothing
        if (current.none { it.mediaId == media.id && it.groupId == groupId }) {
            current.add(FavoriteItem(mediaId = media.id, groupId = groupId, media = media))
            saveFavoriteItems(current)
        }
    }

    fun removeFromFavorites(mediaId: Int, groupId: String? = null) {
        val current = getFavoriteItems().filterNot {
            if (groupId == null) it.mediaId == mediaId else (it.mediaId == mediaId && it.groupId == groupId)
        }
        saveFavoriteItems(current)
    }

    fun isFavorite(mediaId: Int): Boolean {
        return getFavoriteItems().any { it.mediaId == mediaId }
    }

    fun clearAllFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    // --- Watched Episodes ---
    fun markEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int) {
        val watched = getWatchedEpisodeKeys().toMutableSet()
        watched.add("$seriesId-$seasonId-$episodeId")
        prefs.edit().putStringSet(KEY_WATCHED_EPISODES, watched).apply()
    }

    fun isEpisodeWatched(seriesId: Int, seasonId: Int, episodeId: Int): Boolean {
        return getWatchedEpisodeKeys().contains("$seriesId-$seasonId-$episodeId")
    }

    private fun getWatchedEpisodeKeys(): Set<String> {
        return prefs.getStringSet(KEY_WATCHED_EPISODES, emptySet()) ?: emptySet()
    }

    // --- App Settings ---
    fun getAppSettings(): AppSettings {
        val themeName = prefs.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        val themeMode = try { ThemeMode.valueOf(themeName) } catch (_: Exception) { ThemeMode.DARK }
        val accent = prefs.getLong(KEY_ACCENT, 0xFFE50914)
        val seek = prefs.getInt(KEY_SEEK_SECONDS, 10)
        val subSize = prefs.getInt(KEY_SUBTITLE_SIZE, 18)
        val subColor = prefs.getLong(KEY_SUBTITLE_COLOR, 0xFFFFFFFF)
        val subBg = prefs.getLong(KEY_SUBTITLE_BG, 0x88000000)

        return AppSettings(
            themeMode = themeMode,
            accentColorHex = accent,
            seekJumpSeconds = seek,
            subtitleFontSizeSp = subSize,
            subtitleTextColorHex = subColor,
            subtitleBackgroundColorHex = subBg
        )
    }

    fun saveAppSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putLong(KEY_ACCENT, settings.accentColorHex)
            .putInt(KEY_SEEK_SECONDS, settings.seekJumpSeconds)
            .putInt(KEY_SUBTITLE_SIZE, settings.subtitleFontSizeSp)
            .putLong(KEY_SUBTITLE_COLOR, settings.subtitleTextColorHex)
            .putLong(KEY_SUBTITLE_BG, settings.subtitleBackgroundColorHex)
            .apply()
    }

    fun resetSettings() {
        saveAppSettings(AppSettings())
    }
}
