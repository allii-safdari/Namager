package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.LocalPreferencesManager
import com.example.data.model.FavoriteGroup
import com.example.data.model.UnifiedMedia
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UnifiedMediaCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CinemaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: CinemaViewModel,
    onMediaClick: (UnifiedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteGroups by viewModel.favoriteGroups.collectAsStateWithLifecycle()
    val favoriteItems by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedFavoriteGroupId.collectAsStateWithLifecycle()

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var groupToRename by remember { mutableStateOf<FavoriteGroup?>(null) }
    var groupToDelete by remember { mutableStateOf<FavoriteGroup?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val displayedItems = remember(favoriteItems, selectedGroupId) {
        if (selectedGroupId == "ALL") {
            favoriteItems.map { it.media }.distinctBy { it.id }
        } else {
            favoriteItems.filter { it.groupId == selectedGroupId }.map { it.media }.distinctBy { it.id }
        }
    }

    val selectedGroup = favoriteGroups.find { it.id == selectedGroupId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .testTag("favorites_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Action Bar: Create group & Clear all
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "پوشه‌های علاقه‌مندی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Add new group
                IconButton(
                    onClick = { showCreateGroupDialog = true },
                    modifier = Modifier.testTag("btn_add_fav_group")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "ساخت گروه جدید",
                        tint = CinemaGold
                    )
                }

                // Clear all
                if (favoriteItems.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearAllDialog = true },
                        modifier = Modifier.testTag("btn_clear_all_fav")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "پاک‌سازی کلی",
                            tint = CinemaRed
                        )
                    }
                }
            }
        }

        // Horizontal Group Chips with actions
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // "همه" option
            item {
                val isSelected = (selectedGroupId == "ALL")
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectFavoriteGroup("ALL") },
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

            items(favoriteGroups, key = { it.id }) { group ->
                val isSelected = (group.id == selectedGroupId)
                InputChip(
                    selected = isSelected,
                    onClick = { viewModel.selectFavoriteGroup(group.id) },
                    label = { Text(group.name) },
                    shape = CircleShape,
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = CinemaGold,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurface,
                        labelColor = TextPrimary
                    ),
                    trailingIcon = if (!group.isDefault) {
                        {
                            Row {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ویرایش",
                                    tint = if (isSelected) Color.Black else TextSecondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { groupToRename = group }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "حذف",
                                    tint = if (isSelected) Color.Black else CinemaRed,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { groupToDelete = group }
                                )
                            }
                        }
                    } else null
                )
            }
        }

        // Selected Group Info Header
        if (selectedGroup != null && selectedGroupId != "ALL") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "گروه: ${selectedGroup.name} (${displayedItems.size} عنوان)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Favorites Grid
        if (displayedItems.isEmpty()) {
            EmptyStateView(
                message = stringResource(id = R.string.empty_watchlist_desc),
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("favorites_grid"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedItems, key = { it.id }) { item ->
                    UnifiedMediaCard(
                        media = item,
                        isWatchlisted = true,
                        onMediaClick = onMediaClick,
                        onToggleWatchlist = {
                            viewModel.removeFromFavorites(
                                it,
                                if (selectedGroupId == "ALL") null else selectedGroupId
                            )
                        }
                    )
                }
            }
        }
    }

    // Dialog: Create New Group
    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("ایجاد گروه جدید", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("نام گروه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.createFavoriteGroup(groupName)
                            showCreateGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black)
                ) {
                    Text("ایجاد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("انصراف")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Rename Group
    if (groupToRename != null) {
        var newName by remember { mutableStateOf(groupToRename!!.name) }
        AlertDialog(
            onDismissRequest = { groupToRename = null },
            title = { Text("تغییر نام گروه", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("نام جدید") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renameFavoriteGroup(groupToRename!!.id, newName)
                            groupToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black)
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToRename = null }) {
                    Text("انصراف")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Delete Group
    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("حذف گروه", fontWeight = FontWeight.Bold) },
            text = {
                Text("آیا از حذف گروه «${groupToDelete!!.name}» و تمام فیلم‌های ذخیره‌شده در آن اطمینان دارید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFavoriteGroup(groupToDelete!!.id)
                        groupToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed, contentColor = Color.White)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("انصراف")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Clear All Favorites
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("پاک‌سازی کلی علاقه‌مندی‌ها", fontWeight = FontWeight.Bold) },
            text = {
                Text("آیا می‌خواهید تمام عناوین ذخیره‌شده در همه گروه‌ها حذف شوند؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFavorites()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed, contentColor = Color.White)
                ) {
                    Text("پاک‌سازی کامل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("انصراف")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
