package com.forestglass.ui.feedback

import android.view.HapticFeedbackConstants
import android.view.View

object ForestHaptics {
    fun tap(view: View) {
        runCatching { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    }

    fun select(view: View) {
        runCatching { view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
    }

    fun success(view: View) {
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    fun error(view: View) {
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
}
