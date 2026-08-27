package com.example.keyboard.internal

import android.view.MotionEvent
import com.example.foundation.common.Constants

/**
 * Handles zero-latency multi-touch pointer tracking and micro-gestures:
 * 1. Spacebar Glide (Cursor sliding $\Delta X$)
 * 2. Backspace Swipe (Word-by-word deletion $\Delta X$)
 * 3. Long-press popup dispatch
 */
class PointerTracker(
    private val listener: KeyboardActionListener
) {
    interface KeyboardActionListener {
        fun onKeyPress(key: Key)
        fun onKeyRelease(key: Key)
        fun onKeyLongPress(key: Key)
        fun onSpacebarSlide(deltaX: Float)
        fun onBackspaceSwipe(deltaX: Float)
    }

    private var activeKey: Key? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isSlidingSpace = false
    private var isSwipingBackspace = false

    fun processTouchEvent(event: MotionEvent, keys: List<Key>): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                initialTouchX = x
                initialTouchY = y
                isSlidingSpace = false
                isSwipingBackspace = false

                val key = findKey(x, y, keys)
                if (key != null) {
                    activeKey = key
                    key.isPressed = true
                    listener.onKeyPress(key)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - initialTouchX
                val key = activeKey

                if (key != null) {
                    if (key.code == Constants.CODE_SPACE && Math.abs(deltaX) > Constants.SPACEBAR_CURSOR_SLIDE_THRESHOLD_DP) {
                        isSlidingSpace = true
                        listener.onSpacebarSlide(deltaX)
                        initialTouchX = x // Continuous sliding
                        return true
                    }

                    if (key.code == Constants.CODE_DELETE && deltaX < -Constants.BACKSPACE_SWIPE_WORD_THRESHOLD_DP) {
                        isSwipingBackspace = true
                        listener.onBackspaceSwipe(deltaX)
                        initialTouchX = x // Step deletion
                        return true
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val key = activeKey
                if (key != null) {
                    key.isPressed = false
                    if (!isSlidingSpace && !isSwipingBackspace) {
                        listener.onKeyRelease(key)
                    }
                    activeKey = null
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                activeKey?.isPressed = false
                activeKey = null
                return true
            }
        }
        return false
    }

    private fun findKey(x: Float, y: Float, keys: List<Key>): Key? {
        for (k in keys) {
            if (k.contains(x, y)) return k
        }
        return null
    }
}
