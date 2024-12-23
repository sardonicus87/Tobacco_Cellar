package com.sardonicus.tobaccocellar.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val itemsRepository: ItemsRepository,
    val preferencesRepo: PreferencesRepo,
): ViewModel() {

    /** Theme Settings */
    private val _themeSetting = MutableStateFlow(ThemeSetting.SYSTEM.value)
//    val themeSetting: StateFlow<String> = _themeSetting.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepo.themeSetting.first().let {
                savedThemeSetting ->
                _themeSetting.value = savedThemeSetting
                if (savedThemeSetting == ThemeSetting.SYSTEM.value) {
                    preferencesRepo.saveThemeSetting(ThemeSetting.SYSTEM.value)
                }
            }
        }
    }

    fun saveThemeSetting(setting: String) {
        viewModelScope.launch {
            preferencesRepo.saveThemeSetting(setting)
        }
    }

    /** Database Settings */
    suspend fun deleteAllItems() {
        itemsRepository.deleteAllItems()
    }

    private val _tinOzConversionRate = MutableStateFlow(1.75)
    val tinOzConversionRate: StateFlow<Double> = _tinOzConversionRate.asStateFlow()

    private val _tinGramsConversionRate = MutableStateFlow(50.0)
    val tinGramsConversionRate: StateFlow<Double> = _tinGramsConversionRate.asStateFlow()

    init {
        viewModelScope.launch {
            _tinOzConversionRate.value = preferencesRepo.getTinOzConversionRate()
            _tinGramsConversionRate.value = preferencesRepo.getTinGramsConversionRate()
        }
    }

    fun setTinConversionRates(ozRate: Double, gramsRate: Double) {
        viewModelScope.launch {
            preferencesRepo.setTinOzConversionRate(ozRate)
            preferencesRepo.setTinGramsConversionRate(gramsRate)
            _tinOzConversionRate.value = ozRate
            _tinGramsConversionRate.value = gramsRate
        }
    }

}

data class ThemeSetting(val value: String) {
    companion object {
        val LIGHT = ThemeSetting("Light")
        val DARK = ThemeSetting("Dark")
        val SYSTEM = ThemeSetting("System")
    }
}