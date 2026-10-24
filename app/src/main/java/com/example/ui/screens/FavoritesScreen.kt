package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.FavoriteGroup
import com.example.domain.model.Media
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MediaCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FavoritesUiState
import com.example.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onMediaClick: (Media) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var groupToRename by remember { mutableStateOf<FavoriteGroup?>(null) }
    var groupToDelete by remember { mutableStateOf<FavoriteGroup?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("favorites_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(
                key = "favorites_header",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "header"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "علاقه‌مندی‌ها",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${state.totalSaved} عنوان ذخیره‌شده",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.testTag("btn_add_fav_group")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreateNewFolder,
                                    contentDescription = "ساخت پوشه جدید",
                                    tint = CinemaGold
                                )
                            }
                            if (state.totalSaved > 0) {
                                IconButton(
                                    onClick = { showClearDialog = true },
                                    modifier = Modifier.testTag("btn_clear_all_fav")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "پاک‌سازی همه علاقه‌مندی‌ها",
                                        tint = CinemaRed
                                    )
                                }
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item(key = "all_groups") {
                            val selected = state.selectedGroupId == FavoritesUiState.ALL_GROUP_ID
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    viewModel.selectGroup(FavoritesUiState.ALL_GROUP_ID)
                                },
                                label = { Text("همه موارد") },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurface,
                                    labelColor = TextPrimary
                                )
                            )
                        }

                        items(state.groups, key = { it.id }) { group ->
                            val selected = group.id == state.selectedGroupId
                            InputChip(
                                selected = selected,
                                onClick = { viewModel.selectGroup(group.id) },
                                label = { Text(group.name) },
                                shape = CircleShape,
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = CinemaGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurface,
                                    labelColor = TextPrimary
                                ),
                                trailingIcon = if (group.isDefault) {
                                    null
                                } else {
                                    {
                                        Row {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "تغییر نام",
                                                tint = if (selected) Color.Black else TextSecondary,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { groupToRename = group }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "حذف پوشه",
                                                tint = if (selected) Color.Black else CinemaRed,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { groupToDelete = group }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (state.selectedGroupId != FavoritesUiState.ALL_GROUP_ID && state.selectedGroup != null) {
                        Text(
                            text = "پوشه ${state.selectedGroup?.name} • ${state.items.size} عنوان",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (state.items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                    EmptyStateView(
                        message = if (state.totalSaved == 0) {
                            "هنوز چیزی ذخیره نکرده‌اید. با زدن نشان روی هر فیلم یا سریال، آن را اینجا ذخیره کنید."
                        } else {
                            "در این پوشه عنوانی وجود ندارد."
                        }
                    )
                }
            } else {
                items(state.items, key = { it.id }, contentType = { "media" }) { media ->
                    MediaCard(
                        media = media,
                        isWatchlisted = true,
                        onMediaClick = onMediaClick,
                        onToggleWatchlist = {
                            viewModel.remove(
                                mediaId = media.id,
                                groupId = state.selectedGroupId.takeIf {
                                    it != FavoritesUiState.ALL_GROUP_ID
                                }
                            )
                        }
                    )
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

    if (showCreateDialog) {
        GroupNameDialog(
            title = "ایجاد پوشه جدید",
            initialName = "",
            confirmLabel = "ایجاد",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createGroup(name)
                showCreateDialog = false
            }
        )
    }

    groupToRename?.let { group ->
        GroupNameDialog(
            title = "تغییر نام پوشه",
            initialName = group.name,
            confirmLabel = "ذخیره",
            onDismiss = { groupToRename = null },
            onConfirm = { name ->
                viewModel.renameGroup(group.id, name)
                groupToRename = null
            }
        )
    }

    groupToDelete?.let { group ->
        ConfirmDialog(
            title = "حذف پوشه",
            message = "آیا پوشه «${group.name}» و همه عناوین داخل آن حذف شود؟",
            confirmLabel = "حذف",
            onDismiss = { groupToDelete = null },
            onConfirm = {
                viewModel.deleteGroup(group.id)
                groupToDelete = null
            }
        )
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "پاک‌سازی همه علاقه‌مندی‌ها",
            message = "همه عناوین ذخیره‌شده در تمام پوشه‌ها حذف می‌شوند. این کار قابل بازگشت نیست.",
            confirmLabel = "پاک‌سازی",
            onDismiss = { showClearDialog = false },
            onConfirm = {
                viewModel.clearAll()
                showClearDialog = false
            }
        )
    }
}

@Composable
private fun GroupNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام پوشه") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed, contentColor = Color.White)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
