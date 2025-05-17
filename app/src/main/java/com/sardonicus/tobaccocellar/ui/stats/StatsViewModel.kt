package com.sardonicus.tobaccocellar.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.home.isMetricLocale
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import com.sardonicus.tobaccocellar.ui.utilities.EventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.round

class StatsViewModel(
    private val itemsRepository: ItemsRepository,
    private val filterViewModel: FilterViewModel,
    private val preferencesRepo: PreferencesRepo
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
                    totalQuantity = it.sumOf { it.items.quantity },
                    estimatedWeight = calculateTotal(it, preferencesRepo.quantityOption.first()),
                    totalOpened = if (it.map { it.tins }.isEmpty() || it.map { it.tins }.all { it.all { it.openDate == null } }) 0
                        else it.flatMap { it.tins }.count { it.openDate != null && it.finished == false },
                    totalZeroQuantity = it.count { it.items.quantity == 0 },

                    totalByType = it.groupingBy {
                        if (it.items.type.isBlank()) "Unassigned" else it.items.type }.eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .associate { it.key to it.value }
                        .let{
                            val mutableMap = it.toMutableMap()
                            val unassignedEntry = mutableMap.remove("Unassigned")
                            if (unassignedEntry != null) {
                                mutableMap["Unassigned"] = unassignedEntry
                            }
                            mutableMap.toMap()
                        },
                    totalBySubgenre = it.groupingBy {
                        if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .associate { it.key to it.value }
                        .let{
                            val mutableMap = it.toMutableMap()
                            val unassignedEntry = mutableMap.remove("Unassigned")
                            if (unassignedEntry != null) {
                                mutableMap["Unassigned"] = unassignedEntry
                            }
                            mutableMap.toMap()
                        },
                    totalByCut = it.groupingBy {
                        if (it.items.cut.isBlank()) "Unassigned" else it.items.cut }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .associate { it.key to it.value }
                        .let {
                            val mutableMap = it.toMutableMap()
                            val unassignedEntry = mutableMap.remove("Unassigned")
                            if (unassignedEntry != null) {
                                mutableMap["Unassigned"] = unassignedEntry
                            }
                            mutableMap.toMap()
                        },
                    totalByContainer = it.flatMap { it.tins }.groupingBy {
                        if (it.container.isBlank()) "Unassigned" else it.container }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .associate { it.key to it.value }
                        .let {
                            val mutableMap = it.toMutableMap()
                            val unassignedEntry = mutableMap.remove("Unassigned")
                            if (unassignedEntry != null) {
                                mutableMap["Unassigned"] = unassignedEntry
                            }
                            mutableMap.toMap()
                        },

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
            filterViewModel.selectedFlavoring,
            filterViewModel.flavorMatching,
            filterViewModel.selectedSubgenre,
            filterViewModel.selectedCut,
            filterViewModel.selectedProduction,
            filterViewModel.selectedOutOfProduction,
            filterViewModel.selectedHasTins,
            filterViewModel.selectedNoTins,
            filterViewModel.selectedContainer,
            filterViewModel.selectedOpened,
            filterViewModel.selectedUnopened,
            filterViewModel.selectedFinished,
            filterViewModel.selectedUnfinished
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
            val compMatching = values[14] as String
            val flavoring = values[15] as List<String>
            val flavorMatching = values[16] as String
            val subgenres = values[17] as List<String>
            val cuts = values[18] as List<String>
            val production = values[19] as Boolean
            val outOfProduction = values[20] as Boolean
            val hasTins = values[21] as Boolean
            val noTins = values[22] as Boolean
            val container = values[23] as List<String>
            val opened = values[24] as Boolean
            val unopened = values[25] as Boolean
            val finished = values[26] as Boolean
            val unfinished = values[27] as Boolean

            val filteredItems = allItems.filter { items ->
                val componentMatching = when (compMatching) {
                    "All" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components)))
                    "Only" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                    else -> ((components.isEmpty() && !components.contains("(None Assigned)")) || ((components.contains("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.any { components.contains(it) })))
                }
                val flavorMatching = when (flavorMatching) {
                    "All" -> ((flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.containsAll(flavoring)))
                    "Only" -> ((flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.containsAll(flavoring) && items.flavoring.size == flavoring.size))
                    else -> ((flavoring.isEmpty() && !flavoring.contains("(None Assigned)")) || ((flavoring.contains("(None Assigned)") && items.flavoring.isEmpty()) || (items.flavoring.map { it.flavoringName }.any { flavoring.contains(it) })))
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
                        flavorMatching &&
                        ((subgenres.isEmpty() && !subgenres.contains("(Unassigned)")) || ((subgenres.contains("(Unassigned)") && items.items.subGenre.isBlank()) || subgenres.contains(items.items.subGenre))) &&
                        ((cuts.isEmpty() && !cuts.contains("(Unassigned)")) || ((cuts.contains("(Unassigned)") && items.items.cut.isBlank()) || cuts.contains(items.items.cut))) &&
                        (!production || items.items.inProduction) &&
                        (!outOfProduction || !items.items.inProduction) &&
                        (!hasTins || items.tins.isNotEmpty()) &&
                        (!noTins || items.tins.isEmpty()) &&
                        ((container.isEmpty() && !container.contains("(Unassigned)")) || ((container.contains("(Unassigned)") && items.tins.any { it.container.isBlank() }) || (items.tins.map { it.container }.any { container.contains(it) }) )) &&
                        (!opened || items.tins.any { it.openDate != null && (it.openDate < System.currentTimeMillis() && !it.finished) }) &&
                        (!unopened || items.tins.any { it.openDate == null || it.openDate > System.currentTimeMillis() }) &&
                        (!finished || items.tins.any { it.finished }) &&
                        (!unfinished || items.tins.any { !it.finished && it.openDate != null && it.openDate < System.currentTimeMillis() })
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
                        unassignedCount = unassignedCount,
                        totalQuantity = filteredItems.sumOf { it.items.quantity },
                        estimatedWeight = calculateTotal(filteredItems, preferencesRepo.quantityOption.first()),
                        totalOpened = if (filteredItems.map { it.tins }.isEmpty() || filteredItems.map { it.tins }.all { it.all { it.openDate == null } }) 0
                            else filteredItems.flatMap { it.tins }.count { it.openDate != null && it.finished == false },
                        totalZeroQuantity = filteredItems.count { it.items.quantity == 0 },

                        totalByType = allItems.groupingBy {
                            if (it.items.type.isBlank()) "Unassigned" else it.items.type }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .associate {
                                it.key to (filteredItems.groupingBy {
                                    if (it.items.type.isBlank()) "Unassigned" else it.items.type
                                }.eachCount()[it.key] ?: 0)
                            }.let {
                                val mutableMap = it.toMutableMap()
                                val unassignedEntry = mutableMap.remove("Unassigned")
                                if (unassignedEntry != null) {
                                    mutableMap["Unassigned"] = unassignedEntry
                                }
                                mutableMap.toMap()
                            },
                        totalBySubgenre = allItems.groupingBy {
                            if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .associate {
                                it.key to (filteredItems.groupingBy {
                                    if (it.items.subGenre.isBlank()) "Unassigned" else it.items.subGenre
                                }.eachCount()[it.key] ?: 0)
                            }.let {
                                val mutableMap = it.toMutableMap()
                                val unassignedEntry = mutableMap.remove("Unassigned")
                                if (unassignedEntry != null) {
                                    mutableMap["Unassigned"] = unassignedEntry
                                }
                                mutableMap.toMap()
                            },
                        totalByCut = allItems.groupingBy {
                            if (it.items.cut.isBlank()) "Unassigned" else it.items.cut }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .associate {
                                it.key to (filteredItems.groupingBy {
                                    if (it.items.cut.isBlank()) "Unassigned" else it.items.cut
                                }.eachCount()[it.key] ?: 0)
                            }.let {
                                val mutableMap = it.toMutableMap()
                                val unassignedEntry = mutableMap.remove("Unassigned")
                                if (unassignedEntry != null) {
                                    mutableMap["Unassigned"] = unassignedEntry
                                }
                                mutableMap.toMap()
                            },
                        totalByContainer = allItems.flatMap { it.tins }.groupingBy {
                            if (it.container.isBlank()) "Unassigned" else it.container }
                            .eachCount()
                            .entries
                            .sortedByDescending { it.value }
                            .associate {
                                it.key to (filteredItems.flatMap { it.tins }.groupingBy {
                                    if (it.container.isBlank()) "Unassigned" else it.container
                                }.eachCount()[it.key] ?: 0)
                            }.let {
                                val mutableMap = it.toMutableMap()
                                val unassignedEntry = mutableMap.remove("Unassigned")
                                if (unassignedEntry != null) {
                                    mutableMap["Unassigned"] = unassignedEntry
                                }
                                mutableMap.toMap()
                            },


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

    private suspend fun calculateTotal(items: List<ItemsComponentsAndTins>, quantityOption: QuantityOption): String {
        val ozRate = preferencesRepo.tinOzConversionRate.first()
        val gramsRate = preferencesRepo.tinGramsConversionRate.first()

        val quantityRemap = when (quantityOption) {
            QuantityOption.TINS -> {
                val isMetric = isMetricLocale()
                if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
            }
            else -> quantityOption
        }

        val sum =
            when (quantityRemap) {
                QuantityOption.OUNCES -> {
                    items.sumOf { it.items.quantity } * ozRate
                }
                QuantityOption.GRAMS -> {
                    items.sumOf { it.items.quantity } * gramsRate
                }
                else -> null
            }

        val formattedSum =
            when (quantityRemap) {
                QuantityOption.OUNCES -> {
                    if (sum != null) {
                        if (sum >= 16.00) {
                            val pounds = sum / 16
                            val rounded = round(pounds * 100) / 100
                            val decimal = String.format("%.2f", rounded)
                            when {
                                decimal.endsWith("00") -> {
                                    decimal.substringBefore(".")
                                }

                                decimal.endsWith("0") -> {
                                    decimal.substring(0, decimal.length - 1)
                                }

                                else -> decimal
                            } + " lbs"
                        } else {
                            val rounded = round(sum * 100) / 100
                            val decimal = String.format("%.2f", rounded)
                            when {
                                decimal.endsWith("00") -> {
                                    decimal.substringBefore(".")
                                }

                                decimal.endsWith("0") -> {
                                    decimal.substring(0, decimal.length - 1)
                                }

                                else -> decimal
                            } + " oz"
                        }
                    } else {
                        null
                    }
                }

                QuantityOption.GRAMS -> {
                    if (sum != null) {
                        val rounded = round(sum * 100) / 100
                        val decimal = String.format("%.2f", rounded)
                        when {
                            decimal.endsWith("00") -> {
                                decimal.substringBefore(".")
                            }

                            decimal.endsWith("0") -> {
                                decimal.substring(0, decimal.length - 1)
                            }

                            else -> decimal
                        } + " g"
                    } else {
                        null
                    }
                }

                else -> {
                    null
                }
            }

        return formattedSum?.toString() ?: ""
    }

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
    val totalQuantity: Int = 0,
    val estimatedWeight: String = "",
    val totalZeroQuantity: Int = 0,
    val totalOpened: Int = 0,

    val totalByType: Map<String, Int> = emptyMap(),
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
    val unassignedCount: Int = 0,
    val totalQuantity: Int = 0,
    val estimatedWeight: String = "",
    val totalZeroQuantity: Int = 0,
    val totalOpened: Int = 0,

    val totalByType: Map<String, Int> = emptyMap(),
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