package com.forestglass.ui.glass

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * حالة الضوء العام على مستوى الشاشة: تُقرأ في مرحلة الرسم فقط (Draw phase)
 * لتمرير وهج اللمس إلى الخلفية السوداء وإلى البطاقات المجاورة بدون إعادة تركيب Compose.
 */
@Stable
class GlobalLightState {
    var screenX by mutableFloatStateOf(0f)
    var screenY by mutableFloatStateOf(0f)
    var intensity by mutableFloatStateOf(0f)
    var isPressed by mutableFloatStateOf(0f)

    fun inject(x: Float, y: Float, pressed: Boolean) {
        screenX = x
        screenY = y
        isPressed = if (pressed) 1f else 0f
        if (pressed) intensity = 1f
    }

    fun move(x: Float, y: Float) {
        screenX = x
        screenY = y
    }

    fun release() {
        isPressed = 0f
    }

    fun step(dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.033f)
        if (isPressed > 0.5f) {
            intensity = min(1f, intensity + dt * 7f)
        } else if (intensity > 0.002f) {
            intensity *= (1f - min(0.92f, dt * 1.85f))
            if (intensity < 0.002f) intensity = 0f
        }
    }
}

@Stable
class WaterState(
    private val cols: Int = 60,
    private val rows: Int = 34
) {
    private val size = cols * rows
    private val hCurr = FloatArray(size)
    private val hPrev = FloatArray(size)
    private val hNext = FloatArray(size)
    private val pixels = IntArray(size)

    val waveBitmap: Bitmap = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)

    var touchNormX by mutableFloatStateOf(0.5f)
    var touchNormY by mutableFloatStateOf(0.5f)
    var touchGlow by mutableFloatStateOf(0f)
    var rippleAge by mutableFloatStateOf(1f)
    var isPressed by mutableFloatStateOf(0f)
    var energy by mutableFloatStateOf(0f)
        private set

    fun injectTouch(normX: Float, normY: Float, pressed: Boolean) {
        val nx = normX.coerceIn(0.02f, 0.98f)
        val ny = normY.coerceIn(0.02f, 0.98f)
        touchNormX = nx
        touchNormY = ny
        isPressed = if (pressed) 1f else 0f
        if (pressed) {
            touchGlow = 1f
            rippleAge = 0f
            val gx = (nx * (cols - 1)).toInt().coerceIn(2, cols - 3)
            val gy = (ny * (rows - 1)).toInt().coerceIn(2, rows - 3)
            for (dy in -2..2) {
                for (dx in -2..2) {
                    val dist2 = dx * dx + dy * dy
                    if (dist2 <= 5) {
                        val falloff = (1.45f - dist2 * 0.22f).coerceAtLeast(0.25f)
                        val idx = (gy + dy) * cols + (gx + dx)
                        hCurr[idx] = (hCurr[idx] + falloff).coerceIn(-1.9f, 1.9f)
                    }
                }
            }
            energy = 1f
        }
    }

    fun moveTouch(normX: Float, normY: Float) {
        val nx = normX.coerceIn(0.02f, 0.98f)
        val ny = normY.coerceIn(0.02f, 0.98f)
        val dx = nx - touchNormX
        val dy = ny - touchNormY
        touchNormX = nx
        touchNormY = ny
        if (dx * dx + dy * dy > 0.0004f) {
            val gx = (nx * (cols - 1)).toInt().coerceIn(1, cols - 2)
            val gy = (ny * (rows - 1)).toInt().coerceIn(1, rows - 2)
            hCurr[gy * cols + gx] = (hCurr[gy * cols + gx] + 0.62f).coerceIn(-1.7f, 1.7f)
            energy = max(energy, 0.65f)
        }
    }

    fun releaseTouch() {
        isPressed = 0f
    }

    fun step(dtSeconds: Float, tiltX: Float, tiltY: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.033f)

        if (isPressed > 0.5f) {
            touchGlow = min(1f, touchGlow + dt * 7f)
            rippleAge = min(1f, rippleAge + dt * 0.60f)
        } else {
            if (touchGlow > 0.002f) {
                touchGlow *= (1f - min(0.92f, dt * 1.85f))
                if (touchGlow < 0.002f) touchGlow = 0f
            }
            if (rippleAge < 1f) {
                rippleAge = min(1f, rippleAge + dt * 1.10f)
            }
        }

        val tiltMag = abs(tiltX) + abs(tiltY)
        if (tiltMag > 0.11f && energy < 0.25f) {
            val ix = ((0.5f + tiltX * 0.35f) * (cols - 1)).toInt().coerceIn(1, cols - 2)
            val iy = ((0.5f + tiltY * 0.35f) * (rows - 1)).toInt().coerceIn(1, rows - 2)
            hCurr[iy * cols + ix] += tiltMag * 0.16f
            energy = max(energy, 0.24f)
        }

        if (energy <= 0.001f && touchGlow <= 0.002f && rippleAge >= 1f) return

        val damping = 0.960f
        var maxEnergy = 0f
        for (y in 1 until rows - 1) {
            val rowOff = y * cols
            for (x in 1 until cols - 1) {
                val i = rowOff + x
                val avg = (hCurr[i - 1] + hCurr[i + 1] + hCurr[i - cols] + hCurr[i + cols]) * 0.5f
                val v = (avg - hPrev[i]) * damping
                val clamped = v.coerceIn(-1.7f, 1.7f)
                hNext[i] = clamped
                val a = abs(clamped)
                if (a > maxEnergy) maxEnergy = a
            }
        }

        for (i in 0 until size) {
            hPrev[i] = hCurr[i]
            hCurr[i] = hNext[i]
        }
        energy = maxEnergy

        for (y in 0 until rows) {
            val rowOff = y * cols
            for (x in 0 until cols) {
                val i = rowOff + x
                val l = if (x > 0) hCurr[i - 1] else hCurr[i]
                val r = if (x < cols - 1) hCurr[i + 1] else hCurr[i]
                val u = if (y > 0) hCurr[i - cols] else hCurr[i]
                val d = if (y < rows - 1) hCurr[i + cols] else hCurr[i]

                val nx = ((r - l) * 0.85f + 0.5f).coerceIn(0f, 1f)
                val ny = ((d - u) * 0.85f + 0.5f).coerceIn(0f, 1f)
                val h = (hCurr[i] * 0.5f + 0.5f).coerceIn(0f, 1f)

                val ir = (nx * 255f).toInt()
                val ig = (ny * 255f).toInt()
                val ib = (h * 255f).toInt()
                pixels[i] = (0xFF shl 24) or (ir shl 16) or (ig shl 8) or ib
            }
        }
        waveBitmap.setPixels(pixels, 0, cols, 0, 0, cols, rows)
    }
}

