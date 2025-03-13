package com.sardonicus.tobaccocellar.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.CheckboxWithLabel
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.AutoSizeText
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


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
    val filterViewModel = LocalCellarApplication.current.filterViewModel

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
                componentUiState = viewModel.componentList,
                tinDetails = viewModel.tinDetailsState,
                tinDetailsList = viewModel.tinDetailsList,
                syncedTins = viewModel.calculateSyncTins(),
                existState = viewModel.existState,
                resetExistState = viewModel::resetExistState,
                onItemValueChange = viewModel::updateUiState,
                onTinValueChange = viewModel::updateTinDetails,
                onComponentChange = viewModel::updateComponentList,
                addTin = viewModel::addTin,
                removeTin = viewModel::removeTin,
                isTinLabelValid = viewModel::isTinLabelValid,
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
                onDeleteClick = { },
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
    componentUiState: ComponentList,
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    syncedTins: Int,
    existState: ExistState,
    onItemValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    onComponentChange: (String) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
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
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
        //    .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            itemUiState = itemUiState,
            componentUiState = componentUiState,
            tinDetails = tinDetails,
            tinDetailsList = tinDetailsList,
            syncedTins = syncedTins,
            onValueChange = onItemValueChange,
            onTinValueChange = onTinValueChange,
            isTinLabelValid = isTinLabelValid,
            onComponentChange = onComponentChange,
            addTin = addTin,
            removeTin = removeTin,
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
    componentUiState: ComponentList,
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    syncedTins: Int,
    isEditEntry: Boolean,
    onValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    onComponentChange: (String) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Details", "Notes", "Tins")

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .padding(bottom = 1.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = LocalContentColor.current,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier
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
                        onClick = { selectedTabIndex = index },
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
        GlowBox(
            color = GlowColor(MaterialTheme.colorScheme.background),
            size = GlowSize(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(.65f)
                    .verticalScroll(rememberScrollState()),
            ) {
                when (selectedTabIndex) {
                    0 ->
                        DetailsEntry(
                            itemDetails = itemDetails,
                            itemUiState = itemUiState,
                            syncedTins = syncedTins,
                            isEditEntry = isEditEntry,
                            onValueChange = onValueChange,
                            componentList = componentUiState,
                            onComponentChange = onComponentChange,
                            modifier = Modifier,
                        )
                    1 ->
                        NotesEntry(
                            itemDetails = itemDetails,
                            onValueChange = onValueChange,
                            modifier = Modifier
                        )
                    2 ->
                        TinsEntry(
                            tinDetails = tinDetails,
                            tinDetailsList = tinDetailsList,
                            onTinValueChange = onTinValueChange,
                            isTinLabelValid = isTinLabelValid,
                            addTin = addTin,
                            removeTin = removeTin,
                            itemUiState = itemUiState,
                            onValueChange = onValueChange,
                            modifier = Modifier
                        )
                    else -> throw IllegalArgumentException("Invalid tab position")
                }
            }
        }
    }
}


