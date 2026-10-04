package com.sardonicus.tobaccocellar.ui.home

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(R.string.help_faq_title),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                showMenu = false
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HelpBody()
        }
    }
}

@Composable
private fun HelpBody(
    modifier: Modifier = Modifier,
) {
    val columnState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val delayMillis = 210.milliseconds

    val sections: List<Pair<String, @Composable () -> Unit>> = listOf(
        stringResource(R.string.cellar_screen) to { CellarScreen() },
        stringResource(R.string.stats_screen) to { StatsPage() },
        stringResource(R.string.dates_screen) to { DatesPage() },
        stringResource(R.string.filtering) to { Filtering() },
        stringResource(R.string.adding_entries) to { AddingItems() },
        stringResource(R.string.editing_entries) to { EditingItems() },
        stringResource(R.string.adding_tins) to { AddingTins() },
        stringResource(R.string.settings_title) to { Settings() },
        stringResource(R.string.multi_device_sync) to { MultiSync() },
    )

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
        state = columnState,
        modifier = modifier.fillMaxWidth().padding(0.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.help_body1),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp, top = 12.dp),
                softWrap = true
            )
        }
        item {
            Text(
                text = stringResource(R.string.help_body2),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                softWrap = true
            )
        }

        // Help sections
        item {
            Text(
                text = stringResource(R.string.help),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                softWrap = true,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        itemsIndexed(sections) { index, section ->
            HelpSection(
                title = section.first,
                content = section.second,
                onExpanded = {
                    scope.launch {
                        delay(delayMillis)
                        columnState.animateScrollToItem(index + 3)
                    }
                }
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}



/** Sections */
@Composable
private fun CellarScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.help_cellar1),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_cellar2),
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_cellar3),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_cellar4),
            softWrap = true
        )

        // View Mode
        Text(
            text = stringResource(R.string.help_cellar5),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_cellar6),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_cellar7),
            softWrap = true
        )

        Text(
            text = stringResource(R.string.help_cellar8),
            softWrap = true
        )
    }
}

@Composable
private fun StatsPage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_stats),
            softWrap = true
        )
    }
}

@Composable
private fun DatesPage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_dates1),
            softWrap = true
        )

        // Aging Tracker
        Text(
            text = "Aging Tracker",
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_dates2),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_dates3),
            softWrap = true
        )

        // Quick Stats
        Text(
            text = stringResource(R.string.help_dates4),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_dates5),
            softWrap = true
        )

        // Oldest/Future Tins
        Text(
            text = stringResource(R.string.help_dates6),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_dates7),
            softWrap = true
        )
    }
}

@Composable
private fun Filtering(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_filtering1),
            softWrap = true
        )

        // Basic Use
        Text(
            text = stringResource(R.string.help_filtering2),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_filtering3),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering4),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering5),
            softWrap = true
        )

        // Filtering
        Text(
            text = stringResource(R.string.help_filtering6),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_filtering7),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering8),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering9),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering10),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering11),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering12),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering13),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering14),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_filtering15),
            softWrap = true
        )
    }
}

@Composable
private fun AddingItems(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_adding1),
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = stringResource(R.string.help_adding2),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_adding3),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_adding4),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_adding5),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_adding6),
            softWrap = true
        )

        Text(
            text = stringResource(R.string.help_adding7),
            softWrap = true
        )
    }
}

@Composable
private fun EditingItems(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_editing1),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_editing2),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_editing3),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_editing4),
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.error,
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_editing5),
            softWrap = true
        )
    }
}

@Composable
private fun AddingTins(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        // Basic use
        Text(
            text = stringResource(R.string.help_add_tin1),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_add_tin2),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_add_tin3),
            softWrap = true
        )

        // Quantity
        Text(
            text = stringResource(R.string.quantity),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_add_tin4),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_add_tin5),
            softWrap = true
        )

        // Dates
        Text(
            text = stringResource(R.string.dates_title),
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_add_tin6),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_add_tin7),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_add_tin8),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_add_tin9),
            softWrap = true
        )
    }
}

@Composable
private fun Settings(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_settings1),
            softWrap = true
        )

        // Multi-Device Sync
        Text(
            text = stringResource(R.string.multi_device_sync),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Text(
            text = stringResource(R.string.help_settings2),
            softWrap = true
        )

        // Backup/restore
        Text(
            text = stringResource(R.string.backup_restore),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_settings3),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_settings4),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_settings5),
            modifier = Modifier,
            softWrap = true,
        )
        Text(
            text = stringResource(R.string.help_settings6),
            modifier = Modifier,
            softWrap = true,
        )

        // Conversion Rates
        Text(
            text = stringResource(R.string.tin_conversion_rates),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_settings7),
            softWrap = true
        )

        // Other Db operations
        Text(
            text = stringResource(R.string.other_operations),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_settings8),
            softWrap = true
        )
    }
}

