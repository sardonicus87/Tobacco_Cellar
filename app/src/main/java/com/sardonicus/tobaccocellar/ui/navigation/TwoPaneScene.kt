package com.sardonicus.tobaccocellar.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
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
import androidx.compose.ui.unit.Dp
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
        val secondExpanded by filterViewModel.secondPaneExpanded.collectAsState()
        val configuration = LocalConfiguration.current
        val expandedWidth = configuration.screenWidthDp.dp / 2
        val expansionTween = 300

        BackHandler(enabled = interceptBack, onBack = onBack)
        BackHandler(!secondExpanded, filterViewModel::toggleSecondPane)

        val paneWidth by animateDpAsState(
            targetValue = if (secondExpanded) expandedWidth else 32.dp,
            animationSpec = tween(expansionTween)
        )
        val buttonOffset by animateDpAsState(
            targetValue = if (secondExpanded) 12.dp else 0.dp,
            animationSpec = tween(expansionTween)
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

        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Main pane
                PaneContainer(
                    entry = mainEntry,
                    fullBackStack = fullBackStack,
                    isMain = true,
                    modifier = Modifier.weight(1f)
                )

                // Second pane
                PaneContainer(
                    entry = secondEntry,
                    fullBackStack = fullBackStack,
                    isMain = false,
                    modifier = Modifier
                        .width(paneWidth)
                        .graphicsLayer { clip = true }
                        .tapToggle(secondExpanded) { showButton = true },
                    expandedWidth = expandedWidth,
                    onEnter = { filterViewModel.setSecondPaneExpansion(true) }
                )
            }

            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
                modifier = Modifier
                    .align(BiasAlignment(1f, -1f))
                    .offset { IntOffset(-buttonOffset.roundToPx(), buttonOffset.roundToPx()) }
            ) {
                TwoPaneButton(
                    secondExpanded = secondExpanded,
                    expansionTween = expansionTween,
                    toggleSecondPane = filterViewModel::toggleSecondPane
                )
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
        if (lastEntryPaneType != PaneType.SECOND) return null

        val mainEntry = entries.findLast { it.metadata[TwoPaneScene.PANE_TYPE] == PaneType.MAIN } ?: return null
        val secondEntry = entries.findLast { it.metadata[TwoPaneScene.PANE_TYPE] == PaneType.SECOND } ?: return null

        val pairing = getPairing(mainEntry.contentKey) ?: return null
        if (!validPairing(pairing, secondEntry.contentKey)) return null

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


@Composable
private fun <T : Any> PaneContainer(
    entry: NavEntry<T>,
    fullBackStack: List<NavEntry<T>>,
    isMain: Boolean,
    modifier: Modifier = Modifier,
    expandedWidth: Dp = 0.dp,
    onEnter: () -> Unit = {}
) {
    val paneState = remember { SeekableTransitionState(entry) }
    val transition = rememberTransition(paneState)

    LaunchedEffect(entry) {
        onEnter()
        if (paneState.currentState != entry) paneState.animateTo(entry) else paneState.snapTo(entry)
    }

    val contentTransform = (transition.targetState.metadata[if (isMain) TwoPaneScene.PANE_ENTER else TwoPaneScene.PANE_ENTER] as? ContentTransform)
        ?: (transition.currentState.metadata[if (isMain) TwoPaneScene.PANE_EXIT else TwoPaneScene.PANE_EXIT] as? ContentTransform)
        ?: (fadeIn(tween(500)) togetherWith fadeOut(tween(500)))

    val isBack = isBack(remember(transition.currentState) { fullBackStack }, fullBackStack)
    val targetZIndex = if (isBack) -1f else 1f

    val contentModifier = if (isMain) modifier else {
        modifier.layout { measurable, constraints ->
            val widthPx = expandedWidth.roundToPx()
            val placeable = measurable.measure(constraints.copy(maxWidth = widthPx, minWidth = widthPx))
            layout(constraints.maxWidth, constraints.maxHeight) { placeable.placeRelative(0, 0) }
        }
    }

    Column(contentModifier) {
        if (!isMain) {
            GlowBox(
                color = GlowColor(Color.Black.copy(alpha = .15f)),
                size = GlowSize(start = 4.dp)
            ) {
                PaneContent(transition, contentTransform, targetZIndex)
            }
        } else {
            PaneContent(transition, contentTransform, targetZIndex)
        }
    }
}

@Composable
private fun <T : Any> PaneContent(
    transition: Transition<NavEntry<T>>,
    transform: ContentTransform,
    zIndex: Float
) {
    transition.AnimatedContent(
        contentKey = { it.contentKey },
        transitionSpec = {
            ContentTransform(
                targetContentEnter = transform.targetContentEnter,
                initialContentExit = transform.initialContentExit,
                targetContentZIndex = zIndex
            )
        }
    ) {
        it.Content()
    }
}


@Composable
private fun TwoPaneButton(
    secondExpanded: Boolean,
    expansionTween: Int,
    toggleSecondPane: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height

    val buttonHeight by animateDpAsState(
        targetValue = if (secondExpanded) 32.dp else screenHeight,
        animationSpec = tween(expansionTween)
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (secondExpanded) .6f else 1f,
        animationSpec = tween(expansionTween)
    )
    val buttonCorner by animateDpAsState(
        targetValue = if (secondExpanded) 4.dp else 0.dp,
        animationSpec = tween(expansionTween)
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (secondExpanded) .3f else 0f,
        animationSpec = tween(expansionTween)
    )

    Box(
        modifier = modifier
            .width(32.dp)
            .height(buttonHeight)
            .background(LocalCustomColors.current.whiteBlack.copy(alpha = buttonAlpha), RoundedCornerShape(buttonCorner))
            .border(1.dp, LocalCustomColors.current.whiteBlackInverted.copy(alpha = borderAlpha), RoundedCornerShape(buttonCorner))
            .clickable(
                indication = null,
                interactionSource = null
            ) { toggleSecondPane() },
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
        ) {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = LocalCustomColors.current.whiteBlackInverted.copy(alpha = buttonAlpha)
            )
        }
    }
}


private fun Modifier.tapToggle(
    enabled: Boolean,
    onTap: () -> Unit
): Modifier = if (!enabled) this else this.pointerInput(Unit) {
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
                        } else if (nextEvent.changes.any { it.isConsumed }) {
                            return@withTimeoutOrNull null
                        }
                    }
                    change
                }

                if (up != null && !up.isConsumed) { onTap() }
            }
        }
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

    val divergingIndex = newBackStack.indices.firstOrNull { index -> newBackStack[index] != oldBackStack[index] }
    // if newBackStack never diverged from oldBackStack, then it is a clean subset of the oldStack
    // and is a pop
    return divergingIndex == null && newBackStack.size != oldBackStack.size
}

private fun getPairing(mainKey: Any): TwoPanePairing? {
    val main = mainKey.toString().substringBefore('(')
    return mainSecondaryMap.entries.find { it.key.toString().substringBefore('(') == main }?.value
}

private fun validPairing(pairing: TwoPanePairing, secondKey: Any): Boolean {
    val second = secondKey.toString().substringBefore('(')
    return pairing.allowedSeconds.any { it.simpleName == second }
}