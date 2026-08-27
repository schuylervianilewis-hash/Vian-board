package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.engine.core.Suggest
import com.example.foundation.common.Constants

/**
 * Encapsulates all persistent user settings for Vian Board.
 */
data class KeyboardSettings(
    val heightScale: Float = Constants.DEFAULT_KEYBOARD_HEIGHT_SCALE,
    val bottomInsetPaddingDp: Int = Constants.DEFAULT_BOTTOM_INSET_PADDING_DP,
    val showNumberRow: Boolean = true,
    val autoCorrectSensitivity: Suggest.Sensitivity = Suggest.Sensitivity.MODEST,
    val hapticFeedbackEnabled: Boolean = true,
    val soundOnKeyPress: Boolean = false,
    val smartMultiplyMorph: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val currencySymbol: String = "$"
)

/**
 * SharedPreferences storage manager for Vian Board settings.
 */
class KeyboardSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): KeyboardSettings {
        return KeyboardSettings(
            heightScale = prefs.getFloat(KEY_HEIGHT_SCALE, Constants.DEFAULT_KEYBOARD_HEIGHT_SCALE),
            bottomInsetPaddingDp = prefs.getInt(KEY_BOTTOM_INSET, Constants.DEFAULT_BOTTOM_INSET_PADDING_DP),
            showNumberRow = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true),
            autoCorrectSensitivity = try {
                Suggest.Sensitivity.valueOf(
                    prefs.getString(KEY_AUTO_CORRECT_SENSITIVITY, Suggest.Sensitivity.MODEST.name)
                        ?: Suggest.Sensitivity.MODEST.name
                )
            } catch (e: Exception) {
                Suggest.Sensitivity.MODEST
            },
            hapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            soundOnKeyPress = prefs.getBoolean(KEY_SOUND, false),
            smartMultiplyMorph = prefs.getBoolean(KEY_SMART_MULTIPLY, true),
            doubleSpacePeriod = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true),
            currencySymbol = prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
        )
    }

    fun saveSettings(settings: KeyboardSettings) {
        prefs.edit()
            .putFloat(KEY_HEIGHT_SCALE, settings.heightScale)
            .putInt(KEY_BOTTOM_INSET, settings.bottomInsetPaddingDp)
            .putBoolean(KEY_SHOW_NUMBER_ROW, settings.showNumberRow)
            .putString(KEY_AUTO_CORRECT_SENSITIVITY, settings.autoCorrectSensitivity.name)
            .putBoolean(KEY_HAPTIC, settings.hapticFeedbackEnabled)
            .putBoolean(KEY_SOUND, settings.soundOnKeyPress)
            .putBoolean(KEY_SMART_MULTIPLY, settings.smartMultiplyMorph)
            .putBoolean(KEY_DOUBLE_SPACE_PERIOD, settings.doubleSpacePeriod)
            .putString(KEY_CURRENCY_SYMBOL, settings.currencySymbol)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "vian_board_settings"
        private const val KEY_HEIGHT_SCALE = "height_scale"
        private const val KEY_BOTTOM_INSET = "bottom_inset"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_AUTO_CORRECT_SENSITIVITY = "auto_correct_sensitivity"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_SMART_MULTIPLY = "smart_multiply"
        private const val KEY_DOUBLE_SPACE_PERIOD = "double_space_period"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    }
}
