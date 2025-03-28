package com.sardonicus.tobaccocellar.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(
    private val itemsRepository: ItemsRepository,
    private val filterViewModel: FilterViewModel
): ViewModel() {

    private val _refresh = MutableSharedFlow<Unit>(replay = 0)
    private val refresh = _refresh.asSharedFlow()

    init {
        viewModelScope.launch {
            EventBus.events.collect {
                if (it is DatabaseRestoreEvent) {
                    _refresh.emit(Unit)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val everythingFlow: Flow<List<ItemsComponentsAndTins>> =
        refresh.onStart { emit(Unit) }.flatMapLatest {
            itemsRepository.getEverythingStream()
        }


    /** Raw stats */
    val rawStats: StateFlow<RawStats> =
        everythingFlow
            .map {
                RawStats(
                    itemsCount = it.size,
                    brandsCount = it.groupingBy { it.items.brand }.eachCount().size,
                    favoriteCount = it.count { it.items.favorite },
                    dislikedCount = it.count { it.items.disliked },
                    totalByBrand = it.groupingBy { it.items.brand }.eachCount(),
                    totalByType = it.groupingBy {
                        if (it.items.type.isBlank()) "Unassigned" else it.items.type }.eachCount(),
                    totalQuantity = it.sumOf { it.items.quantity },
                    totalZeroQuantity = it.count { it.items.quantity == 0 },
                    totalBySubgenre = it.groupingBy {
                        if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre }.eachCount(),
                    totalByCut = it.groupingBy {
                        if(it.items.cut.isBlank()) "Unassigned" else it.items.cut }.eachCount(),
                    totalByContainer = it.flatMap { it.tins }.groupingBy {
                        if(it.container.isBlank()) "Unassigned" else it.container }.eachCount(),

                    rawLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = RawStats(rawLoading = true)
            )


    /** Filtered stats */
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredStats: StateFlow<FilteredStats> =
        combine(
            everythingFlow,
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
            filterViewModel.selectedComponent,
            filterViewModel.compMatching,
            filterViewModel.selectedSubgenre,
            filterViewModel.selectedCut,
            filterViewModel.selectedProduction,
            filterViewModel.selectedOutOfProduction
        ) { values ->
            val allItems = values[0] as List<ItemsComponentsAndTins>
            val brands = values[1] as List<String>
            val types = values[2] as List<String>
            val unassigned = values[3] as Boolean
            val favorites = values[4] as Boolean
            val dislikeds = values[5] as Boolean
            val neutral = values[6] as Boolean
            val nonNeutral = values[7] as Boolean
            val inStock = values[8] as Boolean
            val outOfStock = values[9] as Boolean
            val excludedBrands = values[10] as List<String>
            val excludedLikes = values[11] as Boolean
            val excludedDislikes = values[12] as Boolean
            val components = values[13] as List<String>
            val matching = values[14] as String
            val subgenres = values[15] as List<String>
            val cuts = values[16] as List<String>
            val production = values[17] as Boolean
            val outOfProduction = values[18] as Boolean

            val filteredItems = allItems.filter { items ->
                val componentMatching = when (matching) {
                    "All" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components)))
                    "Only" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                    else -> ((components.isEmpty() && !components.contains("(None Assigned)")) || ((components.contains("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.any { components.contains(it) })))
                }

                (brands.isEmpty() || brands.contains(items.items.brand)) &&
                        ((types.isEmpty() && !unassigned) || (types.contains(items.items.type) || (unassigned && items.items.type.isBlank()))) &&
                        (!favorites || items.items.favorite) &&
                        (!dislikeds || items.items.disliked) &&
                        (!neutral || (!items.items.favorite && !items.items.disliked)) &&
                        (!nonNeutral || (items.items.favorite || items.items.disliked)) &&
                        (!inStock || items.items.quantity > 0) &&
                        (!outOfStock || items.items.quantity == 0) &&
                        (excludedBrands.isEmpty() || !excludedBrands.contains(items.items.brand)) &&
                        (!excludedLikes || !items.items.favorite) &&
                        (!excludedDislikes || !items.items.disliked) &&
                        componentMatching &&
                        ((subgenres.isEmpty() && !subgenres.contains("(Unassigned)")) || ((subgenres.contains("(Unassigned)") && items.items.subGenre.isBlank()) || subgenres.contains(items.items.subGenre))) &&
                        ((cuts.isEmpty() && !cuts.contains("(Unassigned)")) || ((cuts.contains("(Unassigned)") && items.items.cut.isBlank()) || cuts.contains(items.items.cut))) &&
                        (!production || items.items.inProduction) &&
                        (!outOfProduction || !items.items.inProduction)
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
                        totalBySubgenre = filteredItems.groupingBy {
                            if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre }.eachCount(),
                        totalByCut = filteredItems.groupingBy {
                            if(it.items.cut.isBlank()) "Unassigned" else it.items.cut }.eachCount(),
                        totalByContainer = filteredItems.flatMap { it.tins }.groupingBy {
                            if(it.container.isBlank()) "Unassigned" else it.container }.eachCount(),


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
                            .filter { it.items.quantity > 0 }
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
                            .filter { it.items.quantity > 0 }
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
                        subgenresByEntries = filteredItems
                            .groupingBy { if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre }
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
                        subgenresByQuantity = filteredItems
                            .filter { it.items.quantity > 0 }
                            .groupingBy { if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre }
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
                        cutsByEntries = filteredItems
                            .groupingBy { if (it.items.cut.isBlank()) "Unassigned" else it.items.cut }
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
                        cutsByQuantity = filteredItems
                            .filter { it.items.quantity > 0 }
                            .groupingBy { if (it.items.cut.isBlank()) "Unassigned" else it.items.cut }
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

                        filteredLoading = false
                    )
                )
            }
        }
            .flattenMerge()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = FilteredStats(filteredLoading = true)
            )

}


data class BrandCount(
    val brand: String,
    val bcount: Int
)

data class TypeCount(
    val type: String,
    val tcount: Int
)

data class RawStats(
    val rawLoading: Boolean = false,

    val itemsCount: Int = 0,
    val brandsCount: Int = 0,
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalByBrand: Map<String, Int> = emptyMap(),
    val totalByType: Map<String, Int> = emptyMap(),
    val totalQuantity: Int = 0,
    val totalZeroQuantity: Int = 0,
    val totalBySubgenre: Map<String, Int> = emptyMap(),
    val totalByCut: Map<String, Int> = emptyMap(),
    val totalByContainer: Map<String, Int> = emptyMap(),
)

data class FilteredStats(
    val filteredLoading: Boolean = false,

    val itemsCount: Int = 0,
    val brandsCount: Int = 0,
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalByType: Map<String, Int> = emptyMap(),
    val unassignedCount: Int = 0,
    val totalQuantity: Int = 0,
    val totalZeroQuantity: Int = 0,
    val totalBySubgenre: Map<String, Int> = emptyMap(),
    val totalByCut: Map<String, Int> = emptyMap(),
    val totalByContainer: Map<String, Int> = emptyMap(),

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
    val subgenresByEntries: Map<String, Int> = emptyMap(),
    val subgenresByQuantity: Map<String, Int> = emptyMap(),
    val cutsByEntries: Map<String, Int> = emptyMap(),
    val cutsByQuantity: Map<String, Int> = emptyMap(),
)
