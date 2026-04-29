package com.sardonicus.tobaccocellar.ui.changelog

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sardonicus.tobaccocellar.BuildConfig
import com.sardonicus.tobaccocellar.CellarTopAppBar
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.TobaccoDatabase
import com.sardonicus.tobaccocellar.ui.theme.LocalCustomColors
import com.sardonicus.tobaccocellar.ui.theme.primaryContainerLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateUp: () -> Unit,
    navigateToChangelog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var selectionFocused by rememberSaveable { mutableStateOf(false) }
    var selectionKey by rememberSaveable { mutableIntStateOf(0) }
    val resetSelection: () -> Unit = {
        selectionKey += 1
        selectionFocused = false
    }

    BackHandler(selectionFocused) {
        if (selectionFocused) {
            resetSelection()
        }
    }

    val context = LocalContext.current

    val appVersion = BuildConfig.VERSION_NAME
    val dbVersion = TobaccoDatabase.getDatabaseVersion(LocalContext.current).toString()
    val versionInfo = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium // SemiBold
            )
        ) { append("App Version: ") }
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium
            )
        ) { append(appVersion) }
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
        ) { append("\nDatabase Version: ") }
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium
            )
        ) { append(dbVersion) }
    }


    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(indication = null, interactionSource = null) { resetSelection() },
        topBar = {
            CellarTopAppBar(
                title = "About",
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp,
                canNavigateBack = true,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = modifier
                    .widthIn(max = 592.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.Top)
            ) {

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
                ) {
                    Text(
                        text = "Tobacco Cellar",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Image(
                        painter = painterResource(R.drawable.tc_logo_crop),
                        contentDescription = "Tobacco Cellar Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .background(primaryContainerLight, CircleShape)
                            .border(3.dp, if (LocalCustomColors.current.isLightTheme) Color(0xFF323232) else Color.Transparent, CircleShape)
                            .padding(16.dp)
                            .size(80.dp)
                    )
                }

                // About
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
                ) {
                    Text(
                        text = "Cobbled together by Sardonicus using Kotlin and Jetpack Compose. " +
                                "Uses Apache Commons CSV for reading and writing CSV files.",
                        modifier = Modifier,
                        fontSize = 15.sp,
                        lineHeight = 1.5.em,
                        softWrap = true,
                    )
                    FlowRow(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(
                            text = "Contact me if you experience any bugs: ",
                            modifier = Modifier,
                            fontSize = 15.sp,
                            lineHeight = 1.5.em,
                            softWrap = true,
                        )
                        key(selectionKey) {
                            SelectionContainer(
                                modifier = Modifier
                                    .onFocusChanged { selectionFocused = it.isFocused }
                            ) {

                                Text(
                                    text = "sardonicus.notadev@gmail.com",
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = null,
                                            indication = LocalIndication.current
                                        ) {
                                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = "mailto:sardonicus.notadev@gmail.com".toUri()
                                                putExtra(Intent.EXTRA_SUBJECT, "Tobacco Cellar Feedback")
                                            }

                                            context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                                        },
                                    fontSize = 14.sp,
                                    softWrap = true,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Normal,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }

                Text(
                    text = versionInfo,
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 1.5.em,
                    softWrap = true,
                )

                // Changelog and links
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            30.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Changelog",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = null
                                ) { navigateToChangelog() }
                                .padding(10.dp, 6.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            lineHeight = 1.em,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ExternalLink("Website", "http://www.tobacco-cellar.com")
                        ExternalLink("Play Store", "https://play.google.com/store/apps/details?id=com.sardonicus.tobaccocellar")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExternalLink("Privacy Policy", "http://www.tobacco-cellar.com/privacy-policy")
                        ExternalLink("Managing Data", "http://www.tobacco-cellar.com/managing-data")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExternalLink(
    text: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current
    val externalLink: (String) -> AnnotatedString = {
        buildAnnotatedString {
            append("$it↗")
            addStyle(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = 0,
                end = length - 1
            )
        }
    }
    Text(
        text = externalLink(text),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(
                indication = LocalIndication.current,
                interactionSource = null
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                uriHandler.openUri(url)
            }
            .padding(vertical = 6.dp)
            .padding(start = 10.dp, end = 6.dp),
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 1.em,
        color = MaterialTheme.colorScheme.primary
    )
}

