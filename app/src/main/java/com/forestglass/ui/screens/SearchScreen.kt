package com.forestglass.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestglass.data.PhysicsIndex
import com.forestglass.data.PhysicsLaw
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.glass.GlassTier
import com.forestglass.ui.theme.FIcon
import com.forestglass.ui.theme.ForestIcon
import com.forestglass.ui.theme.ForestMotion
import com.forestglass.ui.theme.ForestTokens

@Composable
fun SearchScreen(
    laws: List<PhysicsLaw>,
    onOpenLessonById: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val matches = remember(query, laws) { PhysicsIndex.search(query, laws) }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = ForestTokens.ScreenPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .padding(top = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    cornerRadius = 18.dp,
                    tier = GlassTier.Lite,
                    onClick = onBack
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        ForestIcon(FIcon.BACK, size = 19.dp, tint = ForestTokens.TextPrimary)
                    }
                }

                GlassCard(
                    cornerRadius = 20.dp,
                    interactive = false,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ForestIcon(FIcon.SEARCH, size = 18.dp, tint = ForestTokens.MintGlow)
                        Spacer(Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "ابحث عن قانون أو معادلة...",
                                    color = ForestTokens.TextMuted,
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = ForestTokens.TextPrimary,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(ForestTokens.MintGlow),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .animateContentSize(animationSpec = ForestMotion.liquidSpring()),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (matches.isEmpty()) {
                    item {
                        GlassCard(
                            cornerRadius = 20.dp,
                            tier = GlassTier.Lite,
                            interactive = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "لا توجد نتائج مطابقة لـ \"$query\"",
                                color = ForestTokens.TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(matches, key = { it.law.id }) { match ->
                        val law = match.law
                        GlassCard(
                            cornerRadius = 20.dp,
                            onClick = {
                                law.lessonId?.let(onOpenLessonById)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = law.title,
                                        color = ForestTokens.TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = law.chapter,
                                        color = ForestTokens.MintGlow,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = law.formula,
                                    color = ForestTokens.MintGlow,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (law.variables.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = law.variables,
                                        color = ForestTokens.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
