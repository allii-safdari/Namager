package com.example.domain.model

data class FavoriteGroup(
    val id: String,
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_ID = "default_group"
        val Default = FavoriteGroup(
            id = DEFAULT_ID,
            name = "پیش‌فرض",
            isDefault = true
        )
    }
}

data class FavoriteItem(
    val mediaId: Int,
    val groupId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val media: Media
)

enum class ThemeMode(val titleFa: String) {
    SYSTEM("پیروی از سیستم"),
    DARK("حالت تیره"),
    LIGHT("حالت روشن")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColorHex: Long = 0xFFE50914,
    val seekJumpSeconds: Int = 10,
    val subtitleFontSizeSp: Int = 18,
    val subtitleTextColorHex: Long = 0xFFFFFFFF,
    val subtitleBackgroundColorHex: Long = 0x88000000
)
