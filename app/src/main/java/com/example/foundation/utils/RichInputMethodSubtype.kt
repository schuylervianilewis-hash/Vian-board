package com.example.foundation.utils

import android.view.inputmethod.InputMethodSubtype
import java.util.Locale

/**
 * RichInputMethodSubtype encapsulates an Android system InputMethodSubtype
 * with typed Vian Board configuration extensions.
 */
class RichInputMethodSubtype(
    val rawSubtype: InputMethodSubtype?,
    val subtype: Subtype
) {
    val locale: Locale get() = subtype.primaryLocale
    val secondaryLocale: Locale? get() = subtype.secondaryLocale
    val layoutName: String get() = subtype.keyboardLayoutName

    val displayName: String get() = subtype.getDisplayName()

    companion object {
        fun createDefault(): RichInputMethodSubtype {
            return RichInputMethodSubtype(
                rawSubtype = null,
                subtype = Subtype.DEFAULT
            )
        }
    }
}
