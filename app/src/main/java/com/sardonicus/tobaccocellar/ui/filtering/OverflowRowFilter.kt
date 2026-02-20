package com.sardonicus.tobaccocellar.ui.filtering

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FlowMatchOption
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.composables.OverflowRow
import com.sardonicus.tobaccocellar.ui.filtering.sections.Chip

@Composable
fun OverflowFilterSection(
    label: () -> String,
    nothingLabel: () -> String,
    available: () -> List<String>,
    selected: () -> List<String>,
    enabled: () -> Map<String, Boolean>,
    updateSelectedOptions: (String, Boolean) -> Unit,
    overflowCheck: (List<String>, List<String>, Int) -> Boolean,
    nothingAssigned: () -> Boolean,
    modifier: Modifier = Modifier,
    matching: () -> FlowMatchOption? = { null },
    matchOptionEnablement: () -> Map<FlowMatchOption, Boolean> = { mapOf() },
    clearAll: () -> Unit = {},
    onMatchOptionChange: (FlowMatchOption) -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
    ) {
        var showOverflowPopup by remember { mutableStateOf(false) }

        // Header and Match options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier,
                color = if (nothingAssigned()) LocalContentColor.current.copy(alpha = 0.4f) else LocalContentColor.current
            )
            if (matching() != null) {
                FlowFilterMatchOptions(
                    nothingAssigned, matching, matchOptionEnablement, { onMatchOptionChange(it) },
                    Modifier, Arrangement.End
                )
            }
        }

        if (nothingAssigned()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nothingLabel(),
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        } else {
            OverflowWrapper(
                available = available(),
                selected = selected(),
                enabled = enabled(),
                updateSelectedOptions = updateSelectedOptions,
                overflowCheck = overflowCheck,
                showOverflowPopup = { showOverflowPopup = it }
            )

            if (showOverflowPopup) {
                FlowFilterOverflowPopup(
                    onDismiss = { showOverflowPopup = false },
                    label = label,
                    available = available,
                    selected = selected,
                    enabled = enabled,
                    matching = matching,
                    matchOptionEnablement = matchOptionEnablement,
                    enableMatchOption = { matching() != null },
                    onMatchOptionChange = onMatchOptionChange,
                    updateSelectedOptions = updateSelectedOptions,
                    clearAll = clearAll,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun OverflowWrapper(
    available: List<String>,
    selected: List<String>,
    enabled: Map<String, Boolean>,
    updateSelectedOptions: (String, Boolean) -> Unit,
    overflowCheck: (List<String>, List<String>, Int) -> Boolean,
    showOverflowPopup: (Boolean) -> Unit,
) {
    OverflowRow(
        items = available,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Top)
            .padding(horizontal = 4.dp),
        itemSpacing = 6.dp,
        itemContent = {
            val isSelected = selected.contains(it)

            FilterChipWrapper(
                label = { it },
                selected = { isSelected },
                enabled = { enabled[it] ?: false },
                onClick = { updateSelectedOptions(it, !isSelected) },
                modifier = Modifier
                    .widthIn(max = 140.dp)
            )
        },
        enabledAtIndex = { enabled[available[it]] ?: true },
        overflowIndicator = { overflowCount, enabledCount, overflowEnabled -> // overflowEnabled means count > 0
            val overflowedSelected = overflowCheck(selected, available, available.size - overflowCount)

            val labelColor =
                if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
//                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.onSecondaryContainer
//                        else if (overflowedSelected) MaterialTheme.colorScheme.onSecondaryContainer
//                        else if (!overflowEnabled) MaterialTheme.colorScheme.onSurfaceVariant
//                        else MaterialTheme.colorScheme.onSurfaceVariant
            val containerColor =
                if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.background
//                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
//                        else if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer
//                        else if (!overflowEnabled) MaterialTheme.colorScheme.background
//                        else MaterialTheme.colorScheme.background
            val borderColor =
                if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.outlineVariant
//                        if (overflowedSelected && overflowEnabled) MaterialTheme.colorScheme.secondaryContainer
//                        else if (overflowedSelected) MaterialTheme.colorScheme.secondaryContainer
//                        else if (!overflowEnabled) MaterialTheme.colorScheme.outlineVariant
//                        else MaterialTheme.colorScheme.outlineVariant


            Chip(
                text = "+$enabledCount",  // "+$overflowCount"
                onChipClicked = { showOverflowPopup(true) },
                onChipRemoved = { },
                enabled = true,
                trailingIcon = false,
                modifier = Modifier,
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = labelColor,
                    containerColor = containerColor
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = borderColor
                ),
            )
        },
    )
}

@Composable
private fun FilterChipWrapper(
    label: () -> String,
    selected: () -> Boolean,
    enabled: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected(),
        onClick = onClick,
        label = {
            Text(
                text = label(),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier
            .padding(0.dp),
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        enabled = enabled()
    )
}

@Composable
private fun FlowFilterMatchOptions(
    nothingAssigned: () -> Boolean,
    matching: () -> FlowMatchOption?,
    matchOptionEnablement: () -> Map<FlowMatchOption, Boolean>,
    onMatchOptionChange: (FlowMatchOption) -> Unit,
    modifier: Modifier = Modifier,
    arrangement: Arrangement.Horizontal = Arrangement.Center,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Match: ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier,
            color = if (nothingAssigned()) LocalContentColor.current.copy(alpha = 0.4f) else LocalContentColor.current
        )

        FlowMatchOption.entries.forEachIndexed { index, it ->
            val enabled = !nothingAssigned() && (matchOptionEnablement()[it] ?: false)
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable(
                        enabled = enabled,
                        indication = LocalIndication.current,
                        interactionSource = null
                    ) { onMatchOptionChange(it) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it.value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Transparent,
                )
                Text(
                    text = it.value,
                    fontSize = 14.sp,
                    fontWeight = if (matching() == it && !nothingAssigned()) FontWeight.Medium else FontWeight.Normal,
                    color = if (matching() == it && !nothingAssigned()) MaterialTheme.colorScheme.primary else if (enabled) LocalContentColor.current.copy(alpha = .6f) else LocalContentColor.current.copy(alpha = .38f),
                    modifier = Modifier
                )
            }
            if (index != FlowMatchOption.entries.lastIndex) {
                Text(
                    text = " / ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier,
                    color = if (nothingAssigned()) LocalContentColor.current.copy(alpha = 0.6f) else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
private fun FlowFilterOverflowPopup(
    onDismiss: () -> Unit,
    label: () -> String,
    available: () -> List<String>,
    selected: () -> List<String>,
    enabled: () -> Map<String, Boolean>,
    enableMatchOption: () -> Boolean,
    matching: () -> FlowMatchOption?,
    matchOptionEnablement: () -> Map<FlowMatchOption, Boolean>,
    onMatchOptionChange: (FlowMatchOption) -> Unit,
    updateSelectedOptions: (String, Boolean) -> Unit,
    clearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = label(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier
            .fillMaxWidth(.9f),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        ),
        shape = MaterialTheme.shapes.medium,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)
            ) {
                GlowBox(
                    color = GlowColor(MaterialTheme.colorScheme.background),
                    size = GlowSize(vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 280.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        // match options
                        if (enableMatchOption()) {
                            FlowFilterMatchOptions(
                                { false }, matching, matchOptionEnablement, { onMatchOptionChange(it) },
                                Modifier.padding(bottom = 4.dp), Arrangement.Center
                            )
                        }

                        // Chips
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.spacedBy(
                                4.dp,
                                Alignment.Start
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                0.dp,
                                Alignment.Top
                            )
                        ) {
                            available().forEach {
                                FilterChip(
                                    selected = selected().contains(it),
                                    onClick = {
                                        updateSelectedOptions(it, !selected().contains(it))
                                    },
                                    label = { Text(text = it, fontSize = 14.sp) },
                                    modifier = Modifier
                                        .padding(0.dp),
                                    shape = MaterialTheme.shapes.small,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    ),
                                    enabled = enabled()[it] ?: false
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            clearAll()
                        },
                        modifier = Modifier
                            .offset(x = (-4).dp),
                        enabled = selected().isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = 3.dp)
                                .size(20.dp)
                        )
                        Text(
                            text = "Clear All",
                            modifier = Modifier,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
    )
}