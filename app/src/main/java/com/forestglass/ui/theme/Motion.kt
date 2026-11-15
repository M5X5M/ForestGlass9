package com.forestglass.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

object ForestMotion {
    val SmoothEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

    fun <T> liquidSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 380f
    )

    fun <T> tapSpring() = spring<T>(
        dampingRatio = 0.78f,
        stiffness = 550f
    )

    fun <T> pulseSpring() = spring<T>(
        dampingRatio = 0.55f,
        stiffness = 520f
    )

    fun intOffsetSpring() = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 400f
    )

    val screenTransition: ContentTransform
        get() = (fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 40, easing = SmoothEasing)))
            .togetherWith(fadeOut(animationSpec = tween(durationMillis = 130, easing = SmoothEasing)))
}
