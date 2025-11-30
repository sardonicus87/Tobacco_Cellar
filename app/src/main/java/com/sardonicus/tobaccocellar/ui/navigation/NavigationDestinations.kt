package com.sardonicus.tobaccocellar.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sardonicus.tobaccocellar.ui.settings.ChangelogEntryData
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination : NavKey

@Serializable
data class BlendDetailsDestination(val itemsId: Int) : NavKey

@Serializable
data object HelpDestination : NavKey

@Serializable
data object StatsDestination : NavKey

@Serializable
data object DatesDestination : NavKey

@Serializable
data object AddEntryDestination : NavKey

@Serializable
data class EditEntryDestination(val itemsId: Int) : NavKey

@Serializable
data object BulkEditDestination : NavKey

@Serializable
data object SettingsDestination : NavKey

@Serializable
data class ChangelogDestination(val changelogEntries: List<ChangelogEntryData>) : NavKey

@Serializable
data object PlaintextDestination : NavKey

@Serializable
sealed interface CsvFlowKey : NavKey

@Serializable
data object CsvFlowDestination : NavKey

@Serializable
data class CsvImportDestination(val id: String) : CsvFlowKey

@Serializable
data object CsvHelpDestination : CsvFlowKey

@Serializable
data class CsvImportResultsDestination(
    val totalRecords: Int,
    val successCount: Int,
    val successfulInsertions: Int,
    val successfulUpdates: Int,
    val successfulTins: Int,
    val updateFlag: Boolean,
    val tinFlag: Boolean
) : NavKey






















