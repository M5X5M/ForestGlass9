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
 * محاكاة شبكة موجات مائية ثنائية الأبعاد منخفضة الكلفة:
 * تتوقف تلقائياً عند سكون الماء (صفر استهلاك CPU عند السكون).
 */
@Stable
class WaterState(
    private val cols: Int = 36,
    private val rows: Int = 20
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
            touchGlow = max(touchGlow, 0.95f)
            val gx = (nx * (cols - 1)).toInt().coerceIn(1, cols - 2)
            val gy = (ny * (rows - 1)).toInt().coerceIn(1, rows - 2)
            for (dy in -1..1) {
                for (dx in -1..1) {
                    val falloff = if (dx == 0 && dy == 0) 0.95f else 0.45f
                    val idx = (gy + dy) * cols + (gx + dx)
                    hCurr[idx] = (hCurr[idx] + falloff).coerceIn(-1.5f, 1.5f)
                }
            }
            energy = 1f
        }
    }

    fun step(dtSeconds: Float, tiltX: Float, tiltY: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.033f)

        // خفوت الضوء بعد رفع الإصبع
        if (isPressed > 0.5f) {
            touchGlow = min(1f, touchGlow + dt * 5f)
        } else if (touchGlow > 0.002f) {
            touchGlow *= (1f - min(0.95f, dt * 3.8f))
            if (touchGlow < 0.002f) touchGlow = 0f
        }

        val tiltMag = abs(tiltX) + abs(tiltY)
        if (tiltMag > 0.14f && energy < 0.25f) {
            val ix = ((0.5f + tiltX * 0.35f) * (cols - 1)).toInt().coerceIn(1, cols - 2)
            val iy = ((0.5f + tiltY * 0.35f) * (rows - 1)).toInt().coerceIn(1, rows - 2)
            hCurr[iy * cols + ix] += tiltMag * 0.12f
            energy = max(energy, 0.2f)
        }

        if (energy <= 0.001f && touchGlow <= 0.002f) return

        val damping = 0.935f
        var maxEnergy = 0f
        for (y in 1 until rows - 1) {
            val rowOff = y * cols
            for (x in 1 until cols - 1) {
                val i = rowOff + x
                val avg = (hCurr[i - 1] + hCurr[i + 1] + hCurr[i - cols] + hCurr[i + cols]) * 0.5f
                val v = (avg - hPrev[i]) * damping
                val clamped = v.coerceIn(-1.4f, 1.4f)
                hNext[i] = clamped
                val a = abs(clamped)
                if (a > maxEnergy) maxEnergy = a
            }
        }

        // تبديل المصفوفات بدون أي حجز ذاكرة
        for (i in 0 until size) {
            hPrev[i] = hCurr[i]
            hCurr[i] = hNext[i]
        }
        energy = maxEnergy

        // ترميز الارتفاع والميل داخل Bitmap خفيف جداً
        for (y in 0 until rows) {
            val rowOff = y * cols
            for (x in 0 until cols) {
                val i = rowOff + x
                val l = if (x > 0) hCurr[i - 1] else hCurr[i]
                val r = if (x < cols - 1) hCurr[i + 1] else hCurr[i]
                val u = if (y > 0) hCurr[i - cols] else hCurr[i]
                val d = if (y < rows - 1) hCurr[i + cols] else hCurr[i]

                val nx = ((r - l) * 0.5f + 0.5f).coerceIn(0f, 1f)
                val ny = ((d - u) * 0.5f + 0.5f).coerceIn(0f, 1f)
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
        uniform float2 uTilt;
        uniform float uGlow;
        uniform float uSelected;
        uniform float uRadius;
        uniform shader uWave;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / max(uSize, float2(1.0));
            float2 wCoord = uv * uWaveSize;
            half4 wSample = uWave.eval(wCoord);

            float2 normal = (float2(wSample.rg) - 0.5) * 2.0;
            float height = (float(wSample.b) - 0.5) * 2.0;

            // إضاءة الغابة السائلة العميقة + ميلان الجهاز
            float2 lightPos = float2(0.5 + uTilt.x * 0.28, 0.18 + uTilt.y * 0.22);
            float ambient = exp(-distance(uv, lightPos) * 2.1) * 0.14;

            // إضاءة اللمس الفيروزية الناعمة المنتشرة في الماء
            float2 dTouch = (uv - uTouch) * float2(uSize.x / max(uSize.y, 1.0), 1.0);
            float dist = length(dTouch);
            float touchLight = exp(-dist * 3.4) * uGlow * 0.52;

            // لمعان الموجة (Caustics)
            float waveHighlight = clamp((normal.x * 0.7 + normal.y * 0.7 + height * 0.4), 0.0, 1.0);
            float caustic = waveHighlight * (0.14 + uGlow * 0.22);

            // لمعان الحافة العلوية المائل مع الجيروسكوب
            float topRim = smoothstep(0.28, 0.0, uv.y) * (0.08 + max(0.0, -uTilt.y) * 0.12);
            float selectTint = uSelected * 0.16;

            half3 baseCol = half3(0.028, 0.075, 0.078);
            half3 mintCol = half3(0.37, 0.83, 0.72);

            half3 finalCol = baseCol
                + mintCol * half(ambient + touchLight + caustic + topRim + selectTint);

            float alpha = clamp(0.76 + touchLight * 0.2 + selectTint * 0.25, 0.76, 0.96);
            return half4(finalCol * alpha, alpha);
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
