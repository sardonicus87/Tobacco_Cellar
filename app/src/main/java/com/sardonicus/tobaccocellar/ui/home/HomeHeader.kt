package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeHeader(
    viewModel: HomeViewModel,
    filterViewModel: FilterViewModel,
    updateSearchText: (String) -> Unit,
    onSearch: (String) -> Unit,
    updateSearchFocused: (Boolean) -> Unit,
    getPositionTrigger: () -> Unit,
    saveSearchSetting: (String) -> Unit,
    onExpandSearchMenu: (Boolean) -> Unit,
    onShowColumnPop: () -> Unit,
    saveListSorting: (ListSortOption) -> Unit,
    shouldScrollUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LocalCustomColors.current.homeHeaderBg)
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Select view
        ViewSelect(
            viewModel = viewModel,
            //    selectView = selectView,
            modifier = Modifier
                .width(74.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Search field
        Box(
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .weight(1f, false),
        ) {
            SearchField(
                filterViewModel = filterViewModel,
                updateSearchText = updateSearchText,
                onSearch = onSearch,
                updateSearchFocused = updateSearchFocused,
                getPositionTrigger = getPositionTrigger,
                saveSearchSetting = saveSearchSetting,
                onExpandSearchMenu = onExpandSearchMenu,
                modifier = Modifier
            )
        }

        Spacer(Modifier.width(8.dp))

        // total items & list sorting or column hiding
        Row(
            modifier = modifier
                .width(68.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ListColumnMenu(
                viewModel = viewModel,
                shouldScrollUp = shouldScrollUp,
                saveListSorting = saveListSorting,
                onShowColumnPop = onShowColumnPop,
                modifier = Modifier
            )
            Spacer(Modifier.width(6.dp))
            TotalCount(viewModel)
        }
    }
}

@Composable
private fun ViewSelect(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.viewSelect.collectAsState()

    Row(
        modifier = modifier
            .padding(0.dp)
            .width(74.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "View:",
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(0.dp)
        )
        IconButton(
            onClick = viewModel::selectView,
            modifier = Modifier
                .padding(4.dp)
                .size(22.dp)
        ) {
            Icon(
                painter = painterResource(state.toggleIcon),
                contentDescription = stringResource(state.toggleContentDescription),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(20.dp)
                    .padding(0.dp)
            )
        }
    }
}

@Composable
private fun SearchField (
    filterViewModel: FilterViewModel,
    updateSearchText: (String) -> Unit,
    onSearch: (String) -> Unit,
    updateSearchFocused: (Boolean) -> Unit,
    getPositionTrigger: () -> Unit,
    saveSearchSetting: (String) -> Unit,
    onExpandSearchMenu: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by filterViewModel.searchState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    CustomBlendSearch(
        value = { state.searchText },
        onValueChange = {
            updateSearchText(it)
            if (it.isEmpty()) {
                onSearch(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) updateSearchFocused(true)
                else updateSearchFocused(false)
            },
        onImeAction = {
            coroutineScope.launch {
                if (state.searchText.isNotBlank()) {
                    if (!state.searchPerformed) { getPositionTrigger() }
                    delay(15)
                    EventBus.emit(SearchPerformedEvent)
                    onSearch(state.searchText)
                }
            }
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable(
                        enabled = state.settingsEnabled && !state.emptyDatabase,
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { onExpandSearchMenu(!state.searchMenuExpanded) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(20.dp),
                    tint = LocalContentColor.current.copy(alpha = state.searchIconOpacity)
                )
                Icon(
                    painter = painterResource(id = R.drawable.triangle_arrow_down),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = 0.dp)
                        .padding(0.dp)
                        .size(16.dp),
                    tint = if (state.settingsEnabled && !state.emptyDatabase) LocalContentColor.current.copy(alpha = state.searchIconOpacity) else Color.Transparent
                )
            }
            DropdownMenu(
                expanded = state.searchMenuExpanded,
                onDismissRequest = { onExpandSearchMenu(false) },
                modifier = Modifier,
                containerColor = LocalCustomColors.current.textField,
                shadowElevation = 4.dp,
                offset = DpOffset((-2).dp, 2.dp)
            ) {
                state.settingsList.settings.forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.value) },
                        onClick = {
                            saveSearchSetting(it.value)
                            onExpandSearchMenu(false)
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = true,
                    )
                }
            }
        },
        trailingIcon = {
            if (state.searchText.isNotEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.clear_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = null
                        ) {
                            updateSearchText("")
                            onSearch("")

                            if (state.searchPerformed) {
                                coroutineScope.launch {
                                    EventBus.emit(SearchClearedEvent)
                                }
                            }
                        }

                        .padding(0.dp),
                )
            }
        },
        placeholder = "${state.currentSetting.value} Search",
    )
}

