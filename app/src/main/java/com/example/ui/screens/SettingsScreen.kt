package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.domain.model.ThemeMode
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSeekDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("settings_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "settings_title") {
                Column {
                    Text(
                        text = "تنظیمات",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "تجربه تماشا را مطابق سلیقه خود تنظیم کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item(key = "settings_appearance") {
                SettingsSection(title = "ظاهر و پوسته") {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "پوسته برنامه",
                        subtitle = settings.themeMode.titleFa,
                        onClick = { showThemeDialog = true }
                    )

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    /*Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "رنگ اصلی برنامه",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ACCENT_COLORS.forEach { (colorHex, _) ->
                                val isSelected = settings.accentColorHex == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(colorHex), CircleShape)
                                        .clickable { viewModel.setAccentColor(colorHex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }*/
                }
            }

            item(key = "settings_player") {
                SettingsSection(title = "تنظیمات پخش‌کننده") {
                    SettingsRow(
                        icon = Icons.Default.FastForward,
                        title = "زمان جهش عقب/جلو",
                        subtitle = "±${PersianUtils.toPersianDigits(settings.seekJumpSeconds)} ثانیه",
                        onClick = { showSeekDialog = true }
                    )

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    SettingsRow(
                        icon = Icons.Default.Subtitles,
                        title = "اندازه زیرنویس",
                        subtitle = "${PersianUtils.toPersianDigits(settings.subtitleFontSizeSp)}sp",
                        onClick = { showSubtitleDialog = true }
                    )
                }
            }

            item(key = "settings_system") {
                SettingsSection(title = "سیستم و اطلاعات") {
                    SettingsRow(
                        icon = Icons.Default.SystemUpdate,
                        title = "بررسی به‌روزرسانی",
                        subtitle = "نسخه فعلی: ${BuildConfig.VERSION_NAME}",
                        onClick = { showUpdateDialog = true }
                    )

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    SettingsRow(
                        icon = Icons.Default.RestartAlt,
                        title = "بازنشانی تنظیمات",
                        subtitle = "بازگشت همه تنظیمات به حالت پیش‌فرض",
                        tint = CinemaRed,
                        onClick = { showResetDialog = true }
                    )

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "درباره اپلیکیشن",
                        subtitle = "Namager",
                        onClick = { showAboutDialog = true }
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

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("انتخاب پوسته", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = settings.themeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mode.titleFa, color = if (isSelected) CinemaGold else TextPrimary)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("بستن") }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showSeekDialog) {
        AlertDialog(
            onDismissRequest = { showSeekDialog = false },
            title = { Text("زمان جهش پخش‌کننده", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30).forEach { seconds ->
                        val isSelected = settings.seekJumpSeconds == seconds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setSeekJumpSeconds(seconds)
                                    showSeekDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "±${PersianUtils.toPersianDigits(seconds)} ثانیه",
                                color = if (isSelected) CinemaGold else TextPrimary
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeekDialog = false }) { Text("بستن") }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showSubtitleDialog) {
        var fontSize by remember { mutableIntStateOf(settings.subtitleFontSizeSp) }
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("تنظیمات زیرنویس", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "اندازه قلم: ${PersianUtils.toPersianDigits(fontSize)} sp",
                        color = TextPrimary
                    )
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = { fontSize = it.toInt() },
                        valueRange = 12f..32f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = CinemaGold,
                            activeTrackColor = CinemaGold
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "پیش‌نمایش متن زیرنویس فیلم",
                            color = Color.White,
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setSubtitleSize(fontSize)
                        showSubtitleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubtitleDialog = false }) { Text("انصراف") }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("بازنشانی تنظیمات", fontWeight = FontWeight.Bold) },
            text = { Text("همه تنظیمات برنامه به مقادیر اولیه بازگردانده شوند؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reset()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("بازنشانی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("انصراف") }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("بررسی نسخه برنامه", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "شما از آخرین نسخه نصب‌شده (${BuildConfig.VERSION_NAME}) استفاده می‌کنید.",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showUpdateDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text("متوجه شدم")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("درباره اپلیکیشن", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "لوگوی نماگر",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Text(
                        "نماگر (نسخه ${PersianUtils.toPersianDigits(BuildConfig.VERSION_NAME)})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "پلتفرم تماشای آنلاین فیلم و سریال، ساخته‌شده با Jetpack Compose و Material 3 همراه با پخش‌کننده روان Media3 و امکان دانلود مستقیم.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("بستن") }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CinemaGold
            )
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = CinemaGold
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint)
            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
    }
}

private val ACCENT_COLORS = listOf(
    0xFFE50914 to "قرمز سینمایی",
    0xFFF5C518 to "طلایی",
    0xFF4DD0E1 to "فیروزه‌ای",
    0xFF2ECC71 to "سبز زمردی",
    0xFF9B59B6 to "بنفش"
)
