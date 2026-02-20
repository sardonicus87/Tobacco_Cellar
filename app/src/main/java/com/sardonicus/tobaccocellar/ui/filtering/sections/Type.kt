package com.sardonicus.tobaccocellar.ui.filtering.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.FilterViewModel

@Composable
fun TypeFilterSection(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
    types: List<String> = listOf("Aromatic", "English", "Burley", "Virginia", "Other", "(Unassigned)"),
) {
    val selectedTypes by filterViewModel.sheetSelectedTypes.collectAsState()
    val enabled by filterViewModel.typesEnabled.collectAsState()
    val typesExist by filterViewModel.typesExist.collectAsState()

    val onClick = remember {
        { type: String ->
            filterViewModel.updateSelectedTypes(type, !selectedTypes.contains(type))
        }
    }

    Column(
        modifier = modifier
    ) {
        if (!typesExist) {
            Row(
                modifier = Modifier
                    .border(
                        Dp.Hairline,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .heightIn(min = 48.dp, max = 96.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No types assigned to any blends.",
                    modifier = Modifier
                        .padding(0.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                types.forEach {
                    val selected by remember(types) { derivedStateOf { selectedTypes.contains(it) } } // selectedTypes
                    val typeEnabled by remember(types) { derivedStateOf { enabled[it] ?: false } }

                    FilterChip(
                        selected = selected,
                        onClick = { onClick(it) },
                        label = { Text(it, fontSize = 14.sp) },
                        modifier = Modifier
                            .padding(0.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        enabled = typeEnabled
                    )
                }
            }
        }
    }
}