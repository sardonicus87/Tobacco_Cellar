package com.sardonicus.tobaccocellar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.ui.theme.TobaccoCellarTheme

class MainActivity : ComponentActivity() {

    private var backPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
    //    enableEdgeToEdge()
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

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            val application = (application as CellarApplication)
          //  WindowInsets.safeContent
            CompositionLocalProvider(LocalCellarApplication provides application) {
                TobaccoCellarTheme(preferencesRepo = application.preferencesRepo) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
//                            .windowInsetsPadding(WindowInsets.systemBars)
//                            .windowInsetsPadding(WindowInsets.displayCutout)
                            .windowInsetsPadding(WindowInsets.safeContent),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        CellarApp()
                    }
                }
            }
        }
    }
}
