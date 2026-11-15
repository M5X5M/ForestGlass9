package com.forestglass.ui.glass

import android.graphics.Paint
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.forestglass.sensor.TiltState
import com.forestglass.ui.feedback.ForestHaptics
import com.forestglass.ui.theme.ForestMotion
import com.forestglass.ui.theme.ForestTokens

enum class GlassTier { Water, Lite }

val LocalTiltState = compositionLocalOf { TiltState() }
val LocalGlobalLight = compositionLocalOf { GlobalLightState() }

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = ForestTokens.CardRadius,
    tier: GlassTier = GlassTier.Water,
    interactive: Boolean = true,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val tilt = LocalTiltState.current
    val globalLight = LocalGlobalLight.current
    val shape = RoundedCornerShape(cornerRadius)

    val waterState = remember { WaterState() }
    val runtimeShader = remember(tier) {
        if (tier == GlassTier.Water) WaterShaderFactory.createRuntimeShader(waterState.waveBitmap) else null
    }
    val nativePaint = remember(runtimeShader) {
        if (runtimeShader != null) Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = runtimeShader } else null
    }

    val rootCoords = remember { FloatArray(2) }
    var isHolding by remember { mutableStateOf(false) }
    val clickInteraction = remember { MutableInteractionSource() }

    val pressScale by animateFloatAsState(
        targetValue = if (interactive && isHolding) 0.974f else 1f,
        animationSpec = ForestMotion.tapSpring(),
        label = "glassPressScale"
    )
    val selectedAnim by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = ForestMotion.liquidSpring(),
        label = "glassSelected"
    )
    val topRimColor by animateColorAsState(
        targetValue = when {
            selected -> ForestTokens.GlassBorderFocused
            isHolding -> ForestTokens.MintCore.copy(alpha = 0.85f)
            else -> ForestTokens.GlassBorderTop
        },
        animationSpec = ForestMotion.liquidSpring(),
        label = "glassTopRim"
    )

    LaunchedEffect(
        waterState.isPressed,
        waterState.touchGlow > 0.002f,
        waterState.energy > 0.001f,
        waterState.rippleAge < 1f,
        tilt
    ) {
        var lastNs = 0L
        while (
            waterState.isPressed > 0.5f ||
            waterState.touchGlow > 0.002f ||
            waterState.energy > 0.001f ||
            waterState.rippleAge < 1f
        ) {
            withFrameNanos { ns ->
                val dt = if (lastNs == 0L) 1f / 60f else (ns - lastNs) / 1_000_000_000f
                lastNs = ns
                waterState.step(dt, tilt.x, tilt.y)
            }
        }
    }

    val rippleTracker = Modifier.pointerInput(waterState, globalLight, interactive) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val w = size.width.coerceAtLeast(1).toFloat()
                val h = size.height.coerceAtLeast(1).toFloat()
                val first = event.changes.firstOrNull() ?: continue
                val nx = first.position.x / w
                val ny = first.position.y / h
                val gx = rootCoords[0] + first.position.x
                val gy = rootCoords[1] + first.position.y

                if (first.pressed && !first.previousPressed) {
                    if (interactive) isHolding = true
                    waterState.injectTouch(nx, ny, pressed = true)
                    globalLight.inject(gx, gy, pressed = true)
                } else if (first.pressed) {
                    waterState.moveTouch(nx, ny)
                    globalLight.move(gx, gy)
                } else if (event.changes.none { it.pressed }) {
                    isHolding = false
                    waterState.releaseTouch()
                    globalLight.release()
                }
            }
        }
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = clickInteraction,
            indication = null
        ) {
            ForestHaptics.tap(view)
            onClick()
        }
    } else {
        Modifier
    }

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            topRimColor,
            ForestTokens.GlassBorder,
            ForestTokens.MintGlow.copy(alpha = if (selected) 0.48f else 0.25f)
        )
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val p = coords.positionInRoot()
                rootCoords[0] = p.x
                rootCoords[1] = p.y
            }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(shape)
            .drawBehind {
                val w = size.width
                val h = size.height
                val rPx = cornerRadius.toPx()
                val gNormX = if (w > 1f) (globalLight.screenX - rootCoords[0]) / w else 0.5f
                val gNormY = if (h > 1f) (globalLight.screenY - rootCoords[1]) / h else 0.5f
                val gGlow = globalLight.intensity

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    runtimeShader != null &&
                    nativePaint != null &&
                    !WaterShaderFactory.agslFailed
                ) {
                    runCatching {
                        runtimeShader.setFloatUniform("uSize", w, h)
                        runtimeShader.setFloatUniform("uTouch", waterState.touchNormX, waterState.touchNormY)
                        runtimeShader.setFloatUniform("uGlobalTouch", gNormX, gNormY)
                        runtimeShader.setFloatUniform("uTilt", tilt.x, tilt.y)
                        runtimeShader.setFloatUniform("uGlow", waterState.touchGlow)
                        runtimeShader.setFloatUniform("uGlobalGlow", gGlow)
                        runtimeShader.setFloatUniform("uRippleAge", waterState.rippleAge)
                        runtimeShader.setFloatUniform("uSelected", selectedAnim)
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawRoundRect(0f, 0f, w, h, rPx, rPx, nativePaint)
                        }
                    }.onFailure {
                        drawFallbackGlass(w, h, tilt, waterState, gNormX, gNormY, gGlow, selectedAnim)
                    }
                } else {
                    drawFallbackGlass(w, h, tilt, waterState, gNormX, gNormY, gGlow, selectedAnim)
                }
            }
            .border(BorderStroke(1.15.dp, borderBrush), shape)
            .then(rippleTracker)
            .then(clickModifier),
        content = content
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFallbackGlass(
    w: Float,
    h: Float,
    tilt: TiltState,
    water: WaterState,
    gNormX: Float,
    gNormY: Float,
    gGlow: Float,
    selectedAnim: Float
) {
    drawRect(color = ForestTokens.GlassSurfaceLite)
    val ambientCenter = Offset(w * (0.5f + tilt.x * 0.25f), h * (0.15f + tilt.y * 0.2f))
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                ForestTokens.MintGlow.copy(alpha = 0.16f + selectedAnim * 0.16f),
                Color.Transparent
            ),
            center = ambientCenter,
            radius = maxOf(w, h) * 0.8f
        )
    )
    if (gGlow > 0.01f) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    ForestTokens.MintGlow.copy(alpha = gGlow * 0.24f),
                    Color.Transparent
                ),
                center = Offset(w * gNormX, h * gNormY),
                radius = maxOf(w, h) * 0.75f
            )
        )
    }
    if (water.touchGlow > 0.01f) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    ForestTokens.MintCore.copy(alpha = water.touchGlow * 0.56f),
                    ForestTokens.MintGlow.copy(alpha = water.touchGlow * 0.40f),
                    Color.Transparent
                ),
                center = Offset(w * water.touchNormX, h * water.touchNormY),
                radius = maxOf(w, h) * 0.60f
            )
        )
    }
}

@Composable
fun GlassSelectableRow(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            pressed -> ForestTokens.MintPressed
            selected -> ForestTokens.MintSoft
            else -> Color.Transparent
        },
        animationSpec = ForestMotion.liquidSpring(),
        label = "rowGlow"
    )

    val rowBorder by animateColorAsState(
        targetValue = if (selected) ForestTokens.MintGlow.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = ForestMotion.liquidSpring(),
        label = "rowBorder"
    )

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(1.dp, rowBorder, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                ForestHaptics.select(view)
                onClick()
            },
        content = content
    )
}
