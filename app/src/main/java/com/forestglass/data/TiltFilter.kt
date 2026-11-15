package com.forestglass.sensor

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh

object TiltConfig {
    const val INVERT_X = false
    const val INVERT_Y = false
    const val LOW_PASS_ALPHA = 0.22f
    const val RECENTER_RATE = 0.35f
    const val DEADZONE = 0.015f
    const val MAX_TILT_RAD = 0.42f
    const val SPRING_OMEGA = 13.5f
}

data class TiltState(
    val x: Float = 0f,
    val y: Float = 0f
)

class TiltFilter {
    private var lpX = 0f
    private var lpY = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var posX = 0f
    private var posY = 0f
    private var velX = 0f
    private var velY = 0f
    private var initialized = false

    fun reset() {
        lpX = 0f; lpY = 0f
        centerX = 0f; centerY = 0f
        posX = 0f; posY = 0f
        velX = 0f; velY = 0f
        initialized = false
    }

    fun step(rawX: Float, rawY: Float, dtSeconds: Float): TiltState {
        val dt = dtSeconds.coerceIn(0.001f, 0.05f)
        val sx = if (TiltConfig.INVERT_X) -rawX else rawX
        val sy = if (TiltConfig.INVERT_Y) -rawY else rawY

        if (!initialized) {
            lpX = sx; lpY = sy
            centerX = sx; centerY = sy
            initialized = true
            return TiltState(0f, 0f)
        }

        // 1) تمرير منخفض أول للإشارة
        lpX += (sx - lpX) * TiltConfig.LOW_PASS_ALPHA
        lpY += (sy - lpY) * TiltConfig.LOW_PASS_ALPHA

        // 2) إعادة تمركز تلقائية لوضعية إمساك الجهاز
        val recenterAlpha = 1f - kotlin.math.exp(-TiltConfig.RECENTER_RATE * dt)
        centerX += (lpX - centerX) * recenterAlpha
        centerY += (lpY - centerY) * recenterAlpha

        // 3) المنطقة الميتة لإلغاء رعشة اليد + تشبّع ناعم
        val targetX = shapeAxis(lpX - centerX)
        val targetY = shapeAxis(lpY - centerY)

        // 4) نابض بتخميد حرج (zeta = 1.0) مستقل عن معدل الإطارات
        var remaining = dt
        val maxSubStep = 1f / 120f
        val w = TiltConfig.SPRING_OMEGA
        while (remaining > 0f) {
            val h = min(remaining, maxSubStep)
            val ax = w * w * (targetX - posX) - 2f * w * velX
            val ay = w * w * (targetY - posY) - 2f * w * velY
            velX += ax * h
            velY += ay * h
            posX += velX * h
            posY += velY * h
            remaining -= h
        }

        return TiltState(
            x = posX.coerceIn(-1f, 1f),
            y = posY.coerceIn(-1f, 1f)
        )
    }

    private fun shapeAxis(delta: Float): Float {
        val sign = if (delta < 0f) -1f else 1f
        val mag = max(0f, abs(delta) - TiltConfig.DEADZONE)
        if (mag == 0f) return 0f
        return sign * tanh((mag / TiltConfig.MAX_TILT_RAD).toDouble()).toFloat()
    }
}
