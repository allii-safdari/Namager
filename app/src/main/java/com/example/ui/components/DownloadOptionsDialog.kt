package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Source
import com.example.ui.theme.*

@Composable
fun DownloadOptionsDialog(
    source: Source,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit = {},
    onDownloadWithBrowser: () -> Unit = {},
    onDownloadWithADM: () -> Unit = {},
    onOpenInVLC: () -> Unit = {},
    onOpenInMXPlayer: () -> Unit = {},
    onOpenInKMPlayer: () -> Unit = {}
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CinemaGold
                ) {
                    Text(
                        text = source.quality,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "دانلود و پخش در پلیر جانبی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "روش دلخواه برای دانلود یا تماشا را انتخاب کنید:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // 1. Copy Link
                AppActionTile(
                    title = "کپی مستقیم لینک دانلود",
                    subtitle = "جهت انتقال به دانلود منیجر یا اشتراک‌گذاری",
                    icon = Icons.Default.ContentCopy,
                    iconBg = Color(0xFFF5C518),
                    iconTint = Color.Black,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Media URL", source.url)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "لینک دانلود در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                        onCopyLink()
                        onDismiss()
                    }
                )

                // 2. Download with Browser
                AppActionTile(
                    title = "دانلود با مرورگر اینترنت",
                    subtitle = "شروع مستقیم دانلود از مرورگر وب دستگاه",
                    icon = Icons.Default.Language,
                    iconBg = Color(0xFF2980B9),
                    iconTint = Color.White,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در اجرای مرورگر", Toast.LENGTH_SHORT).show()
                        }
                        onDownloadWithBrowser()
                        onDismiss()
                    }
                )

                // 3. Download with ADM
                AppActionTile(
                    title = "دانلود با ADM (Advanced Download Manager)",
                    subtitle = "دانلود پرسرعت چندرشته‌ای در برنامه ADM",
                    icon = Icons.Default.Download,
                    iconBg = Color(0xFF27AE60),
                    iconTint = Color.White,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url)).apply {
                                setPackage("com.dv.adm")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                context.startActivity(fallbackIntent)
                            } catch (_: Exception) {}
                            Toast.makeText(context, "برنامه ADM نصب نیست؛ با مرورگر باز شد", Toast.LENGTH_SHORT).show()
                        }
                        onDownloadWithADM()
                        onDismiss()
                    }
                )

                // 4. Play in VLC Player
                AppActionTile(
                    title = "پخش در VLC Player",
                    subtitle = "استریم مستقیم با پشتیبانی از زیرنویس مجزا",
                    icon = Icons.Default.PlayCircle,
                    iconBg = Color(0xFFE67E22),
                    iconTint = Color.White,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(source.url), "video/*")
                                setPackage("org.videolan.vlc")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(source.url), "video/*")
                                }
                                context.startActivity(genericIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "پلیر VLC نصب نیست", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onOpenInVLC()
                        onDismiss()
                    }
                )

                // 5. Play in MX Player
                AppActionTile(
                    title = "پخش در MX Player",
                    subtitle = "شتاب‌دهنده سخت‌افزاری و کنترل سریع صدا و روشنایی",
                    icon = Icons.Default.VideoLibrary,
                    iconBg = Color(0xFF00ACC1),
                    iconTint = Color.White,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(source.url), "video/*")
                                setPackage("com.mxtech.videoplayer.ad")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(source.url), "video/*")
                                }
                                context.startActivity(genericIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "پلیر MX Player نصب نیست", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onOpenInMXPlayer()
                        onDismiss()
                    }
                )

                // 6. Play in KMPlayer
                AppActionTile(
                    title = "پخش در KMPlayer",
                    subtitle = "پخش روان با کدک‌های پیشرفته",
                    icon = Icons.Default.MovieFilter,
                    iconBg = Color(0xFF8E44AD),
                    iconTint = Color.White,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(source.url), "video/*")
                                setPackage("com.kmplayer")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(source.url), "video/*")
                                }
                                context.startActivity(genericIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "پلیر KMPlayer نصب نیست", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onOpenInKMPlayer()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.testTag("download_options_dialog")
    )
}

@Composable
private fun AppActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
