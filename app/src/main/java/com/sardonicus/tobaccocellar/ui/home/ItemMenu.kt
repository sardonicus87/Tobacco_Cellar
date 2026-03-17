package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun ItemMenu(
    onEditClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(LocalCustomColors.current.listMenuScrim),
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                onEditClick()
                onMenuDismiss()
            },
            modifier = Modifier,
        ) {
            Text(
                text = "Edit Item",
                modifier = Modifier,
                color = LocalContentColor.current,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}