package com.example.ui.app

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.SPLASH_ASSET
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF0F0F14)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private const val EXIT_MS = 200

private const val HOLD_AFTER_ANIMATION_MS = 1200L

private const val MAX_WAIT_MS = 4000L

@Composable
fun NamagerLottieSplash(
    isAppReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val reduceMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        ) == 0f
    }

    val result = rememberLottieComposition(LottieCompositionSpec.Asset(SPLASH_ASSET))
    val composition = result.value
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = !reduceMotion,
        speed = 0.75f
    )

    val animationDone = reduceMotion || result.isFailure ||
            (composition != null && progress >= 1f)

    var exiting by remember { mutableStateOf(false) }

    LaunchedEffect(animationDone, isAppReady) {
        if (animationDone && isAppReady) {
            if (!reduceMotion) delay(HOLD_AFTER_ANIMATION_MS)
            exiting = true
        }
    }

    LaunchedEffect(Unit) {
        delay(MAX_WAIT_MS)
        exiting = true
    }

    val exit by animateFloatAsState(
        targetValue = if (exiting) 1f else 0f,
        animationSpec = tween(EXIT_MS, easing = EmphasizedAccelerate),
        finishedListener = { if (exiting) onFinished() },
        label = "splash_exit"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(1f - exit)
            .graphicsLayer {
                val s = 1f + 0.04f * exit
                scaleX = s
                scaleY = s
            }
            .background(SplashBackground)
            .pointerInput(Unit) { },
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { if (reduceMotion) 1f else progress },
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(800f / 1240f),
            contentScale = ContentScale.Fit
        )
    }
}