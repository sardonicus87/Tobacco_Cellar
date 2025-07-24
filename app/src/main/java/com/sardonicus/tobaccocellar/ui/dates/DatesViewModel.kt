package com.sardonicus.tobaccocellar.ui.dates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.home.calculateAge
import com.sardonicus.tobaccocellar.ui.home.formatDecimal
import com.sardonicus.tobaccocellar.ui.items.formatMediumDate
import com.sardonicus.tobaccocellar.ui.settings.DatabaseRestoreEvent
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import kotlin.math.floor

class DatesViewModel(
    private val itemsRepository: ItemsRepository,
    private val filterViewModel: FilterViewModel,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

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
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalCoroutinesApi::class)
    val datesUiState: StateFlow<DatesUiState> =
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
            filterViewModel.selectedContainer,
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
            val container = values[21] as List<String>

            val filteredItems = allItems.filter { items ->
                val componentMatching = when (compMatching) {
                    "All" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components)))
                    "Only" -> ((components.isEmpty()) || (items.components.map { it.componentName }.containsAll(components) && items.components.size == components.size))
                    else -> ((components.isEmpty() && !components.contains("(None Assigned)")) || ((components.contains("(None Assigned)") && items.components.isEmpty()) || (items.components.map { it.componentName }.any { components.contains(it) })))
                }
                val flavoringMatching = when (flavorMatching) {
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
                        flavoringMatching &&
                        ((subgenres.isEmpty() && !subgenres.contains("(Unassigned)")) || ((subgenres.contains("(Unassigned)") && items.items.subGenre.isBlank()) || subgenres.contains(items.items.subGenre))) &&
                        ((cuts.isEmpty() && !cuts.contains("(Unassigned)")) || ((cuts.contains("(Unassigned)") && items.items.cut.isBlank()) || cuts.contains(items.items.cut))) &&
                        (!production || items.items.inProduction) &&
                        (!outOfProduction || !items.items.inProduction) &&
                        ((container.isEmpty() && !container.contains("(Unassigned)")) || ((container.contains("(Unassigned)") && items.tins.any { it.container.isBlank() }) || (items.tins.map { it.container }.any { container.contains(it) }) ))
            }

            val tins = filteredItems.flatMap { it.tins }
            val tinDates = tins.mapNotNull { it.manufactureDate } + tins.mapNotNull { it.cellarDate } + tins.mapNotNull { it.openDate }

            flow {
                emit(
                    DatesUiState(
                        items = filteredItems,
                        datesExist = tinDates.isNotEmpty(),

                        agedDueThisWeek = agingDue(filteredItems).first,
                        agedDueThisMonth = agingDue(filteredItems).second,

                        averageAgeManufacture = calculateAverageDate(filteredItems, DatePeriod.PAST) { it.manufactureDate },
                        averageAgeCellar = calculateAverageDate(filteredItems, DatePeriod.PAST) { it.cellarDate },
                        averageAgeOpen = calculateAverageDate(filteredItems, DatePeriod.PAST) { if (!it.finished) it.openDate else null },
                        averageWaitTime = calculateAverageDate(filteredItems, DatePeriod.FUTURE) { it.openDate },

                        pastManufacture = findDatedTins(filteredItems, DatePeriod.PAST) { it.manufactureDate },
                        pastCellared = findDatedTins(filteredItems, DatePeriod.PAST) { it.cellarDate },
                        pastOpened = findDatedTins(filteredItems, DatePeriod.PAST) { if (!it.finished) it.openDate else null },
                        futureManufacture = findDatedTins(filteredItems, DatePeriod.FUTURE) { it.manufactureDate },
                        futureCellared = findDatedTins(filteredItems, DatePeriod.FUTURE) { it.cellarDate },
                        futureOpened = findDatedTins(filteredItems, DatePeriod.FUTURE) { it.openDate },

                        loading = false,
                    )
                )
            }
        }
            .flattenMerge()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DatesUiState(loading = true)
            )

    init {
        viewModelScope.launch {
            val currentReady = everythingFlow.first().flatMap { it.tins }
                .filter {
                    it.openDate?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() in LocalDate.now()..LocalDate.now().plusDays(7)
                    } ?: false
                }.joinToString(",") { it.tinId.toString() }

            preferencesRepo.setDatesSeen(currentReady)
        }
    }


    @Suppress("NullableBooleanElvis")
    fun findDatedTins(
        items: List<ItemsComponentsAndTins>,
        period: DatePeriod,
        dateSelector: (Tins) -> Long?,
        ): List<DateInfoItem> {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val itemsWithDate: List<Pair<DateInfoItem, Long?>> = items.flatMap{ item ->
            item.tins.map {
                val originalMillis = dateSelector(it)
                Pair(
                    DateInfoItem(
                        id = item.items.id,
                        brand = item.items.brand,
                        blend = item.items.blend,
                        tinLabel = it.tinLabel,
                        date = formatMediumDate(originalMillis),
                        time = calculateAge(originalMillis, ""),
                    ),
                    originalMillis
                )
            }
        }. filter { it.first.date.isNotBlank() }

        return when (period) {
            DatePeriod.PAST -> itemsWithDate
                .filter { (_, millis) ->
                    millis?.let {
                        val itemDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        itemDate <= now
                    } ?: false
                }
                .sortedBy { (_, millis) -> millis ?: Long.MAX_VALUE }
                .map { it.first }

            DatePeriod.FUTURE -> itemsWithDate
                .filter { (_, millis) ->
                    millis?.let {
                        val itemDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        itemDate > now
                    } ?: false
                }
                .sortedBy { (_, millis) -> millis ?: Long.MAX_VALUE }
                .map { it.first }
        }
            .take(5)
    }


    fun calculateAverageDate(
        filteredItems: List<ItemsComponentsAndTins>,
        periodSelection: DatePeriod,
        field: (Tins) -> Long?
    ): String {
        val now = LocalDate.now()

        val relevantTins = filteredItems.flatMap { it.tins }.mapNotNull {
            val dateMillis = field(it)
            if (dateMillis != null) {
                val then = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                val period = if (periodSelection == DatePeriod.PAST) { then <= now } else { then > now }
                if (period) { it } else { null }
            } else {
                null
            }
        }

        if (relevantTins.isEmpty()) { return "" }

        var totalYears = 0.0
        var totalMonths = 0.0
        var totalDays = 0.0
        val count = relevantTins.size

        for (date in relevantTins) {
            val dateParts = getAgeParts(field(date)!!, periodSelection)
            totalYears += dateParts.first
            totalMonths += dateParts.second
            totalDays += dateParts.third
        }

        var years: Int
        var months: Int
        var days: Double

        val rawYears = totalYears / count
        years = floor(rawYears).toInt()
        val fractionalYearMonths = (rawYears - years) * 12.0

        val rawMonths = (totalMonths + fractionalYearMonths) / count
        months = floor(rawMonths).toInt()
        val fractionalMonthsDays = (rawMonths - months) * 30.4375

        days = (totalDays + fractionalMonthsDays) / count
        val places = if (years == 0 && months == 0) 1 else 0
        val dayString = formatDecimal(days, places)

        val parts = mutableListOf<String>()

        if (years > 0) { parts.add("$years year${if (years > 1) "s" else ""}") }
        if (months > 0) { parts.add("$months month${if (months > 1) "s" else ""}") }
        if (days > 0.09) { parts.add("$dayString day${if (dayString != "1") "s" else ""}") }

        return if (parts.isEmpty()) {
            ""
        } else parts.joinToString(", ")
    }

    fun getAgeParts(date: Long, period: DatePeriod): Triple<Int, Int, Int> {
        val now = LocalDate.now()
        val then = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        val datePeriod = if (period == DatePeriod.PAST) Period.between(then, now) else Period.between(now, then)

        return Triple(datePeriod.years, datePeriod.months, datePeriod.days)
    }


    fun agingDue(filteredItems: List<ItemsComponentsAndTins>): Pair<List<DateInfoItem>, List<DateInfoItem>> {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val oneWeek = LocalDate.now().plusDays(7).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        val endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        val itemsWithDate: List<Pair<DateInfoItem, Long>> = filteredItems.flatMap{ item ->
            item.tins
                .filter { !it.finished && it.openDate != null }
                .map {
                    val originalMillis = it.openDate!!
                    Pair(
                        DateInfoItem(
                            id = item.items.id,
                            brand = item.items.brand,
                            blend = item.items.blend,
                            tinLabel = it.tinLabel,
                            date = formatMediumDate(originalMillis),
                            time = calculateAge(originalMillis, ""),
                        ),
                        originalMillis
                    )
                }
        }. filter { it.first.date.isNotBlank() }

        val thisWeekTins = itemsWithDate.filter { (_, millis) ->
                val openDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                tinTimeInPeriod(openDate, now, oneWeek)
        }.map { it.first }

        val thisMonthTins = itemsWithDate.filter { (_, millis) ->
            val openDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            tinTimeInPeriod(openDate, now, endOfMonth)
        }.map { it.first }.filter { !thisWeekTins.contains(it) }

        return Pair(thisWeekTins, thisMonthTins)
    }


    fun tinTimeInPeriod(tinTime: Long, start: Long, end: Long): Boolean {
        return tinTime >= start && tinTime <= end
    }

}


data class DatesUiState(
    val items: List<ItemsComponentsAndTins> = listOf(),
    val datesExist: Boolean = false,
    val loading: Boolean = false,

    val agedDueThisWeek: List<DateInfoItem> = emptyList(),
    val agedDueThisMonth: List<DateInfoItem> = emptyList(),

    val averageAgeManufacture: String = "",
    val averageAgeCellar: String = "",
    val averageAgeOpen: String = "",
    val averageWaitTime: String = "",

    val pastManufacture: List<DateInfoItem> = emptyList(),
    val pastCellared: List<DateInfoItem> = emptyList(),
    val pastOpened: List<DateInfoItem> = emptyList(),
    val futureManufacture: List<DateInfoItem> = emptyList(),
    val futureCellared: List<DateInfoItem> = emptyList(),
    val futureOpened: List<DateInfoItem> = emptyList(),
)

data class DateInfoItem(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val tinLabel: String = "",
    val date: String = "",
    val time: String = "",
)

enum class DatePeriod { PAST, FUTURE }