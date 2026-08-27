package com.example.voice.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.foundation.utils.ResourceUtils

/**
 * Animated real-time decibel audio waveform visualizer and voice status panel.
 */
class VoiceInputOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface VoiceOverlayListener {
        fun onStopListeningClicked()
        fun onCancelClicked()
    }

    var listener: VoiceOverlayListener? = null

    private val statusTextView: TextView
    private val transcriptionTextView: TextView
    private val waveformView: AudioWaveformCanvasView

    init {
        setBackgroundColor(Color.parseColor("#11111B"))

        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(
                ResourceUtils.dpToPx(context, 16f).toInt(),
                ResourceUtils.dpToPx(context, 16f).toInt(),
                ResourceUtils.dpToPx(context, 16f).toInt(),
                ResourceUtils.dpToPx(context, 16f).toInt()
            )
        }

        statusTextView = TextView(context).apply {
            text = "Listening..."
            textSize = 15f
            setTextColor(Color.parseColor("#89B4FA"))
            gravity = Gravity.CENTER
        }
        contentLayout.addView(statusTextView)

        waveformView = AudioWaveformCanvasView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ResourceUtils.dpToPx(context, 64f).toInt()
            ).apply {
                topMargin = ResourceUtils.dpToPx(context, 8f).toInt()
                bottomMargin = ResourceUtils.dpToPx(context, 8f).toInt()
            }
        }
        contentLayout.addView(waveformView)

        transcriptionTextView = TextView(context).apply {
            text = "Speak now to transcribe audio..."
            textSize = 13f
            setTextColor(Color.parseColor("#BAC2DE"))
            gravity = Gravity.CENTER
        }
        contentLayout.addView(transcriptionTextView)

        addView(contentLayout)
    }

    fun updateRmsDb(rmsDb: Float) {
        waveformView.addAmplitude(rmsDb)
    }

    fun updateTranscription(text: String, isFinal: Boolean = false) {
        transcriptionTextView.text = if (text.isEmpty()) "Listening..." else text
        if (isFinal) {
            statusTextView.text = "Transcription Done"
        } else {
            statusTextView.text = "Listening..."
        }
    }

    fun setStatusText(status: String) {
        statusTextView.text = status
    }

    private class AudioWaveformCanvasView(context: Context) : View(context) {
        private val amplitudes = FloatArray(32)
        private var headIndex = 0

        private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#89B4FA")
            style = Paint.Style.FILL
        }

        fun addAmplitude(rmsDb: Float) {
            amplitudes[headIndex] = rmsDb
            headIndex = (headIndex + 1) % amplitudes.size
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0 || h <= 0) return

            val barCount = amplitudes.size
            val barWidth = w / (barCount * 1.5f)
            val spacing = barWidth * 0.5f

            var currentX = spacing / 2
            val centerY = h / 2

            for (i in 0 until barCount) {
                val idx = (headIndex + i) % barCount
                val normAmp = Math.min(1.0f, Math.max(0.05f, amplitudes[idx] / 100f))
                val barHeight = Math.max(ResourceUtils.dpToPx(context, 4f), h * normAmp * 0.8f)

                val left = currentX
                val top = centerY - barHeight / 2
                val right = left + barWidth
                val bottom = centerY + barHeight / 2

                canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, barPaint)
                currentX += barWidth + spacing
            }
        }
    }
}
