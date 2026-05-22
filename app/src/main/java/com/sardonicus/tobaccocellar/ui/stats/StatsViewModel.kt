package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import com.sardonicus.tobaccocellar.ui.blendDetails.isMetricLocale
import com.sardonicus.tobaccocellar.ui.settings.QuantityOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.floor

class StatsViewModel(
    filterViewModel: FilterViewModel,
    preferencesRepo: PreferencesRepo
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
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate
    ) { allItems, ratingsExist, quantityOption, ozRate, gramsRate ->

        val brandSet = mutableSetOf<String>()
        val typeMap = mutableMapOf<String, Int>()
        val subgenreMap = mutableMapOf<String, Int>()
        val cutMap = mutableMapOf<String, Int>()
        val componentMap = mutableMapOf<String, Int>()
        val flavoringMap = mutableMapOf<String, Int>()
        val containerMap = mutableMapOf<String, Int>()

        var favoriteCount = 0
        var dislikedCount = 0
        var totalQuantity = 0
        var totalZeroQuantity = 0
        var ratedCount = 0
        var ratingSum = 0.0
        var totalOpened = 0
        var hasTins = false

        val quantityRemap = when (quantityOption) {
            QuantityOption.TINS -> if (isMetricLocale()) QuantityOption.GRAMS else QuantityOption.OUNCES
            else -> quantityOption
        }
        var weightAccumulator = 0.0

        for (fullItem in allItems) {
            val item = fullItem.items
            brandSet.add(item.brand)

            if (item.favorite) favoriteCount++
            if (item.disliked) dislikedCount++
            totalQuantity += item.quantity
            if (item.quantity == 0) totalZeroQuantity++
            if (item.rating != null) {
                ratedCount++
                ratingSum += item.rating
            }

            typeMap.increment(item.type.ifBlank { "Unassigned" })
            subgenreMap.increment(item.subGenre.ifBlank { "Unassigned" })
            cutMap.increment(item.cut.ifBlank { "Unassigned" })

            if (fullItem.components.isEmpty()) componentMap.increment("None Assigned")
            else fullItem.components.forEach { componentMap.increment(it.componentName) }

            if (fullItem.flavoring.isEmpty()) flavoringMap.increment("None Assigned")
            else fullItem.flavoring.forEach { flavoringMap.increment(it.flavoringName) }

            // Tins
            var itemTinsWeight = 0.0
            var unfinishedCount = 0
            var allValid = true

            if (fullItem.tins.isNotEmpty()) hasTins = true
            for (tin in fullItem.tins) {
                if (!tin.finished) {
                    unfinishedCount++
                    containerMap.increment(tin.container.ifBlank { "Unassigned" })
                    if (tin.openDate != null) totalOpened++

                    if (tin.unit.isBlank()) allValid = false
                    itemTinsWeight += convertWeight(tin.tinQuantity, tin.unit, quantityRemap)
                }
            }

            weightAccumulator += if (unfinishedCount > 0 && allValid) {
                itemTinsWeight
            } else {
                when (quantityRemap) {
                    QuantityOption.OUNCES -> item.quantity * ozRate
                    QuantityOption.GRAMS -> item.quantity * gramsRate
                    else -> 0.0
                }
            }
        }

        RawStats(
            blendsCount = allItems.size,
            brandsCount = brandSet.size,
            averageRating = if (ratingsExist) formatDecimal(ratingSum / ratedCount) else "",
            favoriteCount = favoriteCount,
            dislikedCount = dislikedCount,
            totalQuantity = totalQuantity,
            estimatedWeight = formatWeight(quantityRemap, weightAccumulator),
            totalZeroQuantity = totalZeroQuantity,
            totalOpened = if (!hasTins) null else totalOpened,

            totalByType = remapUnassigned(typeMap.sortByValue(), "Unassigned"),
            totalBySubgenre = remapUnassigned(subgenreMap.sortByValue(), "Unassigned"),
            totalByCut = remapUnassigned(cutMap.sortByValue(), "Unassigned"),
            totalByComponent = remapUnassigned(componentMap.sortByValue(), "None Assigned"),
            totalByFlavoring = remapUnassigned(flavoringMap.sortByValue(), "None Assigned"),
            totalByContainer = remapUnassigned(containerMap.sortByValue(), "Unassigned"),

            rawLoading = false
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RawStats(rawLoading = true)
        )


    /** Filtered stats */
    @Suppress("UNCHECKED_CAST")
    val filteredStats: StateFlow<FilteredStats> = combine(
        filterViewModel.everythingFlow,
        filterViewModel.unifiedFilteredItems,
        filterViewModel.unifiedFilteredTins,
        filterViewModel.ratingsExist,
        preferencesRepo.quantityOption,
        preferencesRepo.tinOzConversionRate,
        preferencesRepo.tinGramsConversionRate
    ) { array ->
        val allItems = array[0] as List<ItemsComponentsAndTins>
        val filteredItems = array[1] as List<ItemsComponentsAndTins>
        val filteredTins = array[2] as List<Tins>
        val ratingsExist = array[3] as Boolean
        val quantityOption = array[4] as QuantityOption
        val ozRate = array[5] as Double
        val gramsRate = array[6] as Double

        val filteredIds = filteredItems.map { it.items.id }.toSet()
        val filteredTinIds = filteredTins.map { it.tinId }.toSet()

        val typeMap = mutableMapOf<String, Int>()
        val typeMapFiltered = mutableMapOf<String, Int>()
        val subgenreMap = mutableMapOf<String, Int>()
        val subgenreMapFiltered = mutableMapOf<String, Int>()
        val cutMap = mutableMapOf<String, Int>()
        val cutMapFiltered = mutableMapOf<String, Int>()
        val componentMap = mutableMapOf<String, Int>()
        val componentMapFiltered = mutableMapOf<String, Int>()
        val flavoringMap = mutableMapOf<String, Int>()
        val flavoringMapFiltered = mutableMapOf<String, Int>()
        val containerMap = mutableMapOf<String, Int>()
        val containerMapFiltered = mutableMapOf<String, Int>()

        val brandsByEntries = mutableMapOf<String, Int>()
        val brandsByQuantity = mutableMapOf<String, Int>()
        val typesByEntries = mutableMapOf<String, Int>()
        val typesByQuantity = mutableMapOf<String, Int>()
        val subgenresByEntries = mutableMapOf<String, Int>()
        val subgenresByQuantity = mutableMapOf<String, Int>()
        val cutsByEntries = mutableMapOf<String, Int>()
        val cutsByQuantity = mutableMapOf<String, Int>()

        val brandsRatingSum = mutableMapOf<String, Double>()
        val brandsRatingCount = mutableMapOf<String, Int>()

        val ratingsDistribution = mutableMapOf<Double, Int>()
        var unratedCount = 0

        var favoriteCount = 0
        var dislikedCount = 0
        var totalQuantity = 0
        var totalZeroQuantity = 0
        var ratedCount = 0
        var ratingSum = 0.0
        var totalOpened = 0

        val relevantTinsWeight = mutableListOf<Tins>()
        val quantityRemap = when (quantityOption) {
            QuantityOption.TINS -> if (isMetricLocale()) QuantityOption.GRAMS else QuantityOption.OUNCES
            else -> quantityOption
        }
        var weightAccumulator = 0.0

        for (fullItem in allItems) {
            val item = fullItem.items
            val relevant = item.id in filteredIds

            val type = item.type.ifBlank { "Unassigned" }
            val subgenre = item.subGenre.ifBlank { "Unassigned" }
            val cut = item.cut.ifBlank { "Unassigned" }

            typeMap.increment(type)
            subgenreMap.increment(subgenre)
            cutMap.increment(cut)

            if (fullItem.components.isEmpty()) componentMap.increment("None Assigned")
            else fullItem.components.forEach { componentMap.increment(it.componentName) }

            if (fullItem.flavoring.isEmpty()) flavoringMap.increment("None Assigned")
            else fullItem.flavoring.forEach { flavoringMap.increment(it.flavoringName) }

            fullItem.tins.forEach { if (!it.finished) containerMap.increment(it.container.ifBlank { "Unassigned" }) }

            if (relevant) {
                typeMapFiltered.increment(type)
                subgenreMapFiltered.increment(subgenre)
                cutMapFiltered.increment(cut)

                if (fullItem.components.isEmpty()) componentMapFiltered.increment("None Assigned")
                else fullItem.components.forEach { componentMapFiltered.increment(it.componentName) }

                if (fullItem.flavoring.isEmpty()) flavoringMapFiltered.increment("None Assigned")
                else fullItem.flavoring.forEach { flavoringMapFiltered.increment(it.flavoringName) }

                if (item.favorite) favoriteCount++
                if (item.disliked) dislikedCount++
                totalQuantity += item.quantity
                if (item.quantity == 0) totalZeroQuantity++
                if (item.rating != null) {
                    ratedCount++
                    ratingSum += item.rating
                    ratingsDistribution.incrementDist(floor(item.rating * 2) / 2.0)
                    brandsRatingSum[item.brand] = (brandsRatingSum[item.brand] ?: 0.0) + item.rating
                    brandsRatingCount[item.brand] = (brandsRatingCount[item.brand] ?: 0) + 1
                } else { unratedCount++ }

                brandsByEntries.increment(item.brand)
                typesByEntries.increment(item.type)
                subgenresByEntries.increment(item.subGenre)
                cutsByEntries.increment(item.cut)

                if (item.quantity > 0) {
                    brandsByQuantity[item.brand] = (brandsByQuantity[item.brand] ?: 0) + item.quantity
                    typesByQuantity[item.type] = (typesByQuantity[item.type] ?: 0) + item.quantity
                    subgenresByQuantity[item.subGenre] = (subgenresByQuantity[item.subGenre] ?: 0) + item.quantity
                    cutsByQuantity[item.cut] = (cutsByQuantity[item.cut] ?: 0) + item.quantity
                }

                // Tins
                var itemTinsWeight = 0.0
                var unfinishedTinsCount = 0
                var allValid = true

                fullItem.tins.forEach { tin ->
                    if (tin.tinId in filteredTinIds && !tin.finished) {
                        unfinishedTinsCount++
                        relevantTinsWeight.add(tin)
                        containerMapFiltered.increment(tin.container.ifBlank { "Unassigned" })
                        if (tin.openDate != null) totalOpened++
                        if (tin.unit.isBlank()) allValid = false
                        itemTinsWeight += convertWeight(tin.tinQuantity, tin.unit, quantityRemap)
                    }
                }

                weightAccumulator += if (unfinishedTinsCount > 0 && allValid) {
                    itemTinsWeight
                } else {
                    when (quantityRemap) {
                        QuantityOption.OUNCES -> item.quantity * ozRate
                        QuantityOption.GRAMS -> item.quantity * gramsRate
                        else -> 0.0
                    }
                }
            }
        }

        val globalAvg = if (ratedCount > 0) ratingSum / ratedCount else 0.0

        FilteredStats(
            blendsCount = filteredItems.size,
            brandsCount = brandsByEntries.size,
            averageRating = if (ratingsExist && ratedCount > 0) formatDecimal(ratingSum / ratedCount) else "-.-",
            favoriteCount = favoriteCount,
            dislikedCount = dislikedCount,
            totalQuantity = totalQuantity,
            estimatedWeight = formatWeight(quantityRemap, weightAccumulator),
            totalZeroQuantity = totalZeroQuantity,
            totalOpened = totalOpened,

            totalByType = typeMap.buildComp(typeMapFiltered, "Unassigned"),
            totalBySubgenre = subgenreMap.buildComp(subgenreMapFiltered, "Unassigned"),
            totalByCut = cutMap.buildComp(cutMapFiltered, "Unassigned"),
            totalByComponent = componentMap.buildComp(componentMapFiltered, "None Assigned"),
            totalByFlavoring = flavoringMap.buildComp(flavoringMapFiltered, "None Assigned"),
            totalByContainer = containerMap.buildComp(containerMapFiltered, "Unassigned"),


            brandsByEntries = brandsByEntries.reduceToTen(),
            brandsByQuantity = brandsByQuantity.reduceToTen(),
            brandsByRating = brandsRatingCount.mapValues { (brand, count) ->
                val avg = brandsRatingSum[brand]!! / count
                val m = 2
                val weighted = ((count * avg) + (m * globalAvg)) / (count + m)
                BrandRatingStats(avg, weighted, count)
            }.toList().sortedByDescending { it.second.weightedRating }.take(10).toMap(),
            typesByEntries = typesByEntries.sortByValue(),
            typesByQuantity = typesByQuantity.sortByValue(),
            ratingsDistribution = RatingsDistribution(
                distribution = ratingsDistribution,
                unratedCount = unratedCount
            ),
            favDisByEntries = mapOf(
                "Favorite" to favoriteCount,
                "Disliked" to dislikedCount,
                "Neutral" to (filteredItems.size - favoriteCount - dislikedCount)
            ).filterValues { it > 0 }.sortByValue(),
            subgenresByEntries = subgenresByEntries.reduceToTen(),
            subgenresByQuantity = subgenresByQuantity.reduceToTen(),
            cutsByEntries = cutsByEntries.reduceToTen(),
            cutsByQuantity = cutsByQuantity.reduceToTen(),

            filteredLoading = false,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
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
            started = SharingStarted.WhileSubscribed(5000),
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


    /** UI functions/vals **/
    val expanded = MutableStateFlow(false)

    fun updateExpanded(newExpanded: Boolean) {
        expanded.value = newExpanded
    }

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
    val brandsByRating: Map<String, BrandRatingStats> = emptyMap(),
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
data class BrandRatingStats(
    val averageRating: Double = 0.0,
    val weightedRating: Double = 0.0,
    val ratingsCount: Int = 0,
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


/** Helper functions **/
private fun Map<String, Int>.reduceToTen(): Map<String, Int> {
    val sorted = this.entries.sortedByDescending { it.value }
    if (sorted.size <= 10) return sorted.associate { it.key to it.value }

    val result = LinkedHashMap<String, Int>(10)
    for (i in 0 until 9) { result[sorted[i].key] = sorted[i].value }

    result["(Other)"] = sorted.drop(9).sumOf { it.value }
    return result
}

private fun Map<String, Int>.buildComp(filtered: Map<String, Int>, unassigned: String): Map<String, Int> {
    val sorted = this.entries.sortedByDescending { it.value }

    val result = LinkedHashMap<String, Int>(sorted.size)
    var unassignedVal: Int? = null

    for (entry in sorted) {
        val key = entry.key
        val value = filtered[key] ?: 0

        if (key == unassigned) { unassignedVal = value }
        else { result[key] = value }
    }

    if (unassignedVal != null) { result[unassigned] = unassignedVal }

    return result
}

private fun remapUnassigned(map: Map<String, Int>, unassignedKey: String): Map<String, Int> {
    val mutableMap = map.toMutableMap()
    val unassignedValue = mutableMap.remove(unassignedKey)
    if (unassignedValue != null) {
        mutableMap[unassignedKey] = unassignedValue
    }
    return mutableMap
}

private fun convertWeight(tinQuantity: Double, unit: String, quantityOption: QuantityOption): Double {
    return when (quantityOption) {
        QuantityOption.OUNCES -> {
            when (unit) {
                "oz" -> tinQuantity
                "lbs" -> tinQuantity * 16
                "grams" -> tinQuantity / 28.3495
                else -> 0.0
            }
        }
        QuantityOption.GRAMS -> {
            when (unit) {
                "oz" -> tinQuantity * 28.3495
                "lbs" -> tinQuantity * 453.592
                "grams" -> tinQuantity
                else -> 0.0
            }
        }
        else -> 0.0
    }
}

private fun formatWeight(quantityOption: QuantityOption, totalWeight: Double): String {
    return when (quantityOption) {
        QuantityOption.OUNCES -> {
            if (totalWeight >= 16.00) { formatDecimal((totalWeight / 16)) + " lbs" }
            else { formatDecimal(totalWeight) + " oz" }
        }
        QuantityOption.GRAMS -> { formatDecimal(totalWeight) + " g" }
        else -> null
    } ?: ""
}

private fun MutableMap<String, Int>.increment(key: String) {
    this[key] = (this[key] ?: 0) + 1
}

private fun MutableMap<Double, Int>.incrementDist(key: Double) {
    this[key] = (this[key] ?: 0) + 1
}

private fun Map<String, Int>.sortByValue(): Map<String, Int> {
    return this.entries
        .sortedByDescending { it.value }
        .associate { it.key to it.value }
}