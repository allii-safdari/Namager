package com.example.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Source
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val state = viewModel.uiState

    if (state.url.isBlank()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = state.error ?: "آدرس پخش معتبر نیست.",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text("بازگشت")
                }
            }
        }
        return
    }

    ExoPlayerView(
        source = Source(
            id = 0,
            quality = "پخش آنلاین",
            type = "استریم",
            url = state.url
        ),
        title = state.title.ifBlank { "پخش ویدیو" },
        subtitle = state.subtitle,
        seekJumpSeconds = settings.seekJumpSeconds,
        onClose = onClose,
        modifier = modifier.fillMaxSize()
    )
}
