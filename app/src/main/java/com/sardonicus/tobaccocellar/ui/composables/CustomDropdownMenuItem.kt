package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.LanguageState

@Composable
fun CustomDropdownItem(
    text: @Composable (() -> Unit),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null
) {
    CustomMenuItemContent(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )
}

@Composable
private fun CustomMenuItemContent(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    enabled: Boolean,
    colors: MenuItemColors,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource?
) {
    Row(
        modifier =
            modifier
                .clickable(interactionSource, ripple(true), enabled) { onClick() }
                .fillMaxWidth()
                .sizeIn(minWidth = 100.dp, maxWidth = 280.dp, minHeight = 48.dp)
                .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProvideTextStyle(LocalTextStyle.current.copy(
            color = colors.textColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            lineHeight = 1.15.em,
            letterSpacing = .1.sp,
            textDirection = if (LanguageState.isRtl) TextDirection.Rtl else TextDirection.Ltr
        )) {
            if (leadingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides colors.leadingIconColor) {
                    Box(Modifier.defaultMinSize(minWidth = 24.dp)) { leadingIcon() }
                }
            }
            CompositionLocalProvider(LocalContentColor provides colors.textColor) {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(
                            start = if (leadingIcon != null) { 12.dp } else { 0.dp },
                            end = if (trailingIcon != null) { 12.dp } else { 0.dp },
                        )
                ) { text() }
            }
            if (trailingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides colors.trailingIconColor) {
                    Box(Modifier.defaultMinSize(minWidth = 24.dp)) { trailingIcon() }
                }
            }
        }
    }
}