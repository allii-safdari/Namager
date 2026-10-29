package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Media
import com.example.ui.components.NamagerPosterImage
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.theme.FlameLight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Flame
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.SearchViewModel

private val QUICK_TAGS = listOf(
    "درام و اجتماعی",
    "جنایی و پلیسی",
    "کمدی و طنز",
    "عاشقانه",
    "تاریخی",
    "اکشن و ماجراجویی",
    "جشنواره‌ای"
)

@Composable
fun SearchScreen(
    onMediaClick: (Media) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .testTag("search_screen_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "search_header", contentType = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_field"),
                        placeholder = {
                            Text(
                                text = "جستجوی نام فیلم، سریال یا موضوع…",
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
                            if (state.query.isNotEmpty()) {
                                IconButton(
                                    onClick = viewModel::clearSearch,
                                    modifier = Modifier.testTag("search_clear")
                                ) {
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
                        keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text(
                        text = "پیشنهادهای پربازدید:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QUICK_TAGS, key = { it }) { tag ->
                            Surface(
                                onClick = { viewModel.searchQuickTag(tag) },
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

            if (state.hasSearched) {
                item(key = "search_results_header", contentType = "header") {
                    Text(
                        text = when {
                            state.isLoading -> "در حال جستجوی «${state.query.trim()}»…"
                            state.results.isNotEmpty() -> "نتایج برای «${state.query.trim()}»"
                            else -> "نتیجه‌ای برای «${state.query.trim()}» یافت نشد"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            when {
                state.isLoading -> {
                    items(count = 3, contentType = { "skeleton" }) {
                        SkeletonSearchItemCard()
                    }
                }

                state.error != null -> {
                    item(key = "search_error", contentType = "error") {
                        ErrorRetryView(message = state.error.orEmpty(), onRetry = viewModel::retry)
                    }
                }

                state.results.isNotEmpty() -> {
                    items(state.results, key = { it.id }, contentType = { "result" }) { media ->
                        SearchResultCard(
                            media = media,
                            isWatchlisted = state.favoriteIds.contains(media.id),
                            onMediaClick = { onMediaClick(media) },
                            onToggleWatchlist = { viewModel.toggleFavorite(media) }
                        )
                    }
                }

                state.hasSearched && !state.isLoading -> {
                    item(key = "search_empty", contentType = "empty") {
                        EmptyStateView(
                            message = "موردی برای جستجوی «${state.query.trim()}» پیدا نشد. عبارت دیگری را امتحان کنید."
                        )
                    }
                }

                else -> {
                    item(key = "search_idle", contentType = "empty") {
                        EmptyStateView(
                            message = "نام فیلم یا سریال مورد نظر خود را در کادر بالا بنویسید؛ نتایج به‌صورت خودکار نمایش داده می‌شوند."
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 88.dp)
        )
    }
}

@Composable
private fun SearchResultCard(
    media: Media,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(132.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
            ) {
                NamagerPosterImage(
                    imageUrl = media.image,
                    contentDescription = media.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (media.isSeries) FlameLight else Flame ,//Color(0xCCB71C1C) else Color(0xCC000000),
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
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
                            contentDescription = if (isWatchlisted) "حذف از علاقه‌مندی‌ها" else "افزودن به علاقه‌مندی‌ها",
                            tint = if (isWatchlisted) FlameLight else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

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
                }

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

                FilledTonalButton(
                    onClick = onMediaClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
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

@Composable
private fun SkeletonSearchItemCard() {
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
