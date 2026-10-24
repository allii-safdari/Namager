package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Source
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySelectionSheet(
    title: String,
    sources: List<Source>,
    onPlaySource: (Source) -> Unit,
    onDownloadSource: (Source) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSource by remember(sources) { mutableStateOf(sources.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("quality_selection_sheet"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Title and subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(listOf(CinemaGoldDark, CinemaGold)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "انتخاب کیفیت و پخش",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = "یکی از کیفیت‌های زیر را برای تماشای بدون قطعی یا دانلود برگزینید:",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )

            // Sources list cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sources.forEach { source ->
                    val isSelected = (selectedSource?.id == source.id)
                    val isFHD = source.quality.contains("1080", ignoreCase = true)
                    val isHD = source.quality.contains("720", ignoreCase = true)

                    val qualityLabel = when {
                        isFHD -> "کیفیت عالی (Full HD 1080p)"
                        isHD -> "کیفیت مناسب (HD 720p)"
                        else -> "کیفیت استاندارد (${source.quality})"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedSource = source }
                            .testTag("source_option_${source.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkSurfaceHighlight else DarkSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CinemaGold else GlassBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) CinemaGold else Color(0x33FFFFFF)
                                ) {
                                    Text(
                                        text = source.quality,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = qualityLabel,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CinemaGold else TextPrimary
                                    )
                                    Text(
                                        text = "فرمت ${source.type.uppercase()} • صدای استریو",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedSource = source },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CinemaGold,
                                    unselectedColor = TextTertiary
                                )
                            )
                        }
                    }
                }
            }

            // Dual Action Buttons: Play & Download
            if (selectedSource != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Play Button
                    Button(
                        onClick = {
                            selectedSource?.let { onPlaySource(it) }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_sheet_play"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تماشای آنلاین با پلیر داخلی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Download / External player Button
                    OutlinedButton(
                        onClick = {
                            selectedSource?.let { onDownloadSource(it) }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_sheet_download"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.5.dp, CinemaRed),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = CinemaRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "دانلود و پخش در پلیر جانبی (ADM / VLC)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
