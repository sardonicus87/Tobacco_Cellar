package com.example.tobaccocellar.data

import androidx.compose.runtime.compositionLocalOf
import com.example.tobaccocellar.CellarApplication

val LocalCellarApplication = compositionLocalOf<CellarApplication> {
    error("No CellarApplication found!")
}