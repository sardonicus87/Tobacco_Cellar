package com.sardonicus.tobaccocellar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import com.sardonicus.tobaccocellar.ui.theme.TobaccoCellarTheme
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.MainActivityReady
import com.sardonicus.tobaccocellar.ui.utilities.TransitionCache
import com.sardonicus.tobaccocellar.ui.utilities.TransitionCoverReady
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class TransitionActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.argb(0xFF, 0x13, 0x14, 0x13)),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.argb(0xFF, 0x13, 0x14, 0x13))
        )
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        val bitmap = TransitionCache.loadScreenshot(this)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightNavigationBars = false
        windowInsetsController.isAppearanceLightStatusBars = false

        setContent {
            val displayCutoutTop = WindowInsets.displayCutout.getTop(LocalDensity.current)
            SideEffect(displayCutoutTop) { updateStatusBarsVisibility(displayCutoutTop, windowInsetsController) }

            LaunchedEffect(Unit) {
                withTimeoutOrNull(1500.milliseconds) {
                    EventBus.events.collectLatest {
                        if (it is MainActivityReady) {
                            delay(25.milliseconds)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, android.R.anim.fade_out)
                                finish()
                            } else {
                                finish()
                                @Suppress("DEPRECATION")
                                overridePendingTransition(0, android.R.anim.fade_out)
                            }
                        }
                    }
                }
            }

            val prefRepo = (application as CellarApplication).preferencesRepo
            TobaccoCellarTheme(prefRepo) {
                Box(Modifier.fillMaxSize()) {
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize())
                        LaunchedEffect(bitmap) { EventBus.emit(TransitionCoverReady) }
                    }

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(250))
                    ) {
                        Box(Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        }
    }
}