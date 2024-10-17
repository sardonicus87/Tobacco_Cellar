package com.example.tobaccocellar.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tobaccocellar.data.Items
import com.example.tobaccocellar.data.ItemsRepository
import com.example.tobaccocellar.ui.FilterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val itemsRepository: ItemsRepository,
    private val filterViewModel: FilterViewModel
): ViewModel() {

    /** Raw stats */
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

    fun getTopTenBrands(rawStats: RawStats): Map<String, Int> {
        return rawStats.totalByBrand
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .associate { it.key to it.value }
    }


    /** Filtered stats */
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredStats: StateFlow<FilteredStats> =
        combine(
            filterViewModel.selectedBrands,
            filterViewModel.selectedTypes,
            filterViewModel.selectedFavorites,
            filterViewModel.selectedNeutral,
            filterViewModel.selectedDislikeds,
            filterViewModel.selectedInStock,
            filterViewModel.selectedOutOfStock
        ) { values ->
            val brands = values[0] as List<String>
            val types = values[1] as List<String>
            val favorites = values[2] as Boolean
            val neutral = values[3] as Boolean
            val dislikeds = values[4] as Boolean
            val inStock = values[5] as Boolean
            val outOfStock = values[6] as Boolean

            itemsRepository.getFilteredItems(
                brands = brands,
                types = types,
                favorites = favorites,
                neutral = neutral,
                dislikeds = dislikeds,
                inStock = inStock,
                outOfStock = outOfStock
            ).map { filteredItems ->

                FilteredStats(
                    brands = brands,
                    types = types,
                    favorites = favorites,
                    neutral = neutral,
                    dislikeds = dislikeds,
                    inStock = inStock,
                    outOfStock = outOfStock,

                    topBrands = filteredItems.groupingBy { it.brand }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .take(10)
                        .associate { it.key to it.value },
                    entriesByType = filteredItems.groupingBy { it.type }
                        .eachCount(),
                    entriesByRating = calculateEntriesByRating(filteredItems)
                )
            }
        }
            .flattenMerge()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = FilteredStats()
            )

    private fun calculateEntriesByRating(filteredItems: List<Items>): Map<String, Int> {
        val favoriteCount = filteredItems.count { it.favorite }
        val neutralCount = filteredItems.count { !it.favorite && !it.disliked }
        val dislikedCount = filteredItems.count { it.disliked }

        return mapOf(
            "Favorite" to favoriteCount,
            "Neutral" to neutralCount,
            "Dislike" to dislikedCount
        ).filterValues { it > 0 }
    }


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

data class FilteredStats(
    val brands: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val favorites: Boolean = false,
    val neutral: Boolean = false,
    val dislikeds: Boolean = false,
    val inStock: Boolean = false,
    val outOfStock: Boolean = false,

    val topBrands: Map<String, Int> = emptyMap(),
    val entriesByType: Map<String, Int> = emptyMap(),
    val entriesByRating: Map<String, Int> = emptyMap()
)
