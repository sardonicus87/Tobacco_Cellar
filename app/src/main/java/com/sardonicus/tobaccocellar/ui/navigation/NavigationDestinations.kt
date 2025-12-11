package com.sardonicus.tobaccocellar.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sardonicus.tobaccocellar.ui.settings.ChangelogEntryData
import kotlinx.serialization.Serializable

// Used for TwoPane strategy for large screens
enum class PaneType { MAIN, SECOND, NONE }
interface PaneInfo {
    val paneType: PaneType
}

@Serializable
data object HomeDestination : NavKey, PaneInfo {
    override val paneType = PaneType.MAIN
}

@Serializable
data class BlendDetailsDestination(val itemsId: Int) : NavKey, PaneInfo {
    override val paneType = PaneType.SECOND
}

@Serializable
data object HelpDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data object StatsDestination : NavKey, PaneInfo {
    override val paneType = PaneType.MAIN
}

@Serializable
data object DatesDestination : NavKey, PaneInfo {
    override val paneType = PaneType.MAIN
}

@Serializable
data object FilterPaneDestination : NavKey, PaneInfo {
    override val paneType = PaneType.SECOND
}

@Serializable
data object AddEntryDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data class EditEntryDestination(val itemsId: Int) : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data object BulkEditDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data object SettingsDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE // maybe main
}

@Serializable
data class ChangelogDestination(val changelogEntries: List<ChangelogEntryData>) : NavKey, PaneInfo {
    override val paneType = PaneType.NONE // maybe second if settings main and dialogs moved to second
}

@Serializable
data object PlaintextDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
sealed interface CsvFlowKey : NavKey

@Serializable
data object CsvFlowDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data class CsvImportDestination(val id: String) : CsvFlowKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data object CsvHelpDestination : CsvFlowKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data class CsvImportResultsDestination(
    val totalRecords: Int,
    val successCount: Int,
    val successfulInsertions: Int,
    val successfulUpdates: Int,
    val successfulTins: Int,
    val updateFlag: Boolean,
    val tinFlag: Boolean
) : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}






















