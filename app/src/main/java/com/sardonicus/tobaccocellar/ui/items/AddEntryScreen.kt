package com.sardonicus.tobaccocellar.ui.items

import android.R.attr.contentDescription
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object AddEntryDestination : NavigationDestination {
    override val route = "add_entry"
    override val titleRes = R.string.add_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: AddEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current


    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(AddEntryDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                modifier = Modifier
                    .noRippleClickable(onClick = { focusManager.clearFocus() }),
                navigateUp = onNavigateUp,
                showMenu = false,
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AddEntryBody(
                itemUiState = viewModel.itemUiState,
                existState = viewModel.existState,
                tinConversion = viewModel.tinConversion.value,
                resetExistState = viewModel::resetExistState,
                onItemValueChange = viewModel::updateUiState,
                onTinValueChange = viewModel::updateTinConversion,
                onSaveClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            viewModel.checkItemExistsOnSave()
                            if (!viewModel.existState.exists) {
                                viewModel.saveItem()
                                navigateBack()
                            }
                        }
                    }
                },
                onDeleteClick = {
                    coroutineScope.launch {
                        viewModel.deleteItem()
                        navigateBack()
                    }
                },
                isEditEntry = false,
                navigateToEditEntry = navigateToEditEntry,
                modifier = modifier
                    .padding(0.dp)
                    .fillMaxSize(),
            )
        }
    }
}


@Composable
fun AddEntryBody(
    itemUiState: ItemUiState,
    tinConversion: TinConversion,
    existState: ExistState,
    onItemValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinConversion) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isEditEntry: Boolean,
    modifier: Modifier = Modifier,
    navigateToEditEntry: (Int) -> Unit = {},
    resetExistState: () -> Unit = {},
) {
    var deleteConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            itemUiState = itemUiState,
            tinConversion = tinConversion,
            onValueChange = onItemValueChange,
            onTinValueChange = onTinValueChange,
            isEditEntry = isEditEntry,
            modifier = Modifier
                .fillMaxWidth()
        )
        Button(
            onClick = { onSaveClick() },
            enabled = itemUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 0.dp),
        ) {
            Text(text = if (!isEditEntry) stringResource(R.string.save) else stringResource(R.string.update))
        }
        if (existState.existCheck) {
            ItemExistsDialog(
                onItemExistsConfirm = {
                    resetExistState()
                    navigateToEditEntry(existState.transferId)
                },
                onItemExistsCancel = { resetExistState() },
            )
        }
        if (isEditEntry) {
            Button(
                onClick = { deleteConfirm = true },
                enabled = true,
                shape = MaterialTheme.shapes.small,
                colors = ButtonColors(
                    containerColor = LocalCustomColors.current.deleteButton,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 0.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.delete_forever), contentDescription = null)
                Text(text = stringResource(R.string.delete))
            }
            if (deleteConfirm) {
                DeleteConfirmationDialog(
                    onDeleteConfirm = {
                        deleteConfirm = false
                        onDeleteClick()
                    },
                    onDeleteCancel = { deleteConfirm = false },
                    modifier = Modifier
                        .padding(0.dp)
                )
            }
        } else {
            // do nothing //
        }
    }
}


/** Dialogs **/
@Composable
fun ItemExistsDialog(
    onItemExistsConfirm: () -> Unit,
    onItemExistsCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.item_exists)) },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        dismissButton = {
            TextButton(onClick = onItemExistsCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onItemExistsConfirm) {
                Text(stringResource(R.string.yes))
            }
        }
    )
}

@Composable
fun ItemExistsEditDialog(
    onItemExistsConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = {
            Text(
                text = "An entry already exists with this combination of Brand and Blend—the combination of Brand and Blend must be unique for each entry.",
                softWrap = true,
            )
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        confirmButton = {
            TextButton(onClick = onItemExistsConfirm) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.delete_entry)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        }
    )
}


