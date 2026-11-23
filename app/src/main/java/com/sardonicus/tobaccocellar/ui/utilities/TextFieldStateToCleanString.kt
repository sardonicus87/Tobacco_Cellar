package com.sardonicus.tobaccocellar.ui.utilities

import androidx.compose.foundation.text.input.TextFieldState
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.stripControlChar

fun TextFieldState.toCleanString(): String {
    return this.text.toString().stripControlChar()
}