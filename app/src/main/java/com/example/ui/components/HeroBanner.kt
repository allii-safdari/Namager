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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MovieItem
import com.example.ui.theme.*

@Composable
fun HeroBanner(
    movie: MovieItem,
    isWatchlisted: Boolean,
    onWatchClick: (MovieItem) -> Unit,
    onDetailsClick: (MovieItem) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .testTag("hero_banner_${movie.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop Image
            if (movie.backdropRes != null) {
                Image(
                    painter = painterResource(id = movie.backdropRes),
                    contentDescription = movie.title,
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
                                Color(0x660F0F14),
                                Color(0x990F0F14),
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
                // Top Tag: Award or Featured
                if (movie.awards.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CinemaGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = movie.awards.first(),
                            color = CinemaGoldLight,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main Title
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )

                // Subtitle: English title + Director
                Text(
                    text = "${movie.englishTitle} • اثر ${movie.director}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Badges Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    // Rating Pill
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
                                text = "${movie.imdbRating}",
                                color = TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Release Year
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceHighlight
                    ) {
                        Text(
                            text = movie.releaseYear,
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Duration
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceHighlight
                    ) {
                        Text(
                            text = movie.duration,
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Age Rating
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceHighlight
                    ) {
                        Text(
                            text = movie.ageRating,
                            color = CinemaRed,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Watch Button
                    Button(
                        onClick = { onWatchClick(movie) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hero_watch_btn"),
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
                            text = stringResource(id = R.string.btn_watch_now),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Details Button
                    FilledTonalButton(
                        onClick = { onDetailsClick(movie) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hero_details_btn"),
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
                            text = stringResource(id = R.string.btn_more_info),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Bookmark Button
                    IconButton(
                        onClick = { onToggleWatchlist(movie.id) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("hero_bookmark_btn")
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