/** Body Elements **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemInputForm(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    tinConversion: TinConversion,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinConversion) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val titles = listOf("Item Details", "Notes")


    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .padding(0.dp, bottom = 8.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = LocalContentColor.current,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier
                    //    .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            },
            divider = {
                HorizontalDivider(
                    modifier = Modifier,
                    thickness = Dp.Hairline,
                    color = DividerDefaults.color,
                )
            },
        ) {
            titles.forEachIndexed { index, title ->
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                        },
                        modifier = Modifier
                            .background(
                                if (selectedTabIndex == index) MaterialTheme.colorScheme.background
                                else LocalCustomColors.current.backgroundUnselected
                            ),
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.SemiBold,
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onBackground,
                        unselectedContentColor = MaterialTheme.colorScheme.outline,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                }
            }
        }
        when (selectedTabIndex) {
            0 -> ItemDetailsEntry(
                itemDetails = itemDetails,
                itemUiState = itemUiState,
                tinConversion = tinConversion,
                isEditEntry = isEditEntry,
                onValueChange = onValueChange,
                onTinValueChange = onTinValueChange,
                modifier = Modifier,
            )

            1 -> NotesEntry(
                itemDetails = itemDetails,
                onValueChange = onValueChange,
                modifier = Modifier
            )

            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}


@Composable
fun ItemDetailsEntry(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    tinConversion: TinConversion,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinConversion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var showTinConverter by rememberSaveable { mutableStateOf(false) }

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(
                onClick = {
                    focusManager.clearFocus()
                }
            )
            .padding(top = 8.dp, bottom = 0.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Required Fields //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Required Fields:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Brand //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Brand:",
                    modifier = Modifier
                        .width(80.dp)
                )

                val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

                AutoCompleteText(
                    value = itemDetails.brand,
                    onValueChange = {
                        onValueChange(itemDetails.copy(brand = it))

                        if (it.length >= 2) {
                            val startsWith = itemUiState.autoBrands.filter { brand ->
                                brand.startsWith(it, ignoreCase = true)
                            }
                            val otherWordsStartsWith = itemUiState.autoBrands.filter { brand ->
                                brand.split(" ").drop(1).any { word ->
                                    word.startsWith(it, ignoreCase = true)
                                } && !brand.startsWith(it, ignoreCase = true)
                            }
                            val contains = itemUiState.autoBrands.filter { brand ->
                                brand.contains(it, ignoreCase = true)
                                        && !brand.startsWith(it, ignoreCase = true) &&
                                        !otherWordsStartsWith.contains(brand)
                            }
                            val selected = itemUiState.autoBrands.filter { brand ->
                                brand == it
                            }

                            suggestions.value = (startsWith + otherWordsStartsWith + contains) - selected
                        } else {
                            suggestions.value = emptyList()
                        }
                    },
                    onOptionSelected = {
                        onValueChange(itemDetails.copy(brand = it))
                        suggestions.value = emptyList()
                    },
                    suggestions = suggestions.value,
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = {
                        if (isEditEntry) Text(
                            text = "(" + itemDetails.originalBrand + ")",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    },
                    trailingIcon = {
                        if (itemDetails.brand.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable {
                                        onValueChange(itemDetails.copy(brand = ""))
                                    }
                                    .alpha(0.66f)
                                    .size(20.dp)
                                    .focusable(false)
                            )
                        }
                    }
                )
            }

            // Blend //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blend:",
                    modifier = Modifier
                        .width(80.dp)
                )
                TextField(
                    value = itemDetails.blend,
                    onValueChange = { onValueChange(itemDetails.copy(blend = it)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = true,
                    singleLine = true,
                    placeholder = {
                        if (isEditEntry) Text(
                            text = "(" + itemDetails.originalBlend + ")",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    },
                    trailingIcon = {
                        if (itemDetails.blend.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable {
                                        onValueChange(itemDetails.copy(blend = ""))
                                    }
                                    .alpha(0.66f)
                                    .size(20.dp)
                                    .focusable(false)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = LocalCustomColors.current.textField,
                        unfocusedContainerColor = LocalCustomColors.current.textField,
                        disabledContainerColor = LocalCustomColors.current.textField,
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                )
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .size(12.dp)
        )

        // Optional //
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Optional:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Type //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Type:",
                        modifier = Modifier
                            .width(80.dp)
                    )
                    TypeDropDown(
                        selectedValue = itemDetails.type,
                        onValueChange = { onValueChange(itemDetails.copy(type = it)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }

                // Quantity //
                Row(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No. of Tins:",
                        modifier = Modifier
                            .width(80.dp)
                    )
                    Row(
                        modifier = Modifier
                            .padding(0.dp)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val pattern = remember { Regex("^(\\s*|\\d+)\$") }
                        TextField(
                            value = itemDetails.squantity,
                            onValueChange = {
                                if (it.matches(pattern) && it.length <= 2) {
                                    onValueChange(
                                        itemDetails.copy(
                                            squantity = it,
                                            quantity = it.toIntOrNull() ?: 1
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .width(54.dp)
                                .padding(0.dp),
                            visualTransformation = VisualTransformation.None,
                            enabled = true,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = LocalCustomColors.current.textField,
                                unfocusedContainerColor = LocalCustomColors.current.textField,
                                disabledContainerColor = LocalCustomColors.current.textField,
                            ),
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        Row(
                            modifier = Modifier
                                .padding(0.dp)
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_up),
                                contentDescription = "Increase Quantity",
                                modifier = Modifier
                                    .align(Alignment.Top)
                                    .clickable {
                                        if (itemDetails.squantity.isEmpty()) {
                                            onValueChange(
                                                itemDetails.copy(
                                                    squantity = "1",
                                                    quantity = 1
                                                )
                                            )
                                        } else {
                                            if (itemDetails.squantity.toInt() < 99) {
                                                onValueChange(
                                                    itemDetails.copy(
                                                        squantity = (itemDetails.squantity.toInt() + 1).toString(),
                                                        quantity = itemDetails.squantity.toInt() + 1
                                                    )
                                                )
                                            } else {
                                                onValueChange(
                                                    itemDetails.copy(
                                                        squantity = "99",
                                                        quantity = 99
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    .padding(start = 8.dp, end = 2.dp, top = 4.dp, bottom = 4.dp)
                                    .offset(x = 1.dp, y = 2.dp)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_down),
                                contentDescription = "Decrease Quantity",
                                modifier = Modifier
                                    .align(Alignment.Bottom)
                                    .clickable {
                                        if (itemDetails.squantity.isEmpty()) {
                                            /* do nothing */
                                        } else {
                                            if (itemDetails.squantity.toInt() > 0) {
                                                onValueChange(
                                                    itemDetails.copy(
                                                        squantity = (itemDetails.squantity.toInt() - 1).toString(),
                                                        quantity = itemDetails.quantity - 1
                                                    )
                                                )
                                            } else if (itemDetails.squantity.toInt() == 0) {
                                                onValueChange(
                                                    itemDetails.copy(
                                                        squantity = "0",
                                                        quantity = 0
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    .padding(
                                        start = 2.dp,
                                        end = 8.dp,
                                        top = 4.dp,
                                        bottom = 4.dp
                                    )
                                    .offset(x = (-1).dp, y = (-2).dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Tin Converter",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    onTinValueChange(
                                        tinConversion.copy(
                                            amount = "",
                                            unit = ""
                                        )
                                    )
                                    showTinConverter = true
                                }
                                .padding(2.dp)
                        )
                    }
                }