object WaterShaderFactory {
    @Volatile
    var agslFailed: Boolean = false
        private set

    private const val AGSL_GLASS_WATER = """
        uniform float2 uSize;
        uniform float2 uWaveSize;
        uniform float2 uTouch;
        uniform float2 uGlobalTouch;
        uniform float2 uTilt;
        uniform float uGlow;
        uniform float uGlobalGlow;
        uniform float uRippleAge;
        uniform float uSelected;
        uniform shader uWave;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / max(uSize, float2(1.0));
            float aspect = uSize.x / max(uSize.y, 1.0);
            float2 wCoord = uv * uWaveSize;
            half4 wSample = uWave.eval(wCoord);

            float2 normal = (float2(wSample.rg) - 0.5) * 2.0;
            float height = (float(wSample.b) - 0.5) * 2.0;

            // 1) عمق الزجاج وإضاءة الغابة المائلة مع الجيروسكوب
            float2 lightPos = float2(0.5 + uTilt.x * 0.34, 0.14 + uTilt.y * 0.25);
            float ambient = exp(-distance(uv, lightPos) * 2.2) * 0.18;

            // 2) لمعة السطح العلوي والحافة الداخلية
            float topSheen = (1.0 - smoothstep(0.0, 0.24, uv.y)) * (0.12 + max(0.0, -uTilt.y) * 0.14);
            float bottomSheen = smoothstep(0.80, 1.0, uv.y) * 0.06;

            // 3) وهج الإصبع المباشر (هالة فيروزية واسعة + نواة بيضاء ساطعة)
            float2 dTouch = (uv - uTouch) * float2(aspect, 1.0);
            float dist = length(dTouch);
            float glowWide = exp(-dist * 2.15) * uGlow * 0.88;
            float glowCore = exp(-dist * 6.4) * uGlow * 0.68;

            // 4) تسرّب النور من البطاقات المجاورة على الشاشة
            float2 dGlobal = (uv - uGlobalTouch) * float2(aspect, 1.0);
            float gDist = length(dGlobal);
            float globalBleed = exp(-gDist * 2.25) * uGlobalGlow * 0.36;

            // 5) إضاءة Blinn-Phong ثلاثية الأبعاد على قمم موجات الماء فقط
            float waveAmp = clamp(length(normal) * 2.2 + abs(height) * 1.4, 0.0, 1.0);
            float3 n3 = normalize(float3(normal.x * 1.65, normal.y * 1.65, 1.0));
            float3 l3 = normalize(float3(uTouch.x - uv.x, uTouch.y - uv.y, 0.36));
            float3 h3 = normalize(l3 + float3(0.0, 0.0, 1.0));
            float ndh = max(dot(n3, h3), 0.0);
            float ndh2 = ndh * ndh;
            float spec8 = ndh2 * ndh2 * ndh2 * ndh2;
            float waterSpec = spec8 * waveAmp * (0.28 + uGlow * 0.92);

            // 6) موجة حلقية متسعة من نقطة الضغط
            float ringRadius = uRippleAge * 1.05;
            float ringBand = exp(-abs(dist - ringRadius) * 18.0) * (1.0 - uRippleAge) * uGlow * 0.58;

            float selectTint = uSelected * 0.20;

            half3 deepGlass = half3(0.012, 0.042, 0.046);
            half3 cyanLight = half3(0.22, 0.92, 0.83);
            half3 whiteCore = half3(0.88, 1.0, 0.98);

            half3 col = deepGlass
                + cyanLight * half(ambient + topSheen + bottomSheen + glowWide + globalBleed + ringBand * 0.85 + selectTint)
                + whiteCore * half(glowCore + waterSpec + ringBand * 0.35);

            float alpha = clamp(0.82 + glowWide * 0.16 + globalBleed * 0.12 + selectTint * 0.14, 0.82, 0.98);
            return half4(col * alpha, alpha);
        }
    """

    fun createRuntimeShader(waveBitmap: Bitmap): RuntimeShader? {
        if (agslFailed || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return runCatching {
            val shader = RuntimeShader(AGSL_GLASS_WATER)
            val bmpShader = BitmapShader(waveBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            shader.setInputBuffer("uWave", bmpShader)
            shader.setFloatUniform("uWaveSize", waveBitmap.width.toFloat(), waveBitmap.height.toFloat())
            shader
        }.getOrElse {
            agslFailed = true
            null
        }
    }
}
