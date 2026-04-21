package com.sardonicus.tobaccocellar.ui.addEditItems

import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.composables.AutoCompleteText
import com.sardonicus.tobaccocellar.ui.composables.CustomCheckbox
import com.sardonicus.tobaccocellar.ui.composables.CustomDropDown
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

@Composable
fun TinsEntry(
    tinDetailsList: List<TinDetails>,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    itemUiState: ItemUiState,
    addTin: () -> Unit,
    removeTin: (Int) -> Unit,
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    modifier: Modifier = Modifier
) {
    Spacer(Modifier.height(7.dp))
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
        Spacer(Modifier.height(6.dp))

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
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun IndividualTin(
    tinDetails: TinDetails,
    tinDetailsList: List<TinDetails>,
    tempTinId: Int,
    onTinValueChange: (TinDetails) -> Unit,
    isTinLabelValid: (String, Int) -> Boolean,
    showError: Boolean,
    removeTin: () -> Unit,
    itemUiState: ItemUiState,
    validateDates: (Long?, Long?, Long?) -> Triple<Boolean, Boolean, Boolean>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect (tinDetailsList) {
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
                        CustomCheckbox(
                            checked = tinDetails.finished,
                            onCheckedChange = {
                                onTinValueChange(tinDetails.copy(finished = it))
                            },
                            size = 22.dp,
                            checkedIcon = R.drawable.check_box_24,
                            uncheckedIcon = R.drawable.check_box_outline_24,
                            enabled = !disabled,
                            modifier = Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDatePickerDialog(
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