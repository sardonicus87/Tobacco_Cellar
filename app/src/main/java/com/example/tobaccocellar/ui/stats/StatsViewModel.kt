package com.example.tobaccocellar.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.ItemsRepository
import com.example.tobaccocellar.ui.FilterViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val itemsRepository: ItemsRepository,
    private val filterViewModel: FilterViewModel
): ViewModel() {

//    val itemsCount: StateFlow<Int> = itemsRepository.getItemsCount()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
//
//    val brandsCount: StateFlow<Int> = itemsRepository.getBrandsCount()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
//
//    val favoriteCount: StateFlow<Int> = itemsRepository.getTotalFavorite()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
//
//    val dislikedCount: StateFlow<Int> = itemsRepository.getTotalDislike()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
//
//    val totalByBrand: StateFlow<Map<String, Int>> = itemsRepository.getTotalByBrand()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())
//
//    val totalByType: StateFlow<Map<String, Int>> = itemsRepository.getTotalByType()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())
//
//    val totalQuantity: StateFlow<Int> = itemsRepository.getTotalQuantity()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
//
//    val totalZeroQuantity: StateFlow<Int> = itemsRepository.getTotalZeroQuantity()
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

//
//    fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
//    itemsRepository.getItemsCount(): Flow<T1>,
//    itemsRepository.getBrandsCount(): Flow<T2>,
//    itemsRepository.getTotalFavorite(): Flow<T3>,
//    itemsRepository.getTotalDislike(): Flow<T4>,
//    itemsRepository.getTotalByBrand(): Flow<T5>,
//    itemsRepository.getTotalByType(): Flow<T6>,
//    itemsRepository.getTotalQuantity(): Flow<T7>,
//    itemsRepository.getTotalZeroQuantity(): Flow<T8>,
//    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
//): Flow<R> = combine(
//    combine(
//)

    val rawStats: StateFlow<RawStats> = combine(
        combine(
            itemsRepository.getItemsCount(),
            itemsRepository.getBrandsCount(),
            itemsRepository.getTotalFavorite(),
            itemsRepository.getTotalDislike()
        ) { count, brands, favorite, disliked ->
            SetOne(count, brands, favorite, disliked)
        },
        combine(
            itemsRepository.getTotalByBrand(),
            itemsRepository.getTotalByType(),
            itemsRepository.getTotalQuantity(),
            itemsRepository.getTotalZeroQuantity()
        ) { byBrand, byType, quantity, zero ->
            SetTwo(byBrand, byType, quantity, zero)
        }
    ) { setOne, setTwo ->
        val brandMap = setTwo.byBrand.associate { it.brand to it.bcount }
        val typeMap = setTwo.byType.associate {
            if (it.type.isBlank()) "Unassigned" to it.tcount
            else it.type to it.tcount
        }
        RawStats(
            itemsCount = setOne.count,
            brandsCount = setOne.brands,
            favoriteCount = setOne.favorite,
            dislikedCount = setOne.dislike,
            totalByBrand = brandMap,
            totalByType = typeMap,
            totalQuantity = setTwo.quantity,
            totalZeroQuantity = setTwo.zero,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = RawStats()
    )


}

data class SetOne(
    val count: Int,
    val brands: Int,
    val favorite: Int,
    val dislike: Int
)

data class SetTwo(
    val byBrand: List<BrandCount>,
    val byType: List<TypeCount>,
    val quantity: Int,
    val zero: Int
)

data class BrandCount(
    val brand: String,
    val bcount: Int
)

data class TypeCount(
    val type: String,
    val tcount: Int
)

data class RawStats(
    val itemsCount: Int = 0,
    val brandsCount: Int = 0,
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalByBrand: Map<String, Int> = emptyMap(),
    val totalByType: Map<String, Int> = emptyMap(),
    val totalQuantity: Int = 0,
    val totalZeroQuantity: Int = 0
)