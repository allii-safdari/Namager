package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ThemeMode
import com.example.ui.theme.*
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.CinemaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CinemaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSeekDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "تنظیمات برنامه",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Section: Appearance & Theming
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "ظاهر و پوسته",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )

                    // Theme Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showThemeDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = CinemaGold)
                            Column {
                                Text("پوسته برنامه (Theme)", color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text(settings.themeMode.titleFa, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
                    }

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    // Accent Colors
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("رنگ اصلی برنامه (Accent Color)", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                0xFFE50914 to "قرمز سینمایی",
                                0xFFF5C518 to "طلایی",
                                0xFF4DD0E1 to "فیروزه‌ای",
                                0xFF2ECC71 to "سبز زمردی",
                                0xFF9B59B6 to "بنفش"
                            ).forEach { (colorHex, _) ->
                                val isSelected = (settings.accentColorHex == colorHex)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(colorHex), CircleShape)
                                        .clickable { viewModel.updateAccentColor(colorHex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Player Settings
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "تنظیمات پخش‌کننده (Player)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )

                    // Seek interval
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSeekDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FastForward, contentDescription = null, tint = CinemaGold)
                            Column {
                                Text("زمان جهش عقب/جلو (Seek Jump)", color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text("±${PersianUtils.toPersianDigits(settings.seekJumpSeconds)} ثانیه", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
                    }

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    // Subtitle configuration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSubtitleDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = CinemaGold)
                            Column {
                                Text("استایل زیرنویس (Subtitles)", color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text("اندازه ${PersianUtils.toPersianDigits(settings.subtitleFontSizeSp)}sp • رنگ متن سفید", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
                    }
                }
            }
        }

        // Section: System, Updates & About
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "سیستم و اطلاعات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )

                    // Check for Updates via GitHub Releases
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showUpdateDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = CinemaGold)
                            Column {
                                Text("بررسی به‌روزرسانی (GitHub Releases)", color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text("نسخه فعلی: ۱.۰.۰", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
                    }

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    // Reset settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showResetDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = CinemaRed)
                            Text("بازنشانی تنظیمات به پیش‌فرض", color = CinemaRed, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
                    }

                    HorizontalDivider(color = Color(0x22FFFFFF))

                    // About App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CinemaGold)
                            Text("درباره اپلیکیشن", color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextTertiary)
                    }
                }
            }
        }
    }

    // Dialog: Theme Selection
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("انتخاب پوسته", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = (settings.themeMode == mode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mode.titleFa, color = if (isSelected) CinemaGold else TextPrimary)
                            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("بستن") } },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Seek Interval Selection
    if (showSeekDialog) {
        AlertDialog(
            onDismissRequest = { showSeekDialog = false },
            title = { Text("زمان جهش پخش‌کننده", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30).forEach { sec ->
                        val isSelected = (settings.seekJumpSeconds == sec)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateSeekJumpSeconds(sec)
                                    showSeekDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("±${PersianUtils.toPersianDigits(sec)} ثانیه", color = if (isSelected) CinemaGold else TextPrimary)
                            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = CinemaGold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSeekDialog = false }) { Text("بستن") } },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Subtitle Settings
    if (showSubtitleDialog) {
        var fontSize by remember { mutableIntStateOf(settings.subtitleFontSizeSp) }
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("تنظیمات زیرنویس", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("اندازه قلم: ${PersianUtils.toPersianDigits(fontSize)} sp", color = TextPrimary)
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = { fontSize = it.toInt() },
                        valueRange = 12f..32f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = CinemaGold, activeTrackColor = CinemaGold)
                    )
                    // Preview box
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
                        viewModel.updateSubtitleSettings(fontSize, settings.subtitleTextColorHex, settings.subtitleBackgroundColorHex)
                        showSubtitleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black)
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = { TextButton(onClick = { showSubtitleDialog = false }) { Text("انصراف") } },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Check GitHub Releases
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("بررسی نسخه برنامه", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("شما در حال استفاده از آخرین نسخه پایدار هستید (v1.0.0).", color = TextPrimary)
                    Text("تمامی ارتباطات و روت‌های API سرور با موفقیت پیکربندی شده‌اند.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showUpdateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black)
                ) {
                    Text("متوجه شدم")
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: Reset Settings
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("بازنشانی تنظیمات", fontWeight = FontWeight.Bold) },
            text = { Text("آیا می‌خواهید تمام تنظیمات برنامه به مقادیر اولیه بازگردانده شوند؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed, contentColor = Color.White)
                ) {
                    Text("بازنشانی")
                }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("انصراف") } },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog: About App
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("درباره اپلیکیشن", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("پلتفرم تماشای فیلم و سریال ایرانی (Cinematic View)", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("طراحی شده با Jetpack Compose و Material Design 3 برای پلتفرم موبایل، تبلت و Android TV.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("پشتیبانی کامل از فونت Vazirmatn، اعداد فارسی، کنترل‌های ExoPlayer، ریموت و D-pad، و دسته‌بندی علاقه‌مندی‌ها.", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("بستن") } },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
