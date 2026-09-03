package com.sardonicus.tobaccocellar.ui.plaintext

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaintextActionRow(
    viewModel: PlaintextViewModel,
    filterViewModel: FilterViewModel,
    actionRowBounds: (LayoutCoordinates) -> Unit,
    otherBounds: (LayoutCoordinates) -> Unit,
    expanded: Boolean,
    toggleActionRow: () -> Unit,
    showPrintDialog: () -> Unit,
    plainList: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val buttonAlpha by animateFloatAsState(if (expanded) 1f else .5f, tween(300))
    val buttonColor by animateColorAsState(if (expanded) LocalCustomColors.current.homeHeaderBg
        else LocalCustomColors.current.whiteBlack)
    val borderColor by animateColorAsState(if (expanded) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f)
        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
    val backgroundColor by animateColorAsState(if (expanded) LocalCustomColors.current.homeHeaderBg
        else Color.Transparent)
    val iconRotation by animateFloatAsState(if (expanded) 180f else -0f,tween(450))

    SideEffect(plainList) { if (plainList.isBlank() && expanded) { toggleActionRow() } }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(end = if (expanded) 8.dp else 0.dp)
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            IconButton(
                onClick = toggleActionRow,
                shape = RoundedCornerShape(25),
                enabled = plainList.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { alpha = buttonAlpha }
                    .onGloballyPositioned { actionRowBounds(it) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = buttonColor
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = if (expanded) "Hide Action Row" else "Show Action Row",
                    modifier = Modifier.rotate(iconRotation),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(tween(300), Alignment.Start),
            exit = shrinkHorizontally(tween(300), Alignment.Start)
        ) {
            Row(
                modifier = Modifier.onGloballyPositioned { otherBounds(it) },
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Open filter sheet
                Box(contentAlignment = Alignment.Center) {
                    val filteringApplied by filterViewModel.isFilterApplied.collectAsState()
                    val borderColor =
                        if (filteringApplied) MaterialTheme.colorScheme.primary else Color.Transparent
                    val indicatorColor =
                        if (filteringApplied) LocalCustomColors.current.indicatorCircle else Color.Transparent

                    IconButton(
                        onClick = filterViewModel::openBottomSheet,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                        ),
                        modifier = Modifier
                            .padding(0.dp)
                            .size(40.dp)
                    ) { Icon(painterResource(id = R.drawable.filter_24), "Filter") }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(7.dp)
                            .offset((-5).dp, (-9).dp)
                            .clip(CircleShape)
                            .border(0.5.dp, borderColor, CircleShape)
                            .background(indicatorColor)
                    )
                }

                // Sorting
                SortingButton(viewModel)

                // Copy
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Plaintext", plainList)))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = plainList.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                    ),
                    modifier = Modifier
                        .padding(0.dp)
                        .size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.copy_icon),
                        contentDescription = "Copy all",
                        modifier = Modifier.padding(0.dp)
                    )
                }

                // Print
                IconButton(
                    onClick = { showPrintDialog() },
                    enabled = plainList.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                    ),
                    modifier = Modifier
                        .padding(0.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.print_icon),
                        contentDescription = "Print",
                        modifier = Modifier.padding(0.dp),
                    )
                }
            }
        }
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun SortingButton(
    viewModel: PlaintextViewModel,
    modifier: Modifier = Modifier
) {
    val sortState by viewModel.sortState.collectAsState()
    val sortOptions by viewModel.sortOptions.collectAsState()
    val sortMenuState by viewModel.sortMenuState.collectAsState()

    var mainMenu by remember { mutableStateOf(false) }
    var subMenu by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val screenWidth = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val screenHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }

    var anchorPosition by remember { mutableStateOf(Offset.Zero) }
    var mainPosition by remember { mutableStateOf(Offset.Zero) }
    var mainWidth by remember { mutableStateOf(0.dp) }
    val yPositions = remember { mutableStateMapOf<PlaintextSorting, Dp>() }

    BackHandler(mainMenu) { if (mainMenu) { mainMenu = false; subMenu = false } }

    Box(modifier = modifier.onGloballyPositioned { anchorPosition = it.positionOnScreen() }) {
        val alteredColor = Color.Black.copy(alpha = .1f).compositeOver(LocalCustomColors.current.textField)

        IconButton(
            onClick = {
                mainMenu = !mainMenu
                if (sortOptions.subOptions.containsKey(sortMenuState.mainSelection)) subMenu = true
            },
            enabled = viewModel.sortEnabled.collectAsState().value,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .padding(0.dp)
                .size(40.dp)
        ) { Icon(painterResource(id = R.drawable.sort_bars), "Sorting") }

        // Main options
        DropdownMenu(
            expanded = mainMenu,
            onDismissRequest = { mainMenu = false; subMenu = false },
            modifier = Modifier
                .heightIn(max = screenHeight * .65f)
                .onGloballyPositioned {
                    mainWidth = with(density) { it.size.width.toDp() }
                    mainPosition = it.positionOnScreen()
                },
            containerColor = if (subMenu) alteredColor else LocalCustomColors.current.textField,
            shadowElevation = 6.dp
        ) {
            sortOptions.mainOptions.forEach { option ->
                val hasSubOptions = sortOptions.subOptions.containsKey(option)

                if (sortOptions.mainOptions.contains(PlaintextSorting.TIN_DEFAULT)
                    && option == PlaintextSorting.DEFAULT) {
                    HorizontalDivider(Modifier.padding(start = 10.dp, end = 24.dp))
                }
                DropdownMenuItem(
                    text = {
                        Row(Modifier, Arrangement.Start, Alignment.CenterVertically) {
                            Text(
                                text = option.value,
                                modifier = Modifier.padding(end = 2.dp),
                                color = LocalContentColor.current.copy(alpha = if (subMenu) 0.85f else 1.0f)
                            )
                            // Sort indicator and/or submenu
                            if (sortState.value == option.value) {
                                Box {
                                    Image(
                                        painter = painterResource(id = sortState.icon),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(0.dp),
                                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                                    )
                                }
                            } else if (hasSubOptions) {
                                Box {
                                    Image(
                                        painter = painterResource(R.drawable.arrow_right),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        colorFilter = ColorFilter
                                            .tint(LocalContentColor.current.copy(alpha = 0.5f))
                                    )
                                }
                            } else { Spacer(Modifier.width(20.dp)) }
                        }
                    },
                    onClick = {
                        val isNew = sortMenuState.mainSelection != option
                        viewModel.updateSorting(mainOption = option)
                        subMenu = if (hasSubOptions) { if (isNew) true else !subMenu } else false
                    },
                    modifier = Modifier
                        .onGloballyPositioned {
                            yPositions[option] = with(density) { it.positionInParent().y.toDp() }
                        }
                        .background(
                            color = if (sortMenuState.mainSelection == option && subMenu)
                                LocalCustomColors.current.textField else Color.Transparent
                        )
                )
            }
        }

        // Sub sorting menu
        if (subMenu) {
            var subWidth by remember { mutableStateOf(0.dp) }

            val anchorPosDpX = with(density) { anchorPosition.x.toDp() }
            val anchorPosDpY = with(density) { anchorPosition.y.toDp() }
            val mainPosDpX = with(density) { mainPosition.x.toDp() }
            val mainPosDpY = with(density) { mainPosition.y.toDp() }

            val sideSpace = (screenWidth - (mainPosDpX + mainWidth)) * .95f
            val menuShiftX = mainPosDpX - anchorPosDpX
            val xOffset = if (subWidth > sideSpace) menuShiftX - subWidth else menuShiftX + mainWidth

            val menuShiftY = mainPosDpY - anchorPosDpY
            val yOffset = menuShiftY + (yPositions[sortMenuState.mainSelection] ?: 0.dp)

            DropdownMenu(
                expanded = subMenu,
                onDismissRequest = { },
                containerColor = LocalCustomColors.current.textField,
                properties = PopupProperties(focusable = false, clippingEnabled = false),
                modifier = Modifier
                    .heightIn(max = screenHeight * .60f)
                    .onGloballyPositioned { subWidth = with(density) { it.size.width.toDp() } },
                offset = DpOffset(xOffset, yOffset),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "Sub-sorting",
                    fontSize = 14.sp,
                    lineHeight = 1.em,
                    letterSpacing = 0.1.sp,
                    fontWeight = FontWeight.Medium,
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 6.dp, bottom = 2.dp)
                )

                sortOptions.subOptions[sortMenuState.mainSelection]?.forEach { subOption ->
                    if (sortOptions.subOptions[sortMenuState.mainSelection]?.contains(PlaintextSorting.TIN_DEFAULT) == true
                        && subOption == PlaintextSorting.DEFAULT) {
                        HorizontalDivider(Modifier.padding(start = 12.dp, end = 24.dp))
                    }
                    DropdownMenuItem(
                        text = {
                            Row(Modifier.padding(end = 10.dp), Arrangement.Start, Alignment.CenterVertically) {
                                Text("  ${subOption.value}", Modifier.padding(end = 8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (sortMenuState.subSelection == subOption)
                                                LocalContentColor.current else Color.Transparent
                                        )
                                )
                            }
                        },
                        onClick = { viewModel.updateSorting(subOption = subOption) }
                    )
                }
            }
        }
    }
}