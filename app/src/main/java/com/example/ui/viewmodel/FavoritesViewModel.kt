package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FavoriteGroup
import com.example.domain.model.Media
import com.example.domain.repository.LibraryRepository
import com.example.domain.usecase.FavoriteActionsUseCase
import com.example.ui.common.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val groups: List<FavoriteGroup> = emptyList(),
    val items: List<Media> = emptyList(),
    val selectedGroupId: String = ALL_GROUP_ID,
    val totalSaved: Int = 0
) {
    val selectedGroup: FavoriteGroup?
        get() = groups.firstOrNull { it.id == selectedGroupId }

    companion object {
        const val ALL_GROUP_ID = "ALL"
    }
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    libraryRepository: LibraryRepository,
    private val favoriteActions: FavoriteActionsUseCase
) : ViewModel() {

    private val selectedGroupId = MutableStateFlow(FavoritesUiState.ALL_GROUP_ID)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<FavoritesUiState> = combine(
        libraryRepository.groups,
        libraryRepository.items,
        selectedGroupId
    ) { groups, items, selected ->
        val visibleItems = if (selected == FavoritesUiState.ALL_GROUP_ID) {
            items
        } else {
            items.filter { it.groupId == selected }
        }
        FavoritesUiState(
            groups = groups,
            items = visibleItems.map { it.media }.distinctBy { it.id },
            selectedGroupId = selected,
            totalSaved = items.distinctBy { it.mediaId }.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    fun selectGroup(groupId: String) {
        selectedGroupId.value = groupId
    }

    fun createGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { favoriteActions.createGroup(name) }
                .onSuccess { selectedGroupId.value = it.id }
                .onFailure { _messages.tryEmit(it.toUserMessage()) }
        }
    }

    fun renameGroup(groupId: String, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching { favoriteActions.renameGroup(groupId, name) }
                .onFailure { _messages.tryEmit(it.toUserMessage()) }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            runCatching {
                favoriteActions.deleteGroup(groupId)
                if (selectedGroupId.value == groupId) {
                    selectedGroupId.value = FavoritesUiState.ALL_GROUP_ID
                }
            }.onFailure { _messages.tryEmit(it.toUserMessage()) }
        }
    }

    fun remove(mediaId: Int, groupId: String?) {
        viewModelScope.launch {
            try {
                favoriteActions.remove(mediaId, groupId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _messages.tryEmit(error.toUserMessage())
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            runCatching { favoriteActions.clearAll() }
                .onFailure { _messages.tryEmit(it.toUserMessage()) }
        }
    }
}
