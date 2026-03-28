package com.sardonicus.tobaccocellar.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToChangelog: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = viewModel::saveReleaseNotesSeen,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        modifier = modifier,
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
                    .fillMaxHeight(.45f)
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
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Text("OK")
            }
        }
    )

}
