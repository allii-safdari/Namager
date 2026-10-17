package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
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
    isWatchlisted: Boolean,
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

    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    var qualitySelectionDialogData by remember { mutableStateOf<QualityDialogData?>(null) }
    var showFavoriteGroupPicker by remember { mutableStateOf(false) }

    // If ExoPlayer is actively playing, show the full screen player overlay!
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
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Backdrop with Gradient and Back Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    val backdropUrl = if (media.cover.isNotEmpty()) media.cover else media.image
                    CinematicPosterImage(
                        imageUrl = backdropUrl,
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Vignette Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x99000000),
                                        Color.Transparent,
                                        DarkBackground
                                    )
                                )
                            )
                    )

                    // Back & Favorite buttons at top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0x66000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { showFavoriteGroupPicker = true },
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0x66000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "افزودن به علاقه‌مندی",
                                tint = if (isWatchlisted) CinemaGold else Color.White
                            )
                        }
                    }
                }
            }

            // Info Section: Title, IMDb, Year, Duration, Genres
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Meta chips row: IMDb, Year, Duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // IMDb Badge
                        if (media.imdb > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FFB74D),
                                border = BorderStroke(1.dp, Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${PersianUtils.formatRating(media.imdb)} IMDb",
                                        color = Color(0xFFFFB74D),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Year
                        if (media.year > 0) {
                            Text(
                                text = "سال ${PersianUtils.toPersianDigits(media.year)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        // Duration (only if not null or N/A)
                        val safeDuration = PersianUtils.formatDuration(media.duration)
                        if (safeDuration != null) {
                            Text(
                                text = "•  $safeDuration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Genres Row
                    if (media.genres.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(media.genres) { genre ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = DarkSurface,
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Text(
                                        text = genre.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Grand Action Bar for Movies
                    if (!media.isSeries) {
                        val availableSources = if (media.sources.isNotEmpty()) media.sources else listOf(
                            Source(1, "1080p", "mkv", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                            Source(2, "720p", "mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    qualitySelectionDialogData = QualityDialogData(
                                        title = media.title,
                                        sources = availableSources
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("پخش آنلاین", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val first = availableSources.first()
                                    viewModel.openDownloadOptions(first)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("دانلود ویدیو", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Expandable Synopsis Section
            if (media.description.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "خلاصه داستان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                        ExpandableText(
                            text = media.description,
                            collapsedMaxLines = 3
                        )
                    }
                }
            }

            // For Movie: Sources list with quality pill (User selects quality, then chooses Play or Download)
            if (!media.isSeries) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "کیفیت‌های موجود جهت پخش و دانلود",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )

                        if (media.sources.isEmpty()) {
                            // Default sample sources fallback
                            val sampleSources = listOf(
                                Source(1, "1080p", "mkv", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                                Source(2, "720p", "mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                                Source(3, "480p", "mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                            )
                            SourcesQualityList(
                                sources = sampleSources,
                                onSourceClick = { source ->
                                    qualitySelectionDialogData = QualityDialogData(
                                        title = media.title,
                                        sources = sampleSources
                                    )
                                }
                            )
                        } else {
                            SourcesQualityList(
                                sources = media.sources,
                                onSourceClick = { source ->
                                    qualitySelectionDialogData = QualityDialogData(
                                        title = media.title,
                                        sources = media.sources
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // For Series: Seasons Tab and Episodes List with "دیده‌شده" badge and quality selection
            if (media.isSeries) {
                item {
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
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = CinemaGold)
                                }
                            }
                            is UiState.Error -> {
                                ErrorRetryView(
                                    message = state.message,
                                    onRetry = { viewModel.loadSeasons(media.id) }
                                )
                            }
                            is UiState.Empty -> {
                                EmptyStateView(message = "قسمتی برای این سریال ثبت نشده است.")
                            }
                            is UiState.Success -> {
                                val seasons = state.data
                                if (seasons.isNotEmpty()) {
                                    // Season tabs
                                    ScrollableTabRow(
                                        selectedTabIndex = selectedSeasonIndex.coerceIn(0, seasons.size - 1),
                                        containerColor = DarkSurface,
                                        contentColor = CinemaGold,
                                        edgePadding = 0.dp
                                    ) {
                                        seasons.forEachIndexed { index, season ->
                                            Tab(
                                                selected = selectedSeasonIndex == index,
                                                onClick = { selectedSeasonIndex = index },
                                                text = {
                                                    Text(
                                                        text = season.title.ifEmpty { "فصل ${PersianUtils.toPersianDigits(index + 1)}" },
                                                        fontWeight = if (selectedSeasonIndex == index) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    val currentSeason = seasons.getOrNull(selectedSeasonIndex)
                                    val episodes = currentSeason?.episodes ?: emptyList()

                                    if (episodes.isEmpty()) {
                                        EmptyStateView(message = "قسمتی در این فصل یافت نشد.")
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            episodes.forEachIndexed { epIndex, episode ->
                                                val isWatched = viewModel.isEpisodeWatched(
                                                    seriesId = media.id,
                                                    seasonId = currentSeason?.id ?: 1,
                                                    episodeId = episode.id
                                                )

                                                EpisodeListItem(
                                                    episode = episode,
                                                    episodeNumber = epIndex + 1,
                                                    isWatched = isWatched,
                                                    onEpisodeClick = {
                                                        // Show quality selection for this episode
                                                        val sources = if (episode.sources.isNotEmpty()) {
                                                            episode.sources
                                                        } else {
                                                            listOf(
                                                                Source(1, "1080p", "mkv", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                                                                Source(2, "720p", "mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
                                                            )
                                                        }
                                                        qualitySelectionDialogData = QualityDialogData(
                                                            title = "${media.title} - ${episode.title}",
                                                            sources = sources,
                                                            seriesId = media.id,
                                                            seasonId = currentSeason?.id ?: 1,
                                                            episodeId = episode.id
                                                        )
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

    // Modal: Quality Selection (Play vs Download)
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
            onDismiss = { qualitySelectionDialogData = null }
        )
    }

    // Modal: Download & External Player Options Dialog
    activeDownloadSource?.let { source ->
        DownloadOptionsDialog(
            source = source,
            onDismiss = { viewModel.closeDownloadOptions() }
        )
    }

    // Modal: Favorite Group Picker Dialog
    if (showFavoriteGroupPicker) {
        AlertDialog(
            onDismissRequest = { showFavoriteGroupPicker = false },
            title = { Text("ذخیره در علاقه‌مندی‌ها", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("پوشه مورد نظر را انتخاب کنید:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
                            Text(group.name, color = TextPrimary)
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = CinemaGold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFavoriteGroupPicker = false }) {
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

/**
 * List of available qualities for a movie
 */
@Composable
private fun SourcesQualityList(
    sources: List<Source>,
    onSourceClick: (Source) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sources.forEach { source ->
            Card(
                onClick = { onSourceClick(source) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = source.quality,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = source.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

/**
 * Episode List Item with thumbnail, title, duration, and "دیده‌شده" watched indicator
 */
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
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isWatched) Color(0xFF4DD0E1).copy(alpha = 0.4f) else GlassBorder),
        modifier = Modifier.fillMaxWidth().testTag("episode_item_${episode.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Episode Thumbnail
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
            ) {
                CinematicPosterImage(
                    imageUrl = episode.image,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    contentScale = ContentScale.Crop
                )

                // Play overlay icon
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

            // Episode Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قسمت ${PersianUtils.toPersianDigits(episodeNumber)}: ${episode.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // "دیده‌شده" Watched status badge
                    if (isWatched) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x334DD0E1),
                            border = BorderStroke(1.dp, Color(0xFF4DD0E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4DD0E1),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "دیده‌شده",
                                    color = Color(0xFF4DD0E1),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                val safeDuration = PersianUtils.formatDuration(episode.duration)
                if (safeDuration != null) {
                    Text(
                        text = safeDuration,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
