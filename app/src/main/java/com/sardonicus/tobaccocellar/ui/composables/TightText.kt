package com.sardonicus.tobaccocellar.ui.composables

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun TightText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val density = LocalDensity.current
    var finalWidth by remember { mutableStateOf(0.dp) }
    val width = if (finalWidth > 0.dp) Modifier.width(finalWidth) else Modifier

    Text(
        text = text,
        modifier = modifier.then(width),
        color = color,
        autoSize = autoSize,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = style,
        onTextLayout = {
            onTextLayout?.invoke(it)
            if (it.lineCount > 1) {
                var maxLine = 0f
                repeat(it.lineCount) { index ->
                    maxLine = max(maxLine, it.getLineRight(index) - it.getLineLeft(index))
                }
                val calculated = if (maxLine > 0f) with(density) { maxLine.toDp() } else 0.dp
                if (calculated > 0.dp && calculated != finalWidth) { finalWidth = calculated }
            }
        }
    )
}