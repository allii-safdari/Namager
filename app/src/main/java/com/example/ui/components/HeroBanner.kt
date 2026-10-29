package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.Media
import com.example.ui.theme.FlameLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.PersianUtils
import java.util.Locale

@Composable
fun HeroBanner(
    media: Media,
    isWatchlisted: Boolean,
    onDetailsClick: (Media) -> Unit,
    onToggleWatchlist: (Media) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = remember(media.cover, media.image) {
        media.cover.ifBlank { media.image }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 440.dp)
            .aspectRatio(16f / 10f)
            .clickable { onDetailsClick(media) }
            .testTag("hero_banner_${media.id}")
    ) {
        NamagerPosterImage(
            imageUrl = imageUrl,
            contentDescription = media.title,
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.45f to Color(0x990F0F14),
                            0.8f to Color(0xF20F0F14),
                            1.0f to DarkBackground
                        )
                    )
                )
        )

        IconButton(
            onClick = { onToggleWatchlist(media) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x88000000))
                .testTag("hero_bookmark_${media.id}")
        ) {
            Icon(
                imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isWatchlisted) "حذف از علاقه‌مندی‌ها" else "افزودن به علاقه‌مندی‌ها",
                tint = if (isWatchlisted) FlameLight else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (media.imdb > 0 || media.year > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        if (media.imdb > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.imdb_logo),
                                    contentDescription = "IMDb",
                                    modifier = Modifier.height(20.dp)
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", media.imdb)}/10",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (media.year > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                                Text(
                                    text = PersianUtils.toPersianDigits(media.year),
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) 20.dp else 8.dp,
                label = "hero_dot_width"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (selected) Color.White else Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

fun Modifier.fullBleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontal.roundToPx() * 2
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}
