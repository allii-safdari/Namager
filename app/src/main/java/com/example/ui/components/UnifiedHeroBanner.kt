package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.UnifiedMedia
import com.example.ui.theme.*

@Composable
fun UnifiedHeroBanner(
    media: UnifiedMedia,
    isWatchlisted: Boolean,
    onWatchClick: (UnifiedMedia) -> Unit,
    onDetailsClick: (UnifiedMedia) -> Unit,
    onToggleWatchlist: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .testTag("unified_hero_banner_${media.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = if (media.cover.isNotEmpty()) media.cover else media.image
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.img_hero_cinema_1792095559768),
                    error = painterResource(id = R.drawable.img_hero_cinema_1792095559768),
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_cinema_1792095559768),
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Dark Multi-Stop Gradient Overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x550F0F14),
                                Color(0x880F0F14),
                                Color(0xF20F0F14),
                                DarkBackground
                            )
                        )
                    )
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Media Type Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (media.isSeries) CinemaRed.copy(alpha = 0.3f) else CinemaGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (media.isSeries) CinemaRed else CinemaGold.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = if (media.isSeries) "سریال اختصاصی" else "فیلم سینمایی برگزیده",
                        color = if (media.isSeries) Color(0xFFFF8A80) else CinemaGoldLight,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Title
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                // Genres line
                if (media.genres.isNotEmpty()) {
                    Text(
                        text = media.genres.joinToString(" • ") { it.title },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                }

                // Badges Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    if (media.imdb > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xCC000000),
                            border = BorderStroke(0.5.dp, CinemaGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", media.imdb),
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (media.year > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceHighlight
                        ) {
                            Text(
                                text = "${media.year}",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (media.duration != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceHighlight
                        ) {
                            Text(
                                text = media.duration,
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Button
                    Button(
                        onClick = { onWatchClick(media) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_watch_btn_${media.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaGold,
                            contentColor = Color(0xFF1A1400)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پخش و تماشا",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Details Button
                    FilledTonalButton(
                        onClick = { onDetailsClick(media) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hero_details_btn_${media.id}"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0x33FFFFFF),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اطلاعات کامل",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Bookmark Button
                    IconButton(
                        onClick = { onToggleWatchlist(media.id) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("hero_bookmark_btn_${media.id}")
                            .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isWatchlisted) CinemaGold else TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
