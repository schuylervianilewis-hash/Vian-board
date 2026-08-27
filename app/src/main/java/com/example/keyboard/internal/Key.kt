package com.example.keyboard.internal

import android.graphics.RectF

/**
 * Geometric and visual representation of a single touchable key on the keyboard canvas.
 */
data class Key(
    val code: Int,
    val label: String,
    val hintLabel: String? = null,
    val bounds: RectF = RectF(),
    val isFunctional: Boolean = false,
    val moreKeys: List<String> = emptyList()
) {
    var isPressed: Boolean = false

    val x: Float get() = bounds.left
    val y: Float get() = bounds.top
    val width: Float get() = bounds.width()
    val height: Float get() = bounds.height()

    fun contains(touchX: Float, touchY: Float): Boolean {
        return bounds.contains(touchX, touchY)
    }
}
