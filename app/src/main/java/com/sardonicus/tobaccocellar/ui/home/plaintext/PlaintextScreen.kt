package com.sardonicus.tobaccocellar.ui.home.plaintext

import android.content.ClipData
import android.content.Context
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.PrintHelper
import com.sardonicus.tobaccocellar.ui.AppViewModelProvider
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.composables.CustomTextField
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.IncreaseDecrease
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.home.formatDecimal
import com.sardonicus.tobaccocellar.ui.navigation.NavigationDestination
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import kotlin.math.roundToInt

object PlaintextDestination : NavigationDestination {
    override val route = "plaintext"
    override val titleRes = R.string.plaintext_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaintextScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: PlaintextViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current
    val plaintextState by viewmodel.listState.collectAsState()
    val filterViewModel = LocalCellarApplication.current.filterViewModel
    val templateView by viewmodel.setTemplateView.collectAsState()
    val formatString by viewmodel.formatStringEntry.collectAsState()
    val delimiter by viewmodel.delimiter.collectAsState()
    val printOptions by viewmodel.printOptions.collectAsState()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { focusManager.clearFocus() },
        topBar = {
            CellarTopAppBar(
                title = stringResource(PlaintextDestination.titleRes),
                scrollBehavior = scrollBehavior,
                canNavigateBack = true,
                navigateUp = onNavigateUp,
                showMenu = false,
                modifier = Modifier,
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
            PlaintextBody(
                plaintextState = plaintextState,
                formatString = formatString,
                delimiter = delimiter,
                printOptions = printOptions,
                filterViewModel = filterViewModel,
                saveFormatString = viewmodel::saveFormatString,
                sortMenuState = viewmodel.sortMenuState,
                updateSortMenuState = viewmodel::updateSortMenuState,
                updateSorting = viewmodel::updateSorting,
                updateSubSorting = viewmodel::updateSubSorting,
                setTemplateView = viewmodel::setTemplateView,
                savePrintOptions = viewmodel::savePrintOptions,
                savePreset = viewmodel::savePreset,
                templateView = templateView,
                modifier = Modifier
                    .fillMaxSize()

            )
        }
    }
}

