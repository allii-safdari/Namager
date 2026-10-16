package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MovieItem
import com.example.model.VideoQuality
import com.example.ui.theme.*

@Composable
fun VideoPlayerPreview(
    movie: MovieItem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    episodeTitle: String? = null,
    currentQuality: VideoQuality? = null,
    onQualityClick: (() -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.24f) }
    var isMuted by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clickable { showControls = !showControls }
        ) {
            // Backdrop representation
            if (movie.backdropRes != null) {
                Image(
                    painter = painterResource(id = movie.backdropRes),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Dark vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xCC000000),
                                Color(0x33000000),
                                Color(0xEE000000)
                            )
                        )
                    )
            )

            // Top Header: Title & Close
            if (showControls) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = episodeTitle ?: movie.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = movie.englishTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quality Switcher Pill in Top Bar
                        if (currentQuality != null && onQualityClick != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CinemaGold.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold),
                                modifier = Modifier
                                    .testTag("player_quality_pill")
                                    .clickable { onQualityClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Quality",
                                        tint = CinemaGoldLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = currentQuality.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaGoldLight
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .testTag("close_player_btn")
                                .background(Color(0x66000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play / Pause Button
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .testTag("player_play_pause_btn")
                        .background(CinemaGold.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Bottom Controller Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Slider
                    Slider(
                        value = progress,
                        onValueChange = { progress = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .testTag("player_progress_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = CinemaGold,
                            activeTrackColor = CinemaGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "۲۴:۱۸ / ۱۲۵:۰۰",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CinemaRed
                            ) {
                                Text(
                                    text = currentQuality?.resolution ?: "1080p FHD",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { isMuted = !isMuted },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
