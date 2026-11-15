package com.forestglass.player

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.forestglass.CrashLog
import com.forestglass.ui.glass.GlassCard
import com.forestglass.ui.glass.GlassTier
import com.forestglass.ui.theme.FIcon
import com.forestglass.ui.theme.ForestIcon
import com.forestglass.ui.theme.ForestTokens

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeLessonPlayer(
    youtubeId: String,
    lessonTitle: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reloadKey by remember { mutableIntStateOf(0) }
    var isLoading by remember(youtubeId, reloadKey) { mutableStateOf(youtubeId.isNotBlank()) }
    var hasError by remember(youtubeId, reloadKey) { mutableStateOf(false) }

    val webView = remember {
        runCatching {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.BLACK)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadsImagesAutomatically = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webChromeClient = WebChromeClient()
            }
        }.onFailure {
            CrashLog.recordNonFatal(context, "WebViewInit", it)
        }.getOrNull()
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                webView?.stopLoading()
                webView?.loadUrl("about:blank")
                webView?.destroy()
            }
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        when {
            youtubeId.isBlank() -> {
                EmptyVideoNotice(
                    lessonTitle = lessonTitle,
                    modifier = Modifier.fillMaxSize()
                )
            }

            webView == null -> {
                ErrorVideoNotice(
                    message = "مكوّن WebView غير متاح على هذا الجهاز",
                    onRetry = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            hasError -> {
                ErrorVideoNotice(
                    message = "تعذّر تحميل الفيديو، تأكد من اتصال الإنترنت",
                    onRetry = {
                        hasError = false
                        isLoading = true
                        reloadKey++
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                AndroidView(
                    factory = {
                        webView.apply {
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        hasError = true
                                    }
                                }
                            }
                        }
                    },
                    update = { view ->
                        val targetTag = "$youtubeId#$reloadKey"
                        if (view.tag != targetTag) {
                            view.tag = targetTag
                            val html = buildYouTubeEmbedHtml(youtubeId)
                            view.loadDataWithBaseURL(
                                "https://www.youtube-nocookie.com",
                                html,
                                "text/html",
                                "utf-8",
                                null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ForestTokens.MintGlow,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }

        // زر ملء الشاشة / الخروج من ملء الشاشة داخل زاوية المشغل
        GlassCard(
            cornerRadius = 14.dp,
            tier = GlassTier.Lite,
            onClick = onToggleFullscreen,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ForestIcon(
                    icon = if (isFullscreen) FIcon.EXIT_FULLSCREEN else FIcon.FULLSCREEN,
                    size = 18.dp,
                    tint = ForestTokens.TextPrimary
                )
                if (isFullscreen) {
                    Text(
                        text = "تصغير",
                        color = ForestTokens.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyVideoNotice(lessonTitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(ForestTokens.MintBadge, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ForestIcon(FIcon.PLAY, size = 22.dp, tint = ForestTokens.MintGlow)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = lessonTitle,
            color = ForestTokens.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "أضف معرّف فيديو يوتيوب لهذا الدرس في Lessons.kt لبدء المشاهدة",
            color = ForestTokens.TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorVideoNotice(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ForestIcon(FIcon.WARNING, size = 28.dp, tint = ForestTokens.Warning)
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = ForestTokens.TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            GlassCard(
                cornerRadius = 14.dp,
                tier = GlassTier.Lite,
                onClick = onRetry
            ) {
                Text(
                    text = "إعادة المحاولة",
                    color = ForestTokens.MintGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun buildYouTubeEmbedHtml(videoId: String): String {
    val safeId = videoId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
            <style>
                html, body { margin:0; padding:0; width:100%; height:100%; background:#000; overflow:hidden; }
                iframe { border:0; width:100%; height:100%; }
            </style>
        </head>
        <body>
            <iframe
                src="https://www.youtube-nocookie.com/embed/$safeId?playsinline=1&rel=0&modestbranding=1"
                allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
                allowfullscreen>
            </iframe>
        </body>
        </html>
    """.trimIndent()
}
