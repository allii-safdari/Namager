package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.util.PersianUtils
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * ذره جرقه و خاکستر آتش شناور در باد
 */
private data class FireSpark(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val speed: Float,
    val swaySpeed: Float,
    val color: Color
)

/**
 * صفحه اسپلش اسکرین اختصاصی نماگر با انیمیشن شعله‌های آتش در باد،
 * ذرات درخشان آتش و نسخه فعلی اپلیکیشن در پایین‌ترین نقطه صفحه.
 */
@Composable
fun NamagerSplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ترنزیشن ورودی لوگو و المان‌ها
    val entranceAlpha = remember { Animatable(0f) }
    val entranceScale = remember { Animatable(0.78f) }
    val textAlpha = remember { Animatable(0f) }

    // ترنزیشن پیوسته برای باد، چرخش و پرش شعله‌های آتش
    val infiniteTransition = rememberInfiniteTransition(label = "FireWindTransition")

    // زاویه و حرکت متناوب باد
    val windCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "windCycle"
    )

    // تپش و شدت درخشش نور آتش (Glowing Flame Aura)
    val flameGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameGlow"
    )

    // محاسبه‌ی اثر وزش باد بر شعله آتش: انحراف زاویه‌ای، جابجایی افقی و کشیدگی عمودی
    val flameAngle = (sin(windCycle * 2f) * 2.4f + cos(windCycle * 3.5f) * 1.2f)
    val flameShiftX = (sin(windCycle * 1.5f) * 5f)
    val flameStretchY = 1f + 0.035f * sin(windCycle * 3f)
    val flameSquashX = 1f - 0.018f * sin(windCycle * 3f)

    // زمان‌بندی انیمیشن ورود و انتقال به اپلیکیشن
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        entranceScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        // مکث کوتاه برای لذت بردن از انیمیشن آتش و سپس ورود به اپ
        delay(2600)
        onFinished()
    }

    // ذرات ثابت با موقعیت‌های تصادفی تعیین‌شده
    val sparks = remember {
        val colors = listOf(
            Color(0xFFFFD54F), // طلایی
            Color(0xFFFF9100), // کهربایی
            Color(0xFFFF3D00), // قرمز آتشین
            Color(0xFFFFE082)  // جرقه سفید-زرد
        )
        List(30) { index ->
            FireSpark(
                xRatio = (0.2f + (index * 0.021f) % 0.6f),
                yRatio = (index * 0.033f) % 1f,
                radius = 1.6f + (index % 4) * 0.8f,
                speed = 0.25f + (index % 5) * 0.12f,
                swaySpeed = 1.2f + (index % 3) * 0.7f,
                color = colors[index % colors.size]
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // کاربر در صورت تمایل می‌تواند با تپ روی صفحه فوراً رد شود
                onFinished()
            }
            .testTag("namager_splash_screen")
    ) {
        // ۱. بوم پس‌زمینه: ذرات جرقه و خاکستر آتش که به سمت بالا حرکت می‌کنند و در باد می‌رقصند
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            sparks.forEachIndexed { idx, spark ->
                // حرکت رو به بالا با زمان
                val animatedY = (spark.yRatio - (windCycle / (2 * Math.PI).toFloat()) * spark.speed) % 1f
                val normalizedY = if (animatedY < 0) animatedY + 1f else animatedY
                val actualY = normalizedY * canvasHeight

                // تکان خوردن افقی در باد
                val horizontalSway = sin(windCycle * spark.swaySpeed + idx) * 22f
                val actualX = spark.xRatio * canvasWidth + horizontalSway

                // محو شدن ذره در بالا و پایین بوم
                val sparkAlpha = sin(normalizedY * Math.PI.toFloat()).coerceIn(0f, 1f) * 0.85f

                drawCircle(
                    color = spark.color.copy(alpha = sparkAlpha),
                    radius = spark.radius.dp.toPx(),
                    center = Offset(actualX, actualY)
                )
            }
        }

        // ۲. هاله نور و گرمای درخشان آتشین پشت لوگو
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(340.dp)
                .graphicsLayer {
                    alpha = entranceAlpha.value * flameGlow
                    scaleX = 1f + 0.08f * sin(windCycle * 2f)
                    scaleY = 1f + 0.08f * cos(windCycle * 2f)
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9100).copy(alpha = 0.38f),
                            Color(0xFFFF3D00).copy(alpha = 0.20f),
                            Color(0xFFDD2C00).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ۳. محتوای مرکزی: لوگوی آپلود شده همراه با تکان خوردن شعله‌ها توسط باد
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // تصویر لوگوی اصلی با افکت فیزیکی شعله‌های آتش در باد
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(230.dp)
                    .graphicsLayer {
                        // شبیه‌سازی رقص شعله‌های آتش در برابر وزش باد
                        rotationZ = flameAngle
                        translationX = flameShiftX.dp.toPx()
                        scaleX = entranceScale.value * flameSquashX
                        scaleY = entranceScale.value * flameStretchY
                        alpha = entranceAlpha.value
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "لوگوی نماگر",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // شعار اپلیکیشن با نمایش نرم و شکیل
            Text(
                text = "پلتفرم تماشای برترین فیلم‌ها و سریال‌ها",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xCCFFFFFF),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = (1f - textAlpha.value) * 16.dp.toPx()
                }
            )
        }

        // ۴. نسخه فعلی اپلیکیشن بصورت کوچک در پایین‌ترین نقطه صفحه (با رعایت حاشیه ناوبری)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "نسخه ${PersianUtils.toPersianDigits(BuildConfig.VERSION_NAME)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0x88FFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                }
            )
        }
    }
}
