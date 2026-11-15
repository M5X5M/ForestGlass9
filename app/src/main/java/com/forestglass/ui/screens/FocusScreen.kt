package com.forestglass.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestglass.ui.feedback.ForestHaptics
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.glass.GlassTier
import com.forestglass.ui.theme.FIcon
import com.forestglass.ui.theme.ForestIcon
import com.forestglass.ui.theme.ForestMotion
import com.forestglass.ui.theme.ForestTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val DEFAULT_EMERGENCY_PIN = "1234"

@Composable
fun FocusScreen(
    emergencyPin: String,
    onExitFocus: () -> Unit,
    onOpenDecoy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current

    var remainingSeconds by rememberSaveable { mutableIntStateOf(25 * 60) }
    var showPinPad by rememberSaveable { mutableStateOf(false) }

    // تفعيل تثبيت التطبيق (App Pinning) عند الدخول وإلغاؤه عند الخروج
    DisposableEffect(Unit) {
        runCatching { activity?.startLockTask() }
        onDispose {
            runCatching { activity?.stopLockTask() }
        }
    }

    // المؤقت يستمر بالعد دائماً حتى عند فتح لوحة الرمز
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1_000L)
            remainingSeconds--
        }
        ForestHaptics.success(view)
        runCatching { activity?.stopLockTask() }
        onExitFocus()
    }

    // منع الخروج بزر الرجوع أثناء القفل (إلا لإغلاق لوحة الرمز للعودة للمؤقت)
    BackHandler(enabled = true) {
        if (showPinPad) {
            showPinPad = false
        }
    }

    AnimatedContent(
        targetState = showPinPad,
        transitionSpec = { ForestMotion.screenTransition },
        label = "focusPinSwitch",
        modifier = modifier
            .fillMaxSize()
            .background(ForestTokens.BlackOled)
            .statusBarsPadding()
    ) { pinVisible ->
        if (!pinVisible) {
            FocusTimerContent(
                remainingSeconds = remainingSeconds,
                onRequestExit = { showPinPad = true },
                onOpenDecoy = onOpenDecoy
            )
        } else {
            PinPadContent(
                expectedPin = emergencyPin.ifBlank { DEFAULT_EMERGENCY_PIN },
                onSuccess = {
                    runCatching { activity?.stopLockTask() }
                    onExitFocus()
                },
                onCancel = { showPinPad = false }
            )
        }
    }
}

@Composable
private fun FocusTimerContent(
    remainingSeconds: Int,
    onRequestExit: () -> Unit,
    onOpenDecoy: () -> Unit
) {
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formatted = "%02d:%02d".format(mins, secs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        GlassCard(
            cornerRadius = 14.dp,
            tier = GlassTier.Lite,
            onClick = onOpenDecoy,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ForestIcon(FIcon.CALCULATOR, size = 16.dp, tint = ForestTokens.TextSecondary)
                Text("حاسبة", color = ForestTokens.TextSecondary, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(
                cornerRadius = 36.dp,
                interactive = false,
                modifier = Modifier.size(72.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ForestIcon(FIcon.LOCK, size = 28.dp, tint = ForestTokens.MintGlow)
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "وضع التركيز",
                color = ForestTokens.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "ركّز على الدرس. التطبيق مقفل حتى نهاية الجلسة.",
                color = ForestTokens.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))

            GlassCard(
                cornerRadius = 28.dp,
                interactive = false
            ) {
                Text(
                    text = formatted,
                    color = ForestTokens.TextPrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 36.dp, vertical = 18.dp)
                )
            }
        }

        GlassCard(
            cornerRadius = 20.dp,
            tier = GlassTier.Lite,
            onClick = onRequestExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = "حالة طارئة؟ أدخل رمز الخروج",
                color = ForestTokens.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 13.dp)
            )
        }
    }
}

@Composable
private fun PinPadContent(
    expectedPin: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var entered by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    fun onDigit(digit: String) {
        if (entered.length >= expectedPin.length) return
        isError = false
        val next = entered + digit
        entered = next

        if (next.length == expectedPin.length) {
            if (next == expectedPin) {
                ForestHaptics.success(view)
                onSuccess()
            } else {
                ForestHaptics.error(view)
                isError = true
                scope.launch {
                    val offsets = listOf(-18f, 18f, -12f, 12f, -6f, 0f)
                    for (x in offsets) {
                        shakeOffset.animateTo(x, tween(45))
                    }
                    delay(120)
                    entered = ""
                    isError = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "رمز الخروج",
                color = ForestTokens.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // النقاط من اليسار لليمين مع نبض واهتزاز عند الخطأ
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(expectedPin.length) { idx ->
                        val filled = idx < entered.length
                        val dotScale by animateFloatAsState(
                            targetValue = if (filled) 1.25f else 1f,
                            animationSpec = ForestMotion.pulseSpring(),
                            label = "pinDot"
                        )
                        val dotColor = when {
                            isError -> ForestTokens.Danger
                            filled -> ForestTokens.MintGlow
                            else -> ForestTokens.TextMuted.copy(alpha = 0.45f)
                        }
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .graphicsLayer {
                                    scaleX = dotScale
                                    scaleY = dotScale
                                }
                                .background(dotColor, CircleShape)
                                .border(1.dp, ForestTokens.GlassBorder, CircleShape)
                        )
                    }
                }
            }
        }

        // لوحة الأرقام: إجبار الاتجاه LTR حتى يكون 1 2 3 من اليسار لليمين دائماً
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        row.forEach { num ->
                            PinKeyButton(label = num, onClick = { onDigit(num) })
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.size(72.dp))
                    PinKeyButton(label = "0", onClick = { onDigit("0") })
                    GlassCard(
                        cornerRadius = 36.dp,
                        onClick = {
                            if (entered.isNotEmpty()) entered = entered.dropLast(1)
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ForestIcon(FIcon.BACKSPACE, size = 22.dp, tint = ForestTokens.TextSecondary)
                        }
                    }
                }
            }
        }

        GlassCard(
            cornerRadius = 20.dp,
            tier = GlassTier.Lite,
            onClick = onCancel
        ) {
            Text(
                text = "إلغاء",
                color = ForestTokens.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun PinKeyButton(
    label: String,
    onClick: () -> Unit
) {
    GlassCard(
        cornerRadius = 36.dp,
        onClick = onClick,
        modifier = Modifier.size(72.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = ForestTokens.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
