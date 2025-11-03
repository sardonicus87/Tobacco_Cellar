package com.sardonicus.tobaccocellar.ui.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

class StatsViewModel(
    filterViewModel: FilterViewModel,
    private val preferencesRepo: PreferencesRepo
): ViewModel() {

    /** Raw stats */
    val rawStats: StateFlow<RawStats> =
        combine (
            filterViewModel.everythingFlow,
            preferencesRepo.quantityOption
        ) { it, quantityOption ->
            val ratingsExist = it.any { it.items.rating != null }
            val averageRating = if (ratingsExist) { (it.sumOf { it.items.rating ?: 0.0 }) / (it.count { it.items.rating != null }) } else null

            RawStats(
                itemsCount = it.size,
                brandsCount = it.groupingBy { it.items.brand }.eachCount().size,
                averageRating = if (averageRating != null) formatDecimal(averageRating) else "",
                favoriteCount = it.count { it.items.favorite },
                dislikedCount = it.count { it.items.disliked },
                totalByBrand = it.groupingBy { it.items.brand }.eachCount(),
                totalQuantity = it.sumOf { it.items.quantity },
                estimatedWeight = calculateTotal(it, it.flatMap { it.tins.filter { !it.finished } }, quantityOption),
                totalOpened = if (it.map { it.tins }.isEmpty() || it.map { it.tins }.all { it.all { it.openDate == null } }) null
                    else it.flatMap { it.tins }.count { it.openDate != null && !it.finished },
                totalZeroQuantity = it.count { it.items.quantity == 0 },


                totalByType = it.groupingBy { it.items.type.ifBlank { "Unassigned" } }
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
                totalBySubgenre = it.groupingBy {
                    it.items.subGenre.ifBlank { "Unassigned" } }
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
                    it.items.cut.ifBlank { "Unassigned" } }
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
                totalByComponent = it.flatMap { it.components
                    .ifEmpty { listOf(Components(componentName = "None Assigned")) } }
                    .groupingBy { it.componentName }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val noComps = mutableMap.remove("None Assigned")
                        if (noComps != null) {
                            mutableMap["None Assigned"] = noComps
                        }
                        mutableMap.toMap()
                    },
                totalByFlavoring = it.flatMap { it.flavoring
                    .ifEmpty { listOf(Flavoring(flavoringName = "None Assigned")) } }
                    .groupingBy { it.flavoringName }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val noFlavor = mutableMap.remove("None Assigned")
                        if (noFlavor != null) {
                            mutableMap["None Assigned"] = noFlavor
                        }
                        mutableMap.toMap()
                    },
                totalByContainer = it.flatMap { it.tins }
                    .filter { !it.finished }
                    .groupingBy { it.container.ifBlank { "Unassigned" } }
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
            filterViewModel.everythingFlow,
            filterViewModel.unifiedFilteredItems,
            filterViewModel.unifiedFilteredTins,
            preferencesRepo.quantityOption
        ) { allItems, filteredItems, filteredTins, quantityOption ->

            val tinIds = filteredTins.map { it.tinId }
            val relevantTins = filteredItems.flatMap { it.tins }.filter { it.tinId in tinIds && !it.finished }

            val ratingsExist = allItems.any { it.items.rating != null }
            val filteredRatingsExist = filteredItems.any { it.items.rating != null }
            val averageRating =
                if (ratingsExist && filteredRatingsExist) { (filteredItems.sumOf { it.items.rating ?: 0.0 }) / (filteredItems.count { it.items.rating != null }) }
                else if (!filteredRatingsExist) 0.0
                else null

            FilteredStats(
                itemsCount = filteredItems.size,
                brandsCount = filteredItems.groupingBy { it.items.brand }.eachCount().size,
                averageRating = if (averageRating != null) formatDecimal(averageRating) else "",
                favoriteCount = filteredItems.count { it.items.favorite },
                dislikedCount = filteredItems.count { it.items.disliked },
                unassignedCount = filteredItems.count { it.items.type.isBlank() },
                totalQuantity = filteredItems.sumOf { it.items.quantity },
                estimatedWeight = calculateTotal(filteredItems, filteredItems.flatMap { it.tins.filter { !it.finished && it in filteredTins } }, quantityOption),
                totalOpened = relevantTins.count { it.openDate != null && !it.finished },
                totalZeroQuantity = filteredItems.count { it.items.quantity == 0 },

                totalByType = allItems.groupingBy {
                    it.items.type.ifBlank { "Unassigned" } }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate {
                        it.key to (filteredItems.groupingBy {
                            it.items.type.ifBlank { "Unassigned" }
                        }.eachCount()[it.key] ?: 0) }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val unassignedEntry = mutableMap.remove("Unassigned")
                        if (unassignedEntry != null) {
                            mutableMap["Unassigned"] = unassignedEntry
                        }
                        mutableMap.toMap()
                    },
                totalBySubgenre = allItems.groupingBy {
                    it.items.subGenre.ifBlank { "Unassigned" } }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate {
                        it.key to (filteredItems.groupingBy {
                            it.items.subGenre.ifBlank { "Unassigned" }
                        }.eachCount()[it.key] ?: 0)
                    }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val unassignedEntry = mutableMap.remove("Unassigned")
                        if (unassignedEntry != null) {
                            mutableMap["Unassigned"] = unassignedEntry
                        }
                        mutableMap.toMap()
                    },
                totalByCut = allItems.groupingBy {
                    it.items.cut.ifBlank { "Unassigned" } }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate {
                        it.key to (filteredItems.groupingBy {
                            it.items.cut.ifBlank { "Unassigned" }
                        }.eachCount()[it.key] ?: 0)
                    }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val unassignedEntry = mutableMap.remove("Unassigned")
                        if (unassignedEntry != null) {
                            mutableMap["Unassigned"] = unassignedEntry
                        }
                        mutableMap.toMap()
                    },
                totalByComponent = allItems.flatMap { it.components.ifEmpty {
                    listOf(Components(componentName = "None Assigned")) } }
                    .groupingBy { it.componentName }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate {
                        it.key to (filteredItems.flatMap {
                            it.components.ifEmpty { listOf(Components(componentName = "None Assigned")) } }
                            .groupingBy { it.componentName }.eachCount()[it.key] ?: 0)
                    }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val noComps = mutableMap.remove("None Assigned")
                        if (noComps != null) {
                            mutableMap["None Assigned"] = noComps
                        }
                        mutableMap.toMap()
                    },
                totalByFlavoring = allItems.flatMap { it.flavoring.ifEmpty{
                    listOf(Flavoring(flavoringName = "None Assigned")) } }
                    .groupingBy { it.flavoringName }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate {
                        it.key to (filteredItems.flatMap {
                            it.flavoring.ifEmpty { listOf(Flavoring(flavoringName = "None Assigned")) } }
                            .groupingBy { it.flavoringName }.eachCount()[it.key] ?: 0)
                    }
                    .let {
                        val mutableMap = it.toMutableMap()
                        val noFlavor = mutableMap.remove("None Assigned")
                        if (noFlavor != null) {
                            mutableMap["None Assigned"] = noFlavor
                        }
                        mutableMap.toMap()
                    },
                totalByContainer = allItems.flatMap { it.tins }
                    .filter { !it.finished }
                    .groupingBy { it.container.ifBlank { "Unassigned" } }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .associate {
                        it.key to (relevantTins.groupingBy {
                            it.container.ifBlank { "Unassigned" }
                        }.eachCount()[it.key] ?: 0)
                    }
                    .let {
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
                        .map { (it * 2).roundToInt() / 2.0 }
                        .groupingBy { it }
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
                    .groupingBy { it.items.subGenre.ifBlank { "Unassigned" } }
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
                    .groupingBy { it.items.cut.ifBlank { "Unassigned" } }
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
                    .groupingBy { it.items.cut.ifBlank { "Unassigned" } }
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

                filteredLoading = false,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = FilteredStats(filteredLoading = true)
            )


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

    var expanded by mutableStateOf(false)

    fun updateExpanded(newExpanded: Boolean) {
        expanded = newExpanded
    }

}


data class RawStats(
    val rawLoading: Boolean = false,

    val itemsCount: Int = 0,
    val brandsCount: Int = 0,
    val averageRating: String = "",
    val favoriteCount: Int = 0,
    val dislikedCount: Int = 0,
    val totalByBrand: Map<String, Int> = emptyMap(),
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

data class FilteredStats(
    val filteredLoading: Boolean = false,

    val itemsCount: Int = 0,
    val brandsCount: Int = 0,
    val averageRating: String = "",
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

data class RatingsDistribution(
    val distribution: Map<Double, Int> = emptyMap(),
    val unratedCount: Int = 0
)