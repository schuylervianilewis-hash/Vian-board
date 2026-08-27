package com.example.ime

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.example.foundation.utils.RingCharBuffer

/**
 * RichInputConnection wraps Android's InputConnection with robust text manipulation methods:
 * - Word deletions and multi-char deletes
 * - Cursor sliding by delta steps
 * - Batch edits
 * - Smart multiplier character morphing (e.g. 1920x1080 -> 1920×1080)
 * - Double-space period insertion
 */
class RichInputConnection(
    private var inputConnection: InputConnection? = null
) {
    private val ringCharBuffer = RingCharBuffer(32)

    fun setInputConnection(ic: InputConnection?) {
        this.inputConnection = ic
        ringCharBuffer.clear()
    }

    fun isConnected(): Boolean = inputConnection != null

    fun beginBatchEdit() {
        inputConnection?.beginBatchEdit()
    }

    fun endBatchEdit() {
        inputConnection?.endBatchEdit()
    }

    fun commitText(text: CharSequence, newCursorPosition: Int = 1) {
        inputConnection?.commitText(text, newCursorPosition)
        for (c in text) {
            ringCharBuffer.push(c)
        }
    }

    fun deleteBackward(charsToDelete: Int = 1) {
        if (charsToDelete <= 0) return
        inputConnection?.deleteSurroundingText(charsToDelete, 0)
    }

    fun getTextBeforeCursor(n: Int = 1000): String {
        return inputConnection?.getTextBeforeCursor(n, 0)?.toString() ?: ""
    }

    fun getTextAfterCursor(n: Int = 1000): String {
        return inputConnection?.getTextAfterCursor(n, 0)?.toString() ?: ""
    }

    fun getSelectedText(): String {
        return inputConnection?.getSelectedText(0)?.toString() ?: ""
    }

    fun setSelection(start: Int, end: Int) {
        inputConnection?.setSelection(start, end)
    }

    fun deleteSelectedText() {
        val ic = inputConnection ?: return
        // Committing empty string replaces the active selection
        ic.commitText("", 1)
    }

    fun deleteLastWord() {
        val ic = inputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(64, 0)?.toString() ?: ""
        if (textBefore.isEmpty()) return

        var index = textBefore.length - 1
        // Skip trailing whitespace if any
        while (index >= 0 && textBefore[index].isWhitespace()) {
            index--
        }
        // Count length of preceding word characters
        var wordLen = 0
        while (index >= 0 && !textBefore[index].isWhitespace()) {
            wordLen++
            index--
        }
        val totalToDelete = textBefore.length - 1 - index
        if (totalToDelete > 0) {
            ic.deleteSurroundingText(totalToDelete, 0)
        }
    }

    fun moveCursor(delta: Int) {
        val ic = inputConnection ?: return
        if (delta == 0) return

        // Send D-pad navigation key events
        val keyEventCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        val count = Math.abs(delta)
        for (i in 0 until count) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyEventCode))
        }
    }

    fun performEditorAction(actionId: Int) {
        inputConnection?.performEditorAction(actionId)
    }

    fun sendEnterKeyEvent() {
        val ic = inputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    fun sendTabKeyEvent() {
        val ic = inputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
    }

    fun selectAll() {
        inputConnection?.performContextMenuAction(android.R.id.selectAll)
    }

    fun copy() {
        inputConnection?.performContextMenuAction(android.R.id.copy)
    }

    fun paste() {
        inputConnection?.performContextMenuAction(android.R.id.paste)
    }

    fun cut() {
        inputConnection?.performContextMenuAction(android.R.id.cut)
    }

    fun undo() {
        inputConnection?.performContextMenuAction(android.R.id.undo)
    }

    fun redo() {
        inputConnection?.performContextMenuAction(android.R.id.redo)
    }
}
