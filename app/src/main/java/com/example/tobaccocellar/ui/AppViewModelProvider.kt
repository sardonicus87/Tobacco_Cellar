package com.example.tobaccocellar.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tobaccocellar.CellarApplication
import com.example.tobaccocellar.ui.csvimport.CsvImportViewModel
import com.example.tobaccocellar.ui.home.HomeViewModel
import com.example.tobaccocellar.ui.items.AddEntryViewModel
import com.example.tobaccocellar.ui.items.EditEntryViewModel
import com.example.tobaccocellar.ui.settings.SettingsViewModel
import com.example.tobaccocellar.ui.stats.StatsViewModel

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

