package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JumpToButton(
    columnState: LazyListState,
    itemCountPass: () -> Boolean,
    onScrollToTop: () -> Unit,
    onScrollToBottom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val jumpToState = rememberJumpToState(columnState)

    AnimatedVisibility(
        visible = jumpToState.first.value && itemCountPass(),
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = {
                jumpToState.second.value
                if (jumpToState.second.value == ScrollDirection.DOWN) {
                    onScrollToBottom()
                } else {
                    onScrollToTop()
                }
            },
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            containerColor = LocalCustomColors.current.whiteBlack.copy(alpha = 0.45f),
            contentColor = LocalCustomColors.current.whiteBlackInverted.copy(alpha = 0.45f),
            modifier = modifier
                .border(Dp.Hairline, LocalCustomColors.current.whiteBlackInverted.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = if (jumpToState.second.value == ScrollDirection.DOWN) R.drawable.double_down else R.drawable.double_up),
                contentDescription = if (jumpToState.second.value == ScrollDirection.DOWN) "Scroll to bottom" else "Scroll to top",
                modifier = Modifier
                    .size(36.dp),
            )
        }
    }
}

@Composable
private fun rememberJumpToState(
    lazyListState: LazyListState,
): Pair<State<Boolean>, State<ScrollDirection>> {
    val scrollDirection = produceState(initialValue = ScrollDirection.UP, key1 = lazyListState) {
        var previousIndex = 0
        val updatePrevious: (Int) -> Unit = { previousIndex = it }
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex > previousIndex) {
                    value = ScrollDirection.DOWN
                } else if (currentIndex < previousIndex) {
                    value = ScrollDirection.UP
                }
                updatePrevious(currentIndex)
            }
    }

    val isVisible = produceState(initialValue = false, key1 = lazyListState, key2 = scrollDirection.value) {
        var delayJob: Job? = null
        val updateJob: (Job) -> Unit = { delayJob = it }

        snapshotFlow { Triple(
            lazyListState.isScrollInProgress,
            !lazyListState.canScrollBackward,
            !lazyListState.canScrollForward
        ) }.collect { (isScrolling, atTop, atBottom) ->
            delayJob?.cancel()

            val overScroll = (atTop && scrollDirection.value == ScrollDirection.UP) ||
                    (atBottom && scrollDirection.value == ScrollDirection.DOWN)

            if (isScrolling && !overScroll) {
                if (!value) {
                    delay(25)
                    value = true
                }
            } else {
                updateJob(
                    launch {
                        val delayMillis = if (atTop || atBottom) 0 else 1500L
                        delay(delayMillis)
                        value = false
                    }
                )
            }
        }
    }
    return Pair(isVisible, scrollDirection)
}


enum class ScrollDirection { UP, DOWN }