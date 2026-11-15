package com.forestglass.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

object ForestMotion {
    val SmoothEasing = CubicBezierEasing(0.16f, 1.0f, 0.30f, 1.0f)

    fun <T> liquidSpring() = spring<T>(
        dampingRatio = 0.80f,
        stiffness = 320f
    )

    fun <T> tapSpring() = spring<T>(
        dampingRatio = 0.65f,
        stiffness = 500f
    )

    fun <T> pulseSpring() = spring<T>(
        dampingRatio = 0.50f,
        stiffness = 460f
    )

    fun intOffsetSpring() = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 380f
    )

    val screenTransition: ContentTransform
        get() = (
            fadeIn(animationSpec = tween(durationMillis = 250, delayMillis = 30, easing = SmoothEasing)) +
                scaleIn(
                    initialScale = 0.978f,
                    animationSpec = tween(durationMillis = 260, delayMillis = 30, easing = SmoothEasing)
                )
            ).togetherWith(
                fadeOut(animationSpec = tween(durationMillis = 120, easing = SmoothEasing))
            )
}
