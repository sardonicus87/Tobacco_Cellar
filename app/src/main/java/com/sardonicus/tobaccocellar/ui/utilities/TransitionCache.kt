package com.sardonicus.tobaccocellar.ui.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object TransitionCache {
    private const val FILE_NAME = "transition_screenshot.jpg"

    fun saveScreenshot(context: Context, bitmap: Bitmap) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        } catch (_: Exception) { }
    }

    fun loadScreenshot(context: Context): Bitmap? {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return null
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_ : Exception) { return null }
    }

    fun deleteScreenshot(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) { file.delete() }
        } catch (_: Exception) { }
    }
}