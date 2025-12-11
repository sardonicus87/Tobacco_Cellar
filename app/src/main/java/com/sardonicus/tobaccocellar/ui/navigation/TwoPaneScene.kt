package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND


data class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val mainEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(mainEntry, secondEntry)
    override val content: @Composable (() -> Unit) = {
        val defaultTransition = fadeIn(tween(500)) togetherWith fadeOut(tween(500))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(.5f)) {
                AnimatedContent(
                    targetState = mainEntry,
                    contentKey = { it.contentKey },
                    transitionSpec = {
                        val enterSpec = targetState.metadata[PANE_ENTER] as? ContentTransform
                        val exitSpec = initialState.metadata[PANE_EXIT] as? ContentTransform
                        when {
                            enterSpec != null -> enterSpec
                            exitSpec != null -> exitSpec
                            else -> defaultTransition
                        }
                    }
                ) {
                            it.Content()
                }
            }
            Column(modifier = Modifier.weight(.5f)) {
                AnimatedContent(
                    targetState = secondEntry,
                    contentKey = { it.contentKey },
                    transitionSpec = {
                        val enterSpec = targetState.metadata[PANE_ENTER] as? ContentTransform
                        val exitSpec = initialState.metadata[PANE_EXIT] as? ContentTransform
                        when {
                            enterSpec != null -> enterSpec
                            exitSpec != null -> exitSpec
                            else -> defaultTransition
                        }
                    }
                ) {
                    it.Content()
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
fun <T : Any> rememberTwoPaneStrategy(): TwoPaneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val sceneKey = remember { mutableIntStateOf(0) }
    val previousMain = remember { mutableStateOf<Any?>(null) }
    val previousSecond = remember { mutableStateOf<Any?>(null) }

    return remember(windowSizeClass) {
        TwoPaneStrategy(
            windowSizeClass,
            sceneKey,
            previousMain,
            previousSecond
        )
    }
}

class TwoPaneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val sceneKey: MutableState<Int>,
    private val previousMain: MutableState<Any?>,
    private val previousSecond: MutableState<Any?>
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val isLarge = windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        if (!isLarge) return null

        val lastEntry = entries.lastOrNull() ?: return null
        val lastEntryPaneType = lastEntry.metadata[TwoPaneScene.PANE_TYPE] as? PaneType

        val twoPaneCompatible = lastEntryPaneType != null && lastEntryPaneType != PaneType.NONE
        if (!twoPaneCompatible) return null

        val mainEntry = entries.findLast { it.metadata[TwoPaneScene.PANE_TYPE] == PaneType.MAIN } ?: return null
        val secondEntry = entries.findLast { it.metadata[TwoPaneScene.PANE_TYPE] == PaneType.SECOND } ?: return null

        val currentMain = mainEntry.contentKey
        val currentSecond = secondEntry.contentKey
        if (currentMain != previousMain.value && currentSecond != previousSecond.value) {
            sceneKey.value++
        }

        previousMain.value = currentMain
        previousSecond.value = currentSecond

        if (mainEntry.contentKey == secondEntry.contentKey) return null

        val previousEntries = entries.filter { it.contentKey != mainEntry.contentKey && it.contentKey != secondEntry.contentKey }

        return TwoPaneScene(
                key = sceneKey,
                previousEntries = previousEntries,
                mainEntry = mainEntry,
                secondEntry = secondEntry
            )
    }
}