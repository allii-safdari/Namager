package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UnifiedMedia
import com.example.ui.components.CinematicPosterImage
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.SkeletonMediaCard
import com.example.ui.theme.*
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.CinemaViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: CinemaViewModel,
    onMediaClick: (UnifiedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val watchlistIds by viewModel.watchlistIds.collectAsStateWithLifecycle()

    val quickTags = listOf(
        "درام و اجتماعی",
        "جنایی و پلیسی",
        "کمدی و طنز",
        "عاشقانه",
        "تاریخی",
        "اکشن و ماجراجویی",
        "جشنواره‌ای"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Search Box with Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier

                            .fillMaxWidth()
                            .testTag("search_text_field"),
                        placeholder = {
                            Text(
                                text = "جستجوی نام فیلم، سریال، موضوع...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "پاک کردن",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.executeSearch() }),
                      //  shape = RoundedCornerShape(32.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Quick Topic/Genre Suggestion Chips
                Text(
                    text = "پیشنهادهای پربازدید:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickTags) { tag ->
                        Surface(
                            onClick = {
                                viewModel.updateSearchQuery(tag)
                                viewModel.executeSearch()
                            },
                            shape = CircleShape,
                            color = DarkSurface,
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Results
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "نتایج برای «$searchQuery»" else "فیلم‌ها و سریال‌های پیشنهادی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Search State Handling
        when (val state = searchState) {
            is UiState.Loading -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) {
                            SkeletonSearchItemCard()
                        }
                    }
                }
            }
            is UiState.Error -> {
                item {
                    ErrorRetryView(
                        message = state.message,
                        onRetry = { viewModel.executeSearch() }
                    )
                }
            }
            is UiState.Empty -> {
                item {
                    if (searchQuery.isNotEmpty()) {
                        EmptyStateView(message = "موردی برای جستجوی «$searchQuery» یافت نشد.")
                    } else {
                        EmptyStateView(message = "نام فیلم یا موضوع مورد نظر خود را در کادر بالا وارد کرده و دکمه جستجو را لمس کنید.")
                    }
                }
            }
            is UiState.Success -> {
                items(state.data, key = { it.id }) { item ->
                    LuxuriousSearchResultCard(
                        media = item,
                        isWatchlisted = watchlistIds.contains(item.id),
                        onMediaClick = { onMediaClick(item) },
                        onToggleWatchlist = { viewModel.toggleWatchlist(item.id) }
                    )
                }
            }
        }
    }
}

/**
 * Beautiful, organized card for search results with full metadata and high-res poster
 */
@Composable
fun LuxuriousSearchResultCard(
    media: UnifiedMedia,
    isWatchlisted: Boolean,
    onMediaClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onMediaClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_result_card_${media.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-res Poster with aspect ratio
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(138.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
            ) {
                CinematicPosterImage(
                    imageUrl = media.image,
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    contentScale = ContentScale.Crop
                )

                // Type badge on poster
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (media.isSeries) CinemaRed else Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (media.isSeries) "سریال" else "فیلم",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title and bookmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "علاقه‌مندی",
                            tint = if (isWatchlisted) CinemaGold else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // IMDb Rating & Year row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (media.imdb > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22FFB74D),
                            border = BorderStroke(0.5.dp, Color(0xFFFFB74D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${PersianUtils.formatRating(media.imdb)} IMDb",
                                    color = Color(0xFFFFB74D),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (media.year > 0) {
                        Text(
                            text = "سال ${PersianUtils.toPersianDigits(media.year)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    val safeDuration = PersianUtils.formatDuration(media.duration)
                    if (safeDuration != null) {
                        Text(
                            text = "•  $safeDuration",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }

                // Description excerpt
                if (media.description.isNotBlank()) {
                    Text(
                        text = media.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                // Quick Play pill action
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onMediaClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاهده و پخش", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonSearchItemCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(86.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                )
            }
        }
    }
}
