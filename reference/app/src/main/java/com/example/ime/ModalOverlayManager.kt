package com.example.ime

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.foundation.common.Constants
import com.example.foundation.utils.ResourceUtils

/**
 * ModalOverlayManager hosts in-keyboard modal sheets (Clipboard, Prompts, Desktop Nav, Emoji, Vault)
 * with the mandatory Unified 4-Button Bottom Bar [ABC] [SPACE] [⌫] [↵].
 */
class ModalOverlayManager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class ModalType {
        NONE,
        CLIPBOARD,
        PROMPT_LIST,
        DESKTOP_NAV,
        EMOJI,
        VAULT,
        VOICE_INPUT
    }

    interface ModalActionListener {
        fun onDismissModal()
        fun onBottomBarAction(code: Int)
        fun onPasteItem(content: String)
    }

    var listener: ModalActionListener? = null
    var currentModal: ModalType = ModalType.NONE
        private set

    private val contentContainer = FrameLayout(context)
    private val bottomBar = LinearLayout(context)

    init {
        setBackgroundColor(Color.parseColor("#11111B"))
        setupLayout()
        visibility = GONE
    }

    private fun setupLayout() {
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // 1. Modal Content Viewport
        contentContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        )
        rootLayout.addView(contentContainer)

        // 2. Unified 4-Button Bottom Bar: [ABC] [SPACE] [⌫] [↵]
        bottomBar.apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ResourceUtils.dpToPx(context, 48f).toInt()
            )
            setBackgroundColor(Color.parseColor("#181825"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ResourceUtils.dpToPx(context, 4f).toInt(), 0, ResourceUtils.dpToPx(context, 4f).toInt(), 0)
        }

        val buttons = listOf(
            Triple("ABC", Constants.CODE_SWITCH_ALPHA_SYMBOL, 1.4f),
            Triple("SPACE", Constants.CODE_SPACE, 4.6f),
            Triple("⌫", Constants.CODE_DELETE, 1.4f),
            Triple("↵", Constants.CODE_ENTER, 1.4f)
        )

        for ((label, code, weight) in buttons) {
            val btn = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight).apply {
                    setMargins(ResourceUtils.dpToPx(context, 2f).toInt(), ResourceUtils.dpToPx(context, 4f).toInt(), ResourceUtils.dpToPx(context, 2f).toInt(), ResourceUtils.dpToPx(context, 4f).toInt())
                }
                text = label
                gravity = Gravity.CENTER
                textSize = 14f
                setTextColor(Color.parseColor("#CDD6F4"))
                setBackgroundColor(Color.parseColor("#313244"))
                setOnClickListener {
                    if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
                        dismiss()
                    } else {
                        listener?.onBottomBarAction(code)
                    }
                }
            }
            bottomBar.addView(btn)
        }

        rootLayout.addView(bottomBar)
        addView(rootLayout)
    }

    fun showModal(modalType: ModalType, customContentView: View? = null) {
        currentModal = modalType
        contentContainer.removeAllViews()

        if (customContentView != null) {
            contentContainer.addView(customContentView)
        } else {
            // Default placeholder message for sheet
            val tv = TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER
                textSize = 16f
                setTextColor(Color.parseColor("#BAC2DE"))
                text = "Vian Board — ${modalType.name.replace('_', ' ')}"
            }
            contentContainer.addView(tv)
        }

        visibility = VISIBLE
    }

    fun dismiss() {
        currentModal = ModalType.NONE
        contentContainer.removeAllViews()
        visibility = GONE
        listener?.onDismissModal()
    }

    fun isModalShowing(): Boolean = visibility == VISIBLE
}
