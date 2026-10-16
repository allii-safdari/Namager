package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Source
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
                .testTag("quality_selection_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Text(
                text = "کیفیت مورد نظر خود را انتخاب کرده، سپس گزینه پخش یا دانلود را لمس کنید:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            // Sources list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEach { source ->
                    val isSelected = (selectedSource?.id == source.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSource = source }
                            .testTag("source_option_${source.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkSurfaceHighlight else DarkSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CinemaGold else GlassBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "کیفیت ${source.quality}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CinemaGold else TextPrimary
                                )
                                Text(
                                    text = "فرمت: ${source.type.uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
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

            // Action Buttons: Play & Download
            if (selectedSource != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play Online
                    Button(
                        onClick = {
                            selectedSource?.let { onPlaySource(it) }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_sheet_play"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("پخش آنلاین", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Download / External player
                    OutlinedButton(
                        onClick = {
                            selectedSource?.let { onDownloadSource(it) }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_sheet_download"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.5.dp, CinemaRed),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = CinemaRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دانلود / پلیر", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
