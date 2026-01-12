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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.DriveScopes
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.settings.SignInEvent
import com.sardonicus.tobaccocellar.ui.settings.SignOutEvent
import com.sardonicus.tobaccocellar.ui.theme.TobaccoCellarTheme
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var backPressedOnce = false
    private lateinit var windowInsetsController: WindowInsetsControllerCompat
    private lateinit var credentialManager: CredentialManager
    private lateinit var authorizationClient: AuthorizationClient
    private lateinit var authorizationLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var preferencesRepo: PreferencesRepo

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

        preferencesRepo = (application as CellarApplication).preferencesRepo
        credentialManager = CredentialManager.create(this)
        authorizationClient = Identity.getAuthorizationClient(this)
        authorizationLauncher = registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val userEmail = preferencesRepo.signedInUserEmail.first()
                    if (userEmail != null) {
                        preferencesRepo.saveLoginState(userEmail, true)
                        preferencesRepo.saveCrossDeviceSync(true)
                        Toast.makeText(this@MainActivity, "Sync successfully enabled.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Drive permission was denied", Toast.LENGTH_SHORT).show()
            }
        }

        // sign in launch
        lifecycleScope.launch {
            EventBus.events.collect { event ->
                if (event is SignInEvent) {
                    val userEmail = preferencesRepo.signedInUserEmail.first()
                    val hasScope = preferencesRepo.hasDriveScope.first()

                    when {
                        userEmail != null && hasScope -> {
                            lifecycleScope.launch {
                                preferencesRepo.saveCrossDeviceSync(true)
                            }
                        }
                        userEmail != null && !hasScope -> {
                            authorizeDrive()
                        }
                        else -> {
                           signIn()
                        }
                    }
                }
                if (event is SignOutEvent) {
                    signOut()
                }
            }
        }

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

    private fun signIn() {
        lifecycleScope.launch {
            val googleIdOption = GetGoogleIdOption.Builder()  // GetSignInWithGoogleOption.Builder(getString(R.string.web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val userEmail = credential.id

                preferencesRepo.saveLoginState(userEmail, false)

                authorizeDrive()

            } catch (_: NoCredentialException) {
                Toast.makeText(this@MainActivity, "No Google accounts found on this device", Toast.LENGTH_SHORT).show()
            } catch (_: GetCredentialCancellationException) {
            //    Toast.makeText(this@MainActivity, "Sign-in canceled", Toast.LENGTH_SHORT).show()
            } catch (_: GetCredentialException) {
                Toast.makeText(this@MainActivity, "Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signOut() {
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                preferencesRepo.saveCrossDeviceSync(false)
                preferencesRepo.clearLoginState()
                Toast.makeText(this@MainActivity, "Logged out.", Toast.LENGTH_SHORT).show()
            }
            catch (_: Exception) {
                Toast.makeText(this@MainActivity, "Sign-out failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authorizeDrive() {
        val authorizationRequest = AuthorizationRequest.Builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .build()

        authorizationClient.authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
                        authorizationLauncher.launch(intentSenderRequest)
                    }
                } else {
                    lifecycleScope.launch {
                        val userEmail = preferencesRepo.signedInUserEmail.first()
                        if (userEmail != null) {
                            preferencesRepo.saveLoginState(userEmail, true)
                            preferencesRepo.saveCrossDeviceSync(true)
                            Toast.makeText(this@MainActivity, "Sync successfully enabled.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { _ ->
                lifecycleScope.launch {
                    preferencesRepo.saveCrossDeviceSync(false)
                    preferencesRepo.clearLoginState()
                    Toast.makeText(this@MainActivity, "Could not get Drive permission", Toast.LENGTH_SHORT).show()
                }
            }
    }

}


@Composable
private fun SystemBarsProtection(
    statusBarHeight: () -> Float = calculateStatusBar(),
    navigationHeight: () -> Float = calculateNavigation()
) {
 //   val darkTheme: Boolean = isSystemInDarkTheme()
 //   val color = if(darkTheme) Color.Black else Color.Black
    val color = Color.Black

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