package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val cellarNavKeyModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(HomeDestination::class)
        subclass(BlendDetailsDestination::class)
        subclass(HelpDestination::class)
        subclass(StatsDestination::class)
        subclass(DatesDestination::class)
        subclass(FilterPaneDestination::class)
        subclass(AddEntryDestination::class)
        subclass(EditEntryDestination::class)
        subclass(BulkEditDestination::class)
        subclass(SettingsDestination::class)
        subclass(ChangelogDestination::class)
        subclass(PlaintextDestination::class)
        subclass(CsvFlowDestination::class)
        subclass(CsvImportDestination::class)
        subclass(CsvHelpDestination::class)
        subclass(CsvImportResultsDestination::class)
    }
}

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
): NavigationState {

    val config = remember { SavedStateConfiguration { serializersModule = cellarNavKeyModule } }

    val topLevelRoute = rememberSaveable(
        saver = Saver(
            save = { state ->
                encodeToSavedState(
                    configuration = config,
                    serializer = PolymorphicSerializer(NavKey::class),
                    value = state.value
                )
            },
            restore = { savedState ->
                val restoredValue = decodeFromSavedState(
                    configuration = config,
                    deserializer = PolymorphicSerializer(NavKey::class),
                    savedState = savedState,
                )
                mutableStateOf(restoredValue)
            }
        )
    ) { mutableStateOf(startRoute) }

//    val topLevelRoute = rememberSerializable(
//        startRoute,
//        topLevelRoutes,
//        MutableStateSerializer(NavKeySerializer())
//    ) { mutableStateOf(startRoute) }

    val backStacks = topLevelRoutes.associateWith { rememberNavBackStack(it) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}


class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {
    var topLevelRoute: NavKey by topLevelRoute
    val stacksInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

    val canGoBack: Boolean
        get() {
            val currentStack = backStacks[topLevelRoute]
            // We can go back if the current stack has more than one item.
            return (currentStack?.size ?: 0) > 1
        }
}


@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>()
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
    }

    return stacksInUse
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}