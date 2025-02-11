package com.sardonicus.tobaccocellar.ui.items

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.composables.AutoSizeText
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

object BulkEditDestination : NavigationDestination {
    override val route = "bulk_edit"
    override val titleRes = R.string.bulk_edit_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: BulkEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val bulkEditUiState by viewModel.bulkEditUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar = viewModel.showSnackbar.collectAsState()
    val saveIndicator by viewModel.saveIndicator.collectAsState()

    fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

    if (showSnackbar.value) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "Batch edits saved.",
                duration = SnackbarDuration.Short
            )
            viewModel.snackbarShown()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(BulkEditDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = canNavigateBack,
                modifier = Modifier
                    .noRippleClickable(onClick = { focusManager.clearFocus() }),
                navigateUp = onNavigateUp,
                showMenu = false,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(0.dp),
                snackbar = { Snackbar(it) }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BulkEditBody(
                loading = bulkEditUiState.loading,
                saveIndicator = saveIndicator,
                tabIndex = viewModel.tabIndex,
                onTabChange = { viewModel.tabIndex = it },
                items = bulkEditUiState.items,
                selectedItems = viewModel.editingState.selectedItems,
                editingState = viewModel.editingState,
                updateSelection = viewModel::updateSelection,
                clearSelections = viewModel::resetSelectedItems,
                selectAll = viewModel::selectAll,
                onValueChange = viewModel::onValueChange,
                batchEditValidation = viewModel::fieldSelected,
                batchEdit = viewModel::batchEditSave,
                autoGenres = bulkEditUiState.autoGenres,
                autoCuts = bulkEditUiState.autoCuts,
                modifier = modifier
                    .padding(0.dp)
                    .fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEditBody(
    loading: Boolean,
    saveIndicator: Boolean,
    tabIndex: Int,
    onTabChange: (Int) -> Unit,
    items: List<Items>,
    selectedItems: List<Items>,
    editingState: EditingState,
    updateSelection: (Items) -> Unit,
    clearSelections: () -> Unit,
    selectAll: () -> Unit,
    onValueChange: (EditingState) -> Unit,
    batchEditValidation: () -> Boolean,
    batchEdit: () -> Unit,
    autoGenres: List<String>,
    autoCuts: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (loading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1.5f)
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(0.dp)
                        .size(48.dp)
                        .weight(0.5f),
                )
                Spacer(
                    modifier = Modifier
                        .weight(2f)
                )

            }
        } else {
            val titles = listOf("Select Items", "Batch Edit")

            Box {
                Column(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    TabRow(
                        selectedTabIndex = tabIndex,
                        modifier = Modifier
                            .padding(bottom = 1.dp),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = LocalContentColor.current,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[tabIndex]),
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
                                    selected = tabIndex == index,
                                    onClick = {
                                        //   selectedTabIndex = index
                                        onTabChange(index)
                                    },
                                    modifier = Modifier
                                        .background(
                                            if (tabIndex == index) MaterialTheme.colorScheme.background
                                            else LocalCustomColors.current.backgroundUnselected
                                        ),
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.SemiBold,
                                        )
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onBackground,
                                    unselectedContentColor = MaterialTheme.colorScheme.outline,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        when (tabIndex) {
                            0 ->
                                BulkSelections(
                                    items = items,
                                    selectedItems = selectedItems,
                                    updateSelection = updateSelection,
                                    clearSelections = clearSelections,
                                    selectAll = selectAll,
                                    modifier = Modifier
                                )

                            1 ->
                                BulkEditing(
                                    editingState = editingState,
                                    onValueChange = onValueChange,
                                    batchEditValidation = batchEditValidation,
                                    batchEdit = batchEdit,
                                    autoGenres = autoGenres,
                                    autoCuts = autoCuts,
                                    modifier = Modifier
                                )

                            else -> throw IllegalArgumentException("Invalid tab position")
                        }
                    }
                }
                if (saveIndicator) {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = .38f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(
                            modifier = Modifier
                                .weight(1.5f)
                        )
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(0.dp)
                                .size(48.dp)
                                .weight(0.5f),
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(2f)
                        )
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BulkSelections(
    items: List<Items>,
    selectedItems: List<Items>,
    updateSelection: (Items) -> Unit,
    clearSelections: () -> Unit,
    selectAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            TextButton(
                onClick = { selectAll() },
                modifier = Modifier,
            ) {
                Text(
                    text = "Select All",
                )
            }
            TextButton(
                onClick = { clearSelections() },
                modifier = Modifier,
                enabled = selectedItems.isNotEmpty(),
            ) {
                Text(
                    text = "Clear Selections",
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
//            item(span = { GridItemSpan(2) }) {
//                Spacer(
//                    modifier = Modifier
//                        .height(8.dp)
//                )
//            }
            items(items, key = { it.id }) {
                val selected = selectedItems.contains(it)
                BulkSelectionsItem(
                    item = it,
                    selected = selected,
                    onItemClick = { updateSelection(it) },
                )
            }
            item(span = { GridItemSpan(2) }) {
                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

    }
}

@Composable
fun BulkEditing(
    editingState: EditingState,
    onValueChange: (EditingState) -> Unit,
    batchEditValidation: () -> Boolean,
    batchEdit: () -> Unit,
    autoGenres: List<String>,
    autoCuts: List<String>,
    modifier: Modifier = Modifier,
) {
    var confirmEdit by remember { mutableStateOf(false) }
    val selectedItems = editingState.selectedItems

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(.7f)
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
    ) {
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
        // Field selections //
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
        ) {
            // Type //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                ) {
                    Checkbox(
                        checked = editingState.typeSelected,
                        onCheckedChange = {
                            onValueChange(editingState.copy(typeSelected = it))
                        }
                    )
                }
                Text(
                    text = "Type:",
                    modifier = Modifier
                        .width(70.dp)
                        .padding(end = 4.dp),
                    color = if (!editingState.typeSelected) LocalContentColor.current.copy(alpha = 0.50f) else LocalContentColor.current
                )
                CustomDropDown(
                    selectedValue = editingState.type,
                    onValueChange = { onValueChange(editingState.copy(type = it)) },
                    options = listOf("", "Aromatic", "English", "Burley", "Virginia", "Other"),
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = editingState.typeSelected
                )
            }

            // Subgenre //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                ) {
                    Checkbox(
                        checked = editingState.genreSelected,
                        onCheckedChange = {
                            onValueChange(editingState.copy(genreSelected = it))
                        }
                    )
                }
                AutoSizeText(
                    text = "Sub-\ngenre:",
                    fontSize = 16.sp,
                    minFontSize = 8.sp,
                    modifier = Modifier
                        .padding(end = 4.dp),
                    width = 70.dp,
                    height = 38.dp,
                    contentAlignment = Alignment.CenterStart,
                    color = if (!editingState.genreSelected) LocalContentColor.current.copy(
                        alpha = 0.50f
                    ) else LocalContentColor.current
                )

                val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

                AutoCompleteText(
                    value = editingState.subGenre,
                    onValueChange = {
                        onValueChange(editingState.copy(subGenre = it))

                        if (it.length >= 2) {
                            val startsWith = autoGenres.filter { genre ->
                                genre.startsWith(it, ignoreCase = true)
                            }
                            val otherWordsStartsWith = autoGenres.filter { genre ->
                                genre.split(" ").drop(1).any { word ->
                                    word.startsWith(it, ignoreCase = true)
                                } && !genre.startsWith(it, ignoreCase = true)
                            }
                            val contains = autoGenres.filter { genre ->
                                genre.contains(it, ignoreCase = true)
                                        && !genre.startsWith(it, ignoreCase = true) &&
                                        !otherWordsStartsWith.contains(genre)
                            }
                            val selected = autoGenres.filter { genre ->
                                genre == it
                            }

                            suggestions.value =
                                (startsWith + otherWordsStartsWith + contains) - selected
                        } else {
                            suggestions.value = emptyList()
                        }
                    },
                    onOptionSelected = { suggestion, currentText ->
                        onValueChange(editingState.copy(subGenre = suggestion))
                        suggestions.value = emptyList()
                    },
                    suggestions = suggestions.value,
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        if (editingState.subGenre.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable {
                                        onValueChange(editingState.copy(subGenre = ""))
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
                    enabled = editingState.genreSelected
                )
            }

            // Cut //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                ) {
                    Checkbox(
                        checked = editingState.cutSelected,
                        onCheckedChange = {
                            onValueChange(editingState.copy(cutSelected = it))
                        }
                    )
                }
                Text(
                    text = "Cut:",
                    modifier = Modifier
                        .width(70.dp)
                        .padding(end = 4.dp),
                    color = if (!editingState.cutSelected) LocalContentColor.current.copy(alpha = 0.50f) else LocalContentColor.current
                )

                val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

                AutoCompleteText(
                    value = editingState.cut,
                    onValueChange = {
                        onValueChange(editingState.copy(cut = it))

                        if (it.length >= 2) {
                            val startsWith = autoCuts.filter { cut ->
                                cut.startsWith(it, ignoreCase = true)
                            }
                            val otherWordsStartsWith = autoCuts.filter { cut ->
                                cut.split(" ").drop(1).any { word ->
                                    word.startsWith(it, ignoreCase = true)
                                } && !cut.startsWith(it, ignoreCase = true)
                            }
                            val contains = autoCuts.filter { cut ->
                                cut.contains(it, ignoreCase = true)
                                        && !cut.startsWith(it, ignoreCase = true) &&
                                        !otherWordsStartsWith.contains(cut)
                            }
                            val selected = autoCuts.filter { cut ->
                                cut == it
                            }

                            suggestions.value =
                                (startsWith + otherWordsStartsWith + contains) - selected
                        } else {
                            suggestions.value = emptyList()
                        }
                    },
                    onOptionSelected = { suggestion, currentText ->
                        onValueChange(editingState.copy(cut = suggestion))
                        suggestions.value = emptyList()
                    },
                    suggestions = suggestions.value,
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        if (editingState.cut.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable {
                                        onValueChange(editingState.copy(cut = ""))
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
                    ),
                    enabled = editingState.cutSelected
                )
            }

            // Rating //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                ) {
                    Checkbox(
                        checked = editingState.ratingSelected,
                        onCheckedChange = {
                            onValueChange(editingState.copy(ratingSelected = it))
                        }
                    )
                }
                Text(
                    text = "Rating:",
                    modifier = Modifier
                        .width(70.dp)
                        .padding(end = 4.dp),
                    color = if (!editingState.ratingSelected) LocalContentColor.current.copy(
                        alpha = 0.50f
                    ) else LocalContentColor.current
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorite
                    Row(
                        modifier = Modifier
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Favorite?",
                            modifier = Modifier
                                .offset(x = 0.dp, y = 1.dp),
                            color = if (!editingState.ratingSelected) LocalContentColor.current.copy(
                                alpha = 0.50f
                            ) else LocalContentColor.current
                        )
                        CustomCheckBox(
                            checked = editingState.favorite,
                            onCheckedChange = {
                                if (editingState.favorite) {
                                    onValueChange(editingState.copy(favorite = it))
                                } else {
                                    onValueChange(
                                        editingState.copy(
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
                            ),
                            enabled = editingState.ratingSelected
                        )
                    }
                    // Disliked
                    Row(
                        modifier = Modifier
                            .padding(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Disliked?",
                            modifier = Modifier
                                .offset(x = 0.dp, y = 1.dp),
                            color = if (!editingState.ratingSelected) LocalContentColor.current.copy(
                                alpha = 0.50f
                            ) else LocalContentColor.current
                        )
                        CustomCheckBox(
                            checked = editingState.disliked,
                            onCheckedChange = {
                                if (editingState.disliked) {
                                    onValueChange(editingState.copy(disliked = it))
                                } else {
                                    onValueChange(
                                        editingState.copy(
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
                            ),
                            enabled = editingState.ratingSelected
                        )
                    }
                }
            }

            // Production Status //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                ) {
                    Checkbox(
                        checked = editingState.productionSelected,
                        onCheckedChange = {
                            onValueChange(editingState.copy(productionSelected = it))
                        }
                    )
                }
                AutoSizeText(
                    text = "Production Status:",
                    fontSize = 16.sp,
                    minFontSize = 8.sp,
                    modifier = Modifier
                        .padding(end = 4.dp),
                    width = 70.dp,
                    height = 36.dp,
                    contentAlignment = Alignment.CenterStart,
                    color = if (!editingState.productionSelected) LocalContentColor.current.copy(
                        alpha = 0.50f
                    ) else LocalContentColor.current
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "In Production?",
                        modifier = Modifier
                            .offset(x = 0.dp, y = 1.dp),
                        color = if (!editingState.productionSelected) LocalContentColor.current.copy(
                            alpha = 0.50f
                        ) else LocalContentColor.current
                    )
                    CustomCheckBox(
                        checked = editingState.inProduction,
                        onCheckedChange = {
                            onValueChange(editingState.copy(inProduction = it))
                        },
                        checkedIcon = R.drawable.check_box_24,
                        uncheckedIcon = R.drawable.check_box_outline_24,
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = editingState.productionSelected
                    )
                }
            }

            // Sync Tins //
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                ) {
                    Checkbox(
                        checked = editingState.syncTinsSelected,
                        onCheckedChange = {
                            onValueChange(editingState.copy(syncTinsSelected = it))
                        }
                    )
                }
                AutoSizeText(
                    text = "Sync entry/\ntin quantity",
                    fontSize = 16.sp,
                    minFontSize = 8.sp,
                    modifier = Modifier
                        .padding(end = 4.dp),
                    width = 70.dp,
                    height = 36.dp,
                    contentAlignment = Alignment.CenterStart,
                    color = if (!editingState.syncTinsSelected) LocalContentColor.current.copy(
                        alpha = 0.50f
                    ) else LocalContentColor.current
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sync Tins?",
                        modifier = Modifier
                            .offset(x = 0.dp, y = 1.dp),
                        color = if (!editingState.syncTinsSelected) LocalContentColor.current.copy(
                            alpha = 0.50f
                        ) else LocalContentColor.current
                    )
                    CustomCheckBox(
                        checked = editingState.syncTins,
                        onCheckedChange = {
                            onValueChange(editingState.copy(syncTins = it))
                        },
                        checkedIcon = R.drawable.check_box_24,
                        uncheckedIcon = R.drawable.check_box_outline_24,
                        modifier = Modifier
                            .padding(0.dp),
                        enabled = editingState.syncTinsSelected
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .height(12.dp)
        )

        // Batch edit confirm //
        Button(
            onClick = { confirmEdit = true },
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            enabled = selectedItems.isNotEmpty() && batchEditValidation(),
        ) {
            Text(
                text = "Batch Save",
                modifier = Modifier
            )
        }
        if (confirmEdit) {
            ConfirmEditDialog(
                onEditConfirm = {
                    confirmEdit = false
                    batchEdit()
                },
                onEditCancel = { confirmEdit = false }
            )
        }

    }
}

@Composable
private fun ConfirmEditDialog(
    onEditConfirm: () -> Unit,
    onEditCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text("Are you sure?") },
        text = { Text("Are you sure you want to edit these items?") },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        dismissButton = {
            TextButton(onClick = onEditCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onEditConfirm) {
                Text(stringResource(R.string.yes))
            }
        }
    )
}


@Composable
fun BulkSelectionsItem(
    item: Items,
    selected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else LocalCustomColors.current.darkNeutral
    val textColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, shape = RoundedCornerShape(4.dp))
            .background(color = background, shape = RoundedCornerShape(4.dp))
            .clickable(
                onClick = onItemClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() })
            .padding(6.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.blend,
            modifier = Modifier,
            maxLines = 1,
            fontSize = 15.sp,
            overflow = TextOverflow.Ellipsis,
            color = textColor
        )
        Text(
            text = item.brand,
            modifier = Modifier
                .padding(start = 8.dp),
            maxLines = 1,
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Normal,
            color = textColor
        )

    }
}