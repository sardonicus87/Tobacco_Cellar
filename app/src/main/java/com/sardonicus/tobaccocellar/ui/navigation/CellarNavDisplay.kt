package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import androidx.navigationevent.NavigationEvent
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.gestureNavigation
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.aboutChangelog.AboutScreen
import com.sardonicus.tobaccocellar.ui.aboutChangelog.ChangelogScreen
import com.sardonicus.tobaccocellar.ui.aboutChangelog.changelogEntries
import com.sardonicus.tobaccocellar.ui.addEditItems.AddEntryScreen
import com.sardonicus.tobaccocellar.ui.addEditItems.AddEntryViewModel
import com.sardonicus.tobaccocellar.ui.addEditItems.EditEntryScreen
import com.sardonicus.tobaccocellar.ui.addEditItems.EditEntryViewModel
import com.sardonicus.tobaccocellar.ui.blendDetails.BlendDetailsScreen
import com.sardonicus.tobaccocellar.ui.blendDetails.BlendDetailsViewModel
import com.sardonicus.tobaccocellar.ui.bulkEdit.BulkEditScreen
import com.sardonicus.tobaccocellar.ui.bulkEdit.BulkEditViewModel
import com.sardonicus.tobaccocellar.ui.csvimport.CsvHelpScreen
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportResultsScreen
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportScreen
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportViewModel
import com.sardonicus.tobaccocellar.ui.dates.DatesScreen
import com.sardonicus.tobaccocellar.ui.dates.DatesViewModel
import com.sardonicus.tobaccocellar.ui.filtering.FilterPane
import com.sardonicus.tobaccocellar.ui.home.HelpScreen
import com.sardonicus.tobaccocellar.ui.home.HomeScreen
import com.sardonicus.tobaccocellar.ui.home.HomeViewModel
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextScreen
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextViewModel
import com.sardonicus.tobaccocellar.ui.settings.DialogType
import com.sardonicus.tobaccocellar.ui.settings.SettingsScreen
import com.sardonicus.tobaccocellar.ui.settings.SettingsViewModel
import com.sardonicus.tobaccocellar.ui.stats.StatsScreen
import com.sardonicus.tobaccocellar.ui.stats.StatsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CellarNavigation(
    navigator: Navigator,
    navigationState: NavigationState,
    twoPaneAllowed: Boolean,
    twoColumnTabs: Boolean,
    filterVM: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val isGestureNav = gestureNavigation()

    SideEffect(twoPaneAllowed && navigationState.isTwoPane) {
        if (twoPaneAllowed && navigationState.isTwoPane) { filterVM.closeBottomSheet() }
    }

    val twoPaneState by filterVM.twoPaneState.collectAsState()
    LaunchedEffect(navigationState.isTwoPane, twoPaneState, navigationState.currentStack.size) {
        if (twoPaneState && !navigationState.isTwoPane && navigationState.currentStack.size > 1) {
            delay(500.milliseconds)
            filterVM.updateTwoPaneState(navigationState.isTwoPane)
        } else { filterVM.updateTwoPaneState(navigationState.isTwoPane) }
    }

    val app = LocalCellarApplication.current
    val prefsRepo = app.preferencesRepo
    val itemsRepo = app.container.itemsRepository
    val csvHelper = app.csvHelper

    val scope = rememberCoroutineScope()
    val csvHelpScrollState = rememberScrollState()
    var settingsReset by remember { mutableIntStateOf(0) }
    var settingsOpenDialog by remember { mutableStateOf<DialogType?>(null) }

    val slideTrans =
        if (navigationState.cameFrom is BlendDetailsDestination) { slideInHorizontally(tween(500)) { it } togetherWith slideOutHorizontally(tween(500)) { -it } }
        else slideInHorizontally(tween(500)) { it } togetherWith ExitTransition.None
    val slidePop = EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
    val slidePredictive = { edge: Int ->
        if (isGestureNav) {
            when (edge) {
                NavigationEvent.EDGE_RIGHT -> {
                    EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { -it / 2 } + fadeOut()
                }
                NavigationEvent.EDGE_LEFT -> {
                    EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it / 2 } + fadeOut()
                }
                else -> EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
            }
        } else slidePop
    }

    val slideTransition = transitionSpec { slideTrans } + popTransitionSpec { slidePop } +
            predictivePopTransitionSpec { slidePredictive(it) } +
            mapOf("transitionSpec" to slideTrans, "popTransitionSpec" to slidePop, "predictivePopTransitionSpec" to slidePredictive)

    val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        val paneInfo = (key as? PaneInfo)?.paneType?.let { mapOf(TwoPaneScene.PANE_TYPE to it) } ?: emptyMap()

        when (key) {
            is HomeDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: HomeViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { HomeViewModel(prefsRepo, itemsRepo, filterVM, csvHelper, app) }
                    }
                )

                HomeScreen(
                    navigateToBlendDetails = { navigator.navigate(BlendDetailsDestination(it)) },
                    navigateToStats = { navigator.navigate(StatsDestination) },
                    navigateToDates = { navigator.navigate(DatesDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                    navigateToBulkEdit = { navigator.navigate(BulkEditDestination) },
                    navigateToCsvImport = {
                        scope.launch { csvHelpScrollState.scrollTo(0) }
                        navigator.navigate(CsvImportDestination(UUID.randomUUID().toString()))
                    },
                    navigateToPlaintext = { navigator.navigate(PlaintextDestination) },
                    navigateToHelp = { navigator.navigate(HelpDestination) },
                    navigateToAbout = { navigator.navigate(AboutDestination) },
                    navigateToSettings = { navigator.navigate(SettingsDestination) },
                    navigateToChangelog = { navigator.navigate(ChangelogDestination(changelogEntries, it)) },
                    filterViewModel = filterVM,
                    viewModel = viewModel
                )
            }

            is BlendDetailsDestination -> NavEntry(key, metadata = slideTransition + paneInfo) {
                val viewModel: BlendDetailsViewModel = viewModel(
                    key = key.itemsId.toString(),
                    factory = viewModelFactory {
                        initializer {
                            BlendDetailsViewModel(key.itemsId, filterVM, prefsRepo)
                        }
                    }
                )

                BlendDetailsScreen(
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                    onNavigateUp = { navigator.goBack() },
                    isTwoPane = navigationState.isTwoPane,
                    viewModel = viewModel
                )
            }

            is FilterPaneDestination -> NavEntry(key, metadata = paneInfo) { FilterPane(filterVM, twoPaneAllowed) }

            is StatsDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: StatsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { StatsViewModel(filterVM, prefsRepo) }
                    }
                )

                StatsScreen(
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToDates = { navigator.navigate(DatesDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    modifier = Modifier,
                    viewModel = viewModel
                )
            }

            is DatesDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: DatesViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { DatesViewModel(filterVM, prefsRepo) }
                    }
                )

                DatesScreen(
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToStats = { navigator.navigate(StatsDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    navigateToDetails = { navigator.navigate(BlendDetailsDestination(it)) },
                    modifier = Modifier,
                    viewModel = viewModel
                )
            }

            is AddEntryDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: AddEntryViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { AddEntryViewModel(filterVM, itemsRepo, prefsRepo) }
                    }
                )

                AddEntryScreen(
                    navigateBack = { navigator.goBack() },
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                    twoColumnTabs = twoColumnTabs,
                    viewModel = viewModel
                )
            }

            is EditEntryDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: EditEntryViewModel = viewModel(
                    key = key.itemsId.toString(),
                    factory = viewModelFactory {
                        initializer {
                            EditEntryViewModel(key.itemsId, filterVM, itemsRepo, prefsRepo)
                        }
                    }
                )

                EditEntryScreen(
                    navigateBack = { navigator.goBack() },
                    navigateBackSkip = {
                        navigationState.backStacks.forEach { (_, stack) ->
                            stack.removeIf { it is BlendDetailsDestination && it.itemsId == key.itemsId }
                        }
                        navigator.goBack()
                    },
                    twoColumnTabs = twoColumnTabs,
                    viewModel = viewModel
                )
            }

            is BulkEditDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: BulkEditViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { BulkEditViewModel(filterVM, itemsRepo, prefsRepo) }
                    }
                )

                BulkEditScreen(
                    onNavigateUp = { navigator.goBack() },
                    twoColumnTabs = twoColumnTabs,
                    viewModel = viewModel
                )
            }

            is CsvImportDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: CsvImportViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { CsvImportViewModel(itemsRepo, prefsRepo) }
                    }
                )

                CsvImportScreen(
                    navKey = key,
                    onNavigateUp = { navigator.goBack() },
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToCsvHelp = { navigator.navigate(CsvHelpDestination) },
                    navigateToImportResults = { totalRecords, successCount, successfulInsertions, successfulUpdates, successfulTins, updateFlag, tinFlag ->
                        val stack = navigationState.backStacks[HomeDestination]
                        stack?.removeIf { it is CsvImportDestination }
                        navigator.navigate(
                            CsvImportResultsDestination(
                                totalRecords,
                                successCount,
                                successfulInsertions,
                                successfulUpdates,
                                successfulTins,
                                updateFlag,
                                tinFlag
                            )
                        )
                    },
                    viewModel = viewModel
                )
            }

            is CsvHelpDestination -> NavEntry(key, metadata = slideTransition + paneInfo) {
                CsvHelpScreen(
                    onNavigateUp = { navigator.goBack() },
                    scrollState = csvHelpScrollState,
                )
            }

            is CsvImportResultsDestination -> {
                val transition = transitionSpec { EnterTransition.None togetherWith fadeOut(tween(500)) }

                NavEntry(key, metadata = transition + paneInfo) {
                    CsvImportResultsScreen(
                        totalRecords = key.totalRecords,
                        successfulConversions = key.successCount,
                        successfulInsertions = key.successfulInsertions,
                        successfulUpdates = key.successfulUpdates,
                        successfulTins = key.successfulTins,
                        updateFlag = key.updateFlag,
                        tinFlag = key.tinFlag,
                        navigateToHome = { navigator.goBack() },
                        onNavigateUp = { navigator.goBack() }
                    )
                }
            }

            is PlaintextDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: PlaintextViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { PlaintextViewModel(filterVM, prefsRepo) }
                    }
                )

                PlaintextScreen(
                    onNavigateUp = { navigator.goBack() },
                    twoColumnTabs = twoColumnTabs,
                    filterViewModel = filterVM,
                    viewModel = viewModel
                )
            }

            is HelpDestination -> NavEntry(key, metadata = paneInfo) {
                HelpScreen({ navigator.goBack() })
            }

            is AboutDestination -> NavEntry(key, metadata = paneInfo) {
                AboutScreen(
                    onNavigateUp = {
                        if (navigationState.isTwoPane) {
                            val aboutStack = navigationState.backStacks[AboutDestination]
                            aboutStack?.removeIf { it is ChangelogDestination }
                        }
                        navigator.goBack()
                    },
                    navigateToChangelog = { navigator.navigate(ChangelogDestination(changelogEntries)) },
                )
            }

            is ChangelogDestination -> NavEntry(key, metadata = slideTransition + paneInfo) {
                ChangelogScreen(
                    onNavigateUp = { navigator.goBack() },
                    changelogEntries = key.changelogEntries,
                    isTwoPane = navigationState.isTwoPane,
                    targetVersion = key.targetVersion
                )
            }

            is SettingsDestination -> NavEntry(key, metadata = paneInfo) { //  "${key}_${settingsReset}"
                DisposableEffect(Unit) {
                    onDispose {
                        navigationState.backStacks.values.any { it.contains(SettingsDestination) }
                            .let { if (!it) settingsOpenDialog = null }
                    }
                }

                val viewModel: SettingsViewModel = viewModel(
                    key = "settings_v${settingsReset}",
                    factory = viewModelFactory {
                        initializer {
                            SettingsViewModel(itemsRepo, filterVM, prefsRepo, app,
                                settingsOpenDialog) { settingsOpenDialog = it }
                        }
                    }
                )

                SettingsScreen(
                    onNavigateUp = { navigator.goBack() },
                    canNavigateBack = !navigationState.isTwoPane,
                    viewModel = viewModel
                )
            }

            else -> error("Unknown destination: $key")
        }
    }

    val validPairing by remember {
        derivedStateOf {
            val currentStack = navigationState.currentStack
            val mainKey = currentStack.findLast { it is PaneInfo && it.paneType == PaneType.MAIN }
            val lastKey = currentStack.lastOrNull()
            val pairing = mainSecondaryMap.entries.find { map -> map.key::class == mainKey?.let { it::class } }?.value

            if (mainKey != null && lastKey != null && mainKey::class == lastKey::class) { true }
            else { pairing?.allowedSeconds?.contains(lastKey?.let { it::class }) ?: true }
        }
    }
    val twoPaneScene = rememberTwoPaneStrategy<NavKey>(twoPaneAllowed && validPairing, navigationState.interceptBack)

    var wasAllowed by remember { mutableStateOf(twoPaneAllowed) }

    LaunchedEffect(twoPaneAllowed) {
        yield()
        if (wasAllowed != twoPaneAllowed) {
            if (!twoPaneAllowed && navigationState.topLevelRoute == AboutDestination) { // twoPane to Single
                navigationState.backStacks[AboutDestination]?.clear()
                navigationState.topLevelRoute = navigationState.startRoute
                navigationState.backStacks[navigationState.startRoute]?.let { stack ->
                    stack.removeIf { it is SettingsDestination }
                    stack.add(SettingsDestination)
                }
            } else if (twoPaneAllowed) { // Single to twoPane
                val startStack = navigationState.backStacks[navigationState.startRoute]

                if (navigationState.topLevelRoute == navigationState.startRoute && startStack?.lastOrNull() is SettingsDestination) {
                    startStack.removeIf { it is SettingsDestination }
                    navigator.navigate(AboutDestination)
                }
            }
            settingsReset++
            wasAllowed = twoPaneAllowed
        }
    }

    SharedTransitionLayout {
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            modifier = modifier,
            onBack = { navigator.goBack() },
            sceneStrategies = listOf(twoPaneScene, SinglePaneSceneStrategy()),
            sharedTransitionScope = this,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
            popTransitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
            predictivePopTransitionSpec = {
                if (isGestureNav && (initialState.entries.size == 1 || targetState.entries.size == 1)) {
                    fadeIn(tween()) togetherWith scaleOut(targetScale = 0.7f) }
                else fadeIn(tween(500)) togetherWith fadeOut(tween(500))
            }
        )
    }
}