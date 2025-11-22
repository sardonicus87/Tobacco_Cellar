package com.sardonicus.tobaccocellar.ui.items

import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.AutoCompleteText
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckBox
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.composables.RatingRow
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


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


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(AddEntryDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                modifier = Modifier,
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
                flavoringUiState = viewModel.flavoringList,
                tinDetails = viewModel.tinDetailsState,
                tinDetailsList = viewModel.tinDetailsList,
                tabErrorState = viewModel.tabErrorState,
                syncedTins = viewModel.calculateSyncTins(),
                existState = viewModel.existState,
                resetExistState = viewModel::resetExistState,
                onItemValueChange = viewModel::updateUiState,
                onTinValueChange = viewModel::updateTinDetails,
                onComponentChange = viewModel::updateComponentList,
                onFlavoringChange = viewModel::updateFlavoringList,
                addTin = viewModel::addTin,
                removeTin = viewModel::removeTin,
                showRatingPop = viewModel.showRatingPop.value,
                onShowRatingPop = viewModel::onShowRatingPop,
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
                validateDates = { _, _, _ -> Triple(true, true, true) },
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
    flavoringUiState: FlavoringList,
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    tabErrorState: TabErrorState,
    syncedTins: Int,
    existState: ExistState,
    onItemValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    onComponentChange: (String) -> Unit,
    onFlavoringChange: (String) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isEditEntry: Boolean,
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    modifier: Modifier = Modifier,
    navigateToEditEntry: (Int) -> Unit = {},
    resetExistState: () -> Unit = {},
) {
    var deleteConfirm by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            itemUiState = itemUiState,
            componentUiState = componentUiState,
            flavoringUiState = flavoringUiState,
            tinDetails = tinDetails,
            tinDetailsList = tinDetailsList,
            tabErrorState = tabErrorState,
            syncedTins = syncedTins,
            onValueChange = onItemValueChange,
            onTinValueChange = onTinValueChange,
            isTinLabelValid = isTinLabelValid,
            onComponentChange = onComponentChange,
            onFlavoringChange = onFlavoringChange,
            addTin = addTin,
            removeTin = removeTin,
            showRatingPop = showRatingPop,
            onShowRatingPop = onShowRatingPop,
            isEditEntry = isEditEntry,
            validateDates = validateDates,
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
        onDismissRequest = { },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.item_exists)) },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
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
        onDismissRequest = { },
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
        shape = MaterialTheme.shapes.large,
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
        shape = MaterialTheme.shapes.large,
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
@Composable
fun ItemInputForm(
    itemDetails: ItemDetails,
    itemUiState: ItemUiState,
    componentUiState: ComponentList,
    flavoringUiState: FlavoringList,
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    tabErrorState: TabErrorState,
    syncedTins: Int,
    isEditEntry: Boolean,
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    onValueChange: (ItemDetails) -> Unit,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    onComponentChange: (String) -> Unit,
    onFlavoringChange: (String) -> Unit,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Details", "Notes", "Tins")

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .padding(bottom = 1.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = LocalContentColor.current,
            indicator = {
                SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedTabIndex),
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
                val textColor = when (index) {
                    0 -> if (tabErrorState.detailsError) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                    2 -> if (tabErrorState.tinsError) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outline
                }

                LaunchedEffect(index){ onValueChange(itemUiState.itemDetails) }

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
                        unselectedContentColor = textColor, // MaterialTheme.colorScheme.outline,
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
                    0 -> DetailsEntry(
                            itemDetails = itemDetails,
                            itemUiState = itemUiState,
                            syncedTins = syncedTins,
                            isEditEntry = isEditEntry,
                            onValueChange = onValueChange,
                            componentList = componentUiState,
                            onComponentChange = onComponentChange,
                            flavoringList = flavoringUiState,
                            onFlavoringChange = onFlavoringChange,
                            showRatingPop = showRatingPop,
                            onShowRatingPop = onShowRatingPop,
                            modifier = Modifier,
                        )

                    1 -> NotesEntry(
                            itemDetails = itemDetails,
                            onValueChange = onValueChange,
                            modifier = Modifier
                        )

                    2 -> TinsEntry(
                            tinDetails = tinDetails,
                            tinDetailsList = tinDetailsList,
                            onTinValueChange = onTinValueChange,
                            isTinLabelValid = isTinLabelValid,
                            addTin = addTin,
                            removeTin = removeTin,
                            itemUiState = itemUiState,
                            onValueChange = onValueChange,
                            validateDates = validateDates,
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
    flavoringList: FlavoringList,
    onFlavoringChange: (String) -> Unit,
    onValueChange: (ItemDetails) -> Unit,
    showRatingPop: Boolean,
    onShowRatingPop: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
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

            AutoCompleteText(
                value = itemDetails.brand,
                allItems = itemUiState.autoBrands,
                onValueChange = { onValueChange(itemDetails.copy(brand = it)) },
                onOptionSelected = { onValueChange(itemDetails.copy(brand = it)) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    if (isEditEntry) {
                        val text = if (itemDetails.originalBrand.isNotEmpty()) {
                            "(" + itemDetails.originalBrand + ")" } else ""
                        Text(
                            text = text,
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
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(brand = "")) }
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
                        val text = if (itemDetails.originalBlend.isNotEmpty()) {
                            "(" + itemDetails.originalBlend + ")" } else ""
                        Text(
                            text = text,
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
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(blend = "")) }
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

            AutoCompleteText(
                value = itemDetails.subGenre,
                onValueChange = { onValueChange(itemDetails.copy(subGenre = it)) },
                allItems = itemUiState.autoGenres,
                onOptionSelected = { onValueChange(itemDetails.copy(subGenre = it)) },
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.subGenre.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(subGenre = "")) }
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

            AutoCompleteText(
                value = itemDetails.cut,
                onValueChange = { onValueChange(itemDetails.copy(cut = it)) },
                onOptionSelected = { onValueChange(itemDetails.copy(cut = it)) },
                allItems = itemUiState.autoCuts,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (itemDetails.cut.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onValueChange(itemDetails.copy(cut = "")) }
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
            Text(
                text = "Components: ",
                style = TextStyle(
                    color = LocalContentColor.current
                ),
                modifier = Modifier
                    .width(80.dp)
                    .align(Alignment.CenterVertically),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 16.sp,
                    stepSize = .02.sp,
                ),
                maxLines = 1,
            )

            AutoCompleteText(
                value = componentList.componentString,
                allItems = componentList.autoComps,
                onValueChange = { onComponentChange(it) },
                componentField = true,
                onOptionSelected = { onComponentChange(it) },
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (componentList.componentString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onComponentChange("") }
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

        // Flavoring //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Flavoring: ",
                style = TextStyle(
                    color = LocalContentColor.current
                ),
                modifier = Modifier
                    .width(80.dp)
                    .align(Alignment.CenterVertically),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 16.sp,
                    stepSize = .02.sp,
                ),
                maxLines = 1,
            )

            AutoCompleteText(
                value = flavoringList.flavoringString,
                onValueChange = { onFlavoringChange(it) },
                componentField = true,
                onOptionSelected = { onFlavoringChange(it) },
                allItems = flavoringList.autoFlavors,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = {
                    if (flavoringList.flavoringString.length > 4) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                            contentDescription = "Clear",
                            modifier = Modifier
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { onFlavoringChange("") }
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
            Text(
                text = "No. of\nTins:",
                style = TextStyle(
                    color = LocalContentColor.current
                ),
                modifier = Modifier
                    .width(80.dp)
                    .heightIn(max = 48.dp)
                    .wrapContentHeight()
                    .align(Alignment.CenterVertically),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 16.sp,
                    stepSize = .02.sp,
                ),
                maxLines = 2,
            )
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val pattern = remember { Regex("^(\\s*|\\d+)$") }

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
                    IncreaseDecrease(
                        increaseClick = {
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
                        },
                        decreaseClick = {
                            if (itemDetails.quantityString.isEmpty()) {
                                onValueChange(
                                    itemDetails.copy(
                                        quantityString = "0",
                                        quantity = 0
                                    )
                                )
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
                        },
                        increaseEnabled = !itemDetails.isSynced,
                        decreaseEnabled = !itemDetails.isSynced,
                        modifier = Modifier
                            .fillMaxHeight()
                    )

                    // Sync Tins? //
                    Row(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = spacedBy(2.dp, Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Sync?",
                            modifier = Modifier
                                .offset(x = 0.dp, y = 1.dp),
                            fontSize = 14.sp,
                        )
                        CustomCheckBox(
                            checked = itemDetails.isSynced,
                            onCheckedChange = {
                                onValueChange(itemDetails.copy(isSynced = it))
                            },
                            checkedIcon = R.drawable.check_box_24,
                            uncheckedIcon = R.drawable.check_box_outline_24,
                            modifier = Modifier
                        )
                    }
                }
            }
        }

        // Rating //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Rating:",
                modifier = Modifier
                    .width(80.dp)
            )
            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = { onShowRatingPop(true) }
                    ),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RatingRow(
                    rating = itemDetails.rating,
                    showEmpty = true,
                    starSize = 24.dp,
                    emptyColor = LocalContentColor.current,
                )
                if (itemDetails.rating != null) {
                    Text(
                        text = "(${formatDecimal(itemDetails.rating)})",
                        fontSize = 13.sp,
                        modifier = Modifier
                            .alpha(0.75f)
                    )
                }
            }
        }

        // Favorite or Disliked? //
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp, start = 12.dp, end = 12.dp),
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
                text = "In Production",
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

    if (showRatingPop) {
        RatingPopup(
            currentRating = itemDetails.rating,
            onDismiss = { onShowRatingPop(false) },
            onRatingSelected = {
                onValueChange(itemDetails.copy(rating = it))
                onShowRatingPop(false)
            }
        )
    }

}


