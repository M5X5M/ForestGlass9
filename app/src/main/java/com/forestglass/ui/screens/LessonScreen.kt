package com.forestglass.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestglass.data.Lesson
import com.forestglass.data.LessonRepository
import com.forestglass.data.StudyStatEntity
import com.forestglass.data.Subject
import com.forestglass.player.YouTubeLessonPlayer
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.glass.GlassSelectableRow
import com.forestglass.ui.glass.GlassTier
import com.forestglass.ui.theme.FIcon
import com.forestglass.ui.theme.ForestIcon
import com.forestglass.ui.theme.ForestTokens
import kotlinx.coroutines.delay

@Composable
fun LessonScreen(
    subject: Subject,
    currentLesson: Lesson,
    statsMap: Map<String, StudyStatEntity>,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
    onSelectSubject: (Subject) -> Unit,
    onSelectLesson: (Lesson) -> Unit,
    onStartFocus: () -> Unit,
    onTickStudySecond: (Lesson) -> Unit,
    onMarkCompleted: (Lesson) -> Unit,
    onBack: () -> Unit,
    bottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val lessons = remember(subject) { LessonRepository.bySubject(subject) }
    val latestLesson by rememberUpdatedState(currentLesson)

    // تسجيل تقدم الدراسة كل 15 ثانية أثناء فتح شاشة الدرس
    LaunchedEffect(currentLesson.id) {
        while (true) {
            delay(15_000L)
            onTickStudySecond(latestLesson)
        }
    }

    BackHandler(enabled = isFullscreen) {
        onFullscreenChange(false)
    }

    // إبقاء نفس WebView حياً أثناء الدخول والخروج من ملء الشاشة
    val sharedPlayer = remember(currentLesson.id) {
        movableContentOf { fullscreen: Boolean, playerModifier: Modifier ->
            YouTubeLessonPlayer(
                youtubeId = currentLesson.youtubeId,
                lessonTitle = currentLesson.title,
                isFullscreen = fullscreen,
                onToggleFullscreen = { onFullscreenChange(!fullscreen) },
                modifier = playerModifier
            )
        }
    }

    if (isFullscreen) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            sharedPlayer(true, Modifier.fillMaxSize())
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = ForestTokens.ScreenPadding)
            .padding(bottom = bottomPadding.calculateBottomPadding()),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTabletWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .widthIn(max = 1080.dp)
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 10.dp)
        ) {
            // الشريط العلوي: رجوع + تبديل سريع بين المواد
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Subject.entries.forEach { item ->
                        val active = item == subject
                        GlassCard(
                            cornerRadius = 14.dp,
                            tier = GlassTier.Lite,
                            selected = active,
                            onClick = { onSelectSubject(item) }
                        ) {
                            Text(
                                text = item.titleAr,
                                color = if (active) ForestTokens.MintGlow else ForestTokens.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                GlassCard(
                    cornerRadius = 16.dp,
                    tier = GlassTier.Lite,
                    onClick = onBack
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ForestIcon(FIcon.BACK, size = 18.dp, tint = ForestTokens.TextPrimary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isTabletWide) {
                // تخطيط التابلت (لوحتين): الفيديو والمعلومات يميناً، والقائمة وزر التركيز يساراً
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        LessonListCard(
                            lessons = lessons,
                            selectedId = currentLesson.id,
                            statsMap = statsMap,
                            onSelectLesson = onSelectLesson,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(Modifier.height(12.dp))
                        FocusLaunchButton(onStartFocus = onStartFocus)
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        GlassCard(
                            cornerRadius = 22.dp,
                            interactive = false, // لا ينكمش عند لمس الفيديو
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        ) {
                            sharedPlayer(false, Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.height(12.dp))
                        LessonMetaRow(
                            lesson = currentLesson,
                            isCompleted = (statsMap[currentLesson.id]?.completedCount ?: 0) > 0,
                            onToggleFullscreen = { onFullscreenChange(true) },
                            onMarkCompleted = { onMarkCompleted(currentLesson) }
                        )
                    }
                }
            } else {
                // تخطيط الهاتف: الفيديو بالأعلى + القائمة تتمرر داخلياً + زر التركيز مثبّت بالأسفل
                GlassCard(
                    cornerRadius = 20.dp,
                    interactive = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    sharedPlayer(false, Modifier.fillMaxSize())
                }

                Spacer(Modifier.height(10.dp))

                LessonMetaRow(
                    lesson = currentLesson,
                    isCompleted = (statsMap[currentLesson.id]?.completedCount ?: 0) > 0,
                    onToggleFullscreen = { onFullscreenChange(true) },
                    onMarkCompleted = { onMarkCompleted(currentLesson) }
                )

                Spacer(Modifier.height(10.dp))

                LessonListCard(
                    lessons = lessons,
                    selectedId = currentLesson.id,
                    statsMap = statsMap,
                    onSelectLesson = onSelectLesson,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(10.dp))

                FocusLaunchButton(onStartFocus = onStartFocus)
            }
        }
    }
}

@Composable
private fun LessonMetaRow(
    lesson: Lesson,
    isCompleted: Boolean,
    onToggleFullscreen: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lesson.title,
                color = ForestTokens.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${lesson.teacher} · ${lesson.durationMinutes} د",
                color = ForestTokens.TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassCard(
                cornerRadius = 14.dp,
                tier = GlassTier.Lite,
                selected = isCompleted,
                onClick = onMarkCompleted
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ForestIcon(
                        icon = FIcon.CHECK,
                        size = 16.dp,
                        tint = if (isCompleted) ForestTokens.MintGlow else ForestTokens.TextSecondary
                    )
                    Text(
                        text = if (isCompleted) "مُنجز" else "إتمام",
                        color = if (isCompleted) ForestTokens.MintGlow else ForestTokens.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // زر ملء الشاشة بجانب عنوان الدرس
            GlassCard(
                cornerRadius = 14.dp,
                tier = GlassTier.Lite,
                onClick = onToggleFullscreen
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ForestIcon(
                        icon = FIcon.FULLSCREEN,
                        size = 16.dp,
                        tint = ForestTokens.TextPrimary
                    )
                    Text(
                        text = "ملء الشاشة",
                        color = ForestTokens.TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonListCard(
    lessons: List<Lesson>,
    selectedId: String,
    statsMap: Map<String, StudyStatEntity>,
    onSelectLesson: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        cornerRadius = ForestTokens.CardRadius,
        interactive = false,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(lessons, key = { it.id }) { item ->
                val isSelected = item.id == selectedId
                val completed = (statsMap[item.id]?.completedCount ?: 0) > 0

                GlassSelectableRow(
                    selected = isSelected,
                    onClick = { onSelectLesson(item) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = if (isSelected) ForestTokens.MintGlow else ForestTokens.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.chapter,
                                color = ForestTokens.TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (completed) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(ForestTokens.MintBadge, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                ForestIcon(FIcon.CHECK, size = 13.dp, tint = ForestTokens.MintGlow)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusLaunchButton(onStartFocus: () -> Unit) {
    GlassCard(
        cornerRadius = 20.dp,
        selected = true,
        onClick = onStartFocus,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ForestIcon(FIcon.LOCK, size = 19.dp, tint = ForestTokens.MintGlow)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "ابدأ وضع التركيز",
                color = ForestTokens.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
