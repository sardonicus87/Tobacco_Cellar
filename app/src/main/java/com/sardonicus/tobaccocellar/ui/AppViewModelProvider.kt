package com.sardonicus.tobaccocellar.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportViewModel
import com.sardonicus.tobaccocellar.ui.dates.DatesViewModel
import com.sardonicus.tobaccocellar.ui.details.BlendDetailsViewModel
import com.sardonicus.tobaccocellar.ui.home.HomeViewModel
import com.sardonicus.tobaccocellar.ui.items.AddEntryViewModel
import com.sardonicus.tobaccocellar.ui.items.BulkEditViewModel
import com.sardonicus.tobaccocellar.ui.items.EditEntryViewModel
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextViewModel
import com.sardonicus.tobaccocellar.ui.settings.SettingsViewModel
import com.sardonicus.tobaccocellar.ui.stats.StatsViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            FilterViewModel(
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            StatsViewModel(
                cellarApplication().filterViewModel,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            HomeViewModel(
                cellarApplication().preferencesRepo,
                cellarApplication().filterViewModel,
                cellarApplication().csvHelper,
                cellarApplication()
            )
        }

        initializer {
            BlendDetailsViewModel(
                this.createSavedStateHandle(),
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            PlaintextViewModel(
                cellarApplication().filterViewModel,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            DatesViewModel(
                cellarApplication().filterViewModel,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            CsvImportViewModel(
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo,
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
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            EditEntryViewModel(
                this.createSavedStateHandle(),
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo
            )
        }

        initializer {
            BulkEditViewModel(
                cellarApplication().container.itemsRepository,
                cellarApplication().preferencesRepo
            )
        }
    }
}

fun CreationExtras.cellarApplication(): CellarApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CellarApplication)

