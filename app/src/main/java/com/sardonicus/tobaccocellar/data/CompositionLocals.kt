package com.sardonicus.tobaccocellar.data

import androidx.compose.runtime.compositionLocalOf
import com.sardonicus.tobaccocellar.CellarApplication

val LocalCellarApplication = compositionLocalOf<CellarApplication> {
    error("No CellarApplication found!")
}