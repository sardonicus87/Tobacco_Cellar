package com.example.tobaccocellar.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import com.example.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
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
                resetExistState = viewModel::resetExistState,
                onItemValueChange = viewModel::updateUiState,
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
    existState: ExistState,
    onItemValueChange: (ItemDetails) -> Unit,
    navigateToEditEntry: (Int) -> Unit,
    resetExistState: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isEditEntry: Boolean,
    modifier: Modifier = Modifier,
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
            onValueChange = onItemValueChange,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemInputForm(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val titles = listOf("Item Details", "Notes")
    val pagerState = rememberPagerState(initialPage = 0) { titles.size }
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        TabRow(
        //    selectedTabIndex = pagerState.currentPage,
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
                    //    selected = pagerState.currentPage == index,
                        selected = selectedTabIndex == index,
                        onClick = {
                            // selectedTabIndex = index
                            selectedTabIndex = index
//                            coroutineScope.launch {
//                                pagerState.scrollToPage(index)
//                            }
                        },
                        modifier = Modifier
                            .background(
//                                if (pagerState.currentPage == index) MaterialTheme.colorScheme.background
//                                else LocalCustomColors.current.backgroundUnselected
                                if (selectedTabIndex == index) MaterialTheme.colorScheme.background
                                else LocalCustomColors.current.backgroundUnselected
                            ),
                        text = {
                            Text(
                                text = title,
                            //    fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.SemiBold,
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

//        LaunchedEffect(pagerState.currentPage) {
//            pagerState.scrollToPage(pagerState.currentPage)
//        }

//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier,
//            verticalAlignment = Alignment.Top,
//        //    pageSize = PageSize.Fill,
//        //    beyondViewportPageCount = pagerState.pageCount,
//        ) {
            when (selectedTabIndex) {
                0 -> ItemDetailsEntry(
                    itemDetails = itemDetails,
                    itemUiState = itemUiState,
                    isEditEntry = isEditEntry,
                    onValueChange = onValueChange,
                    modifier = Modifier,
                )

                1 -> NotesEntry(
                    itemDetails = itemDetails,
                    onValueChange = onValueChange,
                    modifier = Modifier
                )

                else -> throw IllegalArgumentException("Invalid tab position")
            }
//        }

//        LaunchedEffect(pagerState) {
//            snapshotFlow { pagerState.currentPage }.collect { page ->
//                selectedTabIndex = page
//            }
//        }
    }
}


@Composable
fun ItemDetailsEntry(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier,
){
    val focusManager = LocalFocusManager.current

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

// Required Fields //
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
        Row(
            modifier = modifier
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
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

// Brand //
            Row(
                modifier = modifier
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
                            text = itemDetails.originalBrand,
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    },
                )
            }

// Blend //
            Row(
                modifier = modifier
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
                            text = itemDetails.originalBlend,
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    },
                    trailingIcon = {
                        if (itemDetails.blend.length > 5) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = null,
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
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = modifier
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
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

// Type //
                Row(
                    modifier = modifier
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
                    modifier = modifier
                        .padding(0.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quantity:",
                        modifier = Modifier
                            .width(80.dp)
                    )
                    Row(
                        modifier = Modifier
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val pattern = remember { Regex("^(\\s*|\\d+)\$") }
                        TextField(
                            value = itemDetails.squantity,
                            onValueChange = {
                                if (it.matches(pattern)) {
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
                        IconButton(
                            onClick = {
                                if (itemDetails.squantity.isEmpty()) {
                                    onValueChange(
                                        itemDetails.copy(
                                            squantity = "1",
                                            quantity = 1
                                        )
                                    )
                                } else {
                                    onValueChange(
                                        itemDetails.copy(
                                            squantity = (itemDetails.squantity.toInt() + 1).toString(),
                                            quantity = itemDetails.squantity.toInt() + 1
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_up),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .offset(x = 0.dp, y = (-4).dp)
                            )
                        }
                        IconButton(
                            onClick = {
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
                            },
                            modifier = Modifier
                                .padding(0.dp)
                                .offset(x = (-12).dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_down),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .offset(x = 0.dp, y = 4.dp)
                            )
                        }
                    }
                }

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
            trailingIcon = {
                if (value.length > 5) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                if (onValueChange != null) {
                                    onValueChange("")
                                }
                            }
                            .alpha(0.66f)
                            .size(20.dp)
                            .focusable(false)
                    )
                }
            },
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
        textColor = Color.Unspecified,
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
