package com.example.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.Source
import com.example.ui.theme.*
import com.example.ui.util.PersianUtils
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    source: Source,
    title: String,
    subtitle: String? = null,
    seekJumpSeconds: Int = 10,
    onClose: () -> Unit,
    onPlaybackStarted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }

    // Dialog states
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTracksDialog by remember { mutableStateOf(false) }
    var currentTracks by remember { mutableStateOf<Tracks?>(null) }

    var lastSavedPosition by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    DisposableEffect(source.url) {
        val mediaItem = MediaItem.fromUri(source.url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (lastSavedPosition > 0L) {
            exoPlayer.seekTo(lastSavedPosition)
        }
        onPlaybackStarted()

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    playbackError = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackError = "خطا در پخش ویدیو: ${error.localizedMessage ?: "منبع در دسترس نیست"}"
                lastSavedPosition = exoPlayer.currentPosition
            }

            override fun onTracksChanged(tracks: Tracks) {
                currentTracks = tracks
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Polling for timeline progress
    LaunchedEffect(isPlaying) {
        while (true) {
            if (exoPlayer.playbackState == Player.STATE_READY || isPlaying) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                duration = exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    // Auto hide controls after 4 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    val seekMs = seekJumpSeconds * 1000L

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("exo_player_container")
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter -> {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            showControls = true
                            true
                        }
                        Key.DirectionLeft -> {
                            val newPos = (exoPlayer.currentPosition - seekMs).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                            showControls = true
                            true
                        }
                        Key.DirectionRight -> {
                            val newPos = (exoPlayer.currentPosition + seekMs).coerceAtMost(duration)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                            showControls = true
                            true
                        }
                        Key.MediaPlayPause -> {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            showControls = true
                            true
                        }
                        Key.Back -> {
                            onClose()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
            }
    ) {
        // AndroidView wrapping PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We use our own polished M3 Persian controls overlay
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && playbackError == null) {
            CircularProgressIndicator(
                color = CinemaGold,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
            )
        }

        // Error State with Retry keeping position
        if (playbackError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xDD1E1E1E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = CinemaRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = playbackError ?: "خطا در پخش",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            playbackError = null
                            exoPlayer.setMediaItem(MediaItem.fromUri(source.url))
                            exoPlayer.prepare()
                            if (lastSavedPosition > 0L) {
                                exoPlayer.seekTo(lastSavedPosition)
                            }
                            exoPlayer.play()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تلاش دوباره", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xB3000000),
                                Color(0x33000000),
                                Color(0xD9000000)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x55000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بستن",
                            tint = Color.White
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaGold,
                                maxLines = 1
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Speed button
                        FilledTonalButton(
                            onClick = { showSpeedDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0x55000000),
                                contentColor = CinemaGold
                            )
                        ) {
                            Text("${PersianUtils.toPersianDigits(currentSpeed)}x", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Tracks / Audio / Subtitle button
                        IconButton(
                            onClick = { showTracksDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x55000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "صدا و زیرنویس",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play / Pause & Seek Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind N seconds
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - seekMs).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0x55000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "عقب ۱۰ ثانیه",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Play / Pause
                    IconButton(
                        onClick = {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .background(CinemaGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Forward N seconds
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + seekMs).coerceAtMost(duration)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0x55000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "جلو ۱۰ ثانیه",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Timeline & Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Time indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = PersianUtils.formatTime(currentPosition / 1000L),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = PersianUtils.formatTime(duration / 1000L),
                            color = TextTertiary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Seek Slider
                    val progressFloat = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f
                    Slider(
                        value = progressFloat.coerceIn(0f, 1f),
                        onValueChange = { frac ->
                            val targetMs = (frac * duration).toLong()
                            exoPlayer.seekTo(targetMs)
                            currentPosition = targetMs
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = CinemaGold,
                            activeTrackColor = CinemaRed,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bottom info: Quality badge & type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x44FFB74D),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold)
                        ) {
                            Text(
                                text = "کیفیت ${source.quality} • ${source.type.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("سرعت پخش", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = (currentSpeed == speed)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    currentSpeed = speed
                                    exoPlayer.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${PersianUtils.toPersianDigits(speed)} برابر",
                                color = if (isSelected) CinemaGold else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("بستن")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Audio & Subtitle Tracks Dialog
    if (showTracksDialog) {
        AlertDialog(
            onDismissRequest = { showTracksDialog = false },
            title = { Text("انتخاب صدا و زیرنویس", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("صدا (Audio)", style = MaterialTheme.typography.titleSmall, color = CinemaGold)
                    Text("کانال صوتی پیش‌فرض استریم", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    HorizontalDivider(color = Color(0x33FFFFFF))

                    Text("زیرنویس (Subtitles)", style = MaterialTheme.typography.titleSmall, color = CinemaGold)
                    val tracks = currentTracks
                    var textTracksFound = false
                    if (tracks != null) {
                        tracks.groups.forEach { group ->
                            if (group.type == C.TRACK_TYPE_TEXT) {
                                for (i in 0 until group.length) {
                                    val format = group.getTrackFormat(i)
                                    val trackName = format.label ?: format.language ?: "زیرنویس ${i + 1}"
                                    val isSelected = group.isTrackSelected(i)
                                    textTracksFound = true
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                    .buildUpon()
                                                    .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                                    .build()
                                                showTracksDialog = false
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = trackName, color = if (isSelected) CinemaGold else TextPrimary)
                                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold)
                                    }
                                }
                            }
                        }
                    }

                    if (!textTracksFound) {
                        Text("زیرنویس مجزایی در فایل استریم یافت نشد (زیرنویس چسبیده / Hardsub)", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTracksDialog = false }) {
                    Text("تأیید")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
