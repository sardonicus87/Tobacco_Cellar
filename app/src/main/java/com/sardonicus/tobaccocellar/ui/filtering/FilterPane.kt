package com.sardonicus.tobaccocellar.ui.filtering

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.FilterViewModel

@Composable
fun FilterPane(
    filterViewModel: FilterViewModel,
    twoPane: Boolean,
    modifier: Modifier = Modifier
) {
    val searchPerformed by filterViewModel.searchPerformed.collectAsState()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BoxWithConstraints {
                FilterLayout(filterViewModel, twoPane,
                    Modifier.alpha(if (searchPerformed) 0.38f else 1f), paginateLayout = false,
                    searchPerformed = searchPerformed)

                val width = this.maxWidth * .2f
                if (searchPerformed) {
                    Column(Modifier.matchParentSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.filter_search_disabled),
                            contentDescription = "disabled",
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                            alpha = .75f,
                            modifier = Modifier
                                .size(width)
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.filter_search),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LocalContentColor.current.copy(alpha = .75f),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background.copy(alpha = .95f),
                                    RoundedCornerShape(25))
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}