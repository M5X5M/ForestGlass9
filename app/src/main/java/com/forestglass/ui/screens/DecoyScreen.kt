package com.forestglass.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DecoyScreen(
    emergencyPin: String,
    onReturnToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNext by remember { mutableStateOf(false) }

    BackHandler { onReturnToFocus() }

    fun pressDigit(d: String) {
        display = if (display == "0" || resetOnNext) {
            resetOnNext = false
            d
        } else if (display.length < 12) {
            display + d
        } else display
    }

    fun pressOp(op: String) {
        firstOperand = display.toDoubleOrNull() ?: 0.0
        pendingOp = op
        resetOnNext = true
    }

    fun evaluate() {
        if (pendingOp == null && display == emergencyPin) {
            onReturnToFocus()
            return
        }
        val a = firstOperand ?: return
        val b = display.toDoubleOrNull() ?: return
        val res = when (pendingOp) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else Double.NaN
            else -> b
        }
        display = if (res.isNaN()) {
            "Error"
        } else if (res % 1.0 == 0.0) {
            res.toLong().toString()
        } else {
            "%.4f".format(res).trimEnd('0').trimEnd('.')
        }
        firstOperand = null
        pendingOp = null
        resetOnNext = true
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = display,
                color = Color.White,
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 24.dp)
            )

            val pad = listOf(
                listOf("C", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "⌫", "=")
            )

            pad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        val bg = when (key) {
                            "÷", "×", "-", "+", "=" -> Color(0xFFFF9F0A)
                            "C", "±", "%" -> Color(0xFF3A3A3C)
                            else -> Color(0xFF242426)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.15f)
                                .clip(CircleShape)
                                .background(bg)
                                .combinedClickable(
                                    onClick = {
                                        when (key) {
                                            "C" -> {
                                                display = "0"
                                                firstOperand = null
                                                pendingOp = null
                                            }
                                            "⌫" -> {
                                                display = if (display.length > 1) display.dropLast(1) else "0"
                                            }
                                            "±" -> {
                                                display = if (display.startsWith("-")) display.drop(1)
                                                else if (display != "0") "-$display" else "0"
                                            }
                                            "%" -> {
                                                val v = display.toDoubleOrNull() ?: 0.0
                                                display = (v / 100.0).toString()
                                            }
                                            "." -> {
                                                if (!display.contains(".")) display += "."
                                            }
                                            "+", "-", "×", "÷" -> pressOp(key)
                                            "=" -> evaluate()
                                            else -> pressDigit(key)
                                        }
                                    },
                                    onLongClick = {
                                        if (key == "=" || key == "C") onReturnToFocus()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