@Composable
fun PlaintextBody(
    plaintextState: PlaintextListState,
    formatString: String,
    delimiter: String,
    printOptions: PrintOptions,
    filterViewModel: FilterViewModel,
    saveFormatString: (String, String) -> Unit,
    sortMenuState: SortMenuState,
    updateSortMenuState: (SortMenuState) -> Unit,
    updateSorting: (String, Boolean) -> Unit,
    updateSubSorting: (String) -> Unit,
    setTemplateView: (Boolean) -> Unit,
    savePrintOptions: (Float, Double) -> Unit,
    savePreset: (Int, String, String) -> Unit,
    templateView: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val printList = plaintextState.plainList

    val setTemplateText = if (templateView) "See List" else "Set Format"
    val screenHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp }.dp - 96.dp
    val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp }.dp

    var printDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(sortMenuState.mainMenu || sortMenuState.subMenu) {
        if (sortMenuState.mainMenu) {
            updateSortMenuState(sortMenuState.copy(mainMenu = false))
        }
        if (sortMenuState.subMenu) {
            updateSortMenuState(sortMenuState.copy(subMenu = false))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ){
        // Header
        Row(
            modifier = Modifier
                .background(LocalCustomColors.current.homeHeaderBg)
                .padding(horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set templateView switch
            Box {
                TextButton(
                    onClick = { setTemplateView(!templateView) },
                    modifier = Modifier
                        .heightIn(40.dp, 40.dp),
                    contentPadding = PaddingValues(8.dp, 2.dp),
                ) {
                    Box(
                        modifier = Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Set Format", color = Color.Transparent)
                        Text(setTemplateText)
                    }
                }
            }

            // Open filter sheet
            Box {
                TextButton(
                    onClick = { filterViewModel.openBottomSheet() },
                    modifier = Modifier
                        .heightIn(40.dp, 40.dp),
                    contentPadding = PaddingValues(8.dp, 2.dp),
                ) { Text("Filtering") }
            }

            // Sorting
            Box {
                var reverse: Boolean

                val hasSubOptions = listOf(
                    PlaintextSortOption.TIN_LABEL.value,
                    PlaintextSortOption.TIN_CONTAINER.value,
                    PlaintextSortOption.TIN_QUANTITY.value
                )
                val subOptionsList = listOf(
                    PlaintextSortOption.DEFAULT.value,
                    PlaintextSortOption.TIN_DEFAULT.value,
                    PlaintextSortOption.BRAND.value,
                    PlaintextSortOption.BLEND.value
                )

                val density = LocalDensity.current
                var yPositions by remember { mutableStateOf(mapOf<String, Dp>()) }
                var mainWidth by remember { mutableStateOf(0.dp) }
                var mainPosition by remember { mutableStateOf(0.dp) }
                val alteredColor = Color.Black.copy(alpha = .1f).compositeOver(LocalCustomColors.current.textField)
                val color = if (sortMenuState.subMenu) alteredColor else LocalCustomColors.current.textField

                TextButton(
                    onClick = { updateSortMenuState(sortMenuState.copy(mainMenu = !sortMenuState.mainMenu)) },
                    enabled = plaintextState.sortOptions.isNotEmpty(),
                    modifier = Modifier
                        .heightIn(40.dp, 40.dp),
                    contentPadding = PaddingValues(8.dp, 2.dp),
                ) { Text("Sorting") }

                // Main options
                DropdownMenu(
                    expanded = sortMenuState.mainMenu,
                    onDismissRequest = { updateSortMenuState(sortMenuState.copy(mainMenu = false)) },
                    modifier = Modifier
                        .onGloballyPositioned {
                            mainWidth = with(density) { it.size.width.toDp() }
                            mainPosition = with(density) { it.positionOnScreen().x.toDp() }
                        },
                    containerColor = color,
                ) {
                    plaintextState.sortOptions.forEach { option ->
                        var yPosition by remember { mutableStateOf(0.dp) }

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Text(
                                        text = option.value,
                                        modifier = Modifier
                                            .padding(end = 2.dp),
                                        color = LocalContentColor.current.copy(alpha = if (sortMenuState.subMenu) 0.85f else 1.0f)
                                    )
                                    // Sort indicator and/or submenu
                                    if (plaintextState.sortState.value == option.value) {
                                        Box {
                                            Image(
                                                painter = painterResource(id = plaintextState.sortState.icon),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(0.dp),
                                                colorFilter = ColorFilter.tint(
                                                    LocalContentColor.current
                                                )
                                            )
                                        }
                                    } else if (option.value in hasSubOptions) {
                                        Box {
                                            Image(
                                                painter = painterResource(R.drawable.arrow_right),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp),
                                                colorFilter = ColorFilter.tint(
                                                    LocalContentColor.current.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    } else {
                                        Spacer(Modifier.width(20.dp))
                                    }
                                }
                            },
                            onClick = {
                                if (option.value in hasSubOptions) {
                                    updateSortMenuState(
                                        sortMenuState.copy(
                                            subMenu = true,
                                            mainSelection = option.value,
                                        )
                                    )
                                }
                                else { updateSorting(option.value, true) }
                            },
                            modifier = Modifier
                                .onGloballyPositioned {
                                    yPosition = with(density) { (it.positionInParent().y).toDp() }
                                }
                        )

                        yPositions = yPositions + (option.value to yPosition)

                    }
                }

                // Sub sorting menu
                var subWidth by remember { mutableStateOf(0.dp) }
                val yOffset = yPositions[sortMenuState.mainSelection]
                val mainRightEdge = mainPosition + mainWidth
                val remainingSpace = screenWidth - mainRightEdge
                val xOffset = if (subWidth < (remainingSpace * 1.05f)) mainWidth else -(subWidth)

                DropdownMenu(
                    expanded = sortMenuState.subMenu,
                    onDismissRequest = { updateSortMenuState(sortMenuState.copy(subMenu = false)) },
                    containerColor = LocalCustomColors.current.textField,
                    modifier = Modifier
                        .onGloballyPositioned{
                            subWidth = with(density) { it.size.width.toDp() }
                        },
                    offset = DpOffset(xOffset, yOffset ?: 0.dp)
                ) {
                    subOptionsList.forEach {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    Text(
                                        text = it,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                    )
                                    val color = if (sortMenuState.subSelection == it && plaintextState.sortState.value == sortMenuState.mainSelection) {
                                            LocalContentColor.current } else Color.Transparent
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                }
                            },
                            onClick = {
                                reverse = sortMenuState.subSelection == it && plaintextState.sortState.value == sortMenuState.mainSelection
                                updateSortMenuState(sortMenuState.copy(subSelection = it))
                                updateSorting(sortMenuState.mainSelection, reverse)
                                updateSubSorting(it)
                            }
                        )
                    }
                }
            }

            // Copy
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Plaintext", plaintextState.plainList)))

                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .padding(0.dp)
                    .size(40.dp),
                enabled = plaintextState.plainList.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.copy_icon),
                    contentDescription = "Copy",
                    modifier = Modifier
                        .padding(0.dp),
                )
            }

            // Print
            IconButton(
                onClick = { printDialog = true },
                modifier = Modifier
                    .padding(0.dp)
                    .size(40.dp),
                enabled = plaintextState.plainList.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.print_icon),
                    contentDescription = "Print",
                    modifier = Modifier
                        .padding(0.dp),
                )
            }
        }

        // Main body
        GlowBox(
            color = GlowColor(Color.Black.copy(alpha = 0.3f)),
            size = GlowSize(top = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                if (plaintextState.loading) {
                    LoadingIndicator(modifier = Modifier.height(screenHeight))
                } else {
                    if (templateView) {
                        PlaintextFormatting(
                            plaintextState = plaintextState,
                            formatString = formatString,
                            delimiter = delimiter,
                            saveFormatString = saveFormatString,
                            savePreset = savePreset,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    } else {
                        PlaintextList(
                            plaintextState = plaintextState,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (printDialog) {
        PrintDialog(
            savedFontSize = printOptions.font,
            savedMargin = printOptions.margin,
            onPrintConfirm = { font, margin ->
                printDialog = false

                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "Plaintext Output"

                printManager?.print(jobName, PrintHelper(context, jobName, printList, font, margin), null)
                savePrintOptions(font, margin)
            },
            onPrintCancel = { font, margin ->
                printDialog = false
                savePrintOptions(font, margin)
            },
        )
    }

}


@Composable
fun PlaintextList(
    plaintextState: PlaintextListState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (plaintextState.formatString.isBlank()) {
            Text(
                text = "Please set a format string.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        } else {
            SelectionContainer {
                Text(
                    text = plaintextState.plainList,
                    fontSize = 15.sp,
                )
            }
        }
    }
}


@Composable
fun PlaintextFormatting(
    plaintextState: PlaintextListState,
    formatString: String,
    delimiter: String,
    saveFormatString: (String, String) -> Unit,
    savePreset: (Int, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var saveDialog by rememberSaveable { mutableStateOf(false) }
    var loadDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        // Format
        Text(
            text = "Format Output:",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Min)
            ) {
                Box (
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "String:",
                        modifier = Modifier
                            .padding(end = 12.dp),
                        maxLines = 1
                    )
                }
                Box (
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Delimiter:",
                        modifier = Modifier
                            .padding(end = 12.dp),
                        maxLines = 1
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
            ) {
                // String
                TextField(
                    value = formatString,
                    onValueChange = { saveFormatString(it, delimiter) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 8.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (formatString.length > 4) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.clear_24),
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clickable(
                                        indication = LocalIndication.current,
                                        interactionSource = null
                                    ) {
                                        saveFormatString("", delimiter)
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

                // Delimiter
                TextField (
                    value = delimiter,
                    onValueChange = { saveFormatString(formatString, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
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

        // Load/save presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(.2f))
            TextButton(
                onClick = { saveDialog = true },
                enabled = plaintextState.formatString.isNotBlank(),
                modifier = Modifier
                    .heightIn(40.dp, 40.dp),
                contentPadding = PaddingValues(8.dp, 2.dp),
            ) {
                Text(
                    text = "Save Preset",
                    modifier = Modifier
                )
            }
            Spacer(Modifier.weight(.2f))
            TextButton(
                onClick = { loadDialog = true },
                enabled = plaintextState.presets.any { it.formatString.isNotBlank() },
                modifier = Modifier
                    .heightIn(40.dp, 40.dp),
                contentPadding = PaddingValues(8.dp, 2.dp),
            ) {
                Text(
                    text = "Load Preset",
                    modifier = Modifier
                )
            }
            Spacer(Modifier.weight(.2f))
        }

        Spacer(Modifier.height(16.dp))

        // Preview
        Text(
            text = "Preview:",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .background(
                    LocalCustomColors.current.whiteBlack.copy(alpha = .2f),
                    RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = plaintextState.preview,
                modifier = Modifier,
                minLines = 6,
                maxLines = 6,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(30.dp))

        // Formatting Guide
        Text(
            text = "Formatting Guide",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Anything typed in the format string will show in the text. To reference " +
                    "specific fields, use the placeholders below. Sorting options are generated " +
                    "based on the format string placeholders (set format string before sorting). " +
                    "Using the delimiter field will automatically remove the delimiter from the " +
                    "last line.",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "Use the delimiter line for how to separate records in the generated string. " +
                    "Anything typed here will show up in-between each record. So, to separate " +
                    "each record by a blank line, you would need to enter \"_n__n_\". When tins " +
                    "are passed as a sublist, mark the start of the tins sublist delimiter with " +
                    "a tilde (~) at the end of the tins-sublist formatting, inside the closing " +
                    "tins as sublist bracket (e.g.: {@label~, } or {@label~_n_}.",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )

        // Formatting Options
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                val firstHalf = plaintextState.formatGuide.entries.take((plaintextState.formatGuide.size / 2.0).roundToInt())
                val secondHalf = plaintextState.formatGuide.entries.drop(firstHalf.size)
                val height: Dp = with(LocalDensity.current) { 24.sp.toDp() }

                // first half
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .padding(end = 8.dp),
                        ) {
                            firstHalf.forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "${it.key}:",
                                        modifier = Modifier,
                                        style = TextStyle(
                                            color = LocalContentColor.current,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 10.sp,
                                            maxFontSize = 14.sp,
                                            stepSize = 0.25.sp
                                        )
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier,
                        ) {
                            firstHalf.forEach {
                                Box(
                                    modifier = Modifier
                                        .height(height),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = it.value,
                                        modifier = Modifier,
                                        style = TextStyle(
                                            color = LocalContentColor.current,
                                        ),
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 10.sp,
                                            maxFontSize = 14.sp,
                                            stepSize = 0.25.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.width(36.dp))

                // second half
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .padding(end = 8.dp),
                        ) {
                            secondHalf.forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "${it.key}:",
                                        modifier = Modifier,
                                        style = TextStyle(
                                            color = LocalContentColor.current,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 10.sp,
                                            maxFontSize = 14.sp,
                                            stepSize = 0.25.sp
                                        )
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier,
                        ) {
                            secondHalf.forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = it.value,
                                        modifier = Modifier,
                                        style = TextStyle(
                                            color = LocalContentColor.current,
                                        ),
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 10.sp,
                                            maxFontSize = 14.sp,
                                            stepSize = 0.25.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "The \"@rating_0_0\" tag is to be used in a specific way. The first zero should " +
                    "be replaced with the max rating (for scaling) and the second with the number " +
                    "of decimal places to be rounded to (enter 0 to round to the nearest whole " +
                    "number). For example, to pass the rating on a scale of 1-4 with whole number" +
                    "rounding, enter \"@rating_4_0\" into the formatting. A more advanced example " +
                    "might be: \n \"[@rating_10_0 stars]\" or \"[@rating_4_2/4]\"",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "\"Number\" is a special tag that counts each record in the given sort order " +
                    "(use multiple # to include leading 0's).",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "In order to output raw text rather than special characters, escape the " +
                    "special character with the escape character. For example, to output # in the " +
                    "string, enter: '#. Likewise for example, to output brackets around a field, " +
                    "escape each bracket (e.g. '[@type']). The escape character itself doesn't " +
                    "need to be escaped unless you're trying to use it before an escapable " +
                    "character (e.g to render: '01' you would need to input ''##').",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "Use the square brackets ([ ]) when you only want the text within them " +
                    "to appear if one or more placeholders (also inside the brackets) are " +
                    "found. For instance, if you want the type shown on a new line, but " +
                    "don't want an extra line for a blank type, enter: [_n_@type]. These " +
                    "conditional brackets can also be nested.",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "When sorting by items, if you want the tins organized as a sublist " +
                    "per each item, use the curly braces around the formatting you want for " +
                    "tins (e.g. {@label (@T_qty)}). Conditional brackets can also be used " +
                    "inside the curly braces.",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "To set a delimiter for tins as a sublist, at the very end of the tin line " +
                    "formatting, still inside the tins as sublist brackets, place a tilde (~) " +
                    "just before the desired delimiter, followed by delimiter. For example, to " +
                    "separate each tin in the sublist by a new line, enter: {@label~_n_}.",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "A more advanced example might be to pass the list of tins only if tins exist " +
                    "for that blend and passing the quantity in brackets. For example, entering...",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Text(
            text = "@brand - \"@blend\"[_n_{    - @label '[@T_qty']~_n_}]",
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontSize = 14.sp,
        )
        Text(
            text = "... would result in:",
            modifier = Modifier
                .padding(bottom = 8.dp),
        )
        Box {
            Text(
                text = "Lane Limited - \"Very Cherry\"\n    - Lot 1 [2 oz]\n    - Lot 2 [50 grams]",
                modifier = Modifier,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(24.dp))

        if (saveDialog) {
            SaveDialog(
                savedPresets = plaintextState.presets,
                formatString = formatString,
                delimiter = delimiter,
                onSaveConfirm = { slot, formatString, delimiter ->
                    savePreset(slot, formatString, delimiter)
                },
                onDeleteConfirm = { savePreset(it, "", "") },
                onSaveCancel = { saveDialog = false },
            )
        }
        if (loadDialog) {
            LoadDialog(
                savedPresets = plaintextState.presets,
                onLoadConfirm = { string, delimiter ->
                    saveFormatString(string, delimiter)
                },
                onDeleteConfirm = { savePreset(it, "", "") },
                onLoadCancel = { loadDialog = false },
            )
        }
    }
}


/** Dialogs **/
@Composable
fun PrintDialog(
    savedFontSize: Float,
    savedMargin: Double,
    onPrintConfirm: (Float, Double) -> Unit,
    onPrintCancel: (Float, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var fontSize by rememberSaveable { mutableFloatStateOf(savedFontSize) }
    var margins by rememberSaveable { mutableDoubleStateOf(savedMargin) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val symbols = remember { DecimalFormatSymbols.getInstance(Locale.getDefault()) }
    val decimalSeparator = symbols.decimalSeparator.toString()

    AlertDialog(
        onDismissRequest = { onPrintCancel(fontSize, margins) },
        confirmButton = {
            TextButton(
                onClick = { onPrintConfirm(fontSize, margins) },
            ) {
                Text(text = "Print")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onPrintCancel(fontSize, margins) },
            ) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Print Settings") },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Font size is standard point-font size. Margin value is a multiplier " +
                            "of 1 inch with a range of 0-3 (including decimals, for example 0.5).",
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(Modifier.weight(.5f))
                    // Labels
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .fillMaxHeight()
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Font Size:")
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Margins:")
                        }
                    }

                    // Text fields
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        val fontPattern = remember { Regex("^(\\s*|\\d{0,2})$") }
                        val marginPattern = remember(decimalSeparator) {
                            val ds = Regex.escape(decimalSeparator)
                            Regex("^(\\s*|(\\d?)?($ds\\d{0,2})?)$")
                        }
                        var fontSizeString by rememberSaveable { mutableStateOf(fontSize.toInt().toString()) }
                        var marginsString by rememberSaveable { mutableStateOf(formatDecimal(margins)) }

                        // font
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTextField(
                                value = fontSizeString,
                                onValueChange = {
                                    if (it.matches(fontPattern)) {
                                        fontSizeString = it
                                        fontSize =
                                            if (it.isNotBlank()) {
                                                it.toFloatOrNull() ?: fontSize
                                            } else { 12f }
                                    }
                                },
                                suffix = {
                                    Text(
                                        "pt",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .padding(0.75.dp),
                                        color = LocalContentColor.current.copy(alpha = .8f)
                                    )
                                },
                                modifier = Modifier
                                    .width(68.dp)
                                    .padding(vertical = 4.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.End,
                                    color = LocalContentColor.current
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField.copy(
                                        alpha = 0.66f
                                    ),
                                    disabledTextColor = LocalContentColor.current.copy(alpha = 0.66f),
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                            )

                            // increase/decrease font buttons
                            IncreaseDecrease(
                                increaseClick = {
                                    if (fontSizeString.isEmpty()) {
                                        fontSize = 1f
                                        fontSizeString = "1"
                                    } else {
                                        if (fontSize < 99f) {
                                            fontSize += 1
                                            fontSizeString = fontSize.toInt().toString()
                                        } else {
                                            fontSize = 99f
                                            fontSizeString = "99"
                                        }
                                    }
                                },
                                decreaseClick = {
                                    if (fontSizeString.isEmpty()) {
                                        fontSize = 6f
                                        fontSizeString = "6"
                                    } else {
                                        if (fontSize > 1f) {
                                            fontSize -= 1
                                            fontSizeString = fontSize.toInt().toString()
                                        } else if (fontSize <= 1f) {
                                            fontSize = 1f
                                            fontSizeString = "1"
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight(),
                            )
                        }

                        // margins
                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTextField(
                                value = marginsString,
                                onValueChange = {
                                    if (it.matches(marginPattern)) {
                                        marginsString = it
                                        try {
                                            var parsedDouble: Double?

                                            if (it.isNotBlank()) {
                                                val preNumber =
                                                    if (it.startsWith(decimalSeparator)) {
                                                        "0$it"
                                                    } else it
                                                val number = numberFormat.parse(preNumber)

                                                parsedDouble = number?.toDouble() ?: 1.0
                                            } else {
                                                parsedDouble = 1.0
                                            }

                                            margins = if (parsedDouble <= 3.0) parsedDouble else 3.0
                                        } catch (e: ParseException) {
                                            Log.e("Print dialog", "Input: $it", e)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(68.dp)
                                    .padding(vertical = 4.dp)
                                    .onFocusChanged {
                                        if (!it.hasFocus) marginsString = formatDecimal(margins)
                                    },
                                singleLine = true,
                                suffix = {
                                    Text(
                                        text = "x",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .padding(start = 0.75.dp),
                                        color = LocalContentColor.current.copy(alpha = .8f)
                                    )
                                         },
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.End,
                                    color = LocalContentColor.current
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = LocalCustomColors.current.textField,
                                    unfocusedContainerColor = LocalCustomColors.current.textField,
                                    disabledContainerColor = LocalCustomColors.current.textField.copy(
                                        alpha = 0.66f
                                    ),
                                    disabledTextColor = LocalContentColor.current.copy(alpha = 0.66f),
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                            )

                            // increase/decrease margin buttons
                            IncreaseDecrease(
                                increaseClick = {
                                    if (marginsString.isEmpty()) {
                                        margins = 0.25
                                        marginsString = "0.25"
                                    } else {
                                        if (margins < 3.0) {
                                            margins += 0.25
                                            marginsString = formatDecimal(margins)
                                        } else {
                                            margins = 3.0
                                            marginsString = "3"
                                        }
                                    }
                                },
                                decreaseClick = {
                                    if (marginsString.isEmpty()) {
                                        margins = 0.0
                                        marginsString = formatDecimal(margins)
                                    } else {
                                        if (margins > 0.25) {
                                            margins -= 0.25
                                            marginsString = formatDecimal(margins)
                                        } else if (margins <= 0.0) {
                                            margins = 0.0
                                            marginsString = formatDecimal(margins)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxHeight(),
                            )
                        }
                    }
                    Spacer(Modifier.weight(.5f))
                }
            }
        },
    )
}


@Composable
fun SaveDialog(
    savedPresets: List<PlaintextPreset>,
    formatString: String,
    delimiter: String,
    onSaveConfirm: (Int, String, String) -> Unit,
    onDeleteConfirm: (Int) -> Unit,
    onSaveCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onSaveCancel() },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveConfirm(selectedSlot, formatString, delimiter)
                    onSaveCancel()
                },
                enabled = selectedSlot != -1
            ) { Text(text = "Save") }
        },
        dismissButton = {
            TextButton(
                onClick = { onSaveCancel() },
            ) { Text(text = "Cancel") }
        },
        title = { Text(text = "Save Preset") },
        modifier = modifier
            .fillMaxWidth(.9f)
            .clickable(
                indication = null,
                interactionSource = null
            ) { selectedSlot = -1 },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        shape = MaterialTheme.shapes.small,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                (0..4).forEach {
                    val preset = savedPresets[it]
                    val isSelected = selectedSlot == it
                    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = .07f).compositeOver(LocalCustomColors.current.darkNeutral)

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSelected) selectedColor else
                                    LocalCustomColors.current.darkNeutral,
                                RoundedCornerShape(4.dp)
                            )
                            .combinedClickable(
                                indication = null,
                                interactionSource = null,
                                onClick = { selectedSlot = if (isSelected) -1 else it },
                                onLongClick = {
                                    if (preset.formatString.isNotBlank()) {
                                        selectedSlot = it
                                        confirmDelete = true
                                    }
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Box (
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row (
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val color = if (isSelected) LocalContentColor.current.copy(alpha = .2f) else LocalContentColor.current
                                val color2 = if (isSelected) LocalContentColor.current.copy(alpha = .2f) else LocalContentColor.current.copy(alpha = .5f)
                                Text(
                                    text = "${it + 1}:",
                                    modifier = Modifier
                                        .width(IntrinsicSize.Max)
                                        .padding(end = 8.dp),
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                                Text(
                                    text = preset.formatString,
                                    modifier = Modifier
                                        .weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = color
                                )
                                if (preset.formatString.isNotBlank()) {
                                    VerticalDivider(
                                        modifier = Modifier
                                            .padding(horizontal = 6.dp)
                                            .fillMaxHeight(),
                                        color = color,
                                        thickness = 1.5.dp
                                    )
                                    Text(
                                        text = preset.delimiter.ifBlank{ "n/a" },
                                        modifier = Modifier
                                            .width(44.dp)
                                            .padding(start = 2.dp),
                                        color = if (preset.delimiter.isBlank()) color2 else color,
                                        maxLines = 1,
                                        fontStyle = if (preset.delimiter.isBlank()) FontStyle.Italic else FontStyle.Normal,
                                        textAlign = if (preset.delimiter.isBlank()) TextAlign.Center else TextAlign.Start,
                                    )
                                }
                            }
                            if (isSelected && preset.formatString.isNotBlank()) {
                                Text(
                                    text = "Overwrite?",
                                    modifier = Modifier
                                        .matchParentSize(),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        },
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirm(selectedSlot)
                        selectedSlot = -1
                        confirmDelete = false
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDelete = false },
                ) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = "Delete Preset") },
            text = { Text(text = "Are you sure you want to delete the preset in Slot ${selectedSlot + 1}?") },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground,
        )
    }
}


@Composable
fun LoadDialog(
    savedPresets: List<PlaintextPreset>,
    onLoadConfirm: (String, String) -> Unit,
    onLoadCancel: () -> Unit,
    onDeleteConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onLoadCancel() },
        confirmButton = {
            TextButton(
                onClick = {
                    onLoadConfirm(savedPresets[selectedSlot].formatString, savedPresets[selectedSlot].delimiter)
                    onLoadCancel()
                },
                enabled = selectedSlot != -1
            ) { Text(text = "Load") }
        },
        dismissButton = {
            TextButton(
                onClick = { onLoadCancel() },
            ) { Text(text = "Cancel") }
        },
        title = { Text(text = "Load Preset") },
        modifier = modifier
            .fillMaxWidth(.9f)
            .clickable(
                indication = null,
                interactionSource = null
            ) { selectedSlot = -1 },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                (0..4).forEach {
                    val preset = savedPresets[it]
                    val isSelected = selectedSlot == it
                    val disabled = preset.formatString.isBlank()
                    val selectedColor = MaterialTheme.colorScheme.primary.copy(alpha = .07f).compositeOver(LocalCustomColors.current.darkNeutral)

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                width = Dp.Hairline,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSelected) selectedColor
                                else if (disabled) LocalCustomColors.current.darkNeutral.copy(alpha = .38f)
                                else LocalCustomColors.current.darkNeutral,
                                RoundedCornerShape(4.dp)
                            )
                            .combinedClickable(
                                enabled = preset.formatString.isNotBlank(),
                                indication = null,
                                interactionSource = null,
                                onClick = { selectedSlot = if (isSelected) -1 else it },
                                onLongClick = {
                                    selectedSlot = it
                                    confirmDelete = true
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "${it + 1}:",
                            modifier = Modifier
                                .padding(end = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (disabled) LocalContentColor.current.copy(alpha = .38f) else LocalContentColor.current
                        )
                        Text(
                            text = preset.formatString,
                            modifier = Modifier
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Normal,
                            color = LocalContentColor.current
                        )
                        if (preset.formatString.isNotBlank()) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .fillMaxHeight(),
                                color = LocalContentColor.current,
                                thickness = 1.5.dp
                            )
                            Text(
                                text = preset.delimiter.ifBlank{ "n/a" },
                                modifier = Modifier
                                    .width(44.dp)
                                    .padding(start = 2.dp),
                                color = if (preset.delimiter.isBlank()) LocalContentColor.current.copy(alpha = .5f) else LocalContentColor.current,
                                maxLines = 1,
                                fontStyle = if (preset.delimiter.isBlank()) FontStyle.Italic else FontStyle.Normal,
                                textAlign = if (preset.delimiter.isBlank()) TextAlign.Center else TextAlign.Start,
                            )
                        }
                    }
                }
            }
        },
    )
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirm(selectedSlot)
                        selectedSlot = -1
                        confirmDelete = false
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDelete = false },
                ) {
                    Text(text = "Cancel")
                }
            },
            title = { Text(text = "Delete Preset") },
            text = { Text(text = "Are you sure you want to delete the preset in Slot ${selectedSlot + 1}?") },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            textContentColor = MaterialTheme.colorScheme.onBackground,
        )
    }
}