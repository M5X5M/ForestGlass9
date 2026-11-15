package com.forestglass.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FIcon {
    HOME,
    LESSONS,
    PROGRESS,
    ACCOUNT,
    SEARCH,
    BACK,
    LOCK,
    FULLSCREEN,
    EXIT_FULLSCREEN,
    PLAY,
    PHYSICS,
    CHEMISTRY,
    BIOLOGY,
    MATH,
    BACKSPACE,
    CHECK,
    WARNING,
    CALCULATOR
}

@Composable
fun ForestIcon(
    icon: FIcon,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = ForestTokens.TextPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.minDimension / 24f
        val stroke = Stroke(width = 1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val fill = tint.copy(alpha = 0.16f)

        when (icon) {
            FIcon.HOME -> {
                val p = Path().apply {
                    moveTo(3.5f * u, 10.5f * u)
                    lineTo(12f * u, 3.5f * u)
                    lineTo(20.5f * u, 10.5f * u)
                    lineTo(20.5f * u, 19.5f * u)
                    lineTo(14.5f * u, 19.5f * u)
                    lineTo(14.5f * u, 14f * u)
                    lineTo(9.5f * u, 14f * u)
                    lineTo(9.5f * u, 19.5f * u)
                    lineTo(3.5f * u, 19.5f * u)
                    close()
                }
                drawPath(p, fill)
                drawPath(p, tint, style = stroke)
            }

            FIcon.LESSONS -> {
                drawRoundRect(fill, Offset(4f * u, 4.5f * u), Size(16f * u, 15f * u), CornerRadius(3f * u))
                drawRoundRect(tint, Offset(4f * u, 4.5f * u), Size(16f * u, 15f * u), CornerRadius(3f * u), style = stroke)
                drawLine(tint, Offset(12f * u, 5f * u), Offset(12f * u, 19f * u), strokeWidth = 1.6f * u, cap = StrokeCap.Round)
                drawLine(tint, Offset(7f * u, 9.5f * u), Offset(9.8f * u, 9.5f * u), strokeWidth = 1.6f * u, cap = StrokeCap.Round)
                drawLine(tint, Offset(14.2f * u, 9.5f * u), Offset(17f * u, 9.5f * u), strokeWidth = 1.6f * u, cap = StrokeCap.Round)
            }

            FIcon.PROGRESS -> {
                val bars = listOf(Triple(5f, 13f, 7f), Triple(10.5f, 9f, 11f), Triple(16f, 5f, 15f))
                bars.forEach { (x, y, h) ->
                    drawRoundRect(fill, Offset(x * u, y * u), Size(3.2f * u, h * u), CornerRadius(1.2f * u))
                    drawRoundRect(tint, Offset(x * u, y * u), Size(3.2f * u, h * u), CornerRadius(1.2f * u), style = stroke)
                }
            }

            FIcon.ACCOUNT -> {
                drawCircle(fill, radius = 3.8f * u, center = Offset(12f * u, 8f * u))
                drawCircle(tint, radius = 3.8f * u, center = Offset(12f * u, 8f * u), style = stroke)
                val arc = Path().apply {
                    moveTo(5f * u, 20f * u)
                    cubicTo(5f * u, 15.5f * u, 19f * u, 15.5f * u, 19f * u, 20f * u)
                }
                drawPath(arc, fill)
                drawPath(arc, tint, style = stroke)
            }

            FIcon.SEARCH -> {
                drawCircle(fill, radius = 6.5f * u, center = Offset(10.5f * u, 10.5f * u))
                drawCircle(tint, radius = 6.5f * u, center = Offset(10.5f * u, 10.5f * u), style = stroke)
                drawLine(tint, Offset(15.3f * u, 15.3f * u), Offset(20f * u, 20f * u), strokeWidth = 2f * u, cap = StrokeCap.Round)
            }

            FIcon.BACK -> {
                drawLine(tint, Offset(5f * u, 12f * u), Offset(19f * u, 12f * u), strokeWidth = 1.9f * u, cap = StrokeCap.Round)
                val head = Path().apply {
                    moveTo(13f * u, 6.5f * u)
                    lineTo(19f * u, 12f * u)
                    lineTo(13f * u, 17.5f * u)
                }
                drawPath(head, tint, style = stroke)
            }

            FIcon.LOCK -> {
                drawRoundRect(fill, Offset(5.5f * u, 10.5f * u), Size(13f * u, 9.5f * u), CornerRadius(3f * u))
                drawRoundRect(tint, Offset(5.5f * u, 10.5f * u), Size(13f * u, 9.5f * u), CornerRadius(3f * u), style = stroke)
                val shackle = Path().apply {
                    moveTo(8.5f * u, 10.5f * u)
                    lineTo(8.5f * u, 7.5f * u)
                    cubicTo(8.5f * u, 4f * u, 15.5f * u, 4f * u, 15.5f * u, 7.5f * u)
                    lineTo(15.5f * u, 10.5f * u)
                }
                drawPath(shackle, tint, style = stroke)
            }

            FIcon.FULLSCREEN -> {
                val corners = listOf(
                    listOf(4f to 9f, 4f to 4f, 9f to 4f),
                    listOf(15f to 4f, 20f to 4f, 20f to 9f),
                    listOf(20f to 15f, 20f to 20f, 15f to 20f),
                    listOf(9f to 20f, 4f to 20f, 4f to 15f)
                )
                corners.forEach { pts ->
                    val p = Path().apply {
                        moveTo(pts[0].first * u, pts[0].second * u)
                        lineTo(pts[1].first * u, pts[1].second * u)
                        lineTo(pts[2].first * u, pts[2].second * u)
                    }
                    drawPath(p, tint, style = stroke)
                }
            }

            FIcon.EXIT_FULLSCREEN -> {
                val corners = listOf(
                    listOf(9f to 4f, 9f to 9f, 4f to 9f),
                    listOf(15f to 4f, 15f to 9f, 20f to 9f),
                    listOf(20f to 15f, 15f to 15f, 15f to 20f),
                    listOf(4f to 15f, 9f to 15f, 9f to 20f)
                )
                corners.forEach { pts ->
                    val p = Path().apply {
                        moveTo(pts[0].first * u, pts[0].second * u)
                        lineTo(pts[1].first * u, pts[1].second * u)
                        lineTo(pts[2].first * u, pts[2].second * u)
                    }
                    drawPath(p, tint, style = stroke)
                }
            }

            FIcon.PLAY -> {
                val p = Path().apply {
                    moveTo(7f * u, 5f * u)
                    lineTo(19f * u, 12f * u)
                    lineTo(7f * u, 19f * u)
                    close()
                }
                drawPath(p, fill)
                drawPath(p, tint, style = stroke)
            }

            FIcon.PHYSICS -> {
                drawCircle(tint, radius = 2f * u, center = Offset(12f * u, 12f * u))
                drawOval(fill, Offset(4f * u, 8f * u), Size(16f * u, 8f * u))
                drawOval(tint, Offset(4f * u, 8f * u), Size(16f * u, 8f * u), style = stroke)
                drawOval(tint, Offset(8f * u, 4f * u), Size(8f * u, 16f * u), style = stroke)
            }

            FIcon.CHEMISTRY -> {
                val flask = Path().apply {
                    moveTo(9.5f * u, 4f * u)
                    lineTo(14.5f * u, 4f * u)
                    moveTo(10.5f * u, 4f * u)
                    lineTo(10.5f * u, 9.5f * u)
                    lineTo(5.5f * u, 18.5f * u)
                    cubicTo(5f * u, 19.5f * u, 5.7f * u, 20.5f * u, 7f * u, 20.5f * u)
                    lineTo(17f * u, 20.5f * u)
                    cubicTo(18.3f * u, 20.5f * u, 19f * u, 19.5f * u, 18.5f * u, 18.5f * u)
                    lineTo(13.5f * u, 9.5f * u)
                    lineTo(13.5f * u, 4f * u)
                }
                drawPath(flask, fill)
                drawPath(flask, tint, style = stroke)
            }

            FIcon.BIOLOGY -> {
                val leaf = Path().apply {
                    moveTo(5f * u, 19f * u)
                    cubicTo(5f * u, 9f * u, 11f * u, 4.5f * u, 19.5f * u, 4.5f * u)
                    cubicTo(19.5f * u, 13f * u, 15f * u, 19f * u, 5f * u, 19f * u)
                    close()
                }
                drawPath(leaf, fill)
                drawPath(leaf, tint, style = stroke)
                drawLine(tint, Offset(5f * u, 19f * u), Offset(14.5f * u, 9.5f * u), strokeWidth = 1.6f * u, cap = StrokeCap.Round)
            }

            FIcon.MATH -> {
                val p = Path().apply {
                    moveTo(5f * u, 7f * u)
                    lineTo(18.5f * u, 7f * u)
                    moveTo(9f * u, 7f * u)
                    lineTo(9f * u, 18.5f * u)
                    moveTo(15f * u, 7f * u)
                    lineTo(15f * u, 16.5f * u)
                    cubicTo(15f * u, 18.5f * u, 17f * u, 19f * u, 18.5f * u, 17.5f * u)
                }
                drawPath(p, tint, style = stroke)
            }

            FIcon.BACKSPACE -> {
                val body = Path().apply {
                    moveTo(8.5f * u, 6f * u)
                    lineTo(20f * u, 6f * u)
                    lineTo(20f * u, 18f * u)
                    lineTo(8.5f * u, 18f * u)
                    lineTo(3.5f * u, 12f * u)
                    close()
                }
                drawPath(body, fill)
                drawPath(body, tint, style = stroke)
                drawLine(tint, Offset(11.5f * u, 9.5f * u), Offset(16.5f * u, 14.5f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
                drawLine(tint, Offset(16.5f * u, 9.5f * u), Offset(11.5f * u, 14.5f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
            }

            FIcon.CHECK -> {
                val p = Path().apply {
                    moveTo(5f * u, 12.5f * u)
                    lineTo(10f * u, 17.5f * u)
                    lineTo(19.5f * u, 7f * u)
                }
                drawPath(p, tint, style = stroke)
            }

            FIcon.WARNING -> {
                val tri = Path().apply {
                    moveTo(12f * u, 4f * u)
                    lineTo(20.5f * u, 19.5f * u)
                    lineTo(3.5f * u, 19.5f * u)
                    close()
                }
                drawPath(tri, fill)
                drawPath(tri, tint, style = stroke)
                drawLine(tint, Offset(12f * u, 10f * u), Offset(12f * u, 14.2f * u), strokeWidth = 1.9f * u, cap = StrokeCap.Round)
                drawCircle(tint, radius = 1f * u, center = Offset(12f * u, 17f * u))
            }

            FIcon.CALCULATOR -> {
                drawRoundRect(fill, Offset(5.5f * u, 3.5f * u), Size(13f * u, 17f * u), CornerRadius(2.5f * u))
                drawRoundRect(tint, Offset(5.5f * u, 3.5f * u), Size(13f * u, 17f * u), CornerRadius(2.5f * u), style = stroke)
                drawLine(tint, Offset(8f * u, 8f * u), Offset(16f * u, 8f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
            }
        }
    }
}
