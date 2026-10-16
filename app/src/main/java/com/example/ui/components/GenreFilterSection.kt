package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FilterOrder
import com.example.data.model.Genre
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilterSection(
    genres: List<Genre>,
    selectedGenreId: Int,
    selectedOrder: FilterOrder,
    onGenreSelected: (Int) -> Unit,
    onOrderSelected: (FilterOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sort Order Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null,
                tint = CinemaGold,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "مرتب‌سازی:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterOrder.entries.forEach { order ->
                    val isSelected = (selectedOrder == order)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOrderSelected(order) },
                        label = {
                            Text(
                                text = order.titleFa,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaGold,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurface,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) CinemaGold else Color(0x33FFFFFF),
                            selectedBorderColor = CinemaGold,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("sort_chip_${order.value}")
                    )
                }
            }
        }

        // Genres horizontal list (sorted by title with "همه" first)
        val sortedGenres = listOf(Genre(0, "همه ژانرها")) + genres.filter { it.id != 0 }.sortedBy { it.title }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("genre_filter_row"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedGenres, key = { it.id }) { genre ->
                val isSelected = (genre.id == selectedGenreId)
                FilterChip(
                    selected = isSelected,
                    onClick = { onGenreSelected(genre.id) },
                    label = {
                        Text(
                            text = genre.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CinemaRed,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurface,
                        labelColor = TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) CinemaRed else Color(0x22FFFFFF),
                        selectedBorderColor = CinemaRed,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("genre_chip_${genre.id}")
                )
            }
        }
    }
}
