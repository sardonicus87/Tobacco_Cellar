package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
    largeScreen: Boolean = false,
    globalTwoPane: Boolean = true
): NavigationState {

    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) { mutableStateOf(startRoute) }

    val backStacks = topLevelRoutes.associateWith { rememberNavBackStack(it) }

    LaunchedEffect(largeScreen, topLevelRoutes, globalTwoPane) {
        if (largeScreen && globalTwoPane) {
            val currentStack = backStacks.getValue(topLevelRoute.value)
            val currentTop = topLevelRoute.value
            if (currentTop in mainSecondaryMap) {
                val defaultSecond = mainSecondaryMap.getValue(currentTop).defaultSecondary
                if (!currentStack.contains(defaultSecond)) {
                    currentStack.add(1, defaultSecond)
                }
            }
        } else {
            backStacks.forEach { (navKey, stack) ->
                if (navKey in mainSecondaryMap) {
                    val defaultSecond = mainSecondaryMap.getValue(navKey).defaultSecondary
                    if (stack.size > 1 && stack[1] == defaultSecond) {
                        stack.remove(defaultSecond)
                    }
                }
            }
        }
    }

    return remember(startRoute, topLevelRoutes, largeScreen, globalTwoPane) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
            largeScreen = largeScreen,
            globalTwoPane = globalTwoPane
        )
    }
}


class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    val largeScreen: Boolean = false,
    val globalTwoPane: Boolean = true
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
            return (currentStack?.size ?: 0) > 1 || (topLevelRoute != startRoute)
        }

    val currentStack: List<NavKey>
        get() { return backStacks.getValue(topLevelRoute).toList() }

    var cameFrom: NavKey? by mutableStateOf(currentStack.lastOrNull())

    // For TwoPane Scene
    val twoPaneSceneKey = mutableIntStateOf(0)

    val isTwoPane: Boolean
        get() {
            if (!globalTwoPane) return false

            val currentStack = backStacks.getValue(topLevelRoute)
            val mainKey = currentStack.findLast { it is PaneInfo && it.paneType == PaneType.MAIN }
            val lastKey = currentStack.lastOrNull()

            if (mainKey == null || lastKey == null) return false

            val pairing = mainSecondaryMap[mainKey] ?: return false
            val pairingValid = (lastKey as? PaneInfo)?.paneType == PaneType.SECOND && lastKey::class in pairing.allowedSeconds

            return largeScreen && pairingValid
        }

    val interceptBack: Boolean
        get() {
            val currentStack = backStacks.getValue(topLevelRoute)

            return isTwoPane && (if (topLevelRoute == startRoute) currentStack.size > 2 else true)
        }
}


@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators: List<NavEntryDecorator<NavKey>> = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(removeViewModelStoreOnPop = { true } ),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
    }

    return stacksInUse.flatMap { stackKey ->
        val stackEntries = decoratedEntries[stackKey] ?: emptyList()

        if (largeScreen && globalTwoPane) {
            val defaultSecond = mainSecondaryMap.getValue(stackKey).defaultSecondary
            val stack = backStacks[stackKey]
            val needsDefault = stack?.contains(defaultSecond) == false

            if (needsDefault) {
                val temp = stackEntries.toMutableList()
                temp.add(1, entryProvider(defaultSecond))
                return@flatMap temp
            }
        }

        stackEntries
    }.toMutableStateList()
}