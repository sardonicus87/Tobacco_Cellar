package com.sardonicus.tobaccocellar.ui.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    val state: NavigationState,
    private val twoPaneAllowed: Boolean = false
) {
    private var lastNavigationTime = 0L

    fun navigate(route: NavKey) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime < 300) return
        lastNavigationTime = currentTime

        val (mainBefore, secondBefore) = findPanes(state.currentStack)

        state.cameFrom = state.currentStack.lastOrNull()

        if (route in state.backStacks.keys) { // is a top level route, switch to that backstack

            // For TwoPane, save the current backstack before navigating
            val oldStack = if (twoPaneAllowed && state.topLevelRoute in mainSecondaryMap) {
                state.backStacks.getValue(state.topLevelRoute)
            } else { null }

            state.topLevelRoute = route

            // for TwoPane compatible routes, check and if it doesn't exist, add the default
            // second pane immediately after the main pane
            val currentStack = state.backStacks.getValue(state.topLevelRoute)
            if (twoPaneAllowed && route is PaneInfo && route.paneType == PaneType.MAIN) {
                mainSecondaryMap[route]?.let {
                    if (currentStack.getOrNull(1) != it.defaultSecondary) {
                        currentStack.add(1, it.defaultSecondary)
                    }
                }
            }

            // For TwoPane scene, clean up the previous stack after swapping top level routes
            oldStack?.removeIf { oldStack.indexOf(it) > 1 }

        } else { // not a top level route, add to current backstack
            state.backStacks[state.topLevelRoute]?.add(route)

            // check the newly added route for TwoPane compatibility and add its default second
            val currentStack = state.backStacks.getValue(state.topLevelRoute)
            if (twoPaneAllowed && route is PaneInfo && route.paneType == PaneType.MAIN) {
                mainSecondaryMap[route]?.let {
                    if (currentStack.getOrNull(1) != it.defaultSecondary) {
                        currentStack.add(1, it.defaultSecondary)
                    }
                }
            }

            // for TwoPane compatible routes, check and if it is already on the stack, remove the
            // duplicate
            if (twoPaneAllowed && route is PaneInfo && route.paneType == PaneType.SECOND) {
                val currentStack = state.backStacks.getValue(state.topLevelRoute)
                val duplicate = currentStack.take(currentStack.size - 1).find { it::class == route::class }

                if (duplicate != null) currentStack.remove(duplicate)
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

        val currentRoute2: NavKey =
            if (state.isTwoPane) {
                currentStack[currentStack.size - 2]
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
}

private fun findPanes(stack: List<NavKey>): Pair<NavKey?, NavKey?> {
    val main = stack.findLast { (it as? PaneInfo)?.paneType == PaneType.MAIN }
    val second = stack.findLast { (it as? PaneInfo)?.paneType == PaneType.SECOND }
    return main to second
}