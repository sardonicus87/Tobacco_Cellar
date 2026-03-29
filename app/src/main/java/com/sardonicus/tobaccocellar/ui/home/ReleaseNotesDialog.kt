package com.sardonicus.tobaccocellar.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sardonicus.tobaccocellar.ui.composables.GlowBox
import com.sardonicus.tobaccocellar.ui.composables.GlowColor
import com.sardonicus.tobaccocellar.ui.composables.GlowSize
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors

@Composable
fun ReleaseNotesDialog(
    releaseNotesState: ReleaseNotesState,
    viewModel: HomeViewModel,
    tempHide: Boolean,
    onNavigateToChangelog: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val divisor = if (landscape) .9f else .5f
    val maxHeight = LocalWindowInfo.current.containerDpSize.height * divisor
    val minWidth = if (landscape) 280.dp else Dp.Unspecified
    val maxWidth = if (landscape) LocalWindowInfo.current.containerDpSize.width * .5f else Dp.Unspecified

    AlertDialog(
        onDismissRequest = if (!tempHide) viewModel::saveReleaseNotesSeen else { { } },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = !landscape
        ),
        modifier = modifier
            .heightIn(max = maxHeight)
            .widthIn(minWidth, maxWidth),
        containerColor = LocalCustomColors.current.darkNeutral,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        title = {
            Column {
                Text(
                    text = "What's New",
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.tertiary)
            }
        },
        text = {
            GlowBox(
                color = GlowColor(LocalCustomColors.current.darkNeutral),
                size = GlowSize(vertical = 6.dp),
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(4.dp))
                    releaseNotesState.changelogData.forEachIndexed { index, log ->
                        val alpha = if (index == 0) 1f else 0.8f
                        Text(
                            text = "Version ${log.versionNumber} (${log.buildDate})",
                            modifier = Modifier
                                .padding(bottom = 6.dp),
                            fontSize = if (index == 0) 16.sp else 15.sp,
                            fontWeight = if (index == 0) FontWeight.ExtraBold else FontWeight.Medium,
                            color = LocalContentColor.current.copy(alpha = alpha),
                        )
                        log.releaseNotes.forEach {
                            val text = "- $it"
                            val linkText = "full changelog"
                            val annotatedString = buildAnnotatedString {
                                append(text)
                                val startIndex = text.indexOf(linkText)
                                if (startIndex != -1) {
                                    addLink(
                                        clickable = LinkAnnotation.Clickable(
                                            tag = "changelog",
                                            styles = TextLinkStyles(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                                )
                                            ),
                                            linkInteractionListener = { onNavigateToChangelog(log.versionCode) }
                                        ),
                                        start = startIndex,
                                        end = startIndex + linkText.length
                                    )
                                }
                            }

                            Text(
                                text = annotatedString,
                                fontSize = if (index == 0) 15.sp else 14.sp,
                                color = LocalContentColor.current.copy(alpha = alpha),
                                modifier = Modifier
                                    .padding(start = 12.dp)
                            )
                        }
                        if (index != releaseNotesState.changelogData.lastIndex) {
                            HorizontalDivider(Modifier.padding(vertical = 20.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = viewModel::saveReleaseNotesSeen,
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.saveReleaseNotesSeen()
                    onNavigateToChangelog(null)
                },
                contentPadding = PaddingValues(12.dp, 4.dp),
                modifier = Modifier
                    .heightIn(32.dp, 32.dp)
            ) {
                Text("Full Changelog")
            }
        }
    )

}
