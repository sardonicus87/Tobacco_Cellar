package com.sardonicus.tobaccocellar.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportViewModel
import com.sardonicus.tobaccocellar.ui.home.HomeViewModel
import com.sardonicus.tobaccocellar.ui.items.AddEntryViewModel
import com.sardonicus.tobaccocellar.ui.items.EditEntryViewModel
import com.sardonicus.tobaccocellar.ui.settings.SettingsViewModel
import com.sardonicus.tobaccocellar.ui.stats.StatsViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            FilterViewModel(cellarApplication().container.itemsRepository)
        }

        initializer {
            StatsViewModel(
                cellarApplication().container.itemsRepository,
                cellarApplication().filterViewModel
            )
        }

        initializer {
            HomeViewModel(
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo,
                cellarApplication().filterViewModel,
                cellarApplication().csvHelper,
                cellarApplication()
            )
        }

        initializer {
            CsvImportViewModel(
                cellarApplication().container.itemsRepository,
            )
        }

        initializer {
            SettingsViewModel(
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            AddEntryViewModel(
                cellarApplication().container.itemsRepository
            )
        }

        initializer {
            EditEntryViewModel(
                this.createSavedStateHandle(),
                cellarApplication().container.itemsRepository
            )
        }
    }
}

fun CreationExtras.cellarApplication(): CellarApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CellarApplication)

