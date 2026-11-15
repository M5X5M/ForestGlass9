package com.forestglass

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forestglass.data.LessonRepository
import com.forestglass.data.PhysicsIndex
import com.forestglass.data.StudyTracker
import com.forestglass.data.Subject
import com.forestglass.sensor.TiltSensor
import com.forestglass.sensor.TiltState
import com.forestglass.ui.AppRoute
import com.forestglass.ui.GlassBottomBar
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.glass.GlassTier
import com.forestglass.ui.glass.GlobalLightState
import com.forestglass.ui.glass.LocalGlobalLight
import com.forestglass.ui.glass.LocalTiltState
import com.forestglass.ui.screens.AccountScreen
import com.forestglass.ui.screens.DEFAULT_EMERGENCY_PIN
import com.forestglass.ui.screens.DecoyScreen
import com.forestglass.ui.screens.FocusScreen
import com.forestglass.ui.screens.HomeScreen
import com.forestglass.ui.screens.LessonScreen
import com.forestglass.ui.screens.ProgressScreen
import com.forestglass.ui.screens.SearchScreen
import com.forestglass.ui.theme.ForestMotion
import com.forestglass.ui.theme.ForestTokens
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            ForestGlassRoot(activity = this)
        }
    }
}

@Composable
private fun ForestGlassRoot(activity: ComponentActivity) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("forest_prefs", Context.MODE_PRIVATE) }
    var emergencyPin by remember {
        mutableStateOf(prefs.getString("emergency_pin", DEFAULT_EMERGENCY_PIN) ?: DEFAULT_EMERGENCY_PIN)
    }

    val tracker = remember { StudyTracker(context) }
    val stats by tracker.statsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val statsMap = remember(stats) { stats.associateBy { it.lessonId } }
    val suggestion = remember(stats) { tracker.pickSuggestedReview(stats) }
    val laws = remember { PhysicsIndex.loadFromAssets(context) }

    var route by rememberSaveable { mutableStateOf(AppRoute.HOME) }
    var selectedSubject by rememberSaveable { mutableStateOf(Subject.PHYSICS) }
    var selectedLessonId by rememberSaveable {
        mutableStateOf(LessonRepository.allLessons.first().id)
    }
    var isVideoFullscreen by rememberSaveable { mutableStateOf(false) }
    var crashReport by remember { mutableStateOf(CrashLog.read(context)) }

    var tiltState by remember { mutableStateOf(TiltState()) }
    val tiltSensor = remember {
        TiltSensor(context) { x, y -> tiltState = TiltState(x, y) }
    }
    val globalLight = remember { GlobalLightState() }

    LaunchedEffect(globalLight.isPressed, globalLight.intensity > 0.002f) {
        var lastNs = 0L
        while (globalLight.isPressed > 0.5f || globalLight.intensity > 0.002f) {
            withFrameNanos { ns ->
                val dt = if (lastNs == 0L) 1f / 60f else (ns - lastNs) / 1_000_000_000f
                lastNs = ns
                globalLight.step(dt)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> tiltSensor.start()
                Lifecycle.Event.ON_PAUSE -> tiltSensor.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tiltSensor.stop()
        }
    }

    LaunchedEffect(isVideoFullscreen, route) {
        val activeFullscreen = isVideoFullscreen && route == AppRoute.LESSONS
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (activeFullscreen) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
            if (route != AppRoute.FOCUS) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val currentLesson = remember(selectedLessonId, selectedSubject) {
        LessonRepository.findById(selectedLessonId)
            ?: LessonRepository.bySubject(selectedSubject).firstOrNull()
            ?: LessonRepository.allLessons.first()
    }

    BackHandler(enabled = route == AppRoute.SEARCH || route == AppRoute.LESSONS) {
        if (isVideoFullscreen) {
            isVideoFullscreen = false
        } else {
            route = AppRoute.HOME
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalTiltState provides tiltState,
        LocalGlobalLight provides globalLight
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ForestTokens.BlackOled)
                .drawBehind {
                    val g = globalLight.intensity
                    if (g > 0.005f && route != AppRoute.DECOY) {
                        val radius = size.minDimension * 0.52f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ForestTokens.MintGlow.copy(alpha = g * 0.22f),
                                    ForestTokens.MintGlow.copy(alpha = g * 0.06f),
                                    Color.Transparent
                                ),
                                center = Offset(globalLight.screenX, globalLight.screenY),
                                radius = radius
                            ),
                            radius = radius,
                            center = Offset(globalLight.screenX, globalLight.screenY)
                        )
                    }
                }
        ) {
            val showBottomBar = !isVideoFullscreen &&
                route in setOf(AppRoute.HOME, AppRoute.LESSONS, AppRoute.PROGRESS, AppRoute.ACCOUNT)

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (showBottomBar) {
                        GlassBottomBar(
                            currentRoute = route,
                            onSelectTab = { tab ->
                                isVideoFullscreen = false
                                route = tab.route
                            }
                        )
                    }
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = route,
                    transitionSpec = { ForestMotion.screenTransition },
                    label = "rootNavigation"
                ) { activeRoute ->
                    when (activeRoute) {
                        AppRoute.HOME -> HomeScreen(
                            suggestion = suggestion,
                            onOpenSearch = { route = AppRoute.SEARCH },
                            onOpenLesson = { lesson ->
                                selectedSubject = lesson.subject
                                selectedLessonId = lesson.id
                                route = AppRoute.LESSONS
                            },
                            onSelectSubject = { subject ->
                                selectedSubject = subject
                                selectedLessonId = LessonRepository.bySubject(subject).firstOrNull()?.id
                                    ?: LessonRepository.allLessons.first().id
                                route = AppRoute.LESSONS
                            },
                            bottomPadding = innerPadding
                        )

                        AppRoute.LESSONS -> LessonScreen(
                            subject = selectedSubject,
                            currentLesson = currentLesson,
                            statsMap = statsMap,
                            isFullscreen = isVideoFullscreen,
                            onFullscreenChange = { isVideoFullscreen = it },
                            onSelectSubject = { subject ->
                                selectedSubject = subject
                                selectedLessonId = LessonRepository.bySubject(subject).firstOrNull()?.id
                                    ?: selectedLessonId
                            },
                            onSelectLesson = { lesson -> selectedLessonId = lesson.id },
                            onStartFocus = {
                                isVideoFullscreen = false
                                route = AppRoute.FOCUS
                            },
                            onTickStudySecond = { lesson ->
                                scope.launch {
                                    tracker.recordStudyProgress(lesson, addedSeconds = 15L)
                                }
                            },
                            onMarkCompleted = { lesson ->
                                scope.launch {
                                    tracker.recordStudyProgress(lesson, addedSeconds = 0L, markCompleted = true)
                                }
                            },
                            onBack = { route = AppRoute.HOME },
                            bottomPadding = if (isVideoFullscreen) PaddingValues(0.dp) else innerPadding
                        )

                        AppRoute.SEARCH -> SearchScreen(
                            laws = laws,
                            onOpenLessonById = { lessonId ->
                                LessonRepository.findById(lessonId)?.let { lesson ->
                                    selectedSubject = lesson.subject
                                    selectedLessonId = lesson.id
                                    route = AppRoute.LESSONS
                                }
                            },
                            onBack = { route = AppRoute.HOME }
                        )

                        AppRoute.PROGRESS -> ProgressScreen(
                            stats = stats,
                            bottomPadding = innerPadding
                        )

                        AppRoute.ACCOUNT -> AccountScreen(
                            currentPin = emergencyPin,
                            onUpdatePin = { newPin ->
                                emergencyPin = newPin
                                prefs.edit().putString("emergency_pin", newPin).apply()
                            },
                            onResetStats = {
                                scope.launch { tracker.resetAll() }
                            },
                            bottomPadding = innerPadding
                        )

                        AppRoute.FOCUS -> FocusScreen(
                            emergencyPin = emergencyPin,
                            onExitFocus = { route = AppRoute.LESSONS },
                            onOpenDecoy = { route = AppRoute.DECOY }
                        )

                        AppRoute.DECOY -> DecoyScreen(
                            emergencyPin = emergencyPin,
                            onReturnToFocus = { route = AppRoute.FOCUS }
                        )
                    }
                }
            }

            if (crashReport != null) {
                CrashReportOverlay(
                    report = crashReport!!,
                    onCopy = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("ForestGlass Crash", crashReport))
                    },
                    onDismiss = {
                        CrashLog.clear(context)
                        crashReport = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CrashReportOverlay(
    report: String,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            cornerRadius = 24.dp,
            interactive = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "تم رصد عطل سابق",
                    color = ForestTokens.Warning,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = report.take(900),
                    color = ForestTokens.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .height(180.dp)
                        .verticalScroll(rememberScrollState())
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        cornerRadius = 14.dp,
                        tier = GlassTier.Lite,
                        selected = true,
                        onClick = onCopy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                            Text("نسخ التقرير", color = ForestTokens.MintGlow, fontSize = 14.sp)
                        }
                    }
                    GlassCard(
                        cornerRadius = 14.dp,
                        tier = GlassTier.Lite,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                            Text("إغلاق", color = ForestTokens.TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
