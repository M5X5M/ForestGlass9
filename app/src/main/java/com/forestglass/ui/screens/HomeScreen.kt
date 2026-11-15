package com.forestglass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestglass.data.Lesson
import com.forestglass.data.ReviewSuggestion
import com.forestglass.data.Subject
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.theme.FIcon
import com.forestglass.ui.theme.ForestIcon
import com.forestglass.ui.theme.ForestMotion
import com.forestglass.ui.theme.ForestTokens

@Composable
fun HomeScreen(
    suggestion: ReviewSuggestion,
    onOpenSearch: () -> Unit,
    onOpenLesson: (Lesson) -> Unit,
    onSelectSubject: (Subject) -> Unit,
    bottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ForestTokens.ScreenPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 158.dp),
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = 18.dp,
                bottom = bottomPadding.calculateBottomPadding() + 18.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredReveal(visible = visible, delayMs = 0) {
                    Column {
                        Text(
                            text = "مرحبا بك",
                            color = ForestTokens.TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "ماذا ندرس اليوم؟",
                            color = ForestTokens.TextPrimary,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredReveal(visible = visible, delayMs = 45) {
                    GlassCard(
                        cornerRadius = 20.dp,
                        onClick = onOpenSearch,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ForestIcon(
                                icon = FIcon.SEARCH,
                                size = 19.dp,
                                tint = ForestTokens.TextSecondary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "ابحث عن أي قانون أو معادلة...",
                                color = ForestTokens.TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredReveal(visible = visible, delayMs = 90) {
                    GlassCard(
                        cornerRadius = ForestTokens.CardRadius,
                        selected = true,
                        onClick = { onOpenLesson(suggestion.lesson) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "المراجعة المقترحة: ${suggestion.lesson.title}",
                                    color = ForestTokens.TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${suggestion.lesson.subject.titleAr} · ${suggestion.statusLabel}",
                                    color = ForestTokens.TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(ForestTokens.MintBadge, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                ForestIcon(
                                    icon = FIcon.PLAY,
                                    size = 18.dp,
                                    tint = ForestTokens.MintGlow
                                )
                            }
                        }
                    }
                }
            }

            itemsIndexed(Subject.entries) { index, subject ->
                StaggeredReveal(visible = visible, delayMs = 130 + index * 35) {
                    SubjectCard(
                        subject = subject,
                        onClick = { onSelectSubject(subject) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    val icon = when (subject) {
        Subject.PHYSICS -> FIcon.PHYSICS
        Subject.CHEMISTRY -> FIcon.CHEMISTRY
        Subject.BIOLOGY -> FIcon.BIOLOGY
        Subject.MATH -> FIcon.MATH
    }
    GlassCard(
        cornerRadius = ForestTokens.CardRadius,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(ForestTokens.MintBadge, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ForestIcon(icon = icon, size = 22.dp, tint = ForestTokens.MintGlow)
            }
            Text(
                text = subject.titleAr,
                color = ForestTokens.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StaggeredReveal(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(260, delayMillis = delayMs, easing = ForestMotion.SmoothEasing)
        ) + slideInVertically(
            animationSpec = tween(280, delayMillis = delayMs, easing = ForestMotion.SmoothEasing),
            initialOffsetY = { it / 6 }
        )
    ) {
        content()
    }
}
