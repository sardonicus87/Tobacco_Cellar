package com.sardonicus.tobaccocellar.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize

data class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val interceptBack: Boolean,
    val mainEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
    val fullBackStack: List<NavEntry<T>>,
    val onBack: () -> Unit
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(mainEntry, secondEntry)
    override val content: @Composable (() -> Unit) = {

        BackHandler(enabled = interceptBack, onBack = onBack)

        Row(modifier = Modifier.fillMaxSize()) {
            // Main pane
            Column(modifier = Modifier.weight(.5f)) {
                val mainPaneState = remember { SeekableTransitionState(mainEntry) }
                val mainTransition = rememberTransition(mainPaneState)

                LaunchedEffect(mainEntry) {
                    if (mainPaneState.currentState != mainEntry) {
                        mainPaneState.animateTo(mainEntry)
                    } else {
                        mainPaneState.snapTo(mainEntry)
                    }
                }

                val transition = (mainTransition.targetState.metadata[PANE_ENTER] as? ContentTransform?)
                    ?: (mainTransition.currentState.metadata[PANE_EXIT] as? ContentTransform)
                    ?: (fadeIn(tween(500)) togetherWith fadeOut(tween(500)))

                val priorStack = remember(mainTransition.currentState) { fullBackStack }
                val isBack = isBack(priorStack, fullBackStack)
                val targetZIndex = if (isBack) -1f else 1f

                mainTransition.AnimatedContent(
                    contentKey = { it.contentKey },
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = transition.targetContentEnter,
                            initialContentExit = transition.initialContentExit,
                            targetContentZIndex = targetZIndex
                        )
                    }
                ) {
                    it.Content()
                }
            }

            // Second pane
            Column(modifier = Modifier.weight(.5f)) {
                val secondPaneState = remember { SeekableTransitionState(secondEntry) }
                val secondTransition = rememberTransition(secondPaneState)

                LaunchedEffect(secondEntry) {
                    if (secondPaneState.currentState != secondEntry) {
                        secondPaneState.animateTo(secondEntry)
                    } else {
                        secondPaneState.snapTo(secondEntry)
                    }
                }

                val transition = (secondTransition.targetState.metadata[PANE_ENTER] as? ContentTransform?)
                    ?: (secondTransition.currentState.metadata[PANE_EXIT] as? ContentTransform)
                    ?: (fadeIn(tween(500)) togetherWith fadeOut(tween(500)))

                val priorStack = remember(secondTransition.currentState) { fullBackStack }
                val isBack = isBack(priorStack, fullBackStack)
                val targetZIndex = if (isBack) -1f else 1f

                GlowBox(
                    color = GlowColor(Color.Black.copy(alpha = .15f)),
                    size = GlowSize(start = 4.dp)
                ) {
                    secondTransition.AnimatedContent(
                        contentKey = { it.contentKey },
                        transitionSpec = {
                            ContentTransform(
                                targetContentEnter = transition.targetContentEnter,
                                initialContentExit = transition.initialContentExit,
                                targetContentZIndex = targetZIndex
                            )
                        }
                    ) {
                        it.Content()
                    }
                }
            }
        }
    }

    companion object {
        const val PANE_TYPE = "PaneType"
        const val PANE_ENTER = "PaneEnter"
        const val PANE_EXIT = "PaneExit"
    }
}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun <T : Any> rememberTwoPaneStrategy(sceneKey: Int, interceptBack: Boolean): TwoPaneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(windowSizeClass, sceneKey, interceptBack) {
        TwoPaneStrategy(
            windowSizeClass,
            sceneKey,
            interceptBack
        )
    }
}


class TwoPaneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val sceneKey: Int,
    private val interceptBack: Boolean
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val isLarge = windowSizeClass.isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND)

        if (!isLarge) return null

        val lastEntry = entries.lastOrNull() ?: return null
        val lastEntryPaneType = lastEntry.metadata[TwoPaneScene.PANE_TYPE] as? PaneType

        val twoPaneCompatible = lastEntryPaneType != null && lastEntryPaneType != PaneType.NONE
        if (!twoPaneCompatible) return null

        val mainEntry = entries.findLast { it.metadata[TwoPaneScene.PANE_TYPE] == PaneType.MAIN } ?: return null
        val secondEntry = entries.findLast { it.metadata[TwoPaneScene.PANE_TYPE] == PaneType.SECOND } ?: return null

        if (mainEntry.contentKey == secondEntry.contentKey) return null

        val previousEntries = entries.filter { it.contentKey != mainEntry.contentKey && it.contentKey != secondEntry.contentKey }

        return TwoPaneScene(
            key = sceneKey,
            previousEntries = previousEntries,
            interceptBack = interceptBack,
            mainEntry = mainEntry,
            secondEntry = secondEntry,
            fullBackStack = entries,
            onBack = this.onBack
        )
    }
}

private fun <T : Any> isBack(
    oldBackStack: List<T>,
    newBackStack: List<T>
): Boolean {
    // crash prevention in case something goes wrong
    if (oldBackStack.isEmpty() || newBackStack.isEmpty()) return false

    // entire stack replaced
    if (oldBackStack.first() != newBackStack.first()) return false
    // navigated
    if (newBackStack.size > oldBackStack.size) return false

    val divergingIndex =
        newBackStack.indices.firstOrNull { index -> newBackStack[index] != oldBackStack[index] }
    // if newBackStack never diverged from oldBackStack, then it is a clean subset of the oldStack
    // and is a pop
    return divergingIndex == null && newBackStack.size != oldBackStack.size
}