//                        IconButton(
//                            onClick = {
//                                if (itemDetails.squantity.isEmpty()) {
//                                    onValueChange(
//                                        itemDetails.copy(
//                                            squantity = "1",
//                                            quantity = 1
//                                        )
//                                    )
//                                } else {
//                                    if (itemDetails.squantity.toInt() < 99) {
//                                        onValueChange(
//                                            itemDetails.copy(
//                                                squantity = (itemDetails.squantity.toInt() + 1).toString(),
//                                                quantity = itemDetails.squantity.toInt() + 1
//                                            )
//                                        )
//                                    } else {
//                                        onValueChange(
//                                            itemDetails.copy(
//                                                squantity = "99",
//                                                quantity = 99
//                                            )
//                                        )
//                                    }
//                                }
//                            },
//                            modifier = Modifier
//                                .padding(0.dp)
//                                .semantics { contentDescription = "Increase Quantity" }
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.arrow_up),
//                                contentDescription = "Increase Quantity",
//                                modifier = Modifier
//                                    .padding(0.dp)
//                                    .offset(x = 0.dp, y = (-4).dp)
//                            )
//                        }
//                        IconButton(
//                            onClick = {
//                                if (itemDetails.squantity.isEmpty()) {
//                                    /* do nothing */
//                                } else {
//                                    if (itemDetails.squantity.toInt() > 0) {
//                                        onValueChange(
//                                            itemDetails.copy(
//                                                squantity = (itemDetails.squantity.toInt() - 1).toString(),
//                                                quantity = itemDetails.quantity - 1
//                                            )
//                                        )
//                                    } else if (itemDetails.squantity.toInt() == 0) {
//                                        onValueChange(
//                                            itemDetails.copy(
//                                                squantity = "0",
//                                                quantity = 0
//                                            )
//                                        )
//                                    }
//                                }
//                            },
//                            modifier = Modifier
//                                .padding(0.dp)
//                                .semantics { contentDescription = "Decrease Quantity" }
//                                .offset(x = (-12).dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.arrow_down),
//                                contentDescription = "Decrease Quantity",
//                                modifier = Modifier
//                                    .padding(0.dp)
//                                    .offset(x = 0.dp, y = 4.dp)
//                            )
//                        }
//                        TextButton(
//                            onClick = {
//                                onTinValueChange(
//                                    tinConversion.copy(
//                                        amount = "",
//                                        unit = ""
//                                    )
//                                )
//                                showTinConverter = true
//                            },
//                            modifier = Modifier
//                                .padding(0.dp)
//                        ) {
//                            Text(
//                                text = "Tin Converter",
//                                modifier = Modifier
//                                    .padding(0.dp),
//                            )
//                        }
//                    }
//                }

                // Favorite or Disliked? //
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Favorite?",
                            modifier = Modifier
                                .offset(x = 0.dp, y = 1.dp)
                        )
                        CustomCheckBox(
                            checked = itemDetails.favorite,
                            onCheckedChange = {
                                if (itemDetails.favorite) {
                                    onValueChange(itemDetails.copy(favorite = it))
                                } else {
                                    onValueChange(
                                        itemDetails.copy(
                                            favorite = it,
                                            disliked = false
                                        )
                                    )
                                }
                            },
                            checkedIcon = R.drawable.heart_filled_24,
                            uncheckedIcon = R.drawable.heart_outline_24,
                            modifier = Modifier
                                .padding(0.dp),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContentColor = LocalCustomColors.current.favHeart,
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Disliked?",
                            modifier = Modifier
                                .offset(x = 0.dp, y = 1.dp)
                        )
                        CustomCheckBox(
                            checked = itemDetails.disliked,
                            onCheckedChange = {
                                if (itemDetails.disliked) {
                                    onValueChange(itemDetails.copy(disliked = it))
                                } else {
                                    onValueChange(
                                        itemDetails.copy(
                                            disliked = it,
                                            favorite = false
                                        )
                                    )
                                }
                            },
                            checkedIcon = R.drawable.heartbroken_filled_24,
                            uncheckedIcon = R.drawable.heartbroken_outlined_24,
                            modifier = Modifier
                                .padding(0.dp),
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                checkedContentColor = LocalCustomColors.current.disHeart,
                            )
                        )
                    }
                }
            }
        }

        if (showTinConverter) {
            TinConverterDialog(
                onDismiss = { showTinConverter = false },
                onConfirm = {
                    if (it < 99) {
                        onValueChange(
                            itemDetails.copy(
                                squantity = it.toString(),
                                quantity = it
                            )
                        )
                    } else {
                        onValueChange(
                            itemDetails.copy(
                                squantity = "99",
                                quantity = 99
                            )
                        )
                    }
                    showTinConverter = false
                },
                onAddConversion = {
                    val existingAmount = itemDetails.squantity.toInt()
                    val newAmount = existingAmount + it

                    if (newAmount < 99) {
                        onValueChange(
                            itemDetails.copy(
                                squantity = newAmount.toString(),
                                quantity = newAmount
                            )
                        )
                    } else {
                        onValueChange(
                            itemDetails.copy(
                                squantity = "99",
                                quantity = 99
                            )
                        )
                    }
                    showTinConverter = false
                },
                tinConversion = tinConversion,
                onTinValueChange = onTinValueChange,
                isEditEntry = isEditEntry,
                itemDetails = itemDetails,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun NotesEntry(
    itemDetails: ItemDetails,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
        TextField(
            value = itemDetails.notes,
            onValueChange = {
                var updatedText = it
                if (it.contains("\n")) {
                    val lines = it.lines()
                    if (lines.size > 1) {
                        val lastLine = lines[lines.size - 2]
                        val currentLine = lines.last()
                        val lastWord = lastLine.substringAfterLast(" ")
                        if (currentLine.startsWith(lastWord)) {
                            if (currentLine.length == lastWord.length + 1) {
                                updatedText = it.dropLast(lastWord.length + 1)
                            } else {
                                updatedText = it.dropLast(lastWord.length)
                            }
                        }
                    }
                }
                onValueChange(itemDetails.copy(notes = updatedText))
            },
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None,
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField,
            ),
            shape = MaterialTheme.shapes.extraSmall,
            singleLine = false,
            maxLines = 6,
            minLines = 6,
        )
        Spacer(
            modifier = Modifier
                .height(6.dp)
        )
    }
}


