package com.forestglass.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    val bg by animateColorAsState(
                        targetValue = if (selected) ForestTokens.MintSoft else Color.Transparent,
                        animationSpec = ForestMotion.liquidSpring(),
                        label = "tabBg"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.03f else 1f,
                        animationSpec = ForestMotion.tapSpring(),
                        label = "tabScale"
                    )
                    val interaction = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .background(bg)
                            .clickable(
                                interactionSource = interaction,
                                indication = null
                            ) {
                                ForestHaptics.select(view)
                                onSelectTab(tab)
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
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
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(if (selected) 18.dp else 0.dp)
                                .height(2.5.dp)
                                .background(
                                    if (selected) ForestTokens.MintGlow else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}
