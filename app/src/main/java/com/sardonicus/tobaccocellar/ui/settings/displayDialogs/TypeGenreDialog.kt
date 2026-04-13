package com.sardonicus.tobaccocellar.ui.settings.displayDialogs

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.settings.TypeGenreOption

@Composable
fun TypeGenreDialog(
    onDismiss: () -> Unit,
    typeGenreOption: TypeGenreOption,
    optionEnablement: Map<TypeGenreOption, Boolean>,
    onTypeGenreOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column(
                modifier = modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "This option sets the display of Type, Subgenre or both on the Cellar " +
                            "screen. Fallback options display the option and if it's unused on an " +
                            "entry, fallback to the other (e.g. Subgenre (fallback) would display " +
                            "the subgenre but if an entry has no subgenre, will instead show the " +
                            "type value in parenthesis). This also affects table view when only " +
                            "one or the other of Type or Subgenre columns are shown.",
                    modifier = Modifier
                        .padding(bottom = 12.dp),
                    fontSize = 15.sp,
                    color = LocalContentColor.current
                )
                TypeGenreOption.entries.forEach {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication = LocalIndication.current,
                                interactionSource = null,
                                enabled = optionEnablement[it] ?: false
                            ) { onTypeGenreOption(it.value) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
                    ) {
                        val alpha = if (optionEnablement[it] == true) 1f else .38f
                        RadioButton(
                            selected = typeGenreOption == it,
                            onClick = null,
                            enabled = optionEnablement[it] ?: false,
                            modifier = Modifier
                                .size(36.dp)
                        )
                        Text(
                            text = it.value,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .alpha(alpha),
                            fontSize = 15.sp,

                            )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        shape = MaterialTheme.shapes.large
    )
}