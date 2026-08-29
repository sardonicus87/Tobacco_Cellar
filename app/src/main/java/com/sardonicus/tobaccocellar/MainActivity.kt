package com.sardonicus.tobaccocellar

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.contextmenu.data.TextContextMenuKeys.AutofillKey
import androidx.compose.foundation.text.contextmenu.modifier.filterTextContextMenuComponents
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.DriveScopes
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.composables.LoadingIndicator
import com.sardonicus.tobaccocellar.ui.settings.DismissLoading
import com.sardonicus.tobaccocellar.ui.settings.ShowLoading
import com.sardonicus.tobaccocellar.ui.settings.SignInCancelled
import com.sardonicus.tobaccocellar.ui.settings.SignInEvent
import com.sardonicus.tobaccocellar.ui.settings.SignOutEvent
import com.sardonicus.tobaccocellar.ui.theme.TobaccoCellarTheme
import com.sardonicus.tobaccocellar.ui.utilities.DismissSnackbar
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import com.sardonicus.tobaccocellar.ui.utilities.ShowSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedOnce) { finish(); return }

                    backPressedOnce = true
                    Toast.makeText(this@MainActivity, "Tap again to exit", Toast.LENGTH_SHORT).show()

                    lifecycleScope.launch(Dispatchers.Default) {
                        delay(2000.milliseconds)
                        backPressedOnce = false
                    }
                }
            }
        )

        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        updateSystemBarsForOrientation(resources.configuration.orientation)

        preferencesRepo = (application as CellarApplication).preferencesRepo
        credentialManager = CredentialManager.create(applicationContext)
        authorizationClient = Identity.getAuthorizationClient(applicationContext)
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
            } else { Toast.makeText(this@MainActivity, "Drive permission was denied", Toast.LENGTH_SHORT).show() }
        }


        // sign in launch
        lifecycleScope.launch(Dispatchers.Default) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                EventBus.events.collect { event ->
                    if (event is SignInEvent) {
                        val userEmail = preferencesRepo.signedInUserEmail.first()
                        val hasScope = preferencesRepo.hasDriveScope.first()
                        when {
                            userEmail != null && hasScope -> { preferencesRepo.saveCrossDeviceSync(true) }
                            userEmail != null && !hasScope -> { authorizeDrive() }
                            else -> { signIn() }
                        }
                    }
                    if (event is SignOutEvent) { signOut() }
                }
            }
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            var loading by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                var snackbarJob: Job? = null
                EventBus.events.collect { event ->
                    when (event) {
                        is ShowSnackbar -> {
                            snackbarJob?.cancel()
                            snackbarJob = launch { snackbarHostState.showSnackbar(event.message) }
                        }
                        is DismissSnackbar -> { snackbarHostState.currentSnackbarData?.dismiss() }
                        is ShowLoading -> { loading = true }
                        is DismissLoading -> { loading = false }
                    }
                }
            }

            CompositionLocalProvider(LocalCellarApplication provides this@MainActivity.application as CellarApplication) {
                TobaccoCellarTheme(preferencesRepo = preferencesRepo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .windowInsetsPadding(WindowInsets.displayCutout)
                            .filterTextContextMenuComponents { it.key != AutofillKey }
                    ) {
                        val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
                        val isLarge: Boolean = remember(windowSizeClass) { windowSizeClass.isAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND, HEIGHT_DP_MEDIUM_LOWER_BOUND) }
                        val globalTwoPane by preferencesRepo.globalTwoPane.collectAsState()
                        val twoColumnSetting by preferencesRepo.twoColumnTabs.collectAsState()
                        val landscapeOnly by preferencesRepo.landscapeTwoPane.collectAsState()
                        val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                        val twoPaneAllowed = remember(isLarge, globalTwoPane, landscapeOnly, landscape) {
                            isLarge && globalTwoPane && (if (landscapeOnly) landscape else true)
                        }
                        val twoColumnTabs = remember(isLarge, twoColumnSetting, landscapeOnly, landscape) {
                            isLarge && twoColumnSetting && (if (landscapeOnly) landscape else true)
                        }

                        CellarApp(
                            twoPaneAllowed = twoPaneAllowed,
                            twoColumnTabs = twoColumnTabs
                        )

                        if (loading) { LoadingIndicator(scrimColor = Color.Black.copy(alpha = 0.5f)) }

                        if (snackbarHostState.currentSnackbarData != null) {
                            Dialog(
                                onDismissRequest = { },
                                properties = DialogProperties(
                                    usePlatformDefaultWidth = false,
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                )
                            ) {
                                val view = LocalView.current
                                SideEffect {
                                    (view.parent as? DialogWindowProvider)?.window?.let {
                                        it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                                        it.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                                        it.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
                                        it.setGravity(Gravity.BOTTOM)
                                    }
                                }
                                SnackbarHost(
                                    snackbarHostState,
                                    Modifier.padding(bottom = 10.dp)
                                ) {
                                    val dismissState = rememberSwipeToDismissBoxState()
                                    var isVisible by remember { mutableStateOf(true) }

                                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                        SideEffect {
                                            if (isVisible) {
                                                isVisible = false
                                                EventBus.tryEmit(DismissSnackbar)
                                            }
                                        }
                                    }
                                    if (isVisible) {
                                        SwipeToDismissBox(
                                            state = dismissState,
                                            backgroundContent = { },
                                            onDismiss = {
                                                isVisible = false
                                                EventBus.tryEmit(DismissSnackbar)
                                            },
                                        ) { Snackbar(it) }
                                    }
                                }
                            }
                        }
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
        } else { windowInsetsController.show(WindowInsetsCompat.Type.statusBars()) }
    }

    private fun signIn() {
        lifecycleScope.launch {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val signInOption = GetSignInWithGoogleOption.Builder(
                serverClientId = getString(R.string.web_client_id),
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .addCredentialOption(signInOption)
                .build()

            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val userEmail = credential.id

                preferencesRepo.saveLoginState(userEmail, false)
                authorizeDrive()
            } catch (e: GetCredentialException) { handleSignInFailure(e) }
        }
    }

    private suspend fun handleSignInFailure(e: GetCredentialException) {
        val message = when (e) {
            is GetCredentialCancellationException -> "Sign-in cancelled."
            is NoCredentialException -> "No accounts on this device."
            else -> "Sign-in failed."
        }
        EventBus.emit(SignInCancelled)
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    fun signOut() {
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                preferencesRepo.saveCrossDeviceSync(false)
                preferencesRepo.clearLogin()
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
                            (application as CellarApplication).periodicDownloadSetup()
                            Toast.makeText(this@MainActivity, "Sync successfully enabled.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { _ ->
                lifecycleScope.launch {
                    preferencesRepo.saveCrossDeviceSync(false)
                    preferencesRepo.clearLogin()
                    Toast.makeText(this@MainActivity, "Could not get Drive permission", Toast.LENGTH_SHORT).show()
                }
            }
    }

}


@Composable
private fun SystemBarsProtection() {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density).toFloat()
    val navigationHeight = WindowInsets.navigationBars.getBottom(density).toFloat()

    Spacer(Modifier
        .fillMaxSize()
        .drawBehind {
            drawRect(Color.Black, size = Size(size.width, statusBarHeight))
            drawRect(Color.Black, Offset(0f, size.height - navigationHeight),
                Size(size.width, navigationHeight))
        }
    )
}


@Composable
fun gestureNavigation(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { return false }
    else {
        val insets = WindowInsets.systemGestures
        val density = LocalDensity.current
        val direction = LocalLayoutDirection.current
        val left = insets.getLeft(density, direction)
        val right = insets.getRight(density, direction)
        return left > 0 && right > 0
    }
}