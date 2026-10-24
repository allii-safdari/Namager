package com.example

import com.example.ui.navigation.AppRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSmokeTest {

    @Test
    fun `player route encodes url and extras`() {
        val route = AppRoutes.player(
            url = "https://example.com/video.mkv?token=1&x=2",
            title = "فیلم تست",
            subtitle = "قسمت ۱",
            mediaId = 12,
            seasonId = 3,
            episodeId = 4
        )

        assertTrue(route.startsWith("player?url="))
        assertTrue(route.contains("token%3D1%26x%3D2"))
        assertTrue(route.contains("mediaId=12"))
        assertTrue(route.contains("seasonId=3"))
    }

    @Test
    fun `details route contains media id`() {
        assertEquals("details/42", AppRoutes.details(42))
    }
}
