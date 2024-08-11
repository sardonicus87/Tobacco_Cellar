package com.example.tobaccocellar.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import kotlinx.coroutines.Dispatchers
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
                showMenu = false,
            )
        },
    ) { innerPadding ->
        AddEntryBody(
            itemUiState = viewModel.itemUiState,
            existState = viewModel.existState,
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
            resetExistState = viewModel::resetExistState,
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
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        ItemInputForm(
            itemDetails = itemUiState.itemDetails,
            onValueChange = onItemValueChange,
            modifier = Modifier
                .fillMaxWidth()
        )
         Button(
             onClick = { onSaveClick() },
             enabled = itemUiState.isEntryValid,
             shape = MaterialTheme.shapes.small,
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
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
                enabled = itemUiState.isEntryValid,
                shape = MaterialTheme.shapes.small,
                colors = ButtonColors(
                    containerColor = Color(0xFF990000),
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
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


/** Item Input Form **/

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ItemInputForm(
    itemDetails: ItemDetails,
    onValueChange: (ItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {

// Required Fields //
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 0.dp, start = 0.dp, end = 0.dp),
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
                .padding(start = 16.dp, end = 8.dp, top = 0.dp, bottom = 0.dp),
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
                TextField(
                    value = itemDetails.brand,
                    onValueChange = { onValueChange(itemDetails.copy(brand = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp),
                    enabled = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    )
                )
            }
            /* TODO add autocomplete for brands */

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
                    keyboardOptions = KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
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
                    .padding(start = 16.dp, end = 8.dp, top = 0.dp, bottom = 0.dp),
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
                                painter = painterResource(id = R.drawable.increase),
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
                                painter = painterResource(id = R.drawable.decrease),
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
                        FavoriteHeart(
                            checked = itemDetails.favorite,
                            onCheckedChange = {
                                if (itemDetails.favorite) {
                                    onValueChange(itemDetails.copy(favorite = it))
                                } else {
                                    onValueChange(itemDetails.copy(favorite = it, hated = false))
                                }
                                              },
                            modifier = Modifier
                                .padding(0.dp)
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
                            checked = itemDetails.hated,
                            onCheckedChange = {
                                if (itemDetails.hated) {
                                    onValueChange(itemDetails.copy(hated = it))
                                } else {
                                    onValueChange(itemDetails.copy(hated = it, favorite = false))
                                }
                                              },
                            modifier = Modifier
                                .padding(0.dp)
                        )
                    }
                }
                // Hidden field to hold notes so they're not overwritten //
                Row(
                    modifier = Modifier
                        .padding(0.dp)
                        .width(0.dp)
                        .height(0.dp)
                        .offset(x = 0.dp, y = (-16).dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = itemDetails.notes,
                        onValueChange = { onValueChange(itemDetails.copy(notes = it)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = false,
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
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            enabled = false,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
    AddEntryBody(itemUiState = ItemUiState(
        ItemDetails(
            brand = "Cornell & Diehl", blend = "Eight State Burley (2024)", type = "Burley", quantity = 2, hated = false
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