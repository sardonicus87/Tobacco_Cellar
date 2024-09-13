package com.example.tobaccocellar.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.CellarTopAppBar
import com.example.tobaccocellar.R
import com.example.tobaccocellar.data.PreferencesRepo
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewmodel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CellarTopAppBar(
                title = stringResource(SettingsDestination.titleRes),
                scrollBehavior = scrollBehavior,
                navigateUp = onNavigateUp,
                canNavigateBack = canNavigateBack,
                navigateToCsvImport = {},
                navigateToSettings = {},
                showMenu = false,
            )
        },
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.Top),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
        ) {
            SettingsBody(
                onDeleteAllClick = {
                    coroutineScope.launch {
                        viewmodel.deleteAllItems()
                    }
                },
                saveTheme = { viewmodel.saveThemeSetting(it) },
                preferencesRepo = viewmodel.preferencesRepo,
                modifier = modifier
                    .fillMaxWidth(),
                contentPadding = innerPadding
            )
        }
    }
}


@Composable
private fun SettingsBody(
    onDeleteAllClick: () -> Unit,
    saveTheme: (String) -> Unit,
    modifier: Modifier = Modifier,
    preferencesRepo: PreferencesRepo,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var deleteAllConfirm by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }

    /* TODO: finish Settings body */
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Display Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp, top = 0.dp, bottom = 0.dp, end = 16.dp)
        )
        TextButton(
            onClick = { showThemeDialog = true },
            enabled = true,
            modifier = Modifier
                .padding(start = 4.dp)
        ) {
            Text(
                text = "Theme",
                modifier = Modifier
                    .padding(0.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            thickness = 1.dp,
        )
        Text(
            text = "Database Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp, top = 0.dp, bottom = 0.dp, end = 16.dp)
        )
        TextButton(
            onClick = { deleteAllConfirm = true },
            enabled = true,
            modifier = Modifier
                .padding(start = 4.dp)
        ) {
            Text(
                text = "Clear Database",
                modifier = Modifier
                    .padding(0.dp)
            )
        }

        if (showThemeDialog) {
            ThemeDialog(
                onThemeSelected = { newTheme ->
                    saveTheme(newTheme)
                },
                preferencesRepo = preferencesRepo,
                onClose = { showThemeDialog = false }
            )
        }
        if (deleteAllConfirm) {
            DeleteAllDialog(
                onDeleteConfirm = {
                    deleteAllConfirm = false
                    onDeleteAllClick()
                },
                onDeleteCancel = { deleteAllConfirm = false },
                modifier = Modifier
                    .padding(0.dp)
            )
        }
    }
}


@Composable
fun ThemeDialog(
    onThemeSelected: (String) -> Unit,
    onClose: () -> Unit,
    preferencesRepo: PreferencesRepo,
    modifier: Modifier = Modifier
) {
    val currentTheme by preferencesRepo.themeSetting.collectAsState(initial = ThemeSetting.SYSTEM.value)

    AlertDialog(
        onDismissRequest = { onClose() },
        modifier = modifier
            .padding(0.dp),
        text = {
            Column (
                modifier = modifier
                    .padding(bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy((-2).dp)
            ) {
                listOf(ThemeSetting.LIGHT, ThemeSetting.DARK, ThemeSetting.SYSTEM).forEach { theme ->
                    Row(
                        modifier = Modifier
                            .padding(0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme.value,
                            onClick = { onThemeSelected(theme.value) }
                        )
                        Text(
                            text = theme.value,
                            modifier = Modifier
                                .clickable { onThemeSelected(theme.value) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onClose() }
            ) {
                Text(stringResource(R.string.save))
            }
        },
    )
}

@Composable
private fun DeleteAllDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.delete_all)) },
        text = { Text(stringResource(R.string.delete_all_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(stringResource(R.string.yes))
            }
        }
    )
}