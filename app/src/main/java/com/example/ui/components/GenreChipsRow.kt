package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Genre
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary

@Composable
fun GenreChipsRow(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedGenres = remember(genres) {
        genres.filter { it.id != 0 }.sortedBy { it.title }
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("genre_filter_row"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedGenres, key = { it.id }) { genre ->
            val isSelected = genre.id == selectedGenreId
            FilterChip(
                selected = isSelected,
                onClick = { onGenreSelected(if (isSelected) null else genre.id) },
                label = { Text(genre.title, fontSize = 12.sp) },
                leadingIcon = if (isSelected) {
                    { androidx.compose.material3.Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                } else null,
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CinemaRed,
                    selectedLabelColor = Color.White,
                    containerColor = DarkSurface,
                    labelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color(0x22FFFFFF),
                    selectedBorderColor = CinemaRed,
                    enabled = true,
                    selected = isSelected
                ),
                modifier = Modifier.testTag("genre_chip_${genre.id}")
            )
        }
    }
}