package com.example.tobaccocellar.ui.csvimport

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CsvImportViewModel(
    private val itemsRepository: ItemsRepository,
) : ViewModel() {

    private val _csvImportState = mutableStateOf(CsvImportState())
    val csvImportState: State<CsvImportState> = _csvImportState
    var csvUiState by mutableStateOf(CsvUiState())


    /** setting states from the CSV data **/
    fun onCsvLoaded(
        header: List<String>, firstRecord: List<String>, allRecords: List<List<String>>, recordCount: Int
    ) {
        _csvImportState.value = CsvImportState(
            header = header.toList(),
            firstRecord = firstRecord.toList(),
            allRecords = allRecords.toList(),
            recordCount = recordCount
        )
    }

    fun generateColumns(columnCount: Int): List<String> {
        val header = csvImportState.value.header
        return if (header.isNotEmpty() && header.size == columnCount) { header }
        else { (1..columnCount).map { "Column $it" } }
            .also {
                csvUiState = csvUiState.copy(columns = it)
            }
    }


    /** handling mapping options and UI state **/
    var mappingOptions by mutableStateOf(MappingOptions())

    fun updateHeaderOptions(hasHeader: Boolean) {
        mappingOptions = mappingOptions.copy(hasHeader = hasHeader)
    }

    enum class CsvField { Brand, Blend, Type, Quantity, Favorite, Disliked, Notes }

    fun updateMappingOptions(field: CsvField, selectedColumn: String) {
        mappingOptions = when (field) {
            CsvField.Brand -> mappingOptions.copy(brandColumn = selectedColumn.ifBlank { "" })
            CsvField.Blend -> mappingOptions.copy(blendColumn = selectedColumn.ifBlank { "" })
            CsvField.Type -> mappingOptions.copy(typeColumn = selectedColumn.ifBlank { "" })
            CsvField.Quantity -> mappingOptions.copy(quantityColumn = selectedColumn.ifBlank { "" })
            CsvField.Favorite -> mappingOptions.copy(favoriteColumn = selectedColumn.ifBlank { "" })
            CsvField.Disliked -> mappingOptions.copy(dislikedColumn = selectedColumn.ifBlank { "" })
            CsvField.Notes -> mappingOptions.copy(notesColumn = selectedColumn.ifBlank { "" })
        }
        csvUiState = csvUiState.copy(isFormValid = validateForm())
    }


    /** Confirm and import **/
    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus = _importStatus.asStateFlow()

    private fun validateForm (csvUiState: MappingOptions = mappingOptions): Boolean {
        return with(csvUiState) {
            brandColumn.isNotBlank() && blendColumn.isNotBlank()
        }
    }

    fun confirmImport() = viewModelScope.launch {
        _importStatus.value = ImportStatus.Loading

        val hasHeader = mappingOptions.hasHeader
        val recordsToImport =
            if (hasHeader) csvImportState.value.allRecords.drop(1)
            else csvImportState.value.allRecords
        val columnIndices = getSelectedColumnIndices()

        val itemsToImport = recordsToImport.map { record ->
            Items(
                brand = if (columnIndices[CsvField.Brand] != null &&
                    columnIndices[CsvField.Brand]!! >= 0 &&
                    columnIndices[CsvField.Brand]!! < record.size)
                    record[columnIndices[CsvField.Brand]!!] else "",
                blend = if (columnIndices[CsvField.Blend] != null &&
                    columnIndices[CsvField.Blend]!! >=0 &&
                    columnIndices[CsvField.Blend]!! < record.size)
                    record[columnIndices[CsvField.Blend]!!] else "",
                type = if (columnIndices[CsvField.Type] != null &&
                    columnIndices[CsvField.Type]!! >= 0 &&
                    columnIndices[CsvField.Type]!! < record.size)
                    record[columnIndices[CsvField.Type]!!] else "",
                quantity = if (columnIndices[CsvField.Quantity] != null &&
                    columnIndices[CsvField.Quantity]!! >= 0 &&
                    columnIndices[CsvField.Quantity]!! < record.size)
                    record[columnIndices[CsvField.Quantity]!!].toIntOrNull() ?: 1 else 1,
                favorite = if (columnIndices[CsvField.Favorite] != null &&
                    columnIndices[CsvField.Favorite]!! >= 0 &&
                    columnIndices[CsvField.Favorite]!! < record.size)
                    record[columnIndices[CsvField.Favorite]!!].toBoolean() else false,
                disliked = if (columnIndices[CsvField.Disliked] != null &&
                    columnIndices[CsvField.Disliked]!! >= 0 &&
                    columnIndices[CsvField.Disliked]!! < record.size)
                    record[columnIndices[CsvField.Disliked]!!].toBoolean() else false,
                notes = if (columnIndices[CsvField.Notes] != null &&
                    columnIndices[CsvField.Notes]!! >= 0 &&
                    columnIndices[CsvField.Notes]!! < record.size)
                    record[columnIndices[CsvField.Notes]!!] else ""
            )
        }
        try {
            val insertedIds = withContext(Dispatchers.IO) {
                itemsRepository.insertMultiple(itemsToImport) }
            val successfulConversions = itemsToImport.size
            val successfulInsertions = insertedIds.count { it != -1L }
            val totalRecords =
                if (hasHeader) { csvImportState.value.recordCount - 1 }
                else { csvImportState.value.recordCount }

            delay(1500)

            _importStatus.value = ImportStatus.Success(
                totalRecords = totalRecords,
                successfulConversions = successfulConversions,
                successfulInsertions = successfulInsertions
            )
        } catch (e: Exception) {
            _importStatus.value = ImportStatus.Error(e)
        }
    }


    // Stored CSV UI state to Database Table structure conversion function //
    private fun getSelectedColumnIndices(): Map<CsvField, Int> {
        val header = csvImportState.value.header
        return mapOf(
            CsvField.Brand to header.indexOf(mappingOptions.brandColumn),
            CsvField.Blend to header.indexOf(mappingOptions.blendColumn),
            CsvField.Type to header.indexOf(mappingOptions.typeColumn),
            CsvField.Quantity to header.indexOf(mappingOptions.quantityColumn),
            CsvField.Favorite to header.indexOf(mappingOptions.favoriteColumn),
            CsvField.Disliked to header.indexOf(mappingOptions.dislikedColumn),
            CsvField.Notes to header.indexOf(mappingOptions.notesColumn)
        )
    }


    /** reset import state **/
    fun resetImportState() {
        _importStatus.value = ImportStatus.Idle
        _csvImportState.value = CsvImportState()
        csvUiState = CsvUiState()
        mappingOptions = MappingOptions()
    }
}


data class CsvImportState(
    val header: List<String> = emptyList(),
    val firstRecord: List<String> = emptyList(),
    val allRecords: List<List<String>> = emptyList(),
    val recordCount: Int = 0,
)

data class CsvUiState(
    val csvImportState: CsvImportState = CsvImportState(),
    val columns: List<String> = emptyList(),
    val isFormValid: Boolean = false,
)

data class MappingOptions(
    val hasHeader: Boolean = false,
    val brandColumn: String = "",
    val blendColumn: String = "",
    val typeColumn: String = "",
    val quantityColumn: String = "",
    val favoriteColumn: String = "",
    val dislikedColumn: String = "",
    val notesColumn: String = "",
)

sealed class ImportStatus {
    object Idle : ImportStatus()
    object Loading : ImportStatus()
    data class Success(
        val totalRecords: Int,
        val successfulConversions: Int,
        val successfulInsertions: Int
    ) : ImportStatus()
    data class Error(val exception: Throwable) : ImportStatus()
}

