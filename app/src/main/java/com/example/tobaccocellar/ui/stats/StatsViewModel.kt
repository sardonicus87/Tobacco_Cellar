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
import kotlinx.coroutines.flow.flow
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


    /** Filtered stats */
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredStats: StateFlow<FilteredStats> =
        combine(
            filterViewModel.selectedBrands,
            filterViewModel.selectedTypes,
            filterViewModel.selectedUnassigned,
            filterViewModel.selectedFavorites,
            filterViewModel.selectedDislikeds,
            filterViewModel.selectedNeutral,
            filterViewModel.selectedNonNeutral,
            filterViewModel.selectedInStock,
            filterViewModel.selectedOutOfStock,
            filterViewModel.selectedExcludedBrands,
            itemsRepository.getAllItemsStream()
        ) { values ->
            val brands = values[0] as List<String>
            val types = values[1] as List<String>
            val unassigned = values[2] as Boolean
            val favorites = values[3] as Boolean
            val dislikeds = values[4] as Boolean
            val neutral = values[5] as Boolean
            val nonNeutral = values[6] as Boolean
            val inStock = values[7] as Boolean
            val outOfStock = values[8] as Boolean
            val excludedBrands = values[9] as List<String>
            val allItems = values[10] as List<Items>

            val filteredItems = allItems.filter { items ->
                (brands.isEmpty() || brands.contains(items.brand)) &&
                        (types.isEmpty() || types.contains(items.type)) &&
                        (!unassigned || items.type.isBlank()) &&
                        (!favorites || items.favorite) &&
                        (!dislikeds || items.disliked) &&
                        (!neutral || (!items.favorite && !items.disliked)) &&
                        (!nonNeutral || (items.favorite || items.disliked)) &&
                        (!inStock || items.quantity > 0) &&
                        (!outOfStock || items.quantity == 0) &&
                        (excludedBrands.isEmpty() || !excludedBrands.contains(items.brand))
            }

            val unassignedCount = filteredItems.count { it.type.isBlank() }

            flow { emit(
                FilteredStats(
                    brands = brands,
                    types = types,
                    favorites = favorites,
                    dislikeds = dislikeds,
                    neutral = neutral,
                    nonNeutral = nonNeutral,
                    inStock = inStock,
                    outOfStock = outOfStock,

                    itemsCount = filteredItems.size,
                    brandsCount = filteredItems.groupingBy { it.brand }.eachCount().size,
                    favoriteCount = filteredItems.count { it.favorite },
                    dislikedCount = filteredItems.count { it.disliked },
                    totalByType = filteredItems.groupingBy {
                        if (it.type.isBlank()) "Unassigned" else it.type }
                        .eachCount(),
                    unassignedCount = unassignedCount,
                    totalQuantity = filteredItems.sumOf { it.quantity },
                    totalZeroQuantity = filteredItems.count { it.quantity == 0 },


                    topBrands = filteredItems.groupingBy { it.brand }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .take(10)
                        .associate { it.key to it.value },
                    entriesByType = filteredItems
                        .filterNot { it.type.isBlank() }
                        .groupingBy { it.type }
                        .eachCount(),
                    entriesByRating = calculateEntriesByRating(filteredItems)
                )
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
            "Disliked" to dislikedCount
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
    val itemsCount: Int = 0,
    val brandsCount: Int = 0,
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalByType: Map<String, Int> = emptyMap(),
    val unassignedCount: Int = 0,
    val totalQuantity: Int = 0,
    val totalZeroQuantity: Int = 0,

    val brands: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val favorites: Boolean = false,
    val dislikeds: Boolean = false,
    val neutral: Boolean = false,
    val nonNeutral: Boolean = false,
    val inStock: Boolean = false,
    val outOfStock: Boolean = false,

    val topBrands: Map<String, Int> = emptyMap(),
    val entriesByType: Map<String, Int> = emptyMap(),
    val entriesByRating: Map<String, Int> = emptyMap()
)