@Composable
fun NotesEntry(
    itemDetails: ItemDetails,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(itemDetails){ onValueChange(itemDetails) }

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
                                updatedText = if (currentLine.length == lastWord.length + 1) {
                                    it.dropLast(lastWord.length + 1)
                                } else {
                                    it.dropLast(lastWord.length)
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
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(tinDetailsList) {
        onValueChange(itemUiState.itemDetails)
        if (itemUiState.itemDetails.isSynced) {
            val syncTins = itemUiState.itemDetails.syncedQuantity
            onValueChange(itemUiState.itemDetails.copy(quantity = syncTins, quantityString = syncTins.toString()))
        }
    }

    Spacer(modifier = Modifier.height(7.dp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .background(
                color = if (tinDetailsList.isEmpty()) Color.Transparent else
                    LocalCustomColors.current.textField, RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
    ) {
        Spacer(modifier = Modifier.height(6.dp))

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
            Spacer(modifier = Modifier.height(6.dp))
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
                    validateDates = validateDates,
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
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
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
        var headerWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    headerWidth = with(density) { it.size.width.toDp() }
                }
        ) {
            val textFieldMax = headerWidth - 72.dp

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
                    val icon =
                        if (tinDetails.detailsExpanded) R.drawable.arrow_up else R.drawable.arrow_down
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Expand/contract details",
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { onTinValueChange(tinDetails.copy(detailsExpanded = !tinDetails.detailsExpanded)) }
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
                    var labelIsFocused by rememberSaveable { mutableStateOf(false) }

                    CustomTextField(
                        value = tinDetails.tinLabel,
                        onValueChange = {
                            onTinValueChange(
                                tinDetails.copy(tinLabel = it)
                            )
                        },
                        modifier = Modifier
                            .widthIn(max = textFieldMax)
                            .onFocusChanged { labelIsFocused = it.isFocused },
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
                                color = if (showError || !labelIsFocused) MaterialTheme.colorScheme.error else
                                    LocalContentColor.current
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
                        imageVector = ImageVector.vectorResource(id = R.drawable.remove_outline),
                        contentDescription = "Close",
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null
                            ) { removeTin() }
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

                    AutoCompleteText(
                        value = tinDetails.container,
                        onValueChange = { onTinValueChange(tinDetails.copy(container = it)) },
                        onOptionSelected = { onTinValueChange(tinDetails.copy(container = it)) },
                        allItems = itemUiState.autoContainers,
                        modifier = Modifier
                            .fillMaxWidth(),
                        trailingIcon = {
                            if (tinDetails.container.length > 4) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                    contentDescription = "Clear",
                                    modifier = Modifier
                                        .clickable(
                                            indication = LocalIndication.current,
                                            interactionSource = null
                                        ) { onTinValueChange(tinDetails.copy(container = "")) }
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

                    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
                    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
                    val decimalSeparator = symbols.decimalSeparator.toString()
                    val allowedPattern = remember(decimalSeparator) {
                        val ds = Regex.escape(decimalSeparator)
                        Regex("^(\\s*|(\\d*)?($ds\\d{0,2})?)$")
                    }

                    TextField(
                        value = tinDetails.tinQuantityString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                try {
                                    var parsedDouble: Double?

                                    if (it.isNotBlank()) {
                                        val preNumber = if (it.startsWith(decimalSeparator)) {
                                            "0$it"
                                        } else it
                                        val number = numberFormat.parse(preNumber)
                                        parsedDouble = number?.toDouble() ?: 0.0
                                    } else {
                                        parsedDouble = 0.0
                                    }

                                    onTinValueChange(
                                        tinDetails.copy(
                                            tinQuantityString = it,
                                            tinQuantity = parsedDouble,
                                        )
                                    )
                                } catch (e: ParseException) {
                                    Log.e("Add/Edit Entry", "Input: $it", e)
                                }
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
                        onValueChange = { onTinValueChange(tinDetails.copy(unit = it)) },
                        options = listOf("", "oz", "lbs", "grams"),
                        placeholder = {
                            Text(
                                text = "Unit",
                                modifier = Modifier
                                    .alpha(0.66f),
                                fontSize = 14.sp,
                            )
                        },
                        isError = tinDetails.tinQuantityString.isNotBlank() &&
                                tinDetails.unit.isBlank(),
                        modifier = Modifier
                            .weight(2f),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = LocalCustomColors.current.textField,
                            unfocusedContainerColor = LocalCustomColors.current.textField,
                            disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f),
                            errorPlaceholderColor = MaterialTheme.colorScheme.error,
                            errorContainerColor = LocalCustomColors.current.textField,
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                    val coroutineScope = rememberCoroutineScope()
                    val manuFocusRequester = remember { FocusRequester() }
                    val cellaredFocusRequester = remember { FocusRequester() }
                    val openedFocusRequester = remember { FocusRequester() }
                    val interactionSource = remember { MutableInteractionSource() }

                    var dateFieldWidth by remember { mutableIntStateOf(0) }

                    val (manufactureCellar, manufactureOpen, cellarOpen) = validateDates(tinDetails.manufactureDate, tinDetails.cellarDate, tinDetails.openDate)

                    // Manufacture //
                    OutlinedTextField(
                        value =
                            if (tinDetails.manufactureDateShort.isEmpty()) { " " } else {
                                if (dateFieldWidth > 420) {
                                    tinDetails.manufactureDateLong
                                } else {
                                    tinDetails.manufactureDateShort
                                }
                            },
                        onValueChange = { },
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { dateFieldWidth = it.size.width }
                            .focusRequester(manuFocusRequester),
                        enabled = true,
                        readOnly = true,
                        singleLine = true,
                        interactionSource = interactionSource,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showPicker("Manufacture")
                                    coroutineScope.launch {
                                        delay(50)
                                        manuFocusRequester.requestFocus()
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.calendar_month),
                                    contentDescription = "Select manufacture date",
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
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),

                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),

                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.background,

                            errorContainerColor = MaterialTheme.colorScheme.background,
                            errorBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            errorLabelColor = LocalContentColor.current,
                            errorTrailingIconColor = LocalContentColor.current,
                            errorTextColor = MaterialTheme.colorScheme.error,
                        ),
                        shape = MaterialTheme.shapes.extraSmall,
                        isError = tinDetails.manufactureDate != null && (
                                  (tinDetails.cellarDate != null && !manufactureCellar) ||
                                  (tinDetails.openDate != null && !manufactureOpen)
                                )
                    )

                    // Cellar //
                    OutlinedTextField(
                        value =
                            if (tinDetails.cellarDateShort.isEmpty()) { " " } else {
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
                            .focusRequester(cellaredFocusRequester),
                        enabled = true,
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showPicker("Cellared")
                                    coroutineScope.launch {
                                        delay(50)
                                        cellaredFocusRequester.requestFocus()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.calendar_month),
                                    contentDescription = "Select cellared date",
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
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),

                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),

                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.background,

                            errorContainerColor = MaterialTheme.colorScheme.background,
                            errorBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            errorLabelColor = LocalContentColor.current,
                            errorTrailingIconColor = LocalContentColor.current,
                            errorTextColor = MaterialTheme.colorScheme.error,
                        ),
                        shape = MaterialTheme.shapes.extraSmall,
                        isError = tinDetails.cellarDate != null && (
                                (tinDetails.manufactureDate != null && !manufactureCellar) ||
                                (tinDetails.openDate != null && !cellarOpen)
                                )
                    )

                    // Opened //
                    OutlinedTextField(
                        value =
                            if (tinDetails.openDateShort.isEmpty()) { " " } else {
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
                            .focusRequester(openedFocusRequester),
                        enabled = true,
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showPicker("Opened")
                                    coroutineScope.launch {
                                        delay(50)
                                        openedFocusRequester.requestFocus()
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.calendar_month),
                                    contentDescription = "Select open date",
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
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),

                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),

                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.background,

                            errorContainerColor = MaterialTheme.colorScheme.background,
                            errorBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            errorLabelColor = LocalContentColor.current,
                            errorTrailingIconColor = LocalContentColor.current,
                            errorTextColor = MaterialTheme.colorScheme.error,
                        ),
                        shape = MaterialTheme.shapes.extraSmall,
                        isError = tinDetails.openDate != null && (
                                (tinDetails.manufactureDate != null && !manufactureOpen) ||
                                        (tinDetails.cellarDate != null && !cellarOpen))
                    )

                    val selectableDates = remember(
                        tinDetails.manufactureDate,
                        tinDetails.cellarDate,
                        tinDetails.openDate,
                        datePickerLabel
                    ) {
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                return when (datePickerLabel) {
                                    "Manufacture" -> {
                                        if (tinDetails.cellarDate != null) {
                                            utcTimeMillis <= tinDetails.cellarDate
                                        } else if (tinDetails.openDate != null) {
                                            utcTimeMillis <= tinDetails.openDate
                                        } else {
                                            true
                                        }
                                    }
                                    "Cellared" -> {
                                        val minDate =
                                            tinDetails.manufactureDate?.let {
                                                LocalDateTime.ofInstant(
                                                    Instant.ofEpochMilli(it), ZoneOffset.UTC)
                                                    .toLocalDate()
                                                    .atStartOfDay(ZoneOffset.UTC)
                                                    .toInstant()
                                                    .toEpochMilli()
                                            } ?: Long.MIN_VALUE

                                        val maxDate =
                                            tinDetails.openDate?.let {
                                                LocalDateTime.ofInstant(
                                                    Instant.ofEpochMilli(it), ZoneOffset.UTC)
                                                    .plusDays(1)
                                                    .toLocalDate()
                                                    .atStartOfDay(ZoneOffset.UTC)
                                                    .toInstant()
                                                    .toEpochMilli() - 1
                                        } ?: Long.MAX_VALUE

                                        utcTimeMillis in minDate..maxDate
                                    }
                                    "Opened" -> {
                                        val minDate = tinDetails.cellarDate?.let {
                                            LocalDateTime.ofInstant(
                                                Instant.ofEpochMilli(it), ZoneOffset.UTC)
                                                .toLocalDate()
                                                .atStartOfDay(ZoneOffset.UTC)
                                                .toInstant()
                                                .toEpochMilli()
                                        } ?: tinDetails.manufactureDate?.let {
                                            LocalDateTime.ofInstant(
                                                Instant.ofEpochMilli(it), ZoneOffset.UTC)
                                                .toLocalDate()
                                                .atStartOfDay(ZoneOffset.UTC)
                                                .toInstant()
                                                .toEpochMilli()
                                        } ?: Long.MIN_VALUE
                                        utcTimeMillis >= minDate
                                    }
                                    else -> true
                                }
                            }
                        }
                    }

                    val initialDisplayMonth = when (datePickerLabel) {
                        "Manufacture" -> {
                            if (tinDetails.manufactureDate == null && (tinDetails.cellarDate != null || tinDetails.openDate != null)) {
                                val maxDate =
                                    tinDetails.cellarDate?.let {
                                        LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(it), ZoneOffset.UTC
                                        )
                                            .toLocalDate()
                                            .atStartOfDay(ZoneOffset.UTC)
                                            .toInstant()
                                            .toEpochMilli()
                                    } ?: tinDetails.openDate?.let {
                                        LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(it), ZoneOffset.UTC
                                        )
                                            .toLocalDate()
                                            .atStartOfDay(ZoneOffset.UTC)
                                            .toInstant()
                                            .toEpochMilli()
                                    }
                                maxDate
                            } else {
                                tinDetails.manufactureDate
                            }
                        }
                        "Cellared" -> {
                            if (tinDetails.cellarDate == null && (tinDetails.manufactureDate != null || tinDetails.openDate != null)) {
                                val minDate =
                                    tinDetails.manufactureDate?.let {
                                        LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(it), ZoneOffset.UTC
                                        )
                                            .toLocalDate()
                                            .atStartOfDay(ZoneOffset.UTC)
                                            .toInstant()
                                            .toEpochMilli()
                                    } ?: Long.MIN_VALUE
                                val maxDate =
                                    tinDetails.openDate?.let {
                                        LocalDateTime.ofInstant(
                                            Instant.ofEpochMilli(it), ZoneOffset.UTC
                                        )
                                            .plusDays(1)
                                            .toLocalDate()
                                            .atStartOfDay(ZoneOffset.UTC)
                                            .toInstant()
                                            .toEpochMilli() - 1
                                    } ?: Long.MAX_VALUE
                                if (tinDetails.manufactureDate != null) minDate else maxDate
                            } else {
                                tinDetails.cellarDate
                            }
                        }
                        "Opened" -> {
                            if (tinDetails.openDate == null && (tinDetails.manufactureDate != null || tinDetails.cellarDate != null)) {
                                val minDate = tinDetails.cellarDate?.let {
                                    LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(it), ZoneOffset.UTC
                                    )
                                        .toLocalDate()
                                        .atStartOfDay(ZoneOffset.UTC)
                                        .toInstant()
                                        .toEpochMilli()
                                } ?: tinDetails.manufactureDate?.let {
                                    LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(it), ZoneOffset.UTC
                                    )
                                        .toLocalDate()
                                        .atStartOfDay(ZoneOffset.UTC)
                                        .toInstant()
                                        .toEpochMilli()
                                } ?: Long.MIN_VALUE
                                minDate
                            } else {
                                tinDetails.openDate
                            }
                        }
                        else -> null
                    }

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
                                                openDateLong = dateStringLong,
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
                            initialDisplayMonth = initialDisplayMonth,
                            label = datePickerLabel,
                            selectableDates = selectableDates
                        )
                    }
                }

                // Finished //
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val disabled = tinDetails.openDate != null && tinDetails.openDate > System.currentTimeMillis()
                        LaunchedEffect(disabled) {
                            if (disabled) {
                                onTinValueChange(tinDetails.copy(finished = false))
                            }
                        }

                        Text(
                            text = "Finished",
                            modifier = Modifier
                                .offset(x = 0.dp, y = 1.dp)
                                .alpha(if (disabled) 0.5f else 1f),
                            fontSize = 14.sp,
                        )
                        CustomCheckBox(
                            checked = tinDetails.finished,
                            onCheckedChange = {
                                onTinValueChange(tinDetails.copy(finished = it))
                            },
                            checkedIcon = R.drawable.check_box_24,
                            uncheckedIcon = R.drawable.check_box_outline_24,
                            enabled = !disabled,
                            modifier = Modifier
                                .size(22.dp)
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
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { onTinValueChange(tinDetails.copy(detailsExpanded = true)) }
                    .fillMaxWidth()
            )
        }
    }
}


