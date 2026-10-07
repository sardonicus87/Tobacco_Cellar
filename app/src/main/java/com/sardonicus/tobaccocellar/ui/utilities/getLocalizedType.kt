package com.sardonicus.tobaccocellar.ui.utilities

import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.BlendTypes

fun getLocalizedType(typeKey: String, app: CellarApplication, showUnassigned: Boolean = true): String {
    if (typeKey.isBlank()) {
        return if (showUnassigned) app.getString(R.string.unassigned) else ""
    }
    val blendType = runCatching { BlendTypes.valueOf(typeKey) }.getOrDefault(BlendTypes.UNASSIGNED)
    return app.getString(blendType.resId)
}