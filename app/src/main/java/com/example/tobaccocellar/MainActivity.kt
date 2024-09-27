package com.example.tobaccocellar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.theme.TobaccoCellarTheme

class MainActivity : ComponentActivity() {

    private var backPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

//        enableEdgeToEdge()
//        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            val application = (application as CellarApplication)
         //   WindowInsets.safeDrawing
            CompositionLocalProvider(LocalCellarApplication provides application) {
                TobaccoCellarTheme(preferencesRepo = application.preferencesRepo) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    //    tonalElevation = 1.dp

                    ) {
                        CellarApp()
                    }
                }
            }
        }
    }
}