/** custom composables */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    currentMillis: Long? = null,
    selectableDates: SelectableDates,
    initialDisplayMonth: Long? = null,
    label: String = "Select",
) {
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        initialSelectedDateMillis = currentMillis,
        initialDisplayedMonthMillis = initialDisplayMonth,
        selectableDates = selectableDates
    )
    val datePickerFormatter = remember { DatePickerDefaults.dateFormatter() }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
          .wrapContentHeight(),
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            modifier = Modifier
                .requiredWidth(360.dp)
                .heightIn(max = 582.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.background,
            tonalElevation = DatePickerDefaults.TonalElevation,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // date picker
                Box(Modifier.weight(1f, fill = false)) {
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
                            disabledDayContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                        )
                    )
                }

                // clear option
                if (datePickerState.displayMode == DisplayMode.Picker) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { datePickerState.selectedDateMillis = null },
                            enabled = datePickerState.selectedDateMillis != null,
                            contentPadding = PaddingValues(12.dp, 4.dp),
                            modifier = Modifier
                                .heightIn(32.dp, 32.dp)
                        ) {
                            Text(text = "Clear Date")
                        }
                    }
                }

                // confirm/cancel buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.Top
                ) {
                    TextButton(
                        onClick = { onDismiss() },
                        contentPadding = PaddingValues(12.dp, 4.dp),
                        modifier = Modifier
                            .heightIn(32.dp, 32.dp)
                    ) {
                        Text(text = "Cancel")
                    }
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
                        },
                        contentPadding = PaddingValues(12.dp, 4.dp),
                        modifier = Modifier
                            .heightIn(32.dp, 32.dp)
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}


