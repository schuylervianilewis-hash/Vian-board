package com.example.foundation.utils

import android.content.Context
import android.util.TypedValue

/**
 * Android resource conversion utilities for DP, SP, and PX calculations.
 */
object ResourceUtils {

    /**
     * Converts DP units to physical Pixels.
     */
    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    /**
     * Converts SP units to physical Pixels.
     */
    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }

    /**
     * Converts physical Pixels to DP units.
     */
    fun pxToDp(context: Context, px: Float): Float {
        val density = context.resources.displayMetrics.density
        return if (density > 0) px / density else px
    }
}
