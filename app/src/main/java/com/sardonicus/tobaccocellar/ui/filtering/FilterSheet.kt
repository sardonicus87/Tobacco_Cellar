package com.sardonicus.tobaccocellar.ui.filtering

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.sardonicus.tobaccocellar.ui.BottomSheetState
import com.sardonicus.tobaccocellar.ui.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier
) {
    val bottomSheetState by filterViewModel.bottomSheetState.collectAsState()
    val pagerState = rememberPagerState { 3 }

    if (bottomSheetState == BottomSheetState.OPENED) {
        ModalBottomSheet(
            onDismissRequest = { filterViewModel.closeBottomSheet() },
            modifier = modifier
                .statusBarsPadding(),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            dragHandle = { },
            properties = ModalBottomSheetProperties(shouldDismissOnBackPress = true),
        ) {
            val view = LocalView.current
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                SideEffect {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
                }
            }

            Box {
                val density = LocalDensity.current
                val navigation = WindowInsets.navigationBars.getBottom(density).times(1f)
                FilterLayout(
                    filterViewModel = filterViewModel,
                    closeSheet = filterViewModel::closeBottomSheet,
                    pagerState = pagerState,
                    modifier = Modifier
                )

                Canvas(Modifier.matchParentSize()) {
                    drawRect(
                        color = Color.Black.copy(alpha = .9f),
                        topLeft = Offset(0f, (size.height)),
                        size = Size(size.width, navigation)
                    )
                }
            }
        }
    }
}