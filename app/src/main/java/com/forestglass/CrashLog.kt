package com.forestglass

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLog {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                writeReport(appContext, "Uncaught on ${thread.name}", throwable)
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun recordNonFatal(context: Context, tag: String, throwable: Throwable) {
        runCatching {
            writeReport(context.applicationContext, tag, throwable)
        }
    }

    fun read(context: Context): String? = runCatching {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        if (file.exists() && file.length() > 0L) file.readText() else null
    }.getOrNull()

    fun clear(context: Context) {
        runCatching {
            File(context.applicationContext.filesDir, FILE_NAME).delete()
        }
    }

    private fun writeReport(context: Context, tag: String, throwable: Throwable) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val text = buildString {
            appendLine("ForestGlass Report ($stamp)")
            appendLine("Tag: $tag")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("---")
            appendLine(throwable.stackTraceToString())
        }
        File(context.filesDir, FILE_NAME).writeText(text)
    }
}
