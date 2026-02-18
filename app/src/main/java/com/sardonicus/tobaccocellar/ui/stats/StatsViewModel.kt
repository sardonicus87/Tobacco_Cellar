package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.Components
import com.sardonicus.tobaccocellar.data.Flavoring
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.details.isMetricLocale
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.floor

class StatsViewModel(
    filterViewModel: FilterViewModel,
    private val preferencesRepo: PreferencesRepo
): ViewModel() {

    private val _selectionFocused = MutableStateFlow(false)
    val selectionFocused = _selectionFocused.asStateFlow()

    private val _selectionKey = MutableStateFlow(0)
    val selectionKey = _selectionKey.asStateFlow()

    private val _showLoading = MutableStateFlow(true)
    val showLoading = _showLoading.asStateFlow()
    private fun updateLoading(state: Boolean) { _showLoading.value = state }

    /** Raw stats */
    val rawStats: StateFlow<RawStats> = combine (
        filterViewModel.everythingFlow,
        filterViewModel.ratingsExist,
        preferencesRepo.quantityOption
    ) { it, ratingsExist, quantityOption ->
        val averageRating = if (ratingsExist) { (it.sumOf { it.items.rating ?: 0.0 }) / (it.count { it.items.rating != null }) } else null

        RawStats(
            blendsCount = it.size,
            brandsCount = it.groupingBy { it.items.brand }.eachCount().size,
            averageRating = formatDecimal(averageRating),
            favoriteCount = it.count { it.items.favorite },
            dislikedCount = it.count { it.items.disliked },
            totalQuantity = it.sumOf { it.items.quantity },
            estimatedWeight = calculateTotal(it, it.flatMap { items -> items.tins.filter { !it.finished } }, quantityOption),
            totalZeroQuantity = it.count { it.items.quantity == 0 },
            totalOpened = if (it.map { it.tins }.isEmpty() || it.map { it.tins }.all { tins -> tins.all { it.openDate == null } }) null
                else it.flatMap { it.tins }.count { it.openDate != null && !it.finished },

            totalByType = it.groupingBy { it.items.type.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
                .let{ remapUnassigned(it, "Unassigned") },
            totalBySubgenre = it.groupingBy {
                it.items.subGenre.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
                .let{ remapUnassigned(it, "Unassigned") },
            totalByCut = it.groupingBy {
                it.items.cut.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
                .let { remapUnassigned(it, "Unassigned") },
            totalByComponent = it.flatMap { it.components
                .ifEmpty { listOf(Components(componentName = "None Assigned")) } }
                .groupingBy { it.componentName }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
                .let { remapUnassigned(it, "None Assigned") },
            totalByFlavoring = it.flatMap { it.flavoring
                .ifEmpty { listOf(Flavoring(flavoringName = "None Assigned")) } }
                .groupingBy { it.flavoringName }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
                .let { remapUnassigned(it, "None Assigned") },
            totalByContainer = it.flatMap { it.tins }
                .filter { !it.finished }
                .groupingBy { it.container.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value }
                .let { remapUnassigned(it, "Unassigned") },

            rawLoading = false
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RawStats(rawLoading = true)
        )


    /** Filtered stats */
    val filteredStats: StateFlow<FilteredStats> = combine(
        filterViewModel.everythingFlow,
        filterViewModel.unifiedFilteredItems,
        filterViewModel.unifiedFilteredTins,
        filterViewModel.ratingsExist,
        preferencesRepo.quantityOption
    ) { allItems, filteredItems, filteredTins, ratingsExist, quantityOption ->
        val tinIds = filteredTins.map { it.tinId }
        val relevantTins = filteredItems.flatMap { it.tins }.filter { it.tinId in tinIds && !it.finished }
        val filteredRatingsExist = filteredItems.any { it.items.rating != null }
        val averageRating =
            if (ratingsExist && filteredRatingsExist) { (filteredItems.sumOf { it.items.rating ?: 0.0 }) / (filteredItems.count { it.items.rating != null }) }
            else null

        FilteredStats(
            blendsCount = filteredItems.size,
            brandsCount = filteredItems.groupingBy { it.items.brand }.eachCount().size,
            averageRating = formatDecimal(averageRating).ifBlank { "-.-" },
            favoriteCount = filteredItems.count { it.items.favorite },
            dislikedCount = filteredItems.count { it.items.disliked },
            totalQuantity = filteredItems.sumOf { it.items.quantity },
            estimatedWeight = calculateTotal(filteredItems, filteredItems.flatMap { items -> items.tins.filter { !it.finished && it in filteredTins } }, quantityOption),
            totalZeroQuantity = filteredItems.count { it.items.quantity == 0 },
            totalOpened = relevantTins.count { it.openDate != null && !it.finished },

            totalByType = allItems.groupingBy {
                it.items.type.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { entry ->
                    entry.key to (filteredItems.groupingBy {
                        it.items.type.ifBlank { "Unassigned" }
                    }.eachCount()[entry.key] ?: 0) }
                .let { remapUnassigned(it, "Unassigned") },
            totalBySubgenre = allItems.groupingBy {
                it.items.subGenre.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { entry ->
                    entry.key to (filteredItems.groupingBy {
                        it.items.subGenre.ifBlank { "Unassigned" }
                    }.eachCount()[entry.key] ?: 0)
                }
                .let { remapUnassigned(it, "Unassigned") },
            totalByCut = allItems.groupingBy {
                it.items.cut.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { entry ->
                    entry.key to (filteredItems.groupingBy {
                        it.items.cut.ifBlank { "Unassigned" }
                    }.eachCount()[entry.key] ?: 0)
                }
                .let { remapUnassigned(it, "Unassigned") },
            totalByComponent = allItems.flatMap { it.components.ifEmpty {
                listOf(Components(componentName = "None Assigned")) } }
                .groupingBy { it.componentName }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { entry ->
                    entry.key to (filteredItems.flatMap {
                        it.components.ifEmpty { listOf(Components(componentName = "None Assigned")) } }
                        .groupingBy { it.componentName }.eachCount()[entry.key] ?: 0)
                }
                .let { remapUnassigned(it, "None Assigned") },
            totalByFlavoring = allItems.flatMap { it.flavoring.ifEmpty{
                listOf(Flavoring(flavoringName = "None Assigned")) } }
                .groupingBy { it.flavoringName }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { entry ->
                    entry.key to (filteredItems.flatMap {
                        it.flavoring.ifEmpty { listOf(Flavoring(flavoringName = "None Assigned")) } }
                        .groupingBy { it.flavoringName }.eachCount()[entry.key] ?: 0)
                }
                .let { remapUnassigned(it, "None Assigned") },
            totalByContainer = allItems.flatMap { it.tins }
                .filter { !it.finished }
                .groupingBy { it.container.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { entry ->
                    entry.key to (relevantTins.groupingBy {
                        it.container.ifBlank { "Unassigned" }
                    }.eachCount()[entry.key] ?: 0)
                }
                .let { remapUnassigned(it, "Unassigned") },


            brandsByEntries = filteredItems
                .groupingBy { it.items.brand }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .let { reduceToTen(it) },
            brandsByQuantity = filteredItems
                .filter { it.items.quantity > 0 }
                .groupingBy { it.items.brand }
                .fold(0) { acc, item -> acc + item.items.quantity }
                .entries
                .sortedByDescending { it.value }
                .let { reduceToTen(it) },
            typesByEntries = filteredItems.groupingBy {
                it.items.type.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value },
            typesByQuantity = filteredItems
                .filter { it.items.quantity > 0 }
                .groupingBy { it.items.type.ifBlank { "Unassigned" } }
                .fold(0) { acc, item -> acc + item.items.quantity }
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value },
            ratingsDistribution = RatingsDistribution(
                distribution = filteredItems.mapNotNull { it.items.rating }
                    .groupingBy {
                        when {
                            it < 5.0 -> floor(it * 2) / 2.0
                            else -> 5.0
                        }
                    }
                    .eachCount(),
                unratedCount = filteredItems.map { it.items.rating }.count { it == null }
            ),
            favDisByEntries = filteredItems
                .groupingBy { if (it.items.favorite) "Favorite" else if (it.items.disliked) "Disliked" else "Neutral" }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .associate { it.key to it.value },
            subgenresByEntries = filteredItems
                .groupingBy { it.items.subGenre.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .let { reduceToTen(it) },
            subgenresByQuantity = filteredItems
                .filter { it.items.quantity > 0 }
                .groupingBy { it.items.subGenre.ifBlank { "Unassigned" } }
                .fold(0) { acc, item -> acc + item.items.quantity }
                .entries
                .sortedByDescending { it.value }
                .let { reduceToTen(it) },
            cutsByEntries = filteredItems
                .groupingBy { it.items.cut.ifBlank { "Unassigned" } }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .let { reduceToTen(it) },
            cutsByQuantity = filteredItems
                .filter { it.items.quantity > 0 }
                .groupingBy { it.items.cut.ifBlank { "Unassigned" } }
                .fold(0) { acc, item -> acc + item.items.quantity }
                .entries
                .sortedByDescending { it.value }
                .let { reduceToTen(it) },

            filteredLoading = false,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FilteredStats(filteredLoading = true)
        )


    val availableSections: StateFlow<AvailableSections> =
        combine(rawStats, filteredStats) { raw, filtered ->
            val type = raw.totalByType.any { it.key != "Unassigned" }
            val subgenre = raw.totalBySubgenre.any { it.key != "Unassigned" }
            val cut = raw.totalByCut.any { it.key != "Unassigned" }
            val component = raw.totalByComponent.any { it.key != "None Assigned" }
            val flavoring = raw.totalByFlavoring.any { it.key != "None Assigned" }
            val container = raw.totalByContainer.any { it.key != "Unassigned" }

            AvailableSections(
                anyAvailable = subgenre || cut || component || flavoring || container,
                type = type,
                subgenre = subgenre,
                cut = cut,
                component = component,
                flavoring = flavoring,
                container = container,
                available = listOfNotNull(
                    if (subgenre) { Triple("Subgenre", raw.totalBySubgenre, filtered.totalBySubgenre) } else null,
                    if (cut) { Triple("Cut", raw.totalByCut, filtered.totalByCut) } else null,
                    if (component) { Triple("Component", raw.totalByComponent, filtered.totalByComponent) } else null,
                    if (flavoring) { Triple("Flavoring", raw.totalByFlavoring, filtered.totalByFlavoring) } else null,
                    if (container) { Triple("Container", raw.totalByContainer, filtered.totalByContainer) } else null
                )
            )
        }
            .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AvailableSections()
        )

    init {
        viewModelScope.launch {
            combine(rawStats, filteredStats) { raw, filtered ->
                raw.rawLoading && filtered.filteredLoading
            }.collect {
                if (it) { delay(10) }
                updateLoading(it)
            }
        }
    }


    /** Helper functions **/
    private fun reduceToTen(entries:  List<Map.Entry<String, Int>>): Map<String, Int> {
        return if (entries.size > 10) {
            val topNine = entries.take(9).associate { it.key to it.value }
            val otherCount = entries.drop(9).sumOf { it.value }
            topNine + ("(Other)" to otherCount)
        } else {
            entries.associate { it.key to it.value }
        }
    }

    private fun remapUnassigned(map: Map<String, Int>, unassignedKey: String): Map<String, Int> {
        val mutableMap = map.toMutableMap()
        val unassignedValue = mutableMap.remove(unassignedKey)
        if (unassignedValue != null) {
            mutableMap[unassignedKey] = unassignedValue
        }
        return mutableMap
    }

    private suspend fun calculateTotal(items: List<ItemsComponentsAndTins>, tins: List<Tins>, quantityOption: QuantityOption): String {
        val ozRate = preferencesRepo.tinOzConversionRate.first()
        val gramsRate = preferencesRepo.tinGramsConversionRate.first()
        val quantityRemap = when (quantityOption) {
            QuantityOption.TINS -> {
                val isMetric = isMetricLocale()
                if (isMetric) QuantityOption.GRAMS else QuantityOption.OUNCES
            }
            else -> quantityOption
        }
        val itemTinsMap = tins.groupBy { it.itemsId }

        var totalWeight = 0.0

        for (item in items) {
            val item = item.items
            val itemTins = itemTinsMap[item.id] ?: emptyList()

            var itemWeight = 0.0

            if (itemTins.isNotEmpty() && itemTins.all { it.unit.isNotBlank() }) {
                for (tin in itemTins) {
                    itemWeight += when (quantityRemap) {
                        QuantityOption.OUNCES -> {
                            when (tin.unit) {
                                "oz" -> tin.tinQuantity
                                "lbs" -> tin.tinQuantity * 16
                                "grams" -> tin.tinQuantity / 28.3495
                                else -> 0.0
                            }
                        }
                        QuantityOption.GRAMS -> {
                            when (tin.unit) {
                                "oz" -> tin.tinQuantity * 28.3495
                                "lbs" -> tin.tinQuantity * 453.592
                                "grams" -> tin.tinQuantity
                                else -> 0.0
                            }
                        }
                        else -> 0.0
                    }
                }
            } else {
                itemWeight +=
                    when (quantityRemap) {
                        QuantityOption.OUNCES -> { item.quantity * ozRate }
                        QuantityOption.GRAMS -> { item.quantity * gramsRate }
                        else -> 0.0
                    }
            }
            totalWeight += itemWeight
        }

        val sum = totalWeight

        val formattedSum =
            when (quantityRemap) {
                QuantityOption.OUNCES -> {
                    if (sum >= 16.00) { formatDecimal((sum / 16)) + " lbs" }
                    else { formatDecimal(sum) + " oz" }
                }
                QuantityOption.GRAMS -> { formatDecimal(sum) + " g" }
                else -> null
            }
        return formattedSum ?: ""
    }


    /** UI functions/vals **/
    val expanded = MutableStateFlow(false)

    fun updateExpanded(newExpanded: Boolean) {
        expanded.value = newExpanded
    }

    private val _showValue = MutableStateFlow(false)
    val showValue = _showValue.asStateFlow()

    fun onShowValue() { _showValue.value = !_showValue.value }

    fun updateFocused(focused: Boolean) {
        _selectionFocused.update { focused }
    }

    fun resetSelection() {
        _selectionKey.update { it + 1 }
        updateFocused(false)
    }

}


@Stable
data class RawStats(
    val rawLoading: Boolean = false,

    val blendsCount: Int = 0,
    val brandsCount: Int = 0,
    val averageRating: String = "",
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalQuantity: Int = 0,
    val estimatedWeight: String = "",
    val totalZeroQuantity: Int = 0,
    val totalOpened: Int? = null,

    val totalByType: Map<String, Int> = emptyMap(),
    val totalBySubgenre: Map<String, Int> = emptyMap(),
    val totalByCut: Map<String, Int> = emptyMap(),
    val totalByComponent: Map<String, Int> = emptyMap(),
    val totalByFlavoring: Map<String, Int> = emptyMap(),
    val totalByContainer: Map<String, Int> = emptyMap(),
)

@Stable
data class FilteredStats(
    val filteredLoading: Boolean = false,

    val blendsCount: Int = 0,
    val brandsCount: Int = 0,
    val averageRating: String = "",
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalQuantity: Int = 0,
    val estimatedWeight: String = "",
    val totalZeroQuantity: Int = 0,
    val totalOpened: Int = 0,

    val totalByType: Map<String, Int> = emptyMap(),
    val totalBySubgenre: Map<String, Int> = emptyMap(),
    val totalByCut: Map<String, Int> = emptyMap(),
    val totalByComponent: Map<String, Int> = emptyMap(),
    val totalByFlavoring: Map<String, Int> = emptyMap(),
    val totalByContainer: Map<String, Int> = emptyMap(),

    val brandsByEntries: Map<String, Int> = emptyMap(),
    val brandsByQuantity: Map<String, Int> = emptyMap(),
    val typesByEntries: Map<String, Int> = emptyMap(),
    val typesByQuantity: Map<String, Int> = emptyMap(),
    val ratingsDistribution: RatingsDistribution = RatingsDistribution(),
    val favDisByEntries: Map<String, Int> = emptyMap(),
    val subgenresByEntries: Map<String, Int> = emptyMap(),
    val subgenresByQuantity: Map<String, Int> = emptyMap(),
    val cutsByEntries: Map<String, Int> = emptyMap(),
    val cutsByQuantity: Map<String, Int> = emptyMap(),
)

@Stable
data class AvailableSections(
    val anyAvailable: Boolean = false,
    val type: Boolean = false,
    val subgenre: Boolean = false,
    val cut: Boolean = false,
    val component: Boolean = false,
    val flavoring: Boolean = false,
    val container: Boolean = false,
    val available: List<Triple<String, Map<String, Int>, Map<String, Int>>> = emptyList()
)

@Stable
data class RatingsDistribution(
    val distribution: Map<Double, Int> = emptyMap(),
    val unratedCount: Int = 0
)