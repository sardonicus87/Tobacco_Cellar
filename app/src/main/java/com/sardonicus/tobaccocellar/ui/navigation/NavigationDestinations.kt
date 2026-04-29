package com.sardonicus.tobaccocellar.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sardonicus.tobaccocellar.ui.changelog.ChangelogEntryData
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// Used for TwoPane strategy for large screens
enum class PaneType { MAIN, SECOND, NONE }
interface PaneInfo { val paneType: PaneType }

data class TwoPanePairing(
    val defaultSecondary: NavKey,
    val allowedSeconds: List<KClass<out NavKey>>
)

val mainSecondaryMap: Map<NavKey, TwoPanePairing> = mapOf(
    HomeDestination to TwoPanePairing(
        defaultSecondary = FilterPaneDestination,
        allowedSeconds = listOf(
            FilterPaneDestination::class,
            BlendDetailsDestination::class,
        )
    ),
    StatsDestination to TwoPanePairing(
        defaultSecondary = FilterPaneDestination,
        allowedSeconds = listOf(
            FilterPaneDestination::class,
            BlendDetailsDestination::class,
        )
    ),
    DatesDestination to TwoPanePairing(
        defaultSecondary = FilterPaneDestination,
        allowedSeconds = listOf(
            FilterPaneDestination::class,
            BlendDetailsDestination::class,
        )
    ),
    AboutDestination to TwoPanePairing(
        defaultSecondary = SettingsDestination,
        allowedSeconds = listOf(
            SettingsDestination::class,
            ChangelogDestination::class,
        )
    ),
)

@Serializable
data object HomeDestination : NavKey, PaneInfo {
    override val paneType = PaneType.MAIN
}

@Serializable
data class BlendDetailsDestination(val itemsId: Int) : NavKey, PaneInfo {
    override val paneType = PaneType.SECOND
}

@Serializable
data object FilterPaneDestination : NavKey, PaneInfo {
    override val paneType = PaneType.SECOND
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

@Serializable
data object PlaintextDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data object HelpDestination : NavKey, PaneInfo {
    override val paneType = PaneType.NONE
}

@Serializable
data object AboutDestination : NavKey, PaneInfo {
    override val paneType = PaneType.MAIN
}

@Serializable
data object SettingsDestination : NavKey, PaneInfo {
    override val paneType = PaneType.SECOND
}

@Serializable
data class ChangelogDestination(
    val changelogEntries: List<ChangelogEntryData>,
    val targetVersion: Int? = null
) : NavKey, PaneInfo {
    override val paneType = PaneType.SECOND
}
