package com.forestglass.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestglass.ui.feedback.ForestHaptics
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.theme.FIcon
import com.forestglass.ui.theme.ForestIcon
import com.forestglass.ui.theme.ForestMotion
import com.forestglass.ui.theme.ForestTokens

enum class AppRoute {
    HOME,
    LESSONS,
    PROGRESS,
    ACCOUNT,
    SEARCH,
    FOCUS,
    DECOY
}

enum class MainTab(
    val route: AppRoute,
    val label: String,
    val icon: FIcon
) {
    HOME(AppRoute.HOME, "الرئيسية", FIcon.HOME),
    LESSONS(AppRoute.LESSONS, "الدروس", FIcon.LESSONS),
    PROGRESS(AppRoute.PROGRESS, "تقدمي", FIcon.PROGRESS),
    ACCOUNT(AppRoute.ACCOUNT, "حسابي", FIcon.ACCOUNT)
}

@Composable
fun GlassBottomBar(
    currentRoute: AppRoute,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val activeIndex = MainTab.entries.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            cornerRadius = 26.dp,
            interactive = false,
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            ) {
                val tabWidth = maxWidth / MainTab.entries.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * activeIndex,
                    animationSpec = ForestMotion.liquidSpring(),
                    label = "tabSlide"
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .height(52.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ForestTokens.MintGlow.copy(alpha = 0.34f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(2.5.dp)
                            .background(ForestTokens.MintCore, RoundedCornerShape(2.dp))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        val tint by animateColorAsState(
                            targetValue = if (selected) ForestTokens.MintGlow else ForestTokens.TextSecondary,
                            animationSpec = ForestMotion.liquidSpring(),
                            label = "tabTint"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.06f else 1f,
                            animationSpec = ForestMotion.tapSpring(),
                            label = "tabScale"
                        )
                        val interaction = remember { MutableInteractionSource() }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null
                                ) {
                                    ForestHaptics.select(view)
                                    onSelectTab(tab)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ForestIcon(icon = tab.icon, size = 21.dp, tint = tint)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                color = tint,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
