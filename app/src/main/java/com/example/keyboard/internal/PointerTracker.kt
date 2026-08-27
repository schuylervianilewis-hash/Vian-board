package com.example.keyboard.internal

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import com.example.foundation.common.Constants

/**
 * Handles zero-latency multi-touch pointer tracking and micro-gestures:
 * 1. Spacebar Glide (Cursor sliding ΔX)
 * 2. Backspace Swipe (Word-by-word deletion ΔX)
 * 3. Long-press popup dispatch & CapsLock trigger
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
        fun onBackspaceSwipeRelease()
        fun onMoreKeySelected(candidate: String)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var activeKey: Key? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isSlidingSpace = false
    private var isSwipingBackspace = false
    private var isLongPressTriggered = false
    var isShowingMoreKeys = false

    private val longPressRunnable = Runnable {
        activeKey?.let { key ->
            isLongPressTriggered = true
            listener.onKeyLongPress(key)
        }
    }

    fun processTouchEvent(event: MotionEvent, keys: List<Key>): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                initialTouchX = x
                initialTouchY = y
                isSlidingSpace = false
                isSwipingBackspace = false
                isLongPressTriggered = false
                isShowingMoreKeys = false

                val key = findKey(x, y, keys)
                if (key != null) {
                    activeKey = key
                    key.isPressed = true
                    listener.onKeyPress(key)
                    handler.postDelayed(longPressRunnable, Constants.DEFAULT_LONG_PRESS_TIMEOUT_MS)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - initialTouchX
                val deltaY = y - initialTouchY
                val key = activeKey

                if (isShowingMoreKeys) {
                    // Touch is being dragged across more keys popup
                    return true
                }

                if (key != null) {
                    // Cancel long press if moved significantly
                    if (Math.hypot(deltaX.toDouble(), deltaY.toDouble()) > 20) {
                        handler.removeCallbacks(longPressRunnable)
                    }

                    if (key.code == Constants.CODE_SPACE && (isSlidingSpace || Math.abs(deltaX) > Constants.SPACEBAR_CURSOR_SLIDE_THRESHOLD_DP)) {
                        isSlidingSpace = true
                        listener.onSpacebarSlide(deltaX)
                        initialTouchX = x // Continuous sliding delta
                        return true
                    }

                    if (key.code == Constants.CODE_DELETE && (isSwipingBackspace || deltaX < -Constants.BACKSPACE_SWIPE_WORD_THRESHOLD_DP)) {
                        isSwipingBackspace = true
                        listener.onBackspaceSwipe(deltaX)
                        return true
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                handler.removeCallbacks(longPressRunnable)
                val key = activeKey
                if (key != null) {
                    key.isPressed = false
                    if (isSwipingBackspace) {
                        listener.onBackspaceSwipeRelease()
                    } else if (!isSlidingSpace && !isLongPressTriggered && !isShowingMoreKeys) {
                        listener.onKeyRelease(key)
                    }
                    activeKey = null
                }
                isSlidingSpace = false
                isSwipingBackspace = false
                isShowingMoreKeys = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                if (isSwipingBackspace) {
                    listener.onBackspaceSwipeRelease()
                }
                activeKey?.isPressed = false
                activeKey = null
                isSlidingSpace = false
                isSwipingBackspace = false
                isShowingMoreKeys = false
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

