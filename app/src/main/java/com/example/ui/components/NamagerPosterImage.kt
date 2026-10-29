package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.ui.theme.FlameLight
import com.example.ui.theme.DarkSurfaceVariant

/**
 * Coil-backed poster with a cheap static placeholder. The previous version
 * started an infinite shimmer animation for every visible image, which caused
 * measurable scrolling jank in the grid.
 */
@Composable
fun NamagerPosterImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center
) {
    var state by remember(imageUrl) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(DarkSurfaceVariant)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alignment = alignment,
                filterQuality = FilterQuality.Medium,
                onState = { state = it }
            )
        }

        if (imageUrl.isNullOrBlank() || state is AsyncImagePainter.State.Error) {
            ErrorPosterPlaceholder(shape = shape)
        }
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
            tint = FlameLight.copy(alpha = 0.4f),
            modifier = Modifier.size(34.dp)
        )
    }
}
