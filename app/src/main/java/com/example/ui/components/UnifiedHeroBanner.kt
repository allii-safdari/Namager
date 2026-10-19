package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UnifiedMedia
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun UnifiedHeroBanner(
    media: UnifiedMedia,
    isWatchlisted: Boolean,
    onWatchClick: (UnifiedMedia) -> Unit,
    onDetailsClick: (UnifiedMedia) -> Unit,
    onToggleWatchlist: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = remember(media.cover, media.image) {
        if (media.cover.isNotEmpty()) media.cover else media.image
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // نسبت پوستر در تصویر نمونه
            .clickable { onWatchClick(media) }
            .testTag("unified_hero_banner_${media.id}")
    ) {
        // پوستر تمام‌عرض
        CinematicPosterImage(
            imageUrl = imageUrl,
            contentDescription = media.title,
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter // چهره‌ها بریده نشوند

        )

        // گرادیان پایین که در رنگ پس‌زمینه‌ی صفحه محو می‌شود
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.5f to Color(0x990F0F14),
                            0.8f to Color(0xF20F0F14),
                            1.0f to DarkBackground
                        )
                    )
                )
        )

        // عنوان و اطلاعات (وسط‌چین)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 56.dp), // فضای اندیکاتور
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (media.imdb > 0 || media.year > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                // ترتیب ثابت: IMDb سمت چپ، سال سمت راست (مستقل از RTL)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (media.imdb > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.imdb_logo),
                                    contentDescription = "IMDb",
                                    modifier = Modifier.height(22.dp)
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        ) {
                                            append(String.format(Locale.US, "%.1f", media.imdb))
                                        }
                                        withStyle(
                                            SpanStyle(
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 16.sp
                                            )
                                        ) {
                                            append("/10")
                                        }
                                    }
                                )
                            }
                        }

                        if (media.year > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = media.year.toString(),
                                    color = TextPrimary,
                                    fontSize = 16.sp,
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

/** نقاط اسلایدر؛ آیتم فعال به شکل قرص کشیده نمایش داده می‌شود. */
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
                label = "dot_width"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (selected) Color.White else Color.White.copy(alpha = 0.9f))
            )
        }
    }
}

/**
 * آیتم را از padding کناری گرید بیرون می‌برد تا بنر تمام‌عرض شود.
 * [horizontal] باید برابر contentPadding افقی گرید باشد.
 */
fun Modifier.fullBleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontal.roundToPx() * 2
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}