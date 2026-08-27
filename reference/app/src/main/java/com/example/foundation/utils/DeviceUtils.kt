package com.example.foundation.utils

import android.content.Context
import android.content.res.Configuration

/**
 * Device configuration, orientation, and screen metrics inspection utilities.
 */
object DeviceUtils {

    /**
     * Checks if device is currently in landscape orientation.
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * Checks if device is a tablet or large screen (sw600dp+).
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    /**
     * Calculates base keyboard height in pixels based on screen height and orientation.
     */
    fun getBaseKeyboardHeight(context: Context, scale: Float = 1.0f): Int {
        val dm = context.resources.displayMetrics
        val screenHeight = dm.heightPixels
        val isLand = isLandscape(context)

        val targetRatio = if (isLand) 0.50f else 0.35f
        val calculated = (screenHeight * targetRatio * scale).toInt()
        val minHeight = ResourceUtils.dpToPx(context, if (isLand) 160f else 200f).toInt()
        val maxHeight = ResourceUtils.dpToPx(context, if (isLand) 260f else 340f).toInt()

        return calculated.coerceIn(minHeight, maxHeight)
    }
}
