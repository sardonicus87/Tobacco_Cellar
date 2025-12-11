package com.sardonicus.tobaccocellar

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.theme.TobaccoCellarTheme

class MainActivity : ComponentActivity() {

    private var backPressedOnce = false
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.BLACK,
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.BLACK,
            )
        )
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    finish()
                    return
                }

                backPressedOnce = true
                Toast.makeText(this@MainActivity, "Tap again to exit", Toast.LENGTH_SHORT)
                    .show()

                Handler(Looper.getMainLooper()).postDelayed({ backPressedOnce = false }, 2000)
            }
        })

        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        updateSystemBarsForOrientation(resources.configuration.orientation)


        setContent {
            val application = (application as CellarApplication)
            val gestureNavigation = gestureNavigation()
            val isGestureNav = remember(gestureNavigation) { gestureNavigation }

            CompositionLocalProvider(LocalCellarApplication provides application) {
                TobaccoCellarTheme(preferencesRepo = application.preferencesRepo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .windowInsetsPadding(WindowInsets.displayCutout)
                    ) {
                        CellarApp(
                            isGestureNav = isGestureNav,
                        )
                    }
                    SystemBarsProtection()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateSystemBarsForOrientation(newConfig.orientation)
    }

    private fun updateSystemBarsForOrientation(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}


@Composable
private fun SystemBarsProtection(
    statusBarHeight: () -> Float = calculateStatusBar(),
    navigationHeight: () -> Float = calculateNavigation()
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val color = if (darkTheme) Color.Black else Color.Black

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val status = statusBarHeight()
        val navigation = navigationHeight()

        drawRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, status)
        )
        drawRect(
            color = color,
            topLeft = Offset(0f, (size.height - navigation)),
            size = Size(size.width, navigation)
        )
    }
}

@Composable
fun calculateStatusBar(): () -> Float {
    val statusBars = WindowInsets.statusBars
    val density = LocalDensity.current
    return { statusBars.getTop(density).times(1f) }
}

@Composable
fun calculateNavigation(): () -> Float {
    val navigation = WindowInsets.navigationBars
    val density = LocalDensity.current
    return { navigation.getBottom(density).times(1f) }
}

@Composable
fun gestureNavigation(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return false
    } else {
        val insets = WindowInsets.systemGestures
        val density = LocalDensity.current
        val direction = LocalLayoutDirection.current
        val left = insets.getLeft(density, direction)
        val right = insets.getRight(density, direction)
        return left > 0 && right > 0
    }
}