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
        color = Color.parseColor("#313244")
    }

    private val keyBgPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#585B70")
    }

    private val keyFunctionalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1E1E2E")
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
    }

    private val numberPadDigitLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 26f)
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A6ADC8")
        textAlign = Paint.Align.RIGHT
        textSize = ResourceUtils.spToPx(context, 10f)
    }

    private val shiftLockedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#89B4FA")
        strokeWidth = ResourceUtils.dpToPx(context, 2f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val shiftActiveLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#89B4FA")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
    }

    // More Keys Popup Paints
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#181825")
        style = Paint.Style.FILL
    }

    private val popupBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#45475A")
        strokeWidth = ResourceUtils.dpToPx(context, 1.5f)
        style = Paint.Style.STROKE
    }

    private val popupItemSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#89B4FA")
        style = Paint.Style.FILL
    }

    private val popupItemTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CDD6F4")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
    }

    private val popupItemSelectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#11111B")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
        isFakeBoldText = true
    }

    private data class MoreKeyPopup(
        val parentKey: Key,
        val items: List<String>,
        val itemBounds: List<RectF>,
        val popupBounds: RectF,
        var selectedIndex: Int
    )

    private var activeMoreKeyPopup: MoreKeyPopup? = null

    private val keyCornerRadius = ResourceUtils.dpToPx(context, 6f)
    private val keyMarginPx = ResourceUtils.dpToPx(context, 2.5f)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setBackgroundColor(Color.parseColor("#181825"))
    }

    fun setLayoutMode(mode: KeyboardLayoutBuilder.LayoutMode) {
        this.layoutMode = mode
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun setCurrencySymbol(currency: String) {
        this.currencySymbol = currency
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun setShowNumberRow(show: Boolean) {
        this.showNumberRow = show
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun showMoreKeysPopup(key: Key): Boolean {
        val isUpper = layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER || layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK
        val candidates = MoreKeySpec.getMoreKeysFor(key.label, isUpper)
        if (candidates.isEmpty()) return false

        val itemWidth = maxOf(key.bounds.width(), ResourceUtils.dpToPx(context, 38f))
        val itemHeight = key.bounds.height()
        val totalWidth = itemWidth * candidates.size
        val minMargin = ResourceUtils.dpToPx(context, 4f)
        val startX = (key.bounds.centerX() - totalWidth / 2f).coerceIn(minMargin, maxOf(minMargin, width - totalWidth - minMargin))
        val topY = (key.bounds.top - itemHeight - ResourceUtils.dpToPx(context, 6f)).coerceAtLeast(minMargin)

        val itemBounds = candidates.indices.map { i ->
            RectF(startX + i * itemWidth, topY, startX + (i + 1) * itemWidth, topY + itemHeight)
        }
        val popupBounds = RectF(startX, topY, startX + totalWidth, topY + itemHeight)

        activeMoreKeyPopup = MoreKeyPopup(
            parentKey = key,
            items = candidates,
            itemBounds = itemBounds,
            popupBounds = popupBounds,
            selectedIndex = 0
        )
        invalidate()
        return true
    }

    fun handleMoreKeysMove(x: Float, y: Float) {
        val popup = activeMoreKeyPopup ?: return
        for (i in popup.itemBounds.indices) {
            val rect = popup.itemBounds[i]
            if (x >= rect.left && x <= rect.right) {
                if (popup.selectedIndex != i) {
                    popup.selectedIndex = i
                    invalidate()
                }
                return
            }
        }
    }

    fun handleMoreKeysUp(x: Float, y: Float): String? {
        val popup = activeMoreKeyPopup ?: return null
        var selected: String? = null
        if (popup.selectedIndex in popup.items.indices) {
            selected = popup.items[popup.selectedIndex]
        }
        activeMoreKeyPopup = null
        invalidate()
        return selected
    }

    fun dismissMoreKeys() {
        if (activeMoreKeyPopup != null) {
            activeMoreKeyPopup = null
            invalidate()
        }
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
                currencySymbol = currencySymbol,
                keyMargin = keyMarginPx
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

            // Determine text paint (highlight shift if active/capslock, larger size for numpad digits)
            val isShiftKey = key.code == com.example.foundation.common.Constants.CODE_SHIFT
            val isShiftActive = isShiftKey && (layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER || layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK)
            val isNumpadDigit = layoutMode == KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD && key.code in '0'.code..'9'.code
            val currentLabelPaint = when {
                isShiftActive -> shiftActiveLabelPaint
                isNumpadDigit -> numberPadDigitLabelPaint
                else -> labelPaint
            }

            // Draw Center Label
            val centerY = key.bounds.centerY() - (currentLabelPaint.descent() + currentLabelPaint.ascent()) / 2
            val displayLabel = if (isShiftKey && layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK) "⇪" else if (isShiftKey && layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER) "⇧" else key.label
            canvas.drawText(displayLabel, key.bounds.centerX(), centerY, currentLabelPaint)

            // Draw CapsLock underline bar if CapsLock is active
            if (isShiftKey && layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK) {
                val barWidth = ResourceUtils.dpToPx(context, 14f)
                val barY = key.bounds.centerY() + ResourceUtils.dpToPx(context, 10f)
                val startX = key.bounds.centerX() - barWidth / 2f
                val endX = key.bounds.centerX() + barWidth / 2f
                canvas.drawLine(startX, barY, endX, barY, shiftLockedBarPaint)
            }

            // Draw Top-Right Hint (if present)
            key.hintLabel?.let { hint ->
                val hintX = key.bounds.right - ResourceUtils.dpToPx(context, 4f)
                val hintY = key.bounds.top + ResourceUtils.dpToPx(context, 12f)
                canvas.drawText(hint, hintX, hintY, hintPaint)
            }
        }

        // Draw More Keys Popup on top if active
        activeMoreKeyPopup?.let { popup ->
            // Popup card background and border
            canvas.drawRoundRect(popup.popupBounds, keyCornerRadius * 1.5f, keyCornerRadius * 1.5f, popupBgPaint)
            canvas.drawRoundRect(popup.popupBounds, keyCornerRadius * 1.5f, keyCornerRadius * 1.5f, popupBorderPaint)

            for (i in popup.items.indices) {
                val rect = popup.itemBounds[i]
                val isSelected = i == popup.selectedIndex

                if (isSelected) {
                    canvas.drawRoundRect(rect, keyCornerRadius, keyCornerRadius, popupItemSelectedPaint)
                }

                val textPaint = if (isSelected) popupItemSelectedTextPaint else popupItemTextPaint
                val centerY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(popup.items[i], rect.centerX(), centerY, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (activeMoreKeyPopup != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    handleMoreKeysMove(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    val selected = handleMoreKeysUp(event.x, event.y)
                    if (selected != null) {
                        actionListener?.onMoreKeySelected(selected)
                    }
                    pointerTracker.isShowingMoreKeys = false
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    dismissMoreKeys()
                    pointerTracker.isShowingMoreKeys = false
                    return true
                }
            }
        }

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
        if (key.code == com.example.foundation.common.Constants.CODE_SHIFT ||
            key.code == com.example.foundation.common.Constants.CODE_DELETE ||
            key.code == com.example.foundation.common.Constants.CODE_NUMPAD) {
            actionListener?.onKeyLongPress(key)
        } else {
            if (showMoreKeysPopup(key)) {
                pointerTracker.isShowingMoreKeys = true
            } else {
                actionListener?.onKeyLongPress(key)
            }
        }
    }

    override fun onSpacebarSlide(deltaX: Float) {
        actionListener?.onSpacebarSlide(deltaX)
    }

    override fun onBackspaceSwipe(deltaX: Float) {
        actionListener?.onBackspaceSwipe(deltaX)
    }

    override fun onBackspaceSwipeRelease() {
        actionListener?.onBackspaceSwipeRelease()
    }

    override fun onMoreKeySelected(candidate: String) {
        actionListener?.onMoreKeySelected(candidate)
    }
}
