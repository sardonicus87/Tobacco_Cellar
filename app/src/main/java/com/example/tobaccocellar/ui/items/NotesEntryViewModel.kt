package com.example.tobaccocellar.ui.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.tobaccocellar.data.ItemsRepository

data class NotesEntryViewModel(
//    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {

}