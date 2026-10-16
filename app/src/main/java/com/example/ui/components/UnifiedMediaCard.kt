package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.UnifiedMedia
import com.example.ui.theme.*
import com.example.ui.util.PersianUtils

@Composable
fun UnifiedMediaCard(
    media: UnifiedMedia,
    isWatchlisted: Boolean,
    onMediaClick: (UnifiedMedia) -> Unit,
    onToggleWatchlist: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val baseModifier = if (cardWidth != null) modifier.width(cardWidth) else modifier.fillMaxWidth()

    Card(
        modifier = baseModifier
            .testTag("media_card_${media.id}")
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onMediaClick(media) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) CinemaGold else GlassBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(DarkSurfaceVariant)
            ) {
                // Async image with Coil
                if (media.image.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media.image)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(id = R.drawable.img_hero_cinema_1792095559768),
                        error = painterResource(id = R.drawable.img_hero_cinema_1792095559768),
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.img_hero_cinema_1792095559768),
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gradient Vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x66000000),
                                    Color.Transparent,
                                    Color(0xDD0F0F14)
                                )
                            )
                        )
                )

                // Top badges row: Rating and Bookmark
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating pill (Saffron #FFB74D) with Persian numbers
                    if (media.imdb > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xDD000000),
                            border = BorderStroke(0.5.dp, Color(0xFFFFB74D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = PersianUtils.formatRating(media.imdb),
                                    color = Color(0xFFFFB74D),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Bookmark action
                    IconButton(
                        onClick = { onToggleWatchlist(media.id) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("bookmark_btn_${media.id}")
                            .background(Color(0x88000000), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "علاقه‌مندی",
                            tint = if (isWatchlisted) CinemaGold else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Type badge (Movie vs Series)
                val badgeText = if (media.isSeries) "سریال" else "فیلم"
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (media.isSeries) CinemaRed else Color(0xCC1A1A1A),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info details: Title, Year, Duration
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val safeDuration = PersianUtils.formatDuration(media.duration)
                val subtitle = when {
                    media.genres.isNotEmpty() -> media.genres.take(2).joinToString("، ") { it.title }
                    safeDuration != null -> safeDuration
                    media.isSeries -> "سریال تلویزیونی"
                    else -> "سینمایی"
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (safeDuration != null) {
                        Text(
                            text = safeDuration,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (media.year > 0) {
                        Text(
                            text = PersianUtils.toPersianDigits(media.year),
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGoldDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Skeleton shimmer placeholder card for loading states
 */
@Composable
fun SkeletonMediaCard(
    modifier: Modifier = Modifier,
    cardWidth: Dp? = null
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF222226),
            Color(0xFF333338),
            Color(0xFF222226)
        ),
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    val baseModifier = if (cardWidth != null) modifier.width(cardWidth) else modifier.fillMaxWidth()

    Card(
        modifier = baseModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(shimmerBrush)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
        }
    }
}
