package com.example.keyboard.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.foundation.utils.ResourceUtils

/**
 * Hardware-accelerated Canvas-based keyboard view.
 * Renders keys, top-right hint labels, press ripples, and dispatches pointer touch events.
 */
class MainKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), PointerTracker.KeyboardActionListener {

    private val layoutBuilder = KeyboardLayoutBuilder()
    private var keys: List<Key> = emptyList()
    private var layoutMode = KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER
    private var currencySymbol = "$"
    private var showNumberRow = true

    var actionListener: PointerTracker.KeyboardActionListener? = null

    private val pointerTracker = PointerTracker(this)

    // Paints
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1E1E2E")
    }

    private val keyBgPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#45475A")
    }

    private val keyFunctionalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#181825")
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C7086")
        textAlign = Paint.Align.RIGHT
        textSize = ResourceUtils.spToPx(context, 10f)
    }

    private val keyCornerRadius = ResourceUtils.dpToPx(context, 8f)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setLayoutMode(mode: KeyboardLayoutBuilder.LayoutMode) {
        this.layoutMode = mode
        rebuildKeys()
        invalidate()
    }

    fun setCurrencySymbol(currency: String) {
        this.currencySymbol = currency
        rebuildKeys()
        invalidate()
    }

    fun setShowNumberRow(show: Boolean) {
        this.showNumberRow = show
        rebuildKeys()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildKeys()
    }

    private fun rebuildKeys() {
        if (width > 0 && height > 0) {
            keys = layoutBuilder.buildKeyboard(
                width = width.toFloat(),
                height = height.toFloat(),
                mode = layoutMode,
                showNumberRow = showNumberRow,
                currencySymbol = currencySymbol
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (key in keys) {
            val paint = when {
                key.isPressed -> keyBgPressedPaint
                key.isFunctional -> keyFunctionalBgPaint
                else -> keyBgPaint
            }

            // Draw Key Background
            canvas.drawRoundRect(key.bounds, keyCornerRadius, keyCornerRadius, paint)

            // Draw Center Label
            val centerY = key.bounds.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2
            canvas.drawText(key.label, key.bounds.centerX(), centerY, labelPaint)

            // Draw Top-Right Hint (if present)
            key.hintLabel?.let { hint ->
                val hintX = key.bounds.right - ResourceUtils.dpToPx(context, 4f)
                val hintY = key.bounds.top + ResourceUtils.dpToPx(context, 12f)
                canvas.drawText(hint, hintX, hintY, hintPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = pointerTracker.processTouchEvent(event, keys)
        invalidate()
        return handled || super.onTouchEvent(event)
    }

    // PointerTracker Callbacks
    override fun onKeyPress(key: Key) {
        actionListener?.onKeyPress(key)
    }

    override fun onKeyRelease(key: Key) {
        actionListener?.onKeyRelease(key)
    }

    override fun onKeyLongPress(key: Key) {
        actionListener?.onKeyLongPress(key)
    }

    override fun onSpacebarSlide(deltaX: Float) {
        actionListener?.onSpacebarSlide(deltaX)
    }

    override fun onBackspaceSwipe(deltaX: Float) {
        actionListener?.onBackspaceSwipe(deltaX)
    }
}
