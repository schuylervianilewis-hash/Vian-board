package com.example.ime

import com.example.foundation.common.LocaleUtils
import com.example.foundation.utils.RichInputMethodSubtype
import com.example.foundation.utils.Subtype
import java.util.Locale

/**
 * Manages active subtypes, multi-language switching, and primary/secondary language toggling.
 */
class SubtypeSwitcher {

    private val availableSubtypes = mutableListOf<RichInputMethodSubtype>()
    private var currentSubtypeIndex = 0

    init {
        // Default Subtypes
        availableSubtypes.add(
            RichInputMethodSubtype(
                rawSubtype = null,
                subtype = Subtype(localeString = "en_US", keyboardLayoutName = "qwerty")
            )
        )
    }

    val currentSubtype: RichInputMethodSubtype
        get() = availableSubtypes.getOrElse(currentSubtypeIndex) { availableSubtypes[0] }

    val currentLocale: Locale
        get() = currentSubtype.locale

    fun setSubtypes(subtypes: List<Subtype>) {
        availableSubtypes.clear()
        if (subtypes.isEmpty()) {
            availableSubtypes.add(RichInputMethodSubtype.createDefault())
        } else {
            subtypes.forEach {
                availableSubtypes.add(RichInputMethodSubtype(rawSubtype = null, subtype = it))
            }
        }
        currentSubtypeIndex = 0
    }

    fun switchToNextSubtype(): RichInputMethodSubtype {
        if (availableSubtypes.size <= 1) return currentSubtype
        currentSubtypeIndex = (currentSubtypeIndex + 1) % availableSubtypes.size
        return currentSubtype
    }

    fun switchToSubtype(subtype: Subtype) {
        val idx = availableSubtypes.indexOfFirst {
            it.subtype.localeString == subtype.localeString && it.subtype.keyboardLayoutName == subtype.keyboardLayoutName
        }
        if (idx >= 0) {
            currentSubtypeIndex = idx
        }
    }
}
