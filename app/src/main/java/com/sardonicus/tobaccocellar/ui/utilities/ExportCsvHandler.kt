package com.sardonicus.tobaccocellar.ui.utilities

import android.net.Uri

interface ExportCsvHandler {
    fun onExportCsvClick(uri: Uri?)
    fun onTinsExportCsvClick(uri: Uri?)
}