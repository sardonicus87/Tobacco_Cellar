package com.example.tobaccocellar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.tobaccocellar.data.PreferencesRepo
import com.example.tobaccocellar.ui.settings.ThemeSetting

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
)


// light //
private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
)

// dark //
private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
)

@Immutable
data class CustomColorScheme(
    val listMenuScrim: Color = Color.Unspecified,
    val deleteButton: Color = Color.Unspecified,
    val favHeart: Color = Color.Unspecified,
    val disHeart: Color = Color.Unspecified,
    val textField: Color = Color.Unspecified,
    val darkNeutral: Color = Color.Unspecified,
    val backgroundVariant: Color = Color.Unspecified,
    val backgroundUnselected: Color = Color.Unspecified,
    val tableBorder: Color = Color.Unspecified,
    val navIcon: Color = Color.Unspecified,
    // pie chart colors
    val pieOne: Color = Color.Unspecified,
    val pieTwo: Color = Color.Unspecified,
    val pieThree: Color = Color.Unspecified,
    val pieFour: Color = Color.Unspecified,
    val pieFive: Color = Color.Unspecified,
    val pieSix: Color = Color.Unspecified,
    val pieSeven: Color = Color.Unspecified,
    val pieEight: Color = Color.Unspecified,
    val pieNine: Color = Color.Unspecified,
    val pieTen: Color = Color.Unspecified,
)

val LocalCustomColors = staticCompositionLocalOf { CustomColorScheme() }

private val customLight = CustomColorScheme(
    listMenuScrim = listMenuScrimLight,
    deleteButton = deleteButtonLight,
    favHeart = favHeartLight,
    disHeart = disHeartLight,
    textField = textFieldLight,
    darkNeutral = darkNeutralLight,
    backgroundVariant = backgroundVariantLight,
    backgroundUnselected = backgroundUnselectedLight,
    tableBorder = tableBorderLight,
    navIcon = navIconLight,
    // pie chart colors
    pieOne = pieOneLight,
    pieTwo = pieTwoLight,
    pieThree = pieThreeLight,
    pieFour = pieFourLight,
    pieFive = pieFiveLight,
    pieSix = pieSixLight,
    pieSeven = pieSevenLight,
    pieEight = pieEightLight,
    pieNine = pieNineLight,
    pieTen = pieTenLight,
)

private val customDark = CustomColorScheme(
    listMenuScrim = listMenuScrimDark,
    deleteButton = deleteButtonDark,
    favHeart = favHeartDark,
    disHeart = disHeartDark,
    textField = textFieldDark,
    darkNeutral = darkNeutralDark,
    backgroundVariant = backgroundVariantDark,
    backgroundUnselected = backgroundUnselectedDark,
    tableBorder = tableBorderDark,
    navIcon = navIconDark,
    // pie chart colors
    pieOne = pieOneDark,
    pieTwo = pieTwoDark,
    pieThree = pieThreeDark,
    pieFour = pieFourDark,
    pieFive = pieFiveDark,
    pieSix = pieSixDark,
    pieSeven = pieSevenDark,
    pieEight = pieEightDark,
    pieNine = pieNineDark,
    pieTen = pieTenDark,
)


@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)



@Composable
fun TobaccoCellarTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    dynamicColor: Boolean = false,
    preferencesRepo: PreferencesRepo,
    content: @Composable () -> Unit
) {
    val userThemeSetting by preferencesRepo.themeSetting.collectAsState(
        initial = ThemeSetting.SYSTEM.value
    )

    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && userThemeSetting == ThemeSetting.SYSTEM.value -> {
//            val context = LocalContext.current
//            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else
//                    dynamicLightColorScheme(context)
//            }

        userThemeSetting == ThemeSetting.DARK.value -> darkScheme
        userThemeSetting == ThemeSetting.LIGHT.value -> lightScheme
        else -> if (userThemeSetting == ThemeSetting.SYSTEM.value) {
            if (isSystemInDarkTheme()) darkScheme else lightScheme
        } else {
            lightScheme
        }
    }

    val customColors = when (userThemeSetting) {
        ThemeSetting.DARK.value -> customDark
        ThemeSetting.LIGHT.value -> customLight
        ThemeSetting.SYSTEM.value -> { if (isSystemInDarkTheme()) customDark else customLight }
        else -> customLight
//        userThemeSetting == ThemeSetting.DARK.value -> customDark
//        userThemeSetting == ThemeSetting.LIGHT.value -> customLight
//        else -> if (userThemeSetting == ThemeSetting.SYSTEM.value) {
//            if (isSystemInDarkTheme()) customDark else customLight
//        } else {
//            customLight
//        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

//@Composable
//fun TobaccoCellarTheme(
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    currentTheme: ColorScheme,
//    content: @Composable () -> Unit
//) {
//    val configuration = LocalConfiguration.current
//    val darkTheme = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
//
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> darkScheme
//        else -> lightScheme
//    }
//
//    MaterialTheme(
//        colorScheme = currentTheme,
//        typography = Typography,
//        content = content
//    )
//}

//val colorScheme = when {
//    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//        val context = LocalContext.current
//        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//    }
//    darkTheme -> darkScheme
//    else -> lightScheme
//}

////    darkTheme: Boolean = isSystemInDarkTheme(),
////    dynamicColor: Boolean = false,
//preferencesRepo: PreferencesRepo,
//content: @Composable () -> Unit
//) {
//    val userThemeSetting by preferencesRepo.themeSetting.collectAsState(
//        initial = ThemeSetting.SYSTEM.value
//    )
//
//    val colorScheme = when {
////        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && userThemeSetting == ThemeSetting.SYSTEM.value -> {
////            val context = LocalContext.current
////            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else
////                    dynamicLightColorScheme(context)
////            }
//
//        userThemeSetting == ThemeSetting.DARK.value -> darkScheme
//        userThemeSetting == ThemeSetting.LIGHT.value -> lightScheme
//        else -> if (userThemeSetting == ThemeSetting.SYSTEM.value) {
//            if (isSystemInDarkTheme()) darkScheme else lightScheme
//        } else {
//            lightScheme
//        }
//    }