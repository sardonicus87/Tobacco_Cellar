package com.sardonicus.tobaccocellar.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Items
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.filter

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
            filterViewModel.selectedExcludeBrands,
            filterViewModel.selectedExcludeLikes,
            filterViewModel.selectedExcludeDislikes,
            itemsRepository.getEverythingStream()
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
            val excludedLikes = values[10] as Boolean
            val excludedDislikes = values[11] as Boolean
            val allItems = values[12] as List<ItemsComponentsAndTins>

            val filteredItems = allItems.filter { items ->
                (brands.isEmpty() || brands.contains(items.items.brand)) &&
                        (types.isEmpty() || types.contains(items.items.type)) &&
                        (!unassigned || items.items.type.isBlank()) &&
                        (!favorites || items.items.favorite) &&
                        (!dislikeds || items.items.disliked) &&
                        (!neutral || (!items.items.favorite && !items.items.disliked)) &&
                        (!nonNeutral || (items.items.favorite || items.items.disliked)) &&
                        (!inStock || items.items.quantity > 0) &&
                        (!outOfStock || items.items.quantity == 0) &&
                        (excludedBrands.isEmpty() || !excludedBrands.contains(items.items.brand)) &&
                        (!excludedLikes || !items.items.favorite) &&
                        (!excludedDislikes || !items.items.disliked)
            }

            val unassignedCount = filteredItems.count { it.items.type.isBlank() }


            flow {
                emit(
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
                        brandsCount = filteredItems.groupingBy { it.items.brand }.eachCount().size,
                        favoriteCount = filteredItems.count { it.items.favorite },
                        dislikedCount = filteredItems.count { it.items.disliked },
                        totalByType = filteredItems.groupingBy {
                            if (it.items.type.isBlank()) "Unassigned" else it.items.type }
                            .eachCount(),
                        unassignedCount = unassignedCount,
                        totalQuantity = filteredItems.sumOf { it.items.quantity },
                        totalZeroQuantity = filteredItems.count { it.items.quantity == 0 },


                        brandsByEntries = filteredItems
                            .groupingBy { it.items.brand }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .let {
                                if (it.size > 10) {
                                    val topNine = it.take(9).associate { it.key to it.value }
                                    val otherCount = it.drop(9).sumOf { it.value }
                                    topNine + ("(Other)" to otherCount)
                                } else {
                                    it.associate { it.key to it.value }
                                }
                            },
                        brandsByQuantity = filteredItems
                            .groupingBy { it.items.brand }
                            .fold(0) { acc, item -> acc + item.items.quantity }
                            .entries
                            .sortedByDescending { it.value }
                            .let {
                                if (it.size > 10) {
                                    val topNine = it.take(9).associate { it.key to it.value }
                                    val otherCount = it.drop(9).sumOf { it.value }
                                    topNine + ("(Other)" to otherCount)
                                } else {
                                    it.associate { it.key to it.value }
                                }
                            },
                        brandsByFavorites = filteredItems
                            .filter{ it.items.favorite }
                            .groupingBy { it.items.brand }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .let {
                                if (it.size > 10) {
                                    val topNine = it.take(9).associate { it.key to it.value }
                                    val otherCount = it.drop(9).sumOf { it.value }
                                    topNine + ("(Other)" to otherCount)
                                } else {
                                    it.associate { it.key to it.value }
                                }
                            },
                        typesByEntries = filteredItems
                            .groupingBy { if (it.items.type.isBlank()) "Unassigned" else it.items.type }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .associate { it.key to it.value },
                        typesByQuantity = filteredItems
                            .groupingBy { if (it.items.type.isBlank()) "Unassigned" else it.items.type }
                            .fold(0) { acc, item -> acc + item.items.quantity }
                            .entries
                            .sortedByDescending { it.value }
                            .associate { it.key to it.value },
                        ratingsByEntries = filteredItems
                            .groupingBy { if (it.items.favorite) "Favorite" else if (it.items.disliked) "Disliked" else "Neutral" }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .associate { it.key to it.value },
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

    val brandsByEntries: Map<String, Int> = emptyMap(),
    val brandsByQuantity: Map<String, Int> = emptyMap(),
    val brandsByFavorites: Map<String, Int> = emptyMap(),
    val typesByEntries: Map<String, Int> = emptyMap(),
    val typesByQuantity: Map<String, Int> = emptyMap(),
    val ratingsByEntries: Map<String, Int> = emptyMap(),
)