@Composable
private fun ListColumnMenu(
    viewModel: HomeViewModel,
    shouldScrollUp: () -> Unit,
    saveListSorting: (ListSortOption) -> Unit,
    onShowColumnPop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.listSortingMenuState.collectAsState()

    Box(modifier = modifier) {
        // List Sorting
        if (!state.isTableView) {
            var sortingMenu by rememberSaveable { mutableStateOf(false) }

            IconButton(
                onClick = { sortingMenu = !sortingMenu },
                modifier = Modifier
                    .padding(4.dp)
                    .size(22.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sort_bars),
                    contentDescription = "List sorting",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(0.dp),
                )
            }
            DropdownMenu(
                expanded = sortingMenu,
                onDismissRequest = { sortingMenu = false },
                shadowElevation = 4.dp,
                modifier = Modifier,
                containerColor = LocalCustomColors.current.textField,
            ) {
                state.sortingOptions.options.forEach {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = it.value,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier
                                )
                                Box(
                                    modifier = Modifier
                                        .width(14.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (state.listSorting.option.value == it.value) {
                                        val icon = state.listSorting.listIcon
                                        Image(
                                            painter = painterResource(id = icon),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .offset(x = (-6).dp)
                                                .padding(0.dp),
                                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            saveListSorting(it)
                            shouldScrollUp()
                        },
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = true,
                    )
                }
            }
        } else {
            IconButton(
                onClick = { onShowColumnPop() },
                modifier = Modifier
                    .padding(4.dp)
                    .size(22.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.table_edit),
                    contentDescription = "Column Visibility",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(0.dp),
                )
            }
        }
    }
}

@Composable
private fun TotalCount(
    viewModel: HomeViewModel,
) {
    val count by viewModel.itemsCount.collectAsState()

    Box (contentAlignment = Alignment.CenterEnd) {
        Text(
            text = "999",
            modifier = Modifier,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            maxLines = 1,
            color = Color.Transparent
        )
        Text(
            text = "$count",
            modifier = Modifier,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            maxLines = 1,
        )
    }
}

data object SearchClearedEvent
data object SearchPerformedEvent

@Composable
private fun CustomBlendSearch(
    value: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Blend Search",
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    onImeAction: () -> Unit = {},
) {
    var showCursor by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value(),
        onValueChange = onValueChange,
        modifier = modifier
            .background(LocalCustomColors.current.textField, RoundedCornerShape(100f))
            .height(30.dp)
            .onFocusChanged { focusState ->
                hasFocus = focusState.hasFocus
                showCursor = focusState.hasFocus
                if (!focusState.hasFocus) {
                    focusManager.clearFocus()
                }
            }
            .padding(horizontal = 8.dp),
        textStyle = LocalTextStyle.current.copy(
            color = LocalContentColor.current,
            fontSize = TextUnit.Unspecified,
            lineHeight = 16.sp
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onImeAction()
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        cursorBrush = if (showCursor) {
            SolidColor(MaterialTheme.colorScheme.primary)
        } else {
            SolidColor(Color.Transparent)
        },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                leadingIcon()
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value().isEmpty() && !hasFocus) {
                        Text(
                            text = placeholder,
                            style = LocalTextStyle.current.copy(
                                color = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
                trailingIcon()
            }
        }
    )
}