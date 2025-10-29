package com.sardonicus.tobaccocellar.ui.utilities

import android.net.Uri
import com.sardonicus.tobaccocellar.ui.settings.ExportRating

interface ExportCsvHandler {
    fun onExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating)
    fun onTinsExportCsvClick(uri: Uri?, allItems: Boolean, exportRating: ExportRating)
}