@Composable
private fun MultiSync(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
    ) {
        Text(
            text = stringResource(R.string.help_multi1),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_multi2),
            softWrap = true
        )

        // Explanation of account link/data safety
        Text(
            text = stringResource(R.string.help_multi3),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_multi4),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_multi5),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_multi6),
            softWrap = true
        )

        val privacy = stringResource(R.string.privacy_policy)
        val data = stringResource(R.string.manage_data)
        val text = stringResource(R.string.help_multi7, privacy, data)

        val uriHandler = LocalUriHandler.current
        val hapticFeedback = LocalHapticFeedback.current
        var pressedRange by remember { mutableStateOf<TextRange?>(null) }

        val annotatedString = buildAnnotatedString {
            append(text)
            val privacyStart = text.indexOf(privacy)
            val privacyEnd = privacyStart + privacy.length
            val dataStart = text.indexOf(data)
            val dataEnd = dataStart + data.length

            if (privacyStart != -1) {
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://www.tobacco-cellar.com/privacy-policy",
                    start = privacyStart,
                    end = privacyEnd
                )
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = privacyStart,
                    end = privacyEnd
                )
            }
            if (dataStart != -1) {
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://www.tobacco-cellar.com/managing-data",
                    start = dataStart,
                    end = dataEnd
                )
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = dataStart,
                    end = dataEnd
                )
            }

            pressedRange?.let {
                addStyle(
                    style = SpanStyle(
                        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    start = it.start,
                    end = it.end
                )
            }
        }

        var pressedUrl by remember { mutableStateOf<String?>(null) }
        var tooltipPosition by remember { mutableStateOf(IntOffset.Zero) }
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        Box {
            Text(
                text = annotatedString,
                softWrap = true,
                onTextLayout = { layoutResult = it },
                modifier = Modifier
                    .pointerInput(annotatedString) {
                        detectTapGestures(
                            onTap = { offset ->
                                layoutResult?.let { result ->
                                    val index = result.getOffsetForPosition(offset)
                                    annotatedString.getStringAnnotations("URL", index, index).firstOrNull()?.let { range ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        uriHandler.openUri(range.item)
                                    }
                                }
                            },
                            onLongPress = { offset ->
                                layoutResult?.let { result ->
                                    val index = result.getOffsetForPosition(offset)
                                    annotatedString.getStringAnnotations("URL", index, index).firstOrNull()?.let { range ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        pressedUrl = range.item
                                        pressedRange = TextRange(range.start, range.end)

                                        val cursorRect = result.getCursorRect(index)
                                        val height = cursorRect.height + 30
                                        tooltipPosition = IntOffset(offset.x.toInt(), (cursorRect.top - height).toInt())
                                    }
                                }
                            },
                        )
                    }
            )

            if (pressedUrl != null) {
                LaunchedEffect(Unit) { delay(30.seconds); pressedUrl = null; pressedRange = null }

                Popup (
                    offset = tooltipPosition,
                    onDismissRequest = { pressedUrl = null; pressedRange = null }
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val clipboard = LocalClipboard.current
                    val clipboardText = stringResource(R.string.copied_clipboard)

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    uriHandler.openUri(pressedUrl!!)
                                    pressedUrl = null; pressedRange = null
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(ClipData.newPlainText("URL", pressedUrl!!))
                                        )
                                        Toast.makeText(context, clipboardText, Toast.LENGTH_SHORT).show()
                                        pressedUrl = null; pressedRange = null
                                    }
                                }
                            )
                    ) {
                        Text(
                            text = pressedUrl!!,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            lineHeight = 1.em,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        // How it Works
        Text(
            text = stringResource(R.string.help_multi8),
            modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = stringResource(R.string.help_multi9),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_multi10),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_multi11),
            softWrap = true
        )
        Text(
            text = stringResource(R.string.help_multi12),
            softWrap = true
        )
    }
}

/** Components **/
// Section  layout //
@Composable
private fun HelpSection(
    title: String,
    modifier: Modifier = Modifier,
    onExpanded: () -> Unit = {},
    content: @Composable (() -> Unit),
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(null, LocalIndication.current) {
                visible = !visible; if (visible) onExpanded() }
            .fillMaxWidth()
            .background(color = LocalCustomColors.current.backgroundVariant),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            painter = painterResource(id = if (visible) R.drawable.triangle_arrow_down else R.drawable.triangle_arrow_up),
            tint = if (visible)MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
            contentDescription = null,
            modifier = Modifier
                .padding(0.dp)
                .size(22.dp)
        )
    }
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 200),
            expandFrom = Alignment.Bottom) + fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 200),
            shrinkTowards = Alignment.Bottom) + fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            content()
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clickable(null, LocalIndication.current) { visible = !visible },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.double_up),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = LocalContentColor.current.copy(alpha = 0.75f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}