package com.example.tobaccocellar.ui.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tobaccocellar.R
import com.example.tobaccocellar.ui.AppViewModelProvider
import com.example.tobaccocellar.ui.navigation.NavigationDestination

object NotesEntryDestination : NavigationDestination {
    override val route = "notes_entry"
    override val titleRes = R.string.notes_entry
}

@Composable
fun NotesEntryScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: AddEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
}
