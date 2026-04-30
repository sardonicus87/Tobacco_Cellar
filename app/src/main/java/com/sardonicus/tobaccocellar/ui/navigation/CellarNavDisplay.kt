package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.sardonicus.tobaccocellar.ui.settings.SettingsScreen
import com.sardonicus.tobaccocellar.ui.settings.SettingsViewModel
import com.sardonicus.tobaccocellar.ui.stats.StatsScreen
import com.sardonicus.tobaccocellar.ui.stats.StatsViewModel
import com.sardonicus.tobaccocellar.ui.utilities.NetworkMonitor
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun CellarNavigation(
    navigator: Navigator,
    navigationState: NavigationState,
    isGestureNav: Boolean,
    twoPaneAllowed: Boolean,
    twoColumnTabs: Boolean,
    filterViewModel: FilterViewModel,
    modifier: Modifier = Modifier,
) {
    val shouldCloseSheet by remember(twoPaneAllowed, navigationState.isTwoPane) {
        derivedStateOf { twoPaneAllowed && navigationState.isTwoPane }
    }
    LaunchedEffect(shouldCloseSheet) {
        if (shouldCloseSheet) { filterViewModel.closeBottomSheet() }
    }

    val twoPaneState by filterViewModel.twoPaneState.collectAsState()
    LaunchedEffect(navigationState.isTwoPane, twoPaneState, navigationState.currentStack.size) {
        if (twoPaneState && !navigationState.isTwoPane && navigationState.currentStack.size > 1) {
            delay(500)
            filterViewModel.updateTwoPaneState(navigationState.isTwoPane)
        } else {
            filterViewModel.updateTwoPaneState(navigationState.isTwoPane)
        }
    }

    val app = LocalCellarApplication.current
    val preferencesRepo = app.preferencesRepo
    val itemsRepository = app.container.itemsRepository
    val csvHelper = app.csvHelper
    val networkMonitor = remember { NetworkMonitor(app) }


    val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        val paneInfo = (key as? PaneInfo)?.paneType?.let { mapOf(TwoPaneScene.PANE_TYPE to it) } ?: emptyMap()

        val slideTransition = if (!navigationState.isTwoPane || (key is PaneInfo && key.paneType == PaneType.NONE)) {
            transitionSpec {
                slideInHorizontally(tween(500)) { it } togetherWith ExitTransition.None
            } + popTransitionSpec {
                EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
            } + predictivePopTransitionSpec { swipeEdge ->
                if (isGestureNav) {
                    when (swipeEdge) {
                        NavigationEvent.EDGE_RIGHT -> {
                            slideInHorizontally(tween(500)) { it / 2 } togetherWith
                                    slideOutHorizontally(tween(500)) { -it / 2 }
                        }

                        NavigationEvent.EDGE_LEFT -> {
                            slideInHorizontally(tween(500)) { -it / 2 } togetherWith
                                    slideOutHorizontally(tween(500)) { it / 2 }
                        }

                        else -> EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
                    }
                } else {
                    EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
                }
            }
        } else emptyMap()

        val twoPaneSlide: Map<String, ContentTransform> = if (navigationState.isTwoPane) {
            mapOf(
                TwoPaneScene.PANE_ENTER to
                        if (navigationState.cameFrom is BlendDetailsDestination) {
                            slideInHorizontally(tween(500)) { it } togetherWith slideOutHorizontally(tween(500)) { -it }
                        } else {
                            slideInHorizontally(tween(500)) { it } togetherWith ExitTransition.None
                        },
                TwoPaneScene.PANE_EXIT to (EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it })
            )
        } else emptyMap()

        when (key) {
            is HomeDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: HomeViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            HomeViewModel(
                                preferencesRepo,
                                itemsRepository,
                                filterViewModel,
                                csvHelper,
                                app
                            )
                        }
                    }
                )
                val secondPaneExpanded by filterViewModel.secondPaneExpanded.collectAsState()

                HomeScreen(
                    navigateToBlendDetails = {
                        navigator.navigate(BlendDetailsDestination(it))
                        if (navigationState.isTwoPane && !secondPaneExpanded ) { filterViewModel.setSecondPaneExpansion(true) }
                    },
                    navigateToStats = { navigator.navigate(StatsDestination) },
                    navigateToDates = { navigator.navigate(DatesDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                    navigateToBulkEdit = { navigator.navigate(BulkEditDestination) },
                    navigateToCsvImport = { navigator.navigate(CsvFlowDestination) },
                    navigateToPlaintext = { navigator.navigate(PlaintextDestination) },
                    navigateToHelp = { navigator.navigate(HelpDestination) },
                    navigateToAbout = { navigator.navigate(AboutDestination) },
                    navigateToSettings = { navigator.navigate(SettingsDestination) },
                    navigateToChangelog = { navigator.navigate(ChangelogDestination(changelogEntries, it)) },
                    filterViewModel = filterViewModel,
                    viewModel = viewModel
                )
            }

            is BlendDetailsDestination -> NavEntry(key, metadata = slideTransition + twoPaneSlide + paneInfo) {
                val viewModel: BlendDetailsViewModel = viewModel(
                    key = key.itemsId.toString(),
                    factory = viewModelFactory {
                        initializer {
                            BlendDetailsViewModel(
                                itemsId = key.itemsId,
                                itemsRepository = itemsRepository,
                                preferencesRepo = preferencesRepo
                            )
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

            is FilterPaneDestination -> NavEntry(key, metadata = paneInfo) {
                FilterPane(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                )
            }

            is StatsDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: StatsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            StatsViewModel(
                                filterViewModel,
                                preferencesRepo
                            )
                        }
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
                        initializer {
                            DatesViewModel(
                                filterViewModel,
                                preferencesRepo
                            )
                        }
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
                        initializer {
                            AddEntryViewModel(
                                filterViewModel,
                                itemsRepository,
                                preferencesRepo,
                            )
                        }
                    }
                )


                AddEntryScreen(
                    navigateBack = { navigator.goBack() },
                    onNavigateUp = { navigator.goBack() },
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
                            EditEntryViewModel(
                                itemsId = key.itemsId,
                                filterViewModel = filterViewModel,
                                itemsRepository = itemsRepository,
                                preferencesRepo = preferencesRepo
                            )
                        }
                    }
                )

                EditEntryScreen(
                    navigateBack = { navigator.goBack() },
                    onNavigateUp = { navigator.goBack() },
                    twoColumnTabs = twoColumnTabs,
                    viewModel = viewModel
                )
            }

            is BulkEditDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: BulkEditViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            BulkEditViewModel(
                                filterViewModel,
                                itemsRepository,
                                preferencesRepo
                            )
                        }
                    }
                )

                BulkEditScreen(
                    onNavigateUp = { navigator.goBack() },
                    twoColumnTabs = twoColumnTabs,
                    viewModel = viewModel
                )
            }

            is CsvFlowDestination -> NavEntry(key) {
                val csvHelpScrollState = rememberScrollState()

                val startRoute = remember { CsvImportDestination(UUID.randomUUID().toString()) }
                val nestedNavigationState = rememberNavigationState(startRoute, setOf(startRoute))
                val nestedNavigator = remember { Navigator(nestedNavigationState) }

                val nestedEntryProvider: (NavKey) -> NavEntry<NavKey> = { nestedKey ->
                    when (val csvKey = nestedKey as CsvFlowKey) {

                        is CsvImportDestination -> NavEntry(csvKey, metadata = paneInfo) {
                            val viewModel: CsvImportViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        CsvImportViewModel(
                                            itemsRepository,
                                            preferencesRepo
                                        )
                                    }
                                }
                            )

                            CsvImportScreen(
                                navKey = csvKey,
                                onNavigateUp = { navigator.goBack() },
                                navigateToHome = { navigator.navigate(HomeDestination) },
                                navigateToCsvHelp = { nestedNavigator.navigate(CsvHelpDestination) },
                                navigateToImportResults = { totalRecords, successCount, successfulInsertions, successfulUpdates, successfulTins, updateFlag, tinFlag ->
                                    navigator.goBack()
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

                        is CsvHelpDestination -> NavEntry(csvKey, metadata = slideTransition + paneInfo) {
                            CsvHelpScreen(
                                onNavigateUp = { nestedNavigator.goBack() },
                                scrollState = csvHelpScrollState,
                            )
                        }
                    }
                }
                NavDisplay(
                    entries = nestedNavigationState.toEntries(nestedEntryProvider),
                    onBack = {
                        if (nestedNavigationState.canGoBack) {
                            nestedNavigator.goBack()
                        } else {
                            navigator.goBack()
                        }
                    }
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
                        initializer {
                            PlaintextViewModel(
                                filterViewModel,
                                preferencesRepo
                            )
                        }
                    }
                )

                PlaintextScreen(
                    onNavigateUp = { navigator.goBack() },
                    twoColumnTabs = twoColumnTabs,
                    viewModel = viewModel
                )
            }

            is HelpDestination -> NavEntry(key, metadata = paneInfo) {
                HelpScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is AboutDestination -> NavEntry(key, metadata = paneInfo) {
                AboutScreen(
                    onNavigateUp = { navigator.goBack() },
                    navigateToChangelog = { navigator.navigate(ChangelogDestination(changelogEntries)) },
                )
            }

            is ChangelogDestination -> NavEntry(key, metadata = slideTransition + twoPaneSlide + paneInfo) {
                ChangelogScreen(
                    onNavigateUp = { navigator.goBack() },
                    changelogEntries = key.changelogEntries,
                    targetVersion = key.targetVersion
                )
            }

            is SettingsDestination -> NavEntry(key, metadata = paneInfo) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            SettingsViewModel(
                                itemsRepository,
                                filterViewModel,
                                preferencesRepo,
                                networkMonitor,
                                app
                            )
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

    val twoPaneScene = rememberTwoPaneStrategy<NavKey>(navigationState.twoPaneSceneKey.intValue, navigationState.interceptBack, twoPaneAllowed)

    LaunchedEffect(twoPaneAllowed) {
        if (!twoPaneAllowed && navigationState.topLevelRoute == AboutDestination) {
            val aboutStack = navigationState.backStacks[AboutDestination]

            if (aboutStack?.size == 1 && aboutStack.last() == AboutDestination) {
                navigationState.topLevelRoute = navigationState.startRoute
                val targetStack = navigationState.backStacks[navigationState.startRoute]
                if (targetStack != null && !targetStack.contains(SettingsDestination)) {
                    targetStack.add(SettingsDestination)
                }
            }
        }
        else {
            if (twoPaneAllowed) {
                val startStack = navigationState.backStacks[navigationState.startRoute]

                if (navigationState.topLevelRoute == navigationState.startRoute && startStack?.lastOrNull() == SettingsDestination) {
                    navigator.navigate(AboutDestination)
                    startStack.remove(SettingsDestination)
                }
            }
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier,
        onBack = { navigator.goBack() },
        sceneStrategies = listOf(twoPaneScene, SinglePaneSceneStrategy()),
        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
        popTransitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
        predictivePopTransitionSpec = {
            if (isGestureNav && initialState::class != TwoPaneStrategy::class) {
                fadeIn() togetherWith scaleOut(targetScale = 0.7f)
            } else fadeIn(tween(500)) togetherWith fadeOut(tween(500))
        }
    )
}