@Composable
fun RatingPopup(
    onDismiss: () -> Unit,
    onRatingSelected: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    currentRating: Double? = null,
) {
    var currentRatingString by rememberSaveable { mutableStateOf(formatDecimal(currentRating)) }
    var parsedDouble by rememberSaveable { mutableStateOf<Double?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .wrapContentHeight(),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.small,
        title = {
            Text(
                text = "Rating",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier
            )
        },
        text = {
            Column {
                val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
                val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
                val decimalSeparator = symbols.decimalSeparator.toString()
                val allowedPattern = remember(decimalSeparator) {
                    val ds = Regex.escape(decimalSeparator)
                    Regex("^(\\s*|(\\d)?($ds\\d{0,2})?)$")
                }
                Text(
                    text = "Set a rating (maximum 5). To make an item unrated, make the field " +
                            "blank. Supports fractional ratings (up to 2 decimal places).",
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    TextField(
                        value = currentRatingString,
                        onValueChange = {
                            if (it.matches(allowedPattern)) {
                                currentRatingString = it

                                try {
                                    if (it.isNotBlank()) {
                                        val preNumber = if (it.startsWith(decimalSeparator)) {
                                            "0$it"
                                        } else it
                                        val number = numberFormat.parse(preNumber)
                                        parsedDouble = number?.toDouble() ?: 0.0
                                        if (parsedDouble!! > 5.0) {
                                            parsedDouble = 5.0
                                        }
                                    } else {
                                        parsedDouble = null
                                    }

                                } catch (e: ParseException) {
                                    Log.e("Rating", "Input: $it", e)
                                }
                            }
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .padding(end = 8.dp),
                        enabled = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
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
                    val alpha = if (currentRatingString.isNotBlank()) .75f else 0.38f
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                        contentDescription = "Clear",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = currentRatingString.isNotBlank()
                            ) {
                                currentRatingString = ""
                                parsedDouble = null
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .alpha(alpha)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRatingSelected(parsedDouble) },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropDown(
    selectedValue: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedContainerColor = LocalCustomColors.current.textField,
        unfocusedContainerColor = LocalCustomColors.current.textField,
        disabledContainerColor = LocalCustomColors.current.textField.copy(alpha = 0.50f)),
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val onExpandChange: (Boolean) -> Unit = { expanded = it }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandChange(!expanded) },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedValue,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            colors = colors,
            shape = MaterialTheme.shapes.extraSmall,
            placeholder = placeholder,
            isError = isError,
            enabled = enabled
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier
                .exposedDropdownSize(true),
            containerColor = LocalCustomColors.current.textField,
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = it.ifBlank { "(Blank)" },
                            color = if (it.isBlank()) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                        )
                    },
                    onClick = {
                        onExpandChange(false)
                        onValueChange(it)
                    }
                )
            }
        }
    }
}
