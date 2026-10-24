package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FilterOrder
import com.example.ui.theme.FlameLight
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
- TopAppBar
شیشه‌ای (Glassmorphism) برای صفحات فیلم‌ها و سریال‌ها.
-
اجازه می‌دهد تصاویر اسلایدشو از زیر نوار بالایی دیده شوند در حالی که خوانایی کامل حفظ می‌شود.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinematicGlassTopBar(
    title: String,
    selectedOrder: FilterOrder,
    onOrderSelected: (FilterOrder) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "cinematic_top_bar"
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        // پس‌زمینه شیشه‌ای و گرادیان تاریک محافظ کنتراست
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF5101018),
                            Color(0xDD12121C),
                            Color(0xAA12121C),
                            Color(0x5512121C)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {


                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x44222230))
                            .border(1.dp, GlassBorder, CircleShape)
                            .testTag("top_bar_sort")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "مرتب‌سازی",
                            tint = if (selectedOrder != FilterOrder.CREATED) FlameLight else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier
                            .background(DarkSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = " بر اساس",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        FilterOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.titleFa,
                                        fontWeight = if (order == selectedOrder) FontWeight.Bold else FontWeight.Normal,
                                        color = if (order == selectedOrder) FlameLight else TextPrimary
                                    )
                                },
                                leadingIcon = {
                                    if (order == selectedOrder) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = FlameLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    onOrderSelected(order)
                                    menuOpen = false
                                },
                                modifier = Modifier.testTag("sort_item_${order.value}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x44222230))
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }


                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x44222230))
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("top_bar_search")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

            }

            // خط باریک افقی شیشه‌ای در پایین نوار
            HorizontalDivider(thickness = 0.5.dp, color = GlassBorder)
        }
    }
}