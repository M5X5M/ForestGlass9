package com.forestglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestglass.data.LessonRepository
import com.forestglass.data.StudyStatEntity
import com.forestglass.data.Subject
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.glass.GlassTier
import com.forestglass.ui.theme.ForestTokens

@Composable
fun ProgressScreen(
    stats: List<StudyStatEntity>,
    bottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val totalMinutes = stats.sumOf { it.watchSeconds } / 60L
    val completedCount = stats.count { it.completedCount > 0 }
    val statsBySubject = stats.groupBy { it.subjectId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ForestTokens.ScreenPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = 18.dp,
                bottom = bottomPadding.calculateBottomPadding() + 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "تقدمي الدراسي",
                    color = ForestTokens.TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        title = "دقائق الدراسة",
                        value = "$totalMinutes د",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "الدروس المنجزة",
                        value = "$completedCount / ${LessonRepository.allLessons.size}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            items(Subject.entries) { subject ->
                val subjectLessons = LessonRepository.bySubject(subject)
                val subjectStats = statsBySubject[subject.id].orEmpty()
                val avgMastery = if (subjectLessons.isEmpty()) 0f else {
                    subjectStats.sumOf { it.masteryScore.toDouble() }.toFloat() / subjectLessons.size
                }.coerceIn(0f, 1f)

                GlassCard(
                    cornerRadius = 22.dp,
                    interactive = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = subject.titleAr,
                                color = ForestTokens.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(avgMastery * 100).toInt()}%",
                                color = ForestTokens.MintGlow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(ForestTokens.TextMuted.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(avgMastery.coerceAtLeast(0.03f))
                                    .fillMaxHeight()
                                    .background(ForestTokens.MintGlow, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        cornerRadius = 22.dp,
        interactive = false,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, color = ForestTokens.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = ForestTokens.MintGlow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AccountScreen(
    currentPin: String,
    onUpdatePin: (String) -> Unit,
    onResetStats: () -> Unit,
    bottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var pinDraft by remember(currentPin) { mutableStateOf(currentPin) }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ForestTokens.ScreenPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = 18.dp,
                bottom = bottomPadding.calculateBottomPadding() + 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "حسابي والإعدادات",
                    color = ForestTokens.TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                GlassCard(
                    cornerRadius = 22.dp,
                    interactive = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "رمز الخروج الطارئ لوضع التركيز (4 أرقام)",
                            color = ForestTokens.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassCard(
                                cornerRadius = 14.dp,
                                tier = GlassTier.Lite,
                                interactive = false,
                                modifier = Modifier.weight(1f)
                            ) {
                                BasicTextField(
                                    value = pinDraft,
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }.take(4)
                                        pinDraft = digits
                                        savedMsg = null
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = ForestTokens.TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    cursorBrush = SolidColor(ForestTokens.MintGlow),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }

                            GlassCard(
                                cornerRadius = 14.dp,
                                selected = true,
                                onClick = {
                                    if (pinDraft.length == 4) {
                                        onUpdatePin(pinDraft)
                                        savedMsg = "تم حفظ الرمز الجديد"
                                    } else {
                                        savedMsg = "يجب أن يتكون الرمز من 4 أرقام"
                                    }
                                }
                            ) {
                                Text(
                                    text = "حفظ",
                                    color = ForestTokens.MintGlow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                )
                            }
                        }

                        if (savedMsg != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = savedMsg!!,
                                color = ForestTokens.MintGlow,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                GlassCard(
                    cornerRadius = 22.dp,
                    onClick = {
                        onResetStats()
                        savedMsg = "تم تصفير سجل التقدم"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "إعادة ضبط إحصاءات الدراسة",
                        color = ForestTokens.Danger,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}
