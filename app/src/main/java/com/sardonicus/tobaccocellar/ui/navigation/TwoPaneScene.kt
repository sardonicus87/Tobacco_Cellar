package com.sardonicus.tobaccocellar.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

data class TwoPaneScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val interceptBack: Boolean,
    val mainEntry: NavEntry<T>,
    val secondEntry: NavEntry<T>,
    val fullBackStack: List<NavEntry<T>>,
    val onBack: () -> Unit,
    val filterViewModel: FilterViewModel
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(mainEntry, secondEntry)
    @SuppressLint("ConfigurationScreenWidthHeight")
    override val content: @Composable (() -> Unit) = {

        BackHandler(enabled = interceptBack, onBack = onBack)

        val secondExpanded by filterViewModel.secondPaneExpanded.collectAsState()

        BackHandler(!secondExpanded, filterViewModel::toggleSecondPane)

        val configuration = LocalConfiguration.current
        val expandedWidth = configuration.screenWidthDp.dp / 2
        val screenHeight = LocalWindowInfo.current.containerSize.height.dp
        val expansionTween = 300

        val paneWidth by animateDpAsState(
            targetValue = if (secondExpanded) expandedWidth else 32.dp,
            animationSpec = tween(expansionTween),
            label = "PaneWidth"
        )

        var showButton by remember { mutableStateOf(false) }

        LaunchedEffect(showButton, secondExpanded) {
            if (showButton) {
                if (secondExpanded) {
                    snapshotFlow { paneWidth }.first { it >= expandedWidth }
                    delay(3000)
                    showButton = false
                }
            }
        }

        val buttonHorizontalOffset by animateDpAsState(
            targetValue = if (secondExpanded) (-12).dp else 0.dp,
            animationSpec = tween(expansionTween),
            label = "ButtonHorizontalOffset"
        )
        val buttonVerticalOffset by animateDpAsState(
            targetValue = if (secondExpanded) 12.dp else 0.dp,
            animationSpec = tween(expansionTween),
            label = "ButtonVerticalOffset"
        )
        val buttonHeight by animateDpAsState(
            targetValue = if (secondExpanded) 32.dp else screenHeight,
            animationSpec = tween(expansionTween),
            label = "ButtonHeight"
        )
        val buttonAlpha by animateFloatAsState(
            targetValue = if (secondExpanded) .6f else 1f,
            animationSpec = tween(expansionTween),
            label = "ButtonAlpha"
        )
        val buttonCorner by animateDpAsState(
            targetValue = if (secondExpanded) 4.dp else 0.dp,
            animationSpec = tween(expansionTween),
            label = "ButtonCorner"
        )
        val borderAlpha by animateFloatAsState(
            targetValue = if (secondExpanded) .3f else 0f,
            animationSpec = tween(expansionTween),
            label = "BorderAlpha"
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Main pane
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    val mainPaneState = remember { SeekableTransitionState(mainEntry) }
                    val mainTransition = rememberTransition(mainPaneState)

                    LaunchedEffect(mainEntry) {
                        if (mainPaneState.currentState != mainEntry) {
                            mainPaneState.animateTo(mainEntry)
                        } else {
                            mainPaneState.snapTo(mainEntry)
                        }
                    }

                    val transition =
                        (mainTransition.targetState.metadata[PANE_ENTER] as? ContentTransform?)
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
                Box(
                    modifier = Modifier
                        .width(paneWidth)
                        .graphicsLayer { clip = true }
                ) {
                    Column(
                        modifier = Modifier
                            .width(expandedWidth)
                            .layout { measurable, constraints ->
                                val expandedWidthPx = expandedWidth.roundToPx()
                                val placeable = measurable.measure(
                                    constraints.copy(
                                        maxWidth = expandedWidthPx,
                                        minWidth = expandedWidthPx
                                    )
                                )
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .pointerInput(secondExpanded){
                                if (secondExpanded) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Final)
                                            val down = event.changes.find { it.changedToDown() && !it.isConsumed }

                                            if (down != null) {
                                                val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                                    var change: PointerInputChange? = null
                                                    while (change == null) {
                                                        val nextEvent = awaitPointerEvent(PointerEventPass.Final)
                                                        if (nextEvent.changes.any { it.changedToUp() }) {
                                                            change = nextEvent.changes.find { it.changedToUp() }
                                                        } else if (nextEvent.changes.any { it.isConsumed} ) {
                                                            return@withTimeoutOrNull null
                                                        }
                                                    }
                                                    change
                                                }

                                                if (up != null && !up.isConsumed) {
                                                    showButton = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        val secondPaneState = remember { SeekableTransitionState(secondEntry) }
                        val secondTransition = rememberTransition(secondPaneState)

                        LaunchedEffect(secondEntry) {
                            filterViewModel.setSecondPaneExpansion(true)

                            if (secondPaneState.currentState != secondEntry) {
                                secondPaneState.animateTo(secondEntry)
                            } else {
                                secondPaneState.snapTo(secondEntry)
                            }
                        }

                        val transition =
                            (secondTransition.targetState.metadata[PANE_ENTER] as? ContentTransform?)
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
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
                modifier = Modifier
                    .align(BiasAlignment(horizontalBias = 1f, verticalBias = -1f))
                    .offset {
                        IntOffset(
                            buttonHorizontalOffset.roundToPx(),
                            buttonVerticalOffset.roundToPx()
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(buttonHeight)
                        .background(
                            LocalCustomColors.current.whiteBlack.copy(alpha = buttonAlpha),
                            RoundedCornerShape(buttonCorner)
                        )
                        .border(
                            1.dp,
                            LocalCustomColors.current.whiteBlackInverted.copy(alpha = borderAlpha),
                            RoundedCornerShape(buttonCorner)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) { filterViewModel.toggleSecondPane() },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = !secondExpanded,
                        enter = fadeIn(tween(expansionTween)) + expandVertically(tween(expansionTween), Alignment.CenterVertically),
                        exit = fadeOut(tween(expansionTween)) + shrinkVertically(tween(expansionTween), Alignment.CenterVertically),
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterStart)
                    ) {
                        GlowBox(
                            color = GlowColor(end = LocalCustomColors.current.whiteBlackInverted.copy(alpha = .15f)),
                            size = GlowSize(end = 4.dp),
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Spacer(modifier = Modifier.width(32.dp))
                        }
                    }
                    AnimatedContent(
                        targetState = if (secondExpanded) R.drawable.arrow_right else R.drawable.arrow_left,
                        transitionSpec = { fadeIn(tween(expansionTween)) togetherWith fadeOut(tween(expansionTween)) },
                        label = "IconCrossfade"
                    ) {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            tint = LocalCustomColors.current.whiteBlackInverted.copy(alpha = buttonAlpha),
                            modifier = Modifier
                        )
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
fun <T : Any> rememberTwoPaneStrategy(sceneKey: Int, interceptBack: Boolean, enabled: Boolean): TwoPaneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val filterViewModel = LocalCellarApplication.current.filterViewModel

    return remember(windowSizeClass, sceneKey, interceptBack, enabled, filterViewModel) {
        TwoPaneStrategy(
            windowSizeClass,
            sceneKey,
            interceptBack,
            enabled,
            filterViewModel
        )
    }
}


class TwoPaneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val sceneKey: Int,
    private val interceptBack: Boolean,
    private val enabled: Boolean,
    private val filterViewModel: FilterViewModel
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!enabled) return null

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
            onBack = this.onBack,
            filterViewModel = filterViewModel
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
