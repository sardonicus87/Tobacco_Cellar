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
    twoPaneAllowed: Boolean = true
): NavigationState {

    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) { mutableStateOf(startRoute) }

    val backStacks = topLevelRoutes.associateWith { rememberNavBackStack(it) }

    if (topLevelRoute.value !in topLevelRoutes) { topLevelRoute.value = startRoute }

    LaunchedEffect(topLevelRoutes, twoPaneAllowed, topLevelRoute.value) {
        if (twoPaneAllowed) {
            backStacks[topLevelRoute.value]?.let { currentStack ->
                val currentTop = topLevelRoute.value

                getPairing(currentTop)?.let { pairing ->
                    val secondary = currentStack.getOrNull(1)
                    if (secondary == null || !secondary.isSameKey(pairing.defaultSecondary)) {
                        currentStack.add(1, pairing.defaultSecondary)
                    }
                }
            }
        } else {
            backStacks.forEach { (navKey, stack) ->
                getPairing(navKey)?.let { pairing ->
                    stack.removeIf { it.isSameKey(pairing.defaultSecondary) }
                }
            }
        }
    }

    return remember(startRoute, topLevelRoutes, twoPaneAllowed) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
            twoPaneAllowed = twoPaneAllowed
        )
    }
}


class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    val twoPaneAllowed: Boolean = true
) {
    var topLevelRoute: NavKey by topLevelRoute

    val stacksInUse: List<NavKey>
        get() = if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

//    val canGoBack: Boolean
//        get() {
//            val currentStack = backStacks[topLevelRoute]
//            return (currentStack?.size ?: 0) > 1 || (topLevelRoute != startRoute)
//        }

    val currentStack: List<NavKey>
        get() { return backStacks[topLevelRoute]?.toList() ?: emptyList() }

    var cameFrom: NavKey? by mutableStateOf(currentStack.lastOrNull())

    // For TwoPane Scene
    val twoPaneSceneKey = mutableIntStateOf(0)

    val isTwoPane: Boolean
        get() {
            if (!twoPaneAllowed) return false

            val currentStack = backStacks[topLevelRoute] ?: return false
            val mainKey = currentStack.findLast { it is PaneInfo && it.paneType == PaneType.MAIN }
            val lastKey = currentStack.lastOrNull()

            if (mainKey == null || lastKey == null) return false

            val pairing = getPairing(mainKey) ?: return false
            val pairingValid = (lastKey as? PaneInfo)?.paneType == PaneType.SECOND &&
                    pairing.allowedSeconds.any { it.simpleName == lastKey::class.simpleName }

            return pairingValid
        }

    val interceptBack: Boolean
        get() {
            val currentStack = backStacks[topLevelRoute] ?: return false
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
        val pairing = getPairing(stackKey)

        if (twoPaneAllowed && pairing != null) {
            val hasSecondary = stackEntries.any { it.contentKey.isSameKey(pairing.defaultSecondary) }

            if (!hasSecondary) {
                val temp = stackEntries.toMutableList()
                temp.add(1, entryProvider(pairing.defaultSecondary))
                return@flatMap temp
            }
            stackEntries
        } else {
            if (pairing != null) {
                stackEntries.filter { !it.contentKey.isSameKey(pairing.defaultSecondary) }
            } else stackEntries
        }
    }.toMutableStateList()
}

private fun getPairing(key: NavKey?): TwoPanePairing? {
    if (key == null) return null
    val name = key.toString().substringBefore('(')
    return mainSecondaryMap.entries.find { it.key.toString().substringBefore('(') == name }?.value
}

private fun Any?.isSameKey(other: Any?): Boolean {
    if (this == null || other == null) return false
    return this.toString().substringBefore('(') == other.toString().substringBefore('(')
}