/** custom composables */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinConverterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onAddConversion: (Int) -> Unit,
    tinConversion: TinConversion,
    onTinValueChange: (TinConversion) -> Unit,
    isEditEntry: Boolean,
    itemDetails: ItemDetails,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf(tinConversion.amount) }
    var unit by remember { mutableStateOf(tinConversion.unit) }
    var expanded by remember { mutableStateOf(false) }
    val unitList = listOf("oz", "lb", "grams")

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tin Converter",
                    modifier = Modifier
                        .padding(bottom = 16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // converter inputs //
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .weight(.5f)
                        )
                        val pattern = remember { Regex("^(\\s*|\\d+(\\.\\d{0,2})?)\$") }
                        TextField(
                            value = amount,
                            onValueChange = {
                                if (it.matches(pattern)) {
                                    amount = it
                                    onTinValueChange(
                                        tinConversion.copy(
                                            amount = it
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.8f),
                            placeholder = {
                                Text(
                                    text = "Amount",
                                    color = LocalContentColor.current.copy(alpha = 0.5f),
                                    modifier = Modifier,
                                    style = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                )
                            },
                            visualTransformation = VisualTransformation.None,
                            enabled = true,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = LocalCustomColors.current.textField,
                                unfocusedContainerColor = LocalCustomColors.current.textField,
                                disabledContainerColor = LocalCustomColors.current.textField,
                            ),
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier
                                .weight(2.2f)
                        ) {
                            TextField(
                                value = unit,
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField,
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                placeholder = {
                                    Text(
                                        text = "Unit",
                                        color = LocalContentColor.current.copy(alpha = 0.5f)
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier,
                                matchTextFieldWidth = true,
                                containerColor = LocalCustomColors.current.textField,
                            ) {
                                unitList.forEach { option: String ->
                                    DropdownMenuItem(
                                        text = { Text(text = option) },
                                        onClick = {
                                            expanded = false
                                            unit = option
                                            onTinValueChange(
                                                tinConversion.copy(unit = option)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .weight(.5f)
                        )
                    }
                    // buttons //
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val convertedQuantity = if (tinConversion.isConversionValid) {
                            convertQuantity(
                                tinConversion.amount.toDouble(),
                                tinConversion.unit,
                                tinConversion.ozRate,
                                tinConversion.gramsRate
                            )
                        } else { 0 }

                        // convert/change button //
                        Column(
                            modifier = Modifier,
                            verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.Top),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "(${convertedQuantity} tins)",
                                modifier = Modifier,
                                fontSize = 12.sp,
                                color =
                                    if (tinConversion.isConversionValid)
                                        LocalContentColor.current.copy(alpha = 0.5f)
                                    else Color.Transparent
                            )
                            Button(
                                onClick = {
                                    onConfirm(convertedQuantity)
                                },
                                modifier = Modifier,
                                enabled = tinConversion.isConversionValid,
                            ) {
                                Text(
                                    text = if (isEditEntry) "Change" else "Convert",
                                    modifier = Modifier
                                )
                            }
                        }

                        // add to button for edit entry //
                        if (isEditEntry) {
                            Column(
                                modifier = Modifier,
                                verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.Top),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val newAmount =
                                    if (tinConversion.isConversionValid) {
                                        itemDetails.squantity.toInt() + convertedQuantity
                                    } else { 0 }

                                Text(
                                    text = "(${newAmount} tins)",
                                    modifier = Modifier,
                                    fontSize = 12.sp,
                                    color =
                                        if (tinConversion.isConversionValid)
                                            LocalContentColor.current.copy(alpha = 0.5f)
                                        else Color.Transparent
                                )
                                Button(
                                    onClick = {
                                        onAddConversion(convertedQuantity)
                                    },
                                    modifier = Modifier,
                                    enabled = tinConversion.isConversionValid,
                                ) {
                                    Text(
                                        text = "Add To",
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .height(4.dp)
                    )
                }
            }
        }
    }
}

fun convertQuantity(amount: Double, unit: String, ozRate: Double, gramsRate: Double): Int {
    val convertedAmount = when (unit) {
        "oz" -> amount / ozRate
        "lb" -> (amount * 16) / ozRate
        "grams" -> amount / gramsRate
        else -> amount
    }
    return kotlin.math.round(convertedAmount).toInt()
}

@Composable
fun CustomCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    checkedIcon: Int,
    uncheckedIcon: Int,
    modifier: Modifier = Modifier,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier,
        colors = colors
    ) {
        Icon(
            imageVector = if (checked) {
                ImageVector.vectorResource(id = checkedIcon)
            } else ImageVector.vectorResource(id = uncheckedIcon),
            contentDescription = null,
            modifier = modifier
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteText(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)?,
    onOptionSelected: (String) -> Unit,
    suggestions: List<String> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(value)) }
    val focusRequester = remember { FocusRequester() }
    val focusState = remember { mutableStateOf(false) }

    LaunchedEffect(suggestions) {
        // Delay expanded state evaluation
        expanded = value.isNotEmpty() && suggestions.isNotEmpty()
        if (suggestions.isEmpty()) {
            expanded = false
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            expanded = false
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && focusState.value && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
            .padding(0.dp)
    ) {
        TextField(
            value = textFieldValueState.copy(text = value),
            onValueChange = {
                textFieldValueState = it
                onValueChange?.invoke(it.text)
                expanded = it.text.isNotEmpty() && suggestions.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .onFocusChanged { focusState.value = it.isFocused }
                .focusRequester(focusRequester)
                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
            enabled = true,
            trailingIcon = trailingIcon,
            singleLine = true,
            placeholder = { if (placeholder != null) placeholder() },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField,
            ),
            shape = MaterialTheme.shapes.extraSmall
        )
        DropdownMenu(
            expanded = expanded && focusState.value && suggestions.isNotEmpty(),
            onDismissRequest = { focusState.value },
            modifier = Modifier
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                .heightIn(max = 82.dp),
            properties = PopupProperties(focusable = false),
            offset = DpOffset(32.dp, (-12).dp),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            suggestions.take(3).forEach { label ->
                CustomDropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            modifier = Modifier
                                .padding(0.dp)
                                .focusable(false),
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        onOptionSelected(label)
                        textFieldValueState = TextFieldValue(
                            text = label,
                            selection = TextRange(label.length)
                        )
                        expanded = false
                    },
                    enabled = true,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp)
                        .offset(0.dp, 0.dp)
                        .fillMaxWidth(),
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
    }
}


@Composable
fun CustomDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = false,
    modifier: Modifier,
    colors: MenuItemColors = MenuDefaults.itemColors(
        textColor = LocalContentColor.current,
    ),
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
    ) {
        text()
        if (enabled) {
            colors.copy(
                textColor = colors.textColor
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeDropDown(
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("", "Aromatic", "English", "Burley", "Virginia", "Other")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedValue,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField,
            ),
            shape = MaterialTheme.shapes.extraSmall
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier,
            matchTextFieldWidth = true,
            containerColor = LocalCustomColors.current.textField,
        ) {
            options.forEach { option: String ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    }
                )
            }
        }
    }
}
