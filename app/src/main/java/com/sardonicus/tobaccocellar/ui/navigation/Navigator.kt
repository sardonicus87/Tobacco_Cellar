package com.sardonicus.tobaccocellar.ui.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    val state: NavigationState,
    private val isLarge: Boolean = false,
    private val mainSecondaryMap: Map<NavKey, NavKey> = emptyMap()
) {
    fun navigate(route: NavKey) {
        val (mainBefore, secondBefore) = findPanes(state.currentStack)

        state.cameFrom = state.currentStack.lastOrNull()

        if (route in state.backStacks.keys) { // is a top level route, switch to that backstack

            // Two Pane stuff
//            if (isLarge && state.topLevelRoute in mainSecondaryMap) {
//                // clear the stack of everything but main and default second before swapping stacks
//                val currentStack = state.backStacks.getValue(state.topLevelRoute)
//                currentStack.removeIf { currentStack.indexOf(it) > 1 }
//            }

            // For TwoPane, save the current backstack before navigating
            val oldStack = if (isLarge && state.topLevelRoute in mainSecondaryMap) {
                state.backStacks.getValue(state.topLevelRoute)
            } else { null }

            state.topLevelRoute = route

            // for TwoPane compatible routes, check and if it doesn't exist, add the default
            // second pane immediately after the main pane
            val currentStack = state.backStacks.getValue(state.topLevelRoute)
            if (isLarge && route in mainSecondaryMap) {
                val defaultSecond = mainSecondaryMap.getValue(route)
                if (!currentStack.contains(defaultSecond)) {
                    currentStack.add(1, defaultSecond)
                }
            }

            // For TwoPane scene, clean up the previous stack after swapping top level routes
            oldStack?.removeIf { oldStack.indexOf(it) > 1 }

        } else {
            // for TwoPane compatible routes, check and if it is already on the stack, remove the
            // duplicate
            if (isLarge && route is PaneInfo && route.paneType == PaneType.SECOND) {
                val currentStack = state.backStacks.getValue(state.topLevelRoute)
                val routeClass = route::class.simpleName
                val duplicate = currentStack.find { it::class.simpleName == routeClass }

                if (duplicate != null) currentStack.remove(duplicate)
            }

            // not a top level route, add to current backstack
            state.backStacks[state.topLevelRoute]?.add(route)


            // check the newly added route for TwoPane compatibility and add its default second
            val currentStack = state.backStacks.getValue(state.topLevelRoute)
            if (isLarge && route is PaneInfo && route.paneType == PaneType.MAIN && route in mainSecondaryMap) {
                val defaultSecond = mainSecondaryMap.getValue(route)
                if (!currentStack.contains(defaultSecond)) {
                    currentStack.add(defaultSecond)
                }
            }
        }

        val (mainAfter, secondAfter) = findPanes(state.currentStack)
        if (mainBefore != mainAfter && secondBefore != secondAfter) {
            state.twoPaneSceneKey.intValue++
        }
    }

    fun goBack() {
        val (mainBefore, secondBefore) = findPanes(state.currentStack)

        state.cameFrom = state.currentStack.lastOrNull()

        val currentStack = state.backStacks[state.topLevelRoute] ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        val currentRoute2: NavKey = if (isLarge && currentRoute is PaneInfo && currentRoute.paneType == PaneType.SECOND && mainSecondaryMap.containsValue(currentRoute)) {
            val key = currentStack.size - 2
            currentStack[key]
        } else {
            currentRoute
        }

        if (currentRoute2 == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }

        val (mainAfter, secondAfter) = findPanes(state.currentStack)
        if (mainBefore != mainAfter && secondBefore != secondAfter) {
            state.twoPaneSceneKey.intValue++
        }
    }
//
//    fun removeDefaultSecond() {
//        val currentStack = state.backStacks.getValue(state.topLevelRoute)
//        val defaultSecond = mainSecondaryMap.getValue(state.topLevelRoute)
//        currentStack.removeIf { it == defaultSecond }
//    }
}

private fun findPanes(stack: List<NavKey>): Pair<NavKey?, NavKey?> {
    val main = stack.findLast { (it as? PaneInfo)?.paneType == PaneType.MAIN }
    val second = stack.findLast { (it as? PaneInfo)?.paneType == PaneType.SECOND }
    return main to second
}