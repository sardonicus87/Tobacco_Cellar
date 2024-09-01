package com.example.tobaccocellar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.example.tobaccocellar.data.LocalCellarApplication
import com.example.tobaccocellar.ui.theme.TobaccoCellarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = (application as CellarApplication)
            CompositionLocalProvider(LocalCellarApplication provides application) {
                TobaccoCellarTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CellarApp()
                    }
                }
            }
        }
    }
}