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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
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
    mainSecondaryMap: Map<NavKey, NavKey> = emptyMap()
): NavigationState {

    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) { mutableStateOf(startRoute) }

    val backStacks = topLevelRoutes.associateWith {
        rememberNavBackStack(it)
    }

    LaunchedEffect(largeScreen, topLevelRoutes, mainSecondaryMap) {
        if (largeScreen) {
            val currentStack = backStacks.getValue(topLevelRoute.value)
            val currentTop = topLevelRoute.value
            if (currentTop in mainSecondaryMap) {
                val defaultSecond = mainSecondaryMap.getValue(currentTop)
                if (!currentStack.contains(defaultSecond)) {
                    currentStack.add(1, defaultSecond)
                }
            }
        } else {
            backStacks.forEach { (navKey, stack) ->
                if (navKey in mainSecondaryMap) {
                    val defaultSecond = mainSecondaryMap.getValue(navKey)
                    if (stack.size > 1 && stack[1] == defaultSecond) {
                        stack.remove(defaultSecond)
                    }
                }
            }
        }
    }

    return remember(startRoute, topLevelRoutes, largeScreen) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
            largeScreen = largeScreen,
            mainSecondaryMap = mainSecondaryMap
        )
    }
}


class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    val largeScreen: Boolean = false,
    val mainSecondaryMap: Map<NavKey, NavKey> = emptyMap()
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
            val currentStack = backStacks.getValue(topLevelRoute)
        //    val main = currentStack.findLast { it is PaneInfo && it.paneType == PaneType.MAIN }
        //    val second = currentStack.findLast { it is PaneInfo && it.paneType == PaneType.SECOND }
            val startCompatible = currentStack.findLast { it is PaneInfo && it.paneType == PaneType.MAIN } != null
            val lastCompatible = currentStack.lastOrNull().let { it is PaneInfo && it.paneType == PaneType.SECOND }

            return largeScreen && startCompatible && lastCompatible
        }

    val interceptBack: Boolean
        get() {
            val currentStack = backStacks.getValue(topLevelRoute)
        //    val lastEntry = currentStack.lastOrNull().let { it is PaneInfo && it.paneType == PaneType.SECOND }

            return isTwoPane && (if (topLevelRoute == startRoute) currentStack.size > 2 else true)
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
//        .flatMap { decoratedEntries[it] ?: emptyList() }
        .flatMap { stackKey ->
            val stackEntries = decoratedEntries[stackKey] ?: emptyList()

            if (largeScreen) {
                if (stackKey in mainSecondaryMap) {
                    val defaultSecond = mainSecondaryMap.getValue(stackKey)
                    val needsDefaultSecond = stackEntries.none { it == defaultSecond }
                    if (needsDefaultSecond) {
                        val temp = stackEntries.toMutableList()
                        temp.add(1, entryProvider(defaultSecond))
                        temp
                    } else {
                        stackEntries
                    }
                } else {
                    stackEntries
                }
            } else {
                if (stackKey in mainSecondaryMap) {
                    val defaultSecond = mainSecondaryMap.getValue(stackKey)
                    if (stackEntries.size > 1 && stackEntries[1] == defaultSecond) {
                        stackEntries.filterIndexed { index, _ -> index != 1 }
                    } else {
                        stackEntries
                    }
                } else {
                    stackEntries
                }
            }
        }
        .toMutableStateList()
}