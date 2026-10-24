package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Episode
import com.example.domain.model.Media
import com.example.domain.model.Season
import com.example.domain.model.Source
import com.example.ui.components.CinematicPosterImage
import com.example.ui.components.DownloadOptionsDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.ExpandableText
import com.example.ui.components.QualitySelectionSheet
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldDark
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.DetailsUiState
import com.example.ui.viewmodel.DetailsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    onPlay: (String, String, String?, Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CinemaGold)
                }
            }

            state.media == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = TextPrimary
                        )
                    }
                    ErrorRetryView(
                        message = state.error ?: "اطلاعات این عنوان در دسترس نیست.",
                        onRetry = viewModel::retry
                    )
                }
            }

            else -> {
                MediaDetailsContent(
                    media = state.media!!,
                    state = state,
                    onBack = onBack,
                    onRetrySeasons = viewModel::retrySeasons,
                    onAddToGroup = viewModel::addToGroup,
                    onRemoveFavorite = viewModel::removeFromFavorites,
                    onPlay = onPlay,
                    snackbarHostState = snackbarHostState
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaDetailsContent(
    media: Media,
    state: DetailsUiState,
    onBack: () -> Unit,
    onRetrySeasons: () -> Unit,
    onAddToGroup: (String) -> Unit,
    onRemoveFavorite: () -> Unit,
    onPlay: (String, String, String?, Int, Int, Int) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var selectedTab by rememberSaveable(media.id) { mutableIntStateOf(0) }
    var selectedSeasonIndex by rememberSaveable(media.id) { mutableIntStateOf(0) }
    var showGroupPicker by rememberSaveable(media.id) { mutableStateOf(false) }
    var downloadSource by remember { mutableStateOf<Source?>(null) }
    var qualityDialogData by remember { mutableStateOf<QualityDialogData?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("movie_detail_sheet"),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "details_header") {
            DetailsHeader(
                media = media,
                isFavorite = state.isFavorite,
                onBack = onBack,
                onFavoriteClick = {
                    if (state.isFavorite) onRemoveFavorite() else showGroupPicker = true
                }
            )
        }

        item(key = "details_tabs") {
            val tabTitles = listOf("توضیحات", "پخش آنلاین", "دانلود")
            val tabIcons = listOf(Icons.Default.Info, Icons.Default.PlayCircle, Icons.Default.Download)

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = CinemaGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        selectedContentColor = CinemaGold,
                        unselectedContentColor = TextSecondary,
                        modifier = Modifier.testTag("detail_tab_$index")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item(key = "details_synopsis") {
                    if (media.description.isBlank()) {
                        EmptyStateView(message = "خلاصه داستانی برای این عنوان ثبت نشده است.")
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SectionTitle(icon = Icons.Default.AutoStories, title = "خلاصه داستان")
                                ExpandableText(text = media.description, collapsedMaxLines = 4)
                            }
                        }
                    }
                }

                item(key = "details_specs") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "مشخصات و اطلاعات اثر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CinemaGold
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = GlassBorder)
                            SpecificationRow("نام اثر", media.title)
                            SpecificationRow(
                                "نوع محتوا",
                                if (media.isSeries) "سریال تلویزیونی" else "فیلم سینمایی"
                            )
                            if (media.year > 0) {
                                SpecificationRow("سال ساخت", PersianUtils.toPersianDigits(media.year))
                            }
                            if (media.imdb > 0) {
                                SpecificationRow(
                                    "امتیاز IMDb",
                                    "${PersianUtils.formatRating(media.imdb)} از ۱۰"
                                )
                            }
                            PersianUtils.formatDuration(media.duration)?.let {
                                SpecificationRow("مدت زمان", it)
                            }
                            if (media.genres.isNotEmpty()) {
                                SpecificationRow(
                                    "ژانرها",
                                    media.genres.joinToString("، ") { it.title }
                                )
                            }
                            if (media.country.isNotEmpty()) {
                                SpecificationRow(
                                    "کشور سازنده",
                                    media.country.joinToString("، ") { it.title }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                if (!media.isSeries) {
                    item(key = "movie_play_section") {
                        MoviePlaySection(
                            media = media,
                            onPlay = { source ->
                                onPlay(source.url, media.title, null, media.id, -1, -1)
                            }
                        )
                    }
                } else {
                    when {
                        state.seasonsLoading -> item(key = "seasons_loading") { LoadingCard() }
                        state.seasonsError != null -> item(key = "seasons_error") {
                            ErrorRetryView(
                                message = state.seasonsError.orEmpty(),
                                onRetry = onRetrySeasons
                            )
                        }
                        state.seasons.isEmpty() -> item(key = "seasons_empty") {
                            EmptyStateView(message = "فصلی برای این سریال ثبت نشده است.")
                        }
                        else -> {
                            val safeIndex = selectedSeasonIndex.coerceIn(0, state.seasons.lastIndex)
                            val season = state.seasons[safeIndex]
                            item(key = "season_tabs") {
                                SeasonTabsRow(
                                    seasons = state.seasons,
                                    selectedIndex = safeIndex,
                                    onSelect = { selectedSeasonIndex = it }
                                )
                            }
                            if (season.episodes.isEmpty()) {
                                item(key = "episodes_empty") {
                                    EmptyStateView(message = "قسمتی در این فصل یافت نشد.")
                                }
                            } else {
                                itemsIndexed(
                                    items = season.episodes,
                                    key = { _, episode -> "ep_${season.id}_${episode.id}" }
                                ) { index, episode ->
                                    EpisodePlayItem(
                                        episode = episode,
                                        episodeNumber = index + 1,
                                        isWatched = state.watchedEpisodes.contains(
                                            "${media.id}-${season.id}-${episode.id}"
                                        ),
                                        onPlayClick = {
                                            if (episode.sources.isEmpty()) {
                                                scope.launch {
                                                    snackbarHostState.showMessage("برای این قسمت لینک پخشی ثبت نشده است.")
                                                }
                                            } else {
                                                qualityDialogData = QualityDialogData(
                                                    title = "${media.title} - ${episode.title}",
                                                    sources = episode.sources,
                                                    seriesId = media.id,
                                                    seasonId = season.id,
                                                    episodeId = episode.id
                                                )
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                if (!media.isSeries) {
                    item(key = "movie_download_section") {
                        MovieDownloadSection(
                            media = media,
                            onDownload = { downloadSource = it }
                        )
                    }
                } else {
                    when {
                        state.seasonsLoading -> item(key = "dl_seasons_loading") { LoadingCard() }
                        state.seasonsError != null -> item(key = "dl_seasons_error") {
                            ErrorRetryView(
                                message = state.seasonsError.orEmpty(),
                                onRetry = onRetrySeasons
                            )
                        }
                        state.seasons.isEmpty() -> item(key = "dl_seasons_empty") {
                            EmptyStateView(message = "لینک دانلودی برای این سریال ثبت نشده است.")
                        }
                        else -> {
                            val safeIndex = selectedSeasonIndex.coerceIn(0, state.seasons.lastIndex)
                            val season = state.seasons[safeIndex]
                            item(key = "dl_season_tabs") {
                                SeasonTabsRow(
                                    seasons = state.seasons,
                                    selectedIndex = safeIndex,
                                    onSelect = { selectedSeasonIndex = it }
                                )
                            }
                            if (season.episodes.isEmpty()) {
                                item(key = "dl_episodes_empty") {
                                    EmptyStateView(message = "قسمتی برای دانلود در این فصل یافت نشد.")
                                }
                            } else {
                                itemsIndexed(
                                    items = season.episodes,
                                    key = { _, episode -> "dl_ep_${season.id}_${episode.id}" }
                                ) { index, episode ->
                                    EpisodeDownloadItem(
                                        episode = episode,
                                        episodeNumber = index + 1,
                                        onDownloadClick = {
                                            val source = episode.sources.firstOrNull()
                                            if (source == null) {
                                                scope.launch {
                                                    snackbarHostState.showMessage("لینک دانلودی برای این قسمت موجود نیست.")
                                                }
                                            } else {
                                                downloadSource = source
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGroupPicker) {
        AlertDialog(
            onDismissRequest = { showGroupPicker = false },
            title = {
                Text("ذخیره در پوشه علاقه‌مندی‌ها", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "پوشه مورد نظر را انتخاب کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    state.favoriteGroups.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    onAddToGroup(group.id)
                                    showGroupPicker = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(group.name, color = TextPrimary, fontWeight = FontWeight.Medium)
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
                TextButton(onClick = { showGroupPicker = false }) {
                    Text("بستن", color = CinemaGold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    qualityDialogData?.let { data ->
        QualitySelectionSheet(
            title = data.title,
            sources = data.sources,
            onPlaySource = { source ->
                onPlay(
                    source.url,
                    data.title,
                    null,
                    data.seriesId,
                    data.seasonId,
                    data.episodeId
                )
                qualityDialogData = null
            },
            onDownloadSource = { source ->
                downloadSource = source
                qualityDialogData = null
            },
            onDismiss = { qualityDialogData = null }
        )
    }

    downloadSource?.let { source ->
        DownloadOptionsDialog(
            source = source,
            onDismiss = { downloadSource = null }
        )
    }
}

@Composable
private fun DetailsHeader(
    media: Media,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        val backdropUrl = media.cover.ifBlank { media.image }
        val posterUrl = media.image.ifBlank { media.cover }

        CinematicPosterImage(
            imageUrl = backdropUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE101018),
                            Color(0xC412121C),
                            Color(0x8812121C),
                            Color(0x3312121C)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isFavorite) "حذف از علاقه‌مندی‌ها" else "افزودن به علاقه‌مندی‌ها",
                    tint = if (isFavorite) CinemaGold else Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .width(118.dp)
                    .height(176.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .background(DarkSurface)
            ) {
                CinematicPosterImage(
                    imageUrl = posterUrl,
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (media.isSeries) CinemaRed else CinemaGoldDark
                    ) {
                        Text(
                            text = if (media.isSeries) "سریال" else "فیلم سینمایی",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (media.imdb > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurface,
                            border = BorderStroke(0.5.dp, GlassBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = PersianUtils.formatRating(media.imdb),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                PersianUtils.formatDuration(media.duration)?.let { durationText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = durationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoviePlaySection(
    media: Media,
    onPlay: (Source) -> Unit
) {
    if (media.sources.isEmpty()) {
        EmptyStateView(message = "لینکی برای پخش آنلاین این فیلم موجود نیست.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight),
            border = BorderStroke(1.5.dp, CinemaGold)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(icon = Icons.Default.PlayCircle, title = "پخش سریع فیلم")
                Text(
                    text = "پخش بدون قطعی با پلیر اختصاصی Media3",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Button(
                    onClick = { onPlay(media.sources.first()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "شروع تماشا (${media.sources.first().quality})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = "کیفیت‌های در دسترس:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        media.sources.forEach { source ->
            PlaySourceCard(
                source = source,
                onPlayClick = { onPlay(source) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun MovieDownloadSection(
    media: Media,
    onDownload: (Source) -> Unit
) {
    if (media.sources.isEmpty()) {
        EmptyStateView(message = "لینک دانلودی برای این اثر ثبت نشده است.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "برای دانلود پرسرعت با قابلیت ازسرگیری، استفاده از دانلود منیجرها (ADM یا 1DM) پیشنهاد می‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        media.sources.forEach { source ->
            DownloadSourceCard(
                source = source,
                onDownloadClick = { onDownload(source) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun SeasonTabsRow(
    seasons: List<Season>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = DarkSurface,
        contentColor = CinemaGold,
        edgePadding = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
    ) {
        seasons.forEachIndexed { index, item ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        text = item.title.ifBlank {
                            "فصل ${PersianUtils.toPersianDigits(index + 1)}"
                        },
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CinemaGold)
    }
}

@Composable
private fun PlaySourceCard(
    source: Source,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onPlayClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CinemaGold
                ) {
                    Text(
                        text = source.quality,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column {
                    Text(
                        text = "کیفیت ${source.quality}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "فرمت ${source.type.uppercase()} • پخش آنلاین",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            FilledTonalButton(
                onClick = onPlayClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0x33FFB74D),
                    contentColor = CinemaGold
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("تماشا", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DownloadSourceCard(
    source: Source,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CinemaRed
                ) {
                    Text(
                        text = source.quality,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column {
                    Text(
                        text = "کیفیت ${source.quality}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Button(
                modifier = Modifier.wrapContentWidth(),
                onClick = onDownloadClick,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("دانلود", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EpisodePlayItem(
    episode: Episode,
    episodeNumber: Int,
    isWatched: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onPlayClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
            1.dp,
            if (isWatched) Color(0xFF4DD0E1).copy(alpha = 0.4f) else GlassBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("episode_play_item_${episode.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "قسمت ${PersianUtils.toPersianDigits(episodeNumber)}: ${episode.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PersianUtils.formatDuration(episode.duration)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                if (isWatched) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4DD0E1),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "دیده‌شده",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4DD0E1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeDownloadItem(
    episode: Episode,
    episodeNumber: Int,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "قسمت ${PersianUtils.toPersianDigits(episodeNumber)}: ${episode.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PersianUtils.formatDuration(episode.duration)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Button(
                onClick = onDownloadClick,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("دانلود", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private suspend fun SnackbarHostState.showMessage(message: String) {
    currentSnackbarData?.dismiss()
    showSnackbar(message)
}

private data class QualityDialogData(
    val title: String,
    val sources: List<Source>,
    val seriesId: Int,
    val seasonId: Int,
    val episodeId: Int
)
