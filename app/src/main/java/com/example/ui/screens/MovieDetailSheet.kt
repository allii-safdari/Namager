package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val context = LocalContext.current

    BackHandler {
        onClose()
    }

    // لود فصل‌ها در صورت سریال بودن
    LaunchedEffect(media.id, media.isSeries) {
        if (media.isSeries) {
            viewModel.loadSeasons(media.id)
        }
    }

    val seasonsState by viewModel.seasonsState.collectAsStateWithLifecycle()
    val activePlayingSource by viewModel.activePlayingSource.collectAsStateWithLifecycle()
    val activePlayingTitle by viewModel.activePlayingTitle.collectAsStateWithLifecycle()
    val activePlayingSubtitle by viewModel.activePlayingSubtitle.collectAsStateWithLifecycle()
    val activeDownloadSource by viewModel.activeSourceForDownload.collectAsStateWithLifecycle()
    val favoriteGroups by viewModel.favoriteGroups.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // تب‌های سه‌گانه: توضیحات (۰)، پخش (۱)، دانلود (۲)
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSeasonIndex by remember(media.id) { mutableIntStateOf(0) }
    var qualitySelectionDialogData by remember { mutableStateOf<QualityDialogData?>(null) }
    var showFavoriteGroupPicker by remember { mutableStateOf(false) }

    // پخش تمام‌صفحه پلیر داخلی Media3 ExoPlayer
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
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ۱. بخش بالایی صفحه: بک‌دراپ + پوستر برجسته + دکمه‌های بازگشت و نشان کردن + عنوان و اطلاعات کلیدی
            item(key = "movie_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    val backdropUrl = media.cover.takeIf { it.isNotBlank() } ?: media.image
                    val posterUrl = media.image.takeIf { it.isNotBlank() } ?: media.cover

                    // تصویر پس‌زمینه (Backdrop)
                    CinematicPosterImage(
                        imageUrl = backdropUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        contentScale = ContentScale.Crop
                    )

                    // لایه گرادیان تیره جهت محوشدگی به رنگ پس‌زمینه
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x88000000),
                                        Color(0x44000000),
                                        DarkBackground.copy(alpha = 0.85f),
                                        DarkBackground
                                    )
                                )
                            )
                    )

                    // دکمه‌های بازگشت و علاقه‌مندی در بالای نوار
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
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
                            onClick = { showFavoriteGroupPicker = true },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                                .border(1.dp, GlassBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isWatchlist) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "افزودن به علاقه‌مندی",
                                tint = if (isWatchlist) CinemaGold else Color.White
                            )
                        }
                    }

                    // پوستر فیلم در پایین و سمت راست (یا چپ در RTL)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // پوستر فیلم/سریال با حاشیه شیشه‌ای
                        Box(
                            modifier = Modifier
                                .width(118.dp)
                                .height(176.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            CinematicPosterImage(
                                imageUrl = posterUrl,
                                contentDescription = media.title,
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(12.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // اطلاعات کوتاه در کنار پوستر
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // برچسب نوع اثر و امتیاز IMDb
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // نشان نوع اثر (فیلم / سریال)
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

                                if (media.year > 0) {
                                    Text(
                                        text = PersianUtils.toPersianDigits(media.year),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // زمان فیلم در صورت وجود
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

            // ۲. نوار تب‌های سه‌گانه (توضیحات، پخش، دانلود) به سبک مدرن شیشه‌ای
            item(key = "detail_tabs") {
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

            // ۳. محتوای هر تب
            when (selectedTab) {
                // تب ۰: توضیحات
                0 -> {
                    // خلاصه داستان
                    if (media.description.isNotBlank()) {
                        item(key = "tab_synopsis") {
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoStories,
                                            contentDescription = null,
                                            tint = CinemaGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "خلاصه داستان",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }

                                    ExpandableText(
                                        text = media.description,
                                        collapsedMaxLines = 4
                                    )
                                }
                            }
                        }
                    }

                    // جدول مشخصات کامل
                    item(key = "tab_specifications") {
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
                                SpecificationRow("نوع محتوا", if (media.isSeries) "سریال تلویزیونی" else "فیلم سینمایی")

                                if (media.year > 0) {
                                    SpecificationRow("سال ساخت", PersianUtils.toPersianDigits(media.year))
                                }

                                if (media.imdb > 0) {
                                    SpecificationRow("امتیاز IMDb", "${PersianUtils.formatRating(media.imdb)} از ۱۰")
                                }

                                PersianUtils.formatDuration(media.duration)?.let {
                                    SpecificationRow("مدت زمان", it)
                                }

                                if (media.genres.isNotEmpty()) {
                                    SpecificationRow("ژانرها", media.genres.joinToString("، ") { it.title })
                                }

                                if (media.country.isNotEmpty()) {
                                    SpecificationRow("کشور سازنده", media.country.joinToString("، ") { it.title })
                                }
                            }
                        }
                    }
                }

                // تب ۱: پخش آنلاین
                1 -> {
                    if (!media.isSeries) {
                        // فیلم سینمایی: گزینه‌های پخش مستقیم و کیفیت‌ها
                        val availableSources = media.sources

                        if (availableSources.isEmpty()) {
                            item {
                                EmptyStateView(message = "لینکی برای پخش آنلاین این فیلم موجود نیست.")
                            }
                        } else {
                            // دکمه بزرگ پخش سریع
                            item(key = "movie_quick_play") {
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = CinemaGold,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "پخش سریع فیلم",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "پخش بدون قطعی با پلیر اختصاصی داخلی Media3",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                val bestSource = availableSources.first()
                                                viewModel.playMediaSource(
                                                    source = bestSource,
                                                    title = media.title
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("شروع تماشا (${availableSources.first().quality})", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // لیست تمام کیفیت‌های موجود برای فیلم
                            item(key = "movie_qualities_header") {
                                Text(
                                    text = "کیفیت‌های در دسترس جهت پخش:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            itemsIndexed(availableSources, key = { _, s -> s.id }) { _, source ->
                                PlaySourceCard(
                                    source = source,
                                    onPlayClick = {
                                        viewModel.playMediaSource(source = source, title = media.title)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    } else {
                        // سریال تلویزیونی: انتخاب فصل و لیست قسمت‌ها برای پخش
                        when (val state = seasonsState) {
                            is UiState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = CinemaGold)
                                    }
                                }
                            }

                            is UiState.Error -> {
                                item {
                                    ErrorRetryView(
                                        message = state.message,
                                        onRetry = { viewModel.loadSeasons(media.id) }
                                    )
                                }
                            }

                            is UiState.Empty -> {
                                item {
                                    EmptyStateView(message = "قسمتی برای این سریال ثبت نشده است.")
                                }
                            }

                            is UiState.Success -> {
                                val seasons = state.data
                                if (seasons.isEmpty()) {
                                    item {
                                        EmptyStateView(message = "قسمتی برای این سریال ثبت نشده است.")
                                    }
                                } else {
                                    val safeSeasonIndex = selectedSeasonIndex.coerceIn(0, seasons.lastIndex)
                                    val currentSeason = seasons[safeSeasonIndex]
                                    val episodes = currentSeason.episodes

                                    // نوار اسکرولی فصل‌ها
                                    item(key = "seasons_tab_row") {
                                        ScrollableTabRow(
                                            selectedTabIndex = safeSeasonIndex,
                                            containerColor = DarkSurface,
                                            contentColor = CinemaGold,
                                            edgePadding = 16.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                        ) {
                                            seasons.forEachIndexed { index, season ->
                                                Tab(
                                                    selected = safeSeasonIndex == index,
                                                    onClick = { selectedSeasonIndex = index },
                                                    text = {
                                                        Text(
                                                            text = season.title.ifEmpty { "فصل ${PersianUtils.toPersianDigits(index + 1)}" },
                                                            fontWeight = if (safeSeasonIndex == index) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (episodes.isEmpty()) {
                                        item {
                                            EmptyStateView(message = "قسمتی در این فصل یافت نشد.")
                                        }
                                    } else {
                                        itemsIndexed(episodes, key = { _, ep -> ep.id }) { epIndex, episode ->
                                            val isWatched = viewModel.isEpisodeWatched(
                                                seriesId = media.id,
                                                seasonId = currentSeason.id,
                                                episodeId = episode.id
                                            )

                                            EpisodePlayItem(
                                                episode = episode,
                                                episodeNumber = epIndex + 1,
                                                isWatched = isWatched,
                                                onPlayClick = {
                                                    val sources = episode.sources
                                                    if (sources.isNotEmpty()) {
                                                        qualitySelectionDialogData = QualityDialogData(
                                                            title = "${media.title} - ${episode.title}",
                                                            sources = sources,
                                                            seriesId = media.id,
                                                            seasonId = currentSeason.id,
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
                }

                // تب ۲: دانلود
                2 -> {
                    if (!media.isSeries) {
                        // دانلود فیلم
                        val availableSources = media.sources

                        if (availableSources.isEmpty()) {
                            item {
                                EmptyStateView(message = "لینک دانلودی برای این اثر ثبت نشده است.")
                            }
                        } else {
                            item(key = "download_info_box") {
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
                            }

                            itemsIndexed(availableSources, key = { _, s -> "dl_${s.id}" }) { _, source ->
                                DownloadSourceCard(
                                    source = source,
                                    onDownloadClick = {
                                        viewModel.openDownloadOptions(source)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    } else {
                        // دانلود قسمت‌های سریال
                        when (val state = seasonsState) {
                            is UiState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = CinemaGold)
                                    }
                                }
                            }

                            is UiState.Error -> {
                                item {
                                    ErrorRetryView(
                                        message = state.message,
                                        onRetry = { viewModel.loadSeasons(media.id) }
                                    )
                                }
                            }

                            is UiState.Empty -> {
                                item {
                                    EmptyStateView(message = "لینک دانلودی برای این سریال ثبت نشده است.")
                                }
                            }

                            is UiState.Success -> {
                                val seasons = state.data
                                if (seasons.isEmpty()) {
                                    item {
                                        EmptyStateView(message = "لینک دانلودی برای این سریال ثبت نشده است.")
                                    }
                                } else {
                                    val safeSeasonIndex = selectedSeasonIndex.coerceIn(0, seasons.lastIndex)
                                    val currentSeason = seasons[safeSeasonIndex]
                                    val episodes = currentSeason.episodes

                                    item(key = "dl_seasons_tab_row") {
                                        ScrollableTabRow(
                                            selectedTabIndex = safeSeasonIndex,
                                            containerColor = DarkSurface,
                                            contentColor = CinemaGold,
                                            edgePadding = 16.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                        ) {
                                            seasons.forEachIndexed { index, season ->
                                                Tab(
                                                    selected = safeSeasonIndex == index,
                                                    onClick = { selectedSeasonIndex = index },
                                                    text = {
                                                        Text(
                                                            text = season.title.ifEmpty { "فصل ${PersianUtils.toPersianDigits(index + 1)}" },
                                                            fontWeight = if (safeSeasonIndex == index) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (episodes.isEmpty()) {
                                        item {
                                            EmptyStateView(message = "قسمتی برای دانلود در این فصل یافت نشد.")
                                        }
                                    } else {
                                        itemsIndexed(episodes, key = { _, ep -> "dl_ep_${ep.id}" }) { epIndex, episode ->
                                            EpisodeDownloadItem(
                                                episode = episode,
                                                episodeNumber = epIndex + 1,
                                                onDownloadClick = {
                                                    val firstSource = episode.sources.firstOrNull()
                                                    if (firstSource != null) {
                                                        viewModel.openDownloadOptions(firstSource)
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "لینک دانلودی موجود نیست",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
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
        }
    }

    // دیالوگ انتخاب کیفیت (پخش یا دانلود)
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

    // دیالوگ گزینه‌های دانلود (ADM, VLC, مرورگر و کپی لینک)
    activeDownloadSource?.let { source ->
        DownloadOptionsDialog(
            source = source,
            onDismiss = {
                viewModel.closeDownloadOptions()
            }
        )
    }

    // دیالوگ انتخاب پوشه علاقه‌مندی‌ها
    if (showFavoriteGroupPicker) {
        AlertDialog(
            onDismissRequest = { showFavoriteGroupPicker = false },
            title = {
                Text(
                    text = "ذخیره در پوشه علاقه‌مندی‌ها",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "پوشه مورد نظر را انتخاب کنید:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    favoriteGroups.forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    viewModel.addToFavorites(media, group.id)
                                    showFavoriteGroupPicker = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = group.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, tint = CinemaGold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFavoriteGroupPicker = false }) {
                    Text("بستن", color = CinemaGold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
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
private fun PlaySourceCard(
    source: Source,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onPlayClick() },
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
                        text = "فرمت ${source.type.uppercase()} • پخش آنلاین با کیفیت بالا",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            FilledTonalButton(
                onClick = onPlayClick,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0x33FFB74D), contentColor = CinemaGold),
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
    val context = LocalContext.current

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
                    Text(
                        text = "لینک مستقیم • پشتیبانی از ADM و VLC",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // کپی مستقیم
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("URL", source.url)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "لینک دانلود کپی شد", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "کپی لینک", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                // باز کردن گزینه‌های دانلود
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
            // تصویر بندانگشتی قسمت با آیکون پلی
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

            // اطلاعات قسمت
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
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
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

private data class QualityDialogData(
    val title: String,
    val sources: List<Source>,
    val seriesId: Int? = null,
    val seasonId: Int? = null,
    val episodeId: Int? = null
)
