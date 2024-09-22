package com.example.tobaccocellar.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarApplication
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import com.example.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object AddEntryDestination : NavigationDestination {
    override val route = "add_entry"
    override val titleRes = R.string.add_entry
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
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .noRippleClickable(onClick = { focusManager.clearFocus() }),
        topBar = {
            CellarTopAppBar(
                title = stringResource(AddEntryDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp,
                navigateToCsvImport = {},
                navigateToSettings = {},
                showMenu = false,
            )
        },
    ) { innerPadding ->
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
                .padding(innerPadding)
                .fillMaxSize(),
        )
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
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    var deleteConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
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

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .padding(0.dp, bottom = 8.dp),
            containerColor = Color.Transparent,
            contentColor = LocalContentColor.current,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            },
            divider = {
                HorizontalDivider(
                    modifier = Modifier,
                    thickness = 1.dp,
                )
            },
        ) {
            titles.forEachIndexed { index, title ->
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier,
                        text = { Text(title) },
                    )
                }
            }
        }
        when (selectedTabIndex) {
            0 -> {
// Required Fields //
                Column(
                    modifier = modifier
                        .fillMaxWidth()
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
                            var showSuggestions by remember { mutableStateOf(false) }

                            AutoCompleteText(
                                value = itemDetails.brand,
                                onValueChange = {
                                    if (it.length >= 2) {
                                        val filterText = it.trim().lowercase()
                                        suggestions.value = itemUiState.autoBrands.filter { brand ->
                                            brand.contains(filterText, ignoreCase = true)
                                        }
                                    } else {
                                        suggestions.value = emptyList()
                                    }
                                    showSuggestions = suggestions.value.isNotEmpty()
                                    onValueChange(itemDetails.copy(brand = it))
                                },
                                onOptionSelected = {
                                    onValueChange(itemDetails.copy(brand = it))
                                    TextRange(0, it.length)
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
                                )
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
                                        )
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
                                    .padding(
                                        top = 10.dp,
                                        bottom = 12.dp,
                                        start = 12.dp,
                                        end = 12.dp
                                    ),
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
                                    FavoriteHeart(
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
                                        modifier = Modifier
                                            .padding(0.dp),
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
                                    HatedBrokenHeart(
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
                                        modifier = Modifier
                                            .padding(0.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
                ) {
                    TextField(
                        value = itemDetails.notes,
                        onValueChange = { onValueChange(itemDetails.copy(notes = it)) },
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
                        singleLine = false,
                        maxLines = 5,
                        minLines = 5
                    )
                }
            }
        }
    }
}


@Composable
fun FavoriteHeart(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier,
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContentColor = LocalCustomColors.current.favHeart,
//            checkedContainerColor = MaterialTheme.colorScheme.primary,
//            uncheckedContentColor = MaterialTheme.colorScheme.onPrimary,
//            uncheckedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        )
    ) {
        Icon(
            imageVector = if (checked) {
                ImageVector.vectorResource(id = R.drawable.heart_filled_24)
            } else ImageVector.vectorResource(id = R.drawable.heart_outline_24),
            contentDescription = null,
            modifier = modifier
        )
    }
}


@Composable
fun HatedBrokenHeart(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier,
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContentColor = LocalCustomColors.current.disHeart,
        )
    ) {
        Icon(
            imageVector = if (checked) {
                ImageVector.vectorResource(id = R.drawable.heartbroken_filled_24)
            } else ImageVector.vectorResource(id = R.drawable.heartbroken_outlined_24),
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
//    val dropOffset by remember { mutableIntStateOf(value.length) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .padding(0.dp)
    ) {
        TextField(
            value = value,
            onValueChange = { onValueChange?.invoke(it)
                expanded = it.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
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
            )
        )
        if (expanded && suggestions.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                    .heightIn(max = 82.dp),
                properties = PopupProperties(focusable = false),
                offset = DpOffset(32.dp, (-12).dp),
            ) {
                suggestions.forEach { label ->
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
                            expanded = false
                            emptyList<String>()
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
}

@Composable
fun CustomDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = false,
    modifier: Modifier,
    colors: MenuItemColors,
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
    ) {
        text()
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
            )
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



@Preview(showBackground = true)
@Composable
fun AddEntryScreenPreview(){
    AddEntryBody(
        itemUiState = ItemUiState(
            ItemDetails(
                brand = "Cornell & Diehl", blend = "Eight State Burley (2024)", type = "Burley", quantity = 2, disliked = false
            )
        ),
        onItemValueChange = {},
        onSaveClick = {},
        onDeleteClick = {},
        navigateToEditEntry = {},
        isEditEntry = true,
        existState = ExistState(),
        resetExistState = {},
        innerPadding = PaddingValues(0.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    )
}