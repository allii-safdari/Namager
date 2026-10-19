package com.example.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ui.theme.CinemaGold

@Composable
fun CinematicPosterImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center

) {
    val context = LocalContext.current

    val request = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(300)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    var state by remember(imageUrl) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    Box(modifier = modifier.clip(shape)) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            filterQuality = FilterQuality.High,
            onState = { state = it },
            alignment = alignment
        )

        when (state) {
            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Empty -> {
                if (imageUrl.isNullOrEmpty()) {
                    ErrorPosterPlaceholder(shape = shape)
                } else {
                    ShimmerPosterPlaceholder(shape = shape)
                }
            }
            is AsyncImagePainter.State.Error -> ErrorPosterPlaceholder(shape = shape)
            else -> Unit
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ShimmerPosterPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer_poster")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val diagonal = widthPx + heightPx

        val colors = remember {
            listOf(
                Color(0xFF1E1E24),
                Color(0xFF2C2C36),
                Color(0xFF1E1E24)
            )
        }

        val offset = (progress * 2f - 0.5f) * diagonal
        val shimmerBrush = Brush.linearGradient(
            colors = colors,
            start = Offset(offset - diagonal * 0.4f, offset - diagonal * 0.4f),
            end = Offset(offset, offset)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
        )

        Icon(
            imageVector = Icons.Default.Theaters,
            contentDescription = null,
            tint = Color(0x33FFFFFF),
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun ErrorPosterPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B1B22),
                        Color(0xFF101014)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = null,
            tint = CinemaGold.copy(alpha = 0.4f),
            modifier = Modifier.size(36.dp)
        )
    }
}