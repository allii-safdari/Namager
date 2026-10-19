
package com.example.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Episode
import com.example.data.model.Source
import com.example.data.model.UnifiedMedia
import com.example.ui.components.CinematicPosterImage
import com.example.ui.components.DownloadOptionsDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.ExpandableText
import com.example.ui.components.QualitySelectionSheet
import com.example.ui.player.ExoPlayerView
import com.example.ui.theme.*
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.CinemaViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailSheet(
    media: UnifiedMedia,
    viewModel: CinemaViewModel,
    isWatchlist: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    val seasonsState by viewModel.seasonsState.collectAsStateWithLifecycle()
    val activePlayingSource by viewModel.activePlayingSource.collectAsStateWithLifecycle()
    val activePlayingTitle by viewModel.activePlayingTitle.collectAsStateWithLifecycle()
    val activePlayingSubtitle by viewModel.activePlayingSubtitle.collectAsStateWithLifecycle()
    val activeDownloadSource by viewModel.activeSourceForDownload.collectAsStateWithLifecycle()
    val favoriteGroups by viewModel.favoriteGroups.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedSeasonIndex by remember(media.id) {
        mutableIntStateOf(0)
    }

    var qualitySelectionDialogData by remember {
        mutableStateOf<QualityDialogData?>(null)
    }

    var showFavoriteGroupPicker by remember {
        mutableStateOf(false)
    }

    // Full-screen player
    if (activePlayingSource != null) {
        ExoPlayerView(
            source = activePlayingSource!!,
            title = activePlayingTitle,
            subtitle = activePlayingSubtitle,
            seekJumpSeconds = settings.seekJumpSeconds,
            onClose = { viewModel.closePlayer() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("movie_detail_sheet"),
        containerColor = DarkBackground
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header: backdrop + original poster
            item(key = "movie_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val backdropUrl = media.cover
                        .takeIf { it.isNotBlank() }
                        ?: media.image

                    val posterUrl = media.image
                        .takeIf { it.isNotBlank() }
                        ?: media.cover

                    // Background backdrop
                    CinematicPosterImage(
                        imageUrl = backdropUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x66000000),
                                        Color(0x22000000),
                                        DarkBackground.copy(alpha = 0.72f),
                                        DarkBackground
                                    )
                                )
                            )
                    )

                    // Foreground poster
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp)
                            .width(132.dp)
                            .height(198.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        CinematicPosterImage(
                            imageUrl = posterUrl,
                            contentDescription = media.title,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Header actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Color(0x88000000),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector =
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                showFavoriteGroupPicker = true
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Color(0x88000000),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isWatchlist) {
                                    Icons.Default.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = "افزودن به علاقه‌مندی",
                                tint = if (isWatchlist) {
                                    CinemaGold
                                } else {
                                    Color.White
                                }
                            )
                        }
                    }
                }
            }

            // Main movie information and actions
            item(key = "movie_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Rating, year and duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (media.imdb > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(16.dp)
                                )

                                Text(
                                    text = PersianUtils.formatRating(media.imdb),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "IMDb",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (media.year > 0) {
                            Text(
                                text = PersianUtils.toPersianDigits(media.year),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        PersianUtils.formatDuration(media.duration)?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Genres: show at most 3
                    if (media.genres.isNotEmpty()) {
                        val visibleGenres = media.genres.take(3)
                        val remainingCount =
                            media.genres.size - visibleGenres.size

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            visibleGenres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = DarkSurface,
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Text(
                                        text = genre.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }

                            if (remainingCount > 0) {
                                Text(
                                    text = "+${
                                        PersianUtils.toPersianDigits(
                                            remainingCount
                                        )
                                    }",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CinemaGold
                                )
                            }
                        }
                    }

                    // Primary actions
                    if (!media.isSeries) {
                        val availableSources = media.sources

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (availableSources.isNotEmpty()) {
                                        qualitySelectionDialogData =
                                            QualityDialogData(
                                                title = media.title,
                                                sources = availableSources
                                            )
                                    }
                                },
                                enabled = availableSources.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text(
                                    text = "پخش آنلاین",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    availableSources.firstOrNull()?.let {
                                        viewModel.openDownloadOptions(it)
                                    }
                                },
                                enabled = availableSources.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text(
                                    text = "دانلود",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Synopsis
            if (media.description.isNotBlank()) {
                item(key = "movie_synopsis") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "خلاصه داستان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CinemaGold
                        )

                        ExpandableText(
                            text = media.description,
                            collapsedMaxLines = 3
                        )

                        Log.d("MovieDetailSheet", "ExpandableText: ${media.description}")
                    }
                }
            }

            // Series seasons and episodes
            if (media.isSeries) {
                item(key = "series_episodes") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "فصل‌ها و قسمت‌ها",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )

                        when (val state = seasonsState) {
                            is UiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = CinemaGold
                                    )
                                }
                            }

                            is UiState.Error -> {
                                ErrorRetryView(
                                    message = state.message,
                                    onRetry = {
                                        viewModel.loadSeasons(media.id)
                                    }
                                )
                            }

                            is UiState.Empty -> {
                                EmptyStateView(
                                    message = "قسمتی برای این سریال ثبت نشده است."
                                )
                            }

                            is UiState.Success -> {
                                val seasons = state.data

                                if (seasons.isEmpty()) {
                                    EmptyStateView(
                                        message = "قسمتی برای این سریال ثبت نشده است."
                                    )
                                } else {
                                    val safeSeasonIndex =
                                        selectedSeasonIndex.coerceIn(
                                            0,
                                            seasons.lastIndex
                                        )

                                    ScrollableTabRow(
                                        selectedTabIndex = safeSeasonIndex,
                                        containerColor = DarkSurface,
                                        contentColor = CinemaGold,
                                        edgePadding = 0.dp
                                    ) {
                                        seasons.forEachIndexed { index, season ->
                                            Tab(
                                                selected = safeSeasonIndex == index,
                                                onClick = {
                                                    selectedSeasonIndex = index
                                                },
                                                text = {
                                                    Text(
                                                        text = season.title.ifEmpty {
                                                            "فصل ${
                                                                PersianUtils.toPersianDigits(
                                                                    index + 1
                                                                )
                                                            }"
                                                        },
                                                        fontWeight = if (
                                                            safeSeasonIndex == index
                                                        ) {
                                                            FontWeight.Bold
                                                        } else {
                                                            FontWeight.Normal
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    val currentSeason = seasons[safeSeasonIndex]
                                    val episodes = currentSeason.episodes

                                    if (episodes.isEmpty()) {
                                        EmptyStateView(
                                            message = "قسمتی در این فصل یافت نشد."
                                        )
                                    } else {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            episodes.forEachIndexed { epIndex, episode ->
                                                val isWatched =
                                                    viewModel.isEpisodeWatched(
                                                        seriesId = media.id,
                                                        seasonId = currentSeason.id,
                                                        episodeId = episode.id
                                                    )

                                                EpisodeListItem(
                                                    episode = episode,
                                                    episodeNumber = epIndex + 1,
                                                    isWatched = isWatched,
                                                    onEpisodeClick = {
                                                        val sources = episode.sources

                                                        if (sources.isNotEmpty()) {
                                                            qualitySelectionDialogData =
                                                                QualityDialogData(
                                                                    title = "${media.title} - ${episode.title}",
                                                                    sources = sources,
                                                                    seriesId = media.id,
                                                                    seasonId = currentSeason.id,
                                                                    episodeId = episode.id
                                                                )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Quality selection: play or download
    qualitySelectionDialogData?.let { data ->
        QualitySelectionSheet(
            title = data.title,
            sources = data.sources,
            onPlaySource = { source ->
                viewModel.playMediaSource(
                    source = source,
                    title = data.title,
                    seriesId = data.seriesId,
                    seasonId = data.seasonId,
                    episodeId = data.episodeId
                )
                qualitySelectionDialogData = null
            },
            onDownloadSource = { source ->
                viewModel.openDownloadOptions(source)
                qualitySelectionDialogData = null
            },
            onDismiss = {
                qualitySelectionDialogData = null
            }
        )
    }

    // Download and external player options
    activeDownloadSource?.let { source ->
        DownloadOptionsDialog(
            source = source,
            onDismiss = {
                viewModel.closeDownloadOptions()
            }
        )
    }

    // Favorite group picker
    if (showFavoriteGroupPicker) {
        AlertDialog(
            onDismissRequest = {
                showFavoriteGroupPicker = false
            },
            title = {
                Text(
                    text = "ذخیره در علاقه‌مندی‌ها",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "پوشه مورد نظر را انتخاب کنید:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    favoriteGroups.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.addToFavorites(media, group.id)
                                    showFavoriteGroupPicker = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.name,
                                color = TextPrimary
                            )

                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                tint = CinemaGold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFavoriteGroupPicker = false
                    }
                ) {
                    Text("بستن")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

private data class QualityDialogData(
    val title: String,
    val sources: List<Source>,
    val seriesId: Int? = null,
    val seasonId: Int? = null,
    val episodeId: Int? = null
)

@Composable
private fun EpisodeListItem(
    episode: Episode,
    episodeNumber: Int,
    isWatched: Boolean,
    onEpisodeClick: () -> Unit
) {
    Card(
        onClick = onEpisodeClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isWatched) {
                Color(0xFF4DD0E1).copy(alpha = 0.4f)
            } else {
                GlassBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("episode_item_${episode.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Episode thumbnail
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CinematicPosterImage(
                    imageUrl = episode.image,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x44000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "پخش",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Episode information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "قسمت ${
                        PersianUtils.toPersianDigits(episodeNumber)
                    }: ${episode.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                PersianUtils.formatDuration(episode.duration)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (isWatched) {
                    Text(
                        text = "✓ دیده‌شده",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4DD0E1)
                    )
                }
            }
        }
    }

}
