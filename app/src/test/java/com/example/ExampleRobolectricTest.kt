package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FilterOrder
import com.example.data.model.Source
import com.example.model.ScreenTab
import com.example.ui.viewmodel.CinemaViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Cinematic View", appName)
    }

    @Test
    fun `test viewmodel tab change and watchlist toggle`() {
        val viewModel = CinemaViewModel(application)
        assertEquals(ScreenTab.MOVIES, viewModel.currentTab.value)

        viewModel.setTab(ScreenTab.SEARCH)
        assertEquals(ScreenTab.SEARCH, viewModel.currentTab.value)

        val isOneInitiallySaved = viewModel.isFavorite(1)
        viewModel.toggleWatchlist(1)
        assertEquals(!isOneInitiallySaved, viewModel.isFavorite(1))

        viewModel.toggleWatchlist(1)
        assertEquals(isOneInitiallySaved, viewModel.isFavorite(1))
    }

    @Test
    fun `test filter and sort updates`() {
        val viewModel = CinemaViewModel(application)
        assertEquals(0, viewModel.selectedGenreId.value)
        assertEquals(FilterOrder.CREATED, viewModel.selectedOrder.value)

        viewModel.setGenreFilter(2)
        assertEquals(2, viewModel.selectedGenreId.value)

        viewModel.setSortOrder(FilterOrder.IMDB)
        assertEquals(FilterOrder.IMDB, viewModel.selectedOrder.value)
    }

    @Test
    fun `test search query and player actions`() {
        val viewModel = CinemaViewModel(application)
        viewModel.updateSearchQuery("فروشنده")
        assertEquals("فروشنده", viewModel.searchQuery.value)

        val sampleSource = Source(1, "1080p", "mkv", "https://sample.com/video.mkv")
        viewModel.playMediaSource(sampleSource, "فروشنده")
        assertTrue(viewModel.isPlaying.value)
        assertEquals(sampleSource, viewModel.activePlayingSource.value)

        viewModel.closePlayer()
        assertFalse(viewModel.isPlaying.value)

        viewModel.openDownloadOptions(sampleSource)
        assertEquals(sampleSource, viewModel.activeSourceForDownload.value)

        viewModel.closeDownloadOptions()
        assertNull(viewModel.activeSourceForDownload.value)
    }
}