@Composable
fun DetailsEntry(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    syncedTins: Int,
    isEditEntry: Boolean,
    componentList: ComponentList,
    onComponentChange: (String) -> Unit,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
//    var showTinConverter by rememberSaveable { mutableStateOf(false) }

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
            .noRippleClickable(onClick = { focusManager.clearFocus() })
            .padding(top = 20.dp, bottom = 0.dp, start = 20.dp, end = 20.dp),
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
                onOptionSelected = { suggestion, currentText ->
                    onValueChange(itemDetails.copy(brand = suggestion))
                    suggestions.value = emptyList()
                },
                suggestions = suggestions.value,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    if (isEditEntry) {
                        Text(
                            text = "(" + itemDetails.originalBrand + ")",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    } else {
                        Text(
                            text = "Required",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                )
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
                    if (isEditEntry) {
                        Text(
                            text = "(" + itemDetails.originalBlend + ")",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                        )
                    } else {
                        Text(
                            text = "Required",
                            modifier = Modifier
                                .alpha(0.66f),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
            CustomDropDown(
                selectedValue = itemDetails.type,
                onValueChange = { onValueChange(itemDetails.copy(type = it)) },
                options = listOf("", "Aromatic", "English", "Burley", "Virginia", "Other"),
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }

        // Subgenre //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subgenre:",
                modifier = Modifier
                    .width(80.dp)
            )

            val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

            AutoCompleteText(
                value = itemDetails.subGenre,
                onValueChange = {
                    onValueChange(itemDetails.copy(subGenre = it))

                    if (it.length >= 2) {
                        val startsWith = itemUiState.autoGenres.filter { genre ->
                            genre.startsWith(it, ignoreCase = true)
                        }
                        val otherWordsStartsWith = itemUiState.autoGenres.filter { genre ->
                            genre.split(" ").drop(1).any { word ->
                                word.startsWith(it, ignoreCase = true)
                            } && !genre.startsWith(it, ignoreCase = true)
                        }
                        val contains = itemUiState.autoGenres.filter { genre ->
                            genre.contains(it, ignoreCase = true)
                                    && !genre.startsWith(it, ignoreCase = true) &&
                                    !otherWordsStartsWith.contains(genre)
                        }
                        val selected = itemUiState.autoGenres.filter { genre ->
                            genre == it
                        }

                        suggestions.value = (startsWith + otherWordsStartsWith + contains) - selected
                    } else {
                        suggestions.value = emptyList()
                    }
                },
                onOptionSelected = { suggestion, currentText ->
                    onValueChange(itemDetails.copy(subGenre = suggestion))
                    suggestions.value = emptyList()
                },
                suggestions = suggestions.value,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.subGenre.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable {
                                    onValueChange(itemDetails.copy(subGenre = ""))
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
                )
            )
        }

        // Cut //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cut:",
                modifier = Modifier
                    .width(80.dp)
            )

            val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

            AutoCompleteText(
                value = itemDetails.cut,
                onValueChange = {
                    onValueChange(itemDetails.copy(cut = it))

                    if (it.length >= 2) {
                        val startsWith = itemUiState.autoCuts.filter { cut ->
                            cut.startsWith(it, ignoreCase = true)
                        }
                        val otherWordsStartsWith = itemUiState.autoCuts.filter { cut ->
                            cut.split(" ").drop(1).any { word ->
                                word.startsWith(it, ignoreCase = true)
                            } && !cut.startsWith(it, ignoreCase = true)
                        }
                        val contains = itemUiState.autoCuts.filter { cut ->
                            cut.contains(it, ignoreCase = true)
                                    && !cut.startsWith(it, ignoreCase = true) &&
                                    !otherWordsStartsWith.contains(cut)
                        }
                        val selected = itemUiState.autoCuts.filter { cut ->
                            cut == it
                        }

                        suggestions.value = (startsWith + otherWordsStartsWith + contains) - selected
                    } else {
                        suggestions.value = emptyList()
                    }
                },
                onOptionSelected = { suggestion, currentText ->
                    onValueChange(itemDetails.copy(cut = suggestion))
                    suggestions.value = emptyList()
                },
                suggestions = suggestions.value,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.cut.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable {
                                    onValueChange(itemDetails.copy(cut = ""))
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                )
            )
        }

        // Components //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoSizeText(
                text = "Components:",
                fontSize = 16.sp,
                minFontSize = 8.sp,
                width = 80.dp,
                modifier = Modifier,
                maxLines = 1,
                softWrap = false,
                contentAlignment = Alignment.CenterStart
            )

            val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

            AutoCompleteText(
                value = componentList.componentString,
                onValueChange = { string ->
                    onComponentChange(string)

                    val substring = if (string.contains(", ")) {
                        string.substringAfterLast(", ", "")
                    } else {
                        string
                    }

                    if (substring.length >= 2) {
                        val startsWith = componentList.autoComps.filter { comp ->
                            comp.startsWith(substring, ignoreCase = true)
                        }

                        val otherWordsStartsWith = componentList.autoComps.filter { comp ->
                            comp.split(" ").drop(1).any { word ->
                                word.startsWith(substring, ignoreCase = true)
                            } && !comp.startsWith(substring, ignoreCase = true)
                        }

                        val contains = componentList.autoComps.filter { comp ->
                            comp.contains(substring, ignoreCase = true)
                                    && !comp.startsWith(substring, ignoreCase = true) &&
                                    !otherWordsStartsWith.contains(comp)
                        }

                        val selected = componentList.autoComps.filter { comp ->
                            string.split(", ").filter { string.isNotBlank() }.contains(comp)
                        }

                        suggestions.value = (startsWith + otherWordsStartsWith + contains) - selected
                    } else {
                        suggestions.value = emptyList()
                    }
                },
                componentField = true,
                onOptionSelected = { suggestion, currentText ->
                    val updatedText =
                        if (currentText.contains(", ")) {
                            currentText.substringBeforeLast(", ", "") + ", " + suggestion + ", " }
                        else { "$suggestion, " }
                    onComponentChange(updatedText)
                    suggestions.value = emptyList()
                },
                suggestions = suggestions.value,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (componentList.componentString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable {
                                    onComponentChange("")
                                }
                                .alpha(0.66f)
                                .size(20.dp)
                                .focusable(false)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                maxLines = 1,
                placeholder = {
                    Text(
                        text = "(Separate with comma + space)",
                        modifier = Modifier
                            .alpha(0.66f),
                        fontSize = 13.sp,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            )
        }

        // No. of Tins //
        Row(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AutoSizeText(
                text = "No. of\nTins:",
                fontSize = 16.sp,
                minFontSize = 8.sp,
                modifier = Modifier,
                width = 80.dp,
                height = 48.dp,
                contentAlignment = Alignment.CenterStart
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
                    value =
                        if (itemDetails.isSynced) syncedTins.toString()
                        else itemDetails.quantityString,
                    onValueChange = {
                        if (!itemDetails.isSynced) {
                            if (it.matches(pattern) && it.length <= 2) {
                                onValueChange(
                                    itemDetails.copy(
                                        quantityString = it,
                                        quantity = it.toIntOrNull() ?: 1
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .width(54.dp)
                        .padding(0.dp),
                    visualTransformation = VisualTransformation.None,
                    enabled = !itemDetails.isSynced,
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
                        disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.66f),
                        disabledTextColor = LocalContentColor.current.copy(alpha = 0.66f),
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                )

                // Tin field options //
                Row(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Increase/Decrease Buttons //
                    Row(
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.triangle_arrow_up),
                            contentDescription = "Increase Quantity",
                            modifier = Modifier
                                .align(Alignment.Top)
                                .clickable(enabled = !itemDetails.isSynced) {
                                    if (itemDetails.quantityString.isEmpty()) {
                                        onValueChange(
                                            itemDetails.copy(
                                                quantityString = "1",
                                                quantity = 1
                                            )
                                        )
                                    } else {
                                        if (itemDetails.quantityString.toInt() < 99) {
                                            onValueChange(
                                                itemDetails.copy(
                                                    quantityString = (itemDetails.quantityString.toInt() + 1).toString(),
                                                    quantity = itemDetails.quantityString.toInt() + 1
                                                )
                                            )
                                        } else {
                                            onValueChange(
                                                itemDetails.copy(
                                                    quantityString = "99",
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
                            painter = painterResource(id = R.drawable.triangle_arrow_down),
                            contentDescription = "Decrease Quantity",
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .clickable(enabled = !itemDetails.isSynced) {
                                    if (itemDetails.quantityString.isEmpty()) {
                                        /* do nothing */
                                    } else {
                                        if (itemDetails.quantityString.toInt() > 0) {
                                            onValueChange(
                                                itemDetails.copy(
                                                    quantityString = (itemDetails.quantityString.toInt() - 1).toString(),
                                                    quantity = itemDetails.quantity - 1
                                                )
                                            )
                                        } else if (itemDetails.quantityString.toInt() == 0) {
                                            onValueChange(
                                                itemDetails.copy(
                                                    quantityString = "0",
                                                    quantity = 0
                                                )
                                            )
                                        }
                                    }
                                }
                                .padding(start = 2.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                                .offset(x = (-1).dp, y = (-2).dp)
                        )
                    }

                    // Sync Tins //
                    CheckboxWithLabel(
                        text = "Sync?",
                        checked = itemDetails.isSynced,
                        onCheckedChange = {
                            onValueChange(itemDetails.copy(isSynced = it))
                        },
                        modifier = Modifier,
                        fontSize = 14.sp,
                        height = 22.dp
                    )
                }
            }
        }

        // Favorite or Disliked? //
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 2.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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

        // Production Status //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "In Production?",
                modifier = Modifier
                    .offset(x = 0.dp, y = 1.dp)
            )
            CustomCheckBox(
                checked = itemDetails.inProduction,
                onCheckedChange = {
                    onValueChange(itemDetails.copy(inProduction = it))
                },
                checkedIcon = R.drawable.check_box_24,
                uncheckedIcon = R.drawable.check_box_outline_24,
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
) {
    Column(
        modifier = modifier
            .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Notes //
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Notes:",
                modifier = Modifier
                    .padding(bottom = 4.dp)
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
                            if (currentLine.startsWith(lastWord) && currentLine.length > 1) {
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
                maxLines = 8,
                minLines = 8,
            )
        }
    }
}


@Composable
fun TinsEntry(
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    itemUiState: ItemUiState,
    onValueChange: (ItemDetails) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

    LaunchedEffect(tinDetailsList) {
        onValueChange(itemUiState.itemDetails)
        if (itemUiState.itemDetails.isSynced) {
            val syncTins = itemUiState.itemDetails.syncedQuantity
            onValueChange(itemUiState.itemDetails.copy(quantity = syncTins, quantityString = syncTins.toString()))
        }
    }

    Spacer(
        modifier = Modifier
            .height(7.dp)
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = {
                    focusManager.clearFocus()
                })
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .background(color = if (tinDetailsList.isEmpty()) Color.Transparent else
                LocalCustomColors.current.textField, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
    ) {
        Spacer(
            modifier = Modifier
                .height(6.dp)
        )

        if (tinDetailsList.isEmpty()) {
            Button(
                onClick = { addTin() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = "Add Tin",
                    modifier = Modifier
                )
            }
            Spacer(
                modifier = Modifier
                    .height(6.dp)
            )
        } else {
            tinDetailsList.forEachIndexed { index, tinDetails ->
                IndividualTin(
                    tinDetails = tinDetails,
                    tinDetailsList = tinDetailsList,
                    tempTinId = tinDetails.tempTinId,
                    onTinValueChange = onTinValueChange,
                    showError = tinDetails.labelIsNotValid,
                    isTinLabelValid = isTinLabelValid,
                    removeTin = { removeTin(index) },
                    itemUiState = itemUiState,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
            IconButton(
                onClick = { addTin() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.add_outline),
                    contentDescription = "Add Tin",
                    modifier = Modifier
                        .size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(
                modifier = Modifier
                    .height(6.dp)
            )
        }
    }
}


/** custom composables */
@Composable
fun IndividualTin(
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    tempTinId: Int,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    showError: Boolean,
    removeTin: () -> Unit,
    itemUiState: ItemUiState,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(tinDetails) {
        onValueChange(itemUiState.itemDetails)
    }

    LaunchedEffect(tinDetailsList) {
        onTinValueChange(
            tinDetails.copy(
                labelIsNotValid = isTinLabelValid(tinDetails.tinLabel, tempTinId)
            )
        )
    }

    LaunchedEffect(tempTinId) {
        if (tempTinId == 1 && tinDetailsList.size == 1) {
            onTinValueChange(tinDetails.copy(detailsExpanded = true))
        }
    }


    Column (
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {

        // Header Row //
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val boxWithConstraintsScope = this
            val maxWidth = maxWidth
            val textFieldMax = maxWidth - 72.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
            ) {
                // Expand/Contract Button/Indicator //
                Box(
                    modifier = Modifier
                        .weight(0.5f),
                    contentAlignment = Alignment.TopStart
                ) {
                    Icon(
                        imageVector = if (tinDetails.detailsExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = "Expand/contract details",
                        modifier = Modifier
                            .clickable {
                                onTinValueChange(tinDetails.copy(detailsExpanded = !tinDetails.detailsExpanded))
                            }
                            .padding(4.dp)
                            .size(22.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                // Label //
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomTextField(
                        value = tinDetails.tinLabel,
                        onValueChange = {
                            onTinValueChange(
                                tinDetails.copy(tinLabel = it)
                            )
                        },
                        modifier = Modifier
                            .widthIn(max = textFieldMax),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            color = if (showError) MaterialTheme.colorScheme.error else LocalContentColor.current,
                            fontWeight = FontWeight.Medium,
                        ),
                        placeholder = {
                            Text(
                                text = "Label (Required)",
                                modifier = Modifier
                                    .alpha(0.66f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                softWrap = false,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = 0.5f
                            ),
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = 0.5f
                            ),
                            disabledIndicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = 0.5f
                            ),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = Color.Transparent,
                            focusedTextColor = if (showError) MaterialTheme.colorScheme.error else
                                LocalContentColor.current,
                            unfocusedTextColor = if (showError) MaterialTheme.colorScheme.error else
                                LocalContentColor.current,
                            disabledTextColor = if (showError) MaterialTheme.colorScheme.error else
                                LocalContentColor.current,
                        ),
                        contentPadding = PaddingValues(vertical = 2.dp, horizontal = 0.dp),
                        singleLine = true,
                        maxLines = 1,
                        minLines = 1,
                    )
                    Text(
                        text = "Label must be unique within each entry.",
                        color = if (showError) MaterialTheme.colorScheme.error else Color.Transparent,
                        style = MaterialTheme.typography.bodySmall,
                        softWrap = false,
                        modifier = Modifier
                            .padding(bottom = 4.dp, top = 1.dp)
                    )
                }

                // Remove Button //
                Box(
                    modifier = Modifier
                        .weight(0.5f),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.remove_circle_outline),
                        contentDescription = "Close",
                        modifier = Modifier
                            .clickable {
                                removeTin()
                            }
                            .padding(4.dp)
                            .size(20.dp),
                        tint = LocalCustomColors.current.pieNine.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (tinDetails.detailsExpanded) {
            Column (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
            ) {
                // Container //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Container Type:",
                        modifier = Modifier
                            .width(80.dp)
                    )

                    val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

                    AutoCompleteText(
                        value = tinDetails.container,
                        onValueChange = {
                            onTinValueChange(tinDetails.copy(container = it))

                            if (it.length >= 2) {
                                val startsWith = itemUiState.autoContainers.filter { container ->
                                    container.startsWith(it, ignoreCase = true)
                                }
                                val otherWordsStartsWith = itemUiState.autoContainers.filter { container ->
                                    container.split(" ").drop(1).any { word ->
                                        word.startsWith(it, ignoreCase = true)
                                    } && !container.startsWith(it, ignoreCase = true)
                                }
                                val contains = itemUiState.autoContainers.filter { container ->
                                    container.contains(it, ignoreCase = true)
                                            && !container.startsWith(it, ignoreCase = true) &&
                                            !otherWordsStartsWith.contains(container)
                                }
                                val selected = itemUiState.autoContainers.filter { container ->
                                    container == it
                                }

                                suggestions.value = (startsWith + otherWordsStartsWith + contains) - selected
                            } else {
                                suggestions.value = emptyList()
                            }
                        },
                        onOptionSelected = { suggestion, currentText ->
                            onTinValueChange(tinDetails.copy(container = suggestion))
                            suggestions.value = emptyList()
                        },
                        suggestions = suggestions.value,
                        modifier = Modifier
                            .fillMaxWidth(),
                        trailingIcon = {
                            if (tinDetails.container.length > 4) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                    contentDescription = "Clear",
                                    modifier = Modifier
                                        .clickable {
                                            onTinValueChange(tinDetails.copy(container = ""))
                                        }
                                        .alpha(0.66f)
                                        .size(20.dp)
                                        .focusable(false)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        )
                    )
                }

                // Amount //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amount:",
                        modifier = Modifier
                            .width(80.dp)
                    )

                    val pattern = remember { Regex("^(\\s*|\\d+(\\.\\d{0,2})?)\$") }
                    TextField(
                        value = tinDetails.tinQuantityString,
                        onValueChange = {
                            if (it.matches(pattern)) {
                                onTinValueChange(
                                    tinDetails.copy(
                                        tinQuantityString = it,
                                        tinQuantity = it.toDoubleOrNull() ?: 0.0
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f),
                        enabled = true,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,),
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

                    CustomDropDown(
                        selectedValue = tinDetails.unit,
                        onValueChange = {
                            onTinValueChange(
                                tinDetails.copy(unit = it)
                            )
                        },
                        options = listOf("", "oz", "lbs", "grams"),
                        placeholder = {
                            Text(
                                text = "Unit",
                                modifier = Modifier
                                    .alpha(0.66f),
                                fontSize = 14.sp,
                            )
                        },
                        modifier = Modifier
                            .weight(2f),
                    )
                }

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )

                // Date entry //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var showDatePicker by rememberSaveable { mutableStateOf(false) }
                    var datePickerLabel by rememberSaveable { mutableStateOf("") }
                    fun showPicker (label: String) {
                        datePickerLabel = label
                        showDatePicker = true
                    }

                    var manuIsFocused by rememberSaveable { mutableStateOf(false) }
                    val manuFocusRequester = remember { FocusRequester() }
                    var cellaredIsFocused by rememberSaveable { mutableStateOf(false) }
                    val cellaredFocusRequester = remember { FocusRequester() }
                    var openedIsFocused by rememberSaveable { mutableStateOf(false) }
                    val openedFocusRequester = remember { FocusRequester() }
                    val interactionSource = remember { MutableInteractionSource() }

                    var dateFieldWidth by remember { mutableStateOf(0) }

                    // Manufacture //
                    OutlinedTextField(
                        value = if (tinDetails.manufactureDateShort.isEmpty()) {
                            " " } else {
                            if (dateFieldWidth > 420) {
                                tinDetails.manufactureDateLong
                            } else {
                                tinDetails.manufactureDateShort
                            }
                        },
                        onValueChange = { },
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned {
                                dateFieldWidth = it.size.width
                            }
                            .onFocusChanged { manuIsFocused = it.isFocused }
                            .focusRequester(manuFocusRequester),
                        enabled = true,
                        readOnly = true,
                        singleLine = true,
                        interactionSource = interactionSource,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    manuFocusRequester.requestFocus()
                                    showPicker("Manufacture")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select date",
                                    tint = LocalContentColor.current
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Manuf.",
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )

                    // Cellar //
                    OutlinedTextField(
                        value = if (tinDetails.cellarDateShort.isEmpty()) {
                            " " } else {
                            if (dateFieldWidth > 420) {
                                tinDetails.cellarDateLong
                            } else {
                                tinDetails.cellarDateShort
                            }
                        },
                        onValueChange = { },
                        label = {
                            Text(
                                text = "Cellared",
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { cellaredIsFocused = it.isFocused }
                            .focusRequester(cellaredFocusRequester),
                        enabled = true,
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                cellaredFocusRequester.requestFocus()
                                showPicker("Cellared")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select date",
                                    tint = LocalContentColor.current
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )

                    // Opened //
                    OutlinedTextField(
                        value = if (tinDetails.openDateShort.isEmpty()) {
                            " " } else {
                            if (dateFieldWidth > 420) {
                                tinDetails.openDateLong
                            } else {
                                tinDetails.openDateShort
                            }
                        },
                        onValueChange = { },
                        label = {
                            Text(
                                text = "Opened",
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { openedIsFocused = it.isFocused }
                            .focusRequester(openedFocusRequester),
                        enabled = true,
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                openedFocusRequester.requestFocus()
                                showPicker("Opened")
                            //    focusRequester.requestFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select date",
                                    tint = LocalContentColor.current
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        ),
                        shape = MaterialTheme.shapes.extraSmall
                    )

                    if (showDatePicker) {
                        CustomDatePickerDialog(
                            onDismiss = { showDatePicker = false },
                            onDateSelected = {
                                val dateStringShort = if (it != null) {
                                    val instant = Instant.ofEpochMilli(it)

                                    val shortFormat =
                                        DateTimeFormatter
                                            .ofPattern("MM/yy")
                                            .withZone(ZoneId.systemDefault())

                                    shortFormat.format(instant)
                                } else { "" }

                                val dateStringLong = if (it != null) {
                                    val instant = Instant.ofEpochMilli(it)

                                    val longFormat =
                                        DateTimeFormatter
                                            .ofLocalizedDate(FormatStyle.MEDIUM)
                                    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()

                                    longFormat.format(localDate)
                                } else { "" }

                                when (datePickerLabel) {
                                    "Manufacture" -> {
                                        onTinValueChange(
                                            tinDetails.copy(
                                                manufactureDate = it,
                                                manufactureDateShort = dateStringShort,
                                                manufactureDateLong = dateStringLong
                                            )
                                        )
                                    }
                                    "Cellared" -> {
                                        onTinValueChange(
                                            tinDetails.copy(
                                                cellarDate = it,
                                                cellarDateShort = dateStringShort,
                                                cellarDateLong = dateStringLong
                                            )
                                        )
                                    }
                                    "Opened" -> {
                                        onTinValueChange(
                                            tinDetails.copy(
                                                openDate = it,
                                                openDateShort = dateStringShort,
                                                openDateLong = dateStringLong
                                            )
                                        )
                                    }
                                }
                            },
                            currentMillis = when (datePickerLabel) {
                                "Manufacture" -> { tinDetails.manufactureDate }
                                "Cellared" -> { tinDetails.cellarDate }
                                "Opened" -> { tinDetails.openDate }
                                else -> { null }
                            },
                            label = datePickerLabel
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Expand...",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = LocalContentColor.current.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        onTinValueChange(tinDetails.copy(detailsExpanded = true))
                    }
                    .fillMaxWidth()
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    currentMillis: Long? = null,
    label: String = "Select",
) {
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Input,
        initialSelectedDateMillis = currentMillis
    )
    val datePickerFormatter = remember { DatePickerDefaults.dateFormatter() }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null) {
                        val utcDate =
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(selectedDate), ZoneOffset.UTC
                            )
                        val timeZoneDate = ZonedDateTime.of(utcDate, ZoneId.systemDefault())
                        val timeZoneDateLong = timeZoneDate.toInstant().toEpochMilli()
                        onDateSelected(timeZoneDateLong)
                    } else { onDateSelected(null) }

                    onDismiss()
                }
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = "Cancel")
            }
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            title = {
                Text(
                    text = "$label Date",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                )
            },
            headline = {
                Text(
                    text = "Select a date",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(start = 16.dp)
                )
            },
            dateFormatter = datePickerFormatter,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                headlineContentColor = MaterialTheme.colorScheme.onBackground,
            )
        )
    }
}


@Composable
fun CustomCheckBox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    checkedIcon: Int,
    uncheckedIcon: Int,
    modifier: Modifier = Modifier,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    enabled: Boolean = true,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onCheckedChange?.invoke(it) },
        modifier = modifier
            .size(34.dp),
        colors = colors,
        enabled = enabled
    ) {
        Icon(
            imageVector = if (checked) {
                ImageVector.vectorResource(id = checkedIcon)
            } else ImageVector.vectorResource(id = uncheckedIcon),
            contentDescription = null,
            modifier = Modifier
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
    onOptionSelected: (String, String) -> Unit,
    suggestions: List<String> = emptyList(),
    label: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int. MAX_VALUE,
    minLines: Int = 1,
    supportingText: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    componentField: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var suggestions = suggestions
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(value)) }
    val focusRequester = remember { FocusRequester() }
    val focusState = remember { mutableStateOf(false) }

    LaunchedEffect(suggestions) {
    //    expanded = value.isNotEmpty() && suggestions.isNotEmpty() && focusState.value
        if (suggestions.isEmpty()) {
            expanded = false
        } else {
            expanded = value.isNotEmpty() && suggestions.isNotEmpty() && focusState.value
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
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .onFocusChanged { focusState.value = it.isFocused }
                .focusRequester(focusRequester)
                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
            enabled = enabled,
            trailingIcon = trailingIcon,
            singleLine = true,
            placeholder = placeholder,
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = LocalCustomColors.current.textField,
                unfocusedContainerColor = LocalCustomColors.current.textField,
                disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f),
            ),
            shape = MaterialTheme.shapes.extraSmall,
            label = label,
            maxLines = maxLines,
            minLines = minLines,
            supportingText = supportingText,

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
                        val currentText = textFieldValueState.text
                        onOptionSelected(label, currentText)

                        val updatedText = if (currentText.contains(", ")) {
                            currentText.substringBeforeLast(", ", "") + ", " + label + ", "
                        } else {
                            if (componentField) {
                                label + ", "
                            } else {
                                label
                            }
                        }

                        textFieldValueState = TextFieldValue(
                            text = updatedText,
                            selection = TextRange(updatedText.length)
                        )
                        suggestions = emptyList()
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
fun CustomDropDown(
    selectedValue: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

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
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
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
                disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f),
            ),
            shape = MaterialTheme.shapes.extraSmall,
            placeholder = placeholder,
            enabled = enabled
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
