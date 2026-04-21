package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

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
    val onExpandChange = remember { { it: Boolean -> expanded = it } }

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