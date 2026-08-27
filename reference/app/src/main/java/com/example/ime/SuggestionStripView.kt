package com.example.ime

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.engine.core.SuggestedWordInfo
import com.example.engine.core.SuggestedWords
import com.example.foundation.utils.ResourceUtils

/**
 * SuggestionStripView renders the top candidate bar above the keyboard.
 * Displays 3 primary slots: [Left: Raw/Typed] [Center: Bold Auto-Correct] [Right: Prediction/Next]
 * Plus fast-action buttons when empty (Select All, Copy, Paste).
 */
class SuggestionStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface SuggestionStripListener {
        fun onSuggestionClicked(wordInfo: SuggestedWordInfo)
        fun onFastActionClicked(actionCode: Int)
    }

    var listener: SuggestionStripListener? = null

    private val wordViews = mutableListOf<TextView>()
    private val fastActionViews = mutableListOf<TextView>()

    private val primaryTextColor = Color.parseColor("#CDD6F4")
    private val autoCorrectColor = Color.parseColor("#89B4FA")
    private val dimTextColor = Color.parseColor("#6C7086")
    private val stripBgColor = Color.parseColor("#181825")

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(stripBgColor)
        setPadding(
            ResourceUtils.dpToPx(context, 8f).toInt(),
            0,
            ResourceUtils.dpToPx(context, 8f).toInt(),
            0
        )
        setupViews()
    }

    private fun setupViews() {
        removeAllViews()
        wordViews.clear()

        // Create 3 equal-weighted suggestion slots
        for (i in 0 until 3) {
            val tv = TextView(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f)
                gravity = Gravity.CENTER
                textSize = 15f
                setTextColor(primaryTextColor)
                isClickable = true
                isFocusable = true
                maxLines = 1
                setOnClickListener {
                    val tag = tag as? SuggestedWordInfo
                    if (tag != null) {
                        listener?.onSuggestionClicked(tag)
                    }
                }
            }
            wordViews.add(tv)
            addView(tv)
        }
    }

    /**
     * Updates the suggestion strip with new candidates.
     */
    fun setSuggestions(suggestedWords: SuggestedWords) {
        if (suggestedWords.isEmpty) {
            // Show fast actions or clear
            wordViews.forEach {
                it.text = ""
                it.tag = null
                it.isClickable = false
            }
            return
        }

        val all = suggestedWords.getAll()
        for (i in 0 until 3) {
            val tv = wordViews[i]
            if (i < all.size) {
                val info = all[i]
                tv.text = info.word
                tv.tag = info
                tv.isClickable = true

                if (info.isAutoCorrect) {
                    tv.setTextColor(autoCorrectColor)
                    tv.typeface = Typeface.DEFAULT_BOLD
                } else if (info.isTypedWord) {
                    tv.setTextColor(dimTextColor)
                    tv.typeface = Typeface.DEFAULT
                } else {
                    tv.setTextColor(primaryTextColor)
                    tv.typeface = Typeface.DEFAULT
                }
            } else {
                tv.text = ""
                tv.tag = null
                tv.isClickable = false
            }
        }
    }

    fun clear() {
        wordViews.forEach {
            it.text = ""
            it.tag = null
            it.isClickable = false
        }
    }
}
