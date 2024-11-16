package com.example.tobaccocellar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Window
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.theme.TobaccoCellarTheme

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
            WindowInsets.safeDrawing
            CompositionLocalProvider(LocalCellarApplication provides application) {
                TobaccoCellarTheme(preferencesRepo = application.preferencesRepo) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        CellarApp()
                    }
                }
            }
        }
    }
}
