package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Source
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
            Text(
                text = "کیفیت ${source.quality} (${source.type})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "نحوه دریافت یا پخش ویدیو را انتخاب کنید:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download options
                Text(
                    text = "گزینه‌های دانلود (Download Options)",
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Media URL", source.url)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "لینک دانلود کپی شد", Toast.LENGTH_SHORT).show()
                        onCopyLink()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("کپی لینک دانلود (Copy Link)")
                }

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در باز کردن مرورگر", Toast.LENGTH_SHORT).show()
                        }
                        onDownloadWithBrowser()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("دانلود با مرورگر (Download with Browser)")
                }

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url)).apply {
                                setPackage("com.dv.adm")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // If ADM is not installed, open default download manager or browser
                            try {
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                context.startActivity(fallbackIntent)
                            } catch (_: Exception) {}
                            Toast.makeText(context, "برنامه ADM یافت نشد؛ با مرورگر باز شد", Toast.LENGTH_SHORT).show()
                        }
                        onDownloadWithADM()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("دانلود با ADM (Download with ADM)")
                }

                // Play options
                Text(
                    text = "گزینه‌های پخش در پلیر جانبی (Play Options)",
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("پخش در VLC Player")
                }

                Button(
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("پخش در MX Player")
                }

                Button(
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
                                Toast.makeText(context, "پلیر KM Player نصب نیست", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onOpenInKMPlayer()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("پخش در KM Player")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("انصراف (Cancel)")
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp
    )
}
