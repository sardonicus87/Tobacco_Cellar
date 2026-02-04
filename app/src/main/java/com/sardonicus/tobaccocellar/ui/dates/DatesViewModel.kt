package com.sardonicus.tobaccocellar.ui.dates

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.details.calculateAge
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.items.formatMediumDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import kotlin.math.floor

class DatesViewModel(
    private val filterViewModel: FilterViewModel,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    private val _selectionFocused = MutableStateFlow(false)
    val selectionFocused = _selectionFocused.asStateFlow()

    private val _selectionKey = MutableStateFlow(0)
    val selectionKey = _selectionKey.asStateFlow()

    val datesUiState: StateFlow<DatesUiState> =
        combine(
            filterViewModel.unifiedFilteredItems,
            filterViewModel.unifiedFilteredTins,
            filterViewModel.everythingFlow
        ) { filteredItems, filteredTins, allItems ->

            val filteredItemTins = filteredItems.flatMap { it.tins }.filter { it in filteredTins }
            val tinDates = filteredItemTins.mapNotNull { it.manufactureDate } + filteredItemTins.mapNotNull { it.cellarDate } + filteredItemTins.mapNotNull { it.openDate }

            val agingDueThisWeek = agingDue(allItems).first
            val agingDueThisMonth = agingDue(allItems).second

            val weekSection = AgingSection("Tins ready in the next 7 days:", "No tins ready this week.", agingDueThisWeek)
            val monthSection = AgingSection("Other tins ready this month:", "No more tins ready this month.", agingDueThisMonth)

            val averageManuf = calculateAverageDate(filteredItemTins, DatePeriod.PAST) { it.manufactureDate }
            val averageCellar = calculateAverageDate(filteredItemTins, DatePeriod.PAST) { if (!it.finished) it.cellarDate else null }
            val averageOpen = calculateAverageDate(filteredItemTins, DatePeriod.PAST) { if (!it.finished) it.openDate else null }
            val averageWait = calculateAverageDate(filteredItemTins, DatePeriod.FUTURE) { it.openDate }

            val manufSection = AverageAgeSection("Average age (manuf): ", averageManuf)
            val cellarSection = AverageAgeSection("Average time in cellar: ", averageCellar)
            val openSection = AverageAgeSection("Average opened time: ", averageOpen)
            val waitSection = AverageAgeSection("Average wait (open): ", averageWait)

            val pastManu = findDatedTins(filteredItems, filteredTins, DatePeriod.PAST) { it.manufactureDate }
            val pastCellar = findDatedTins(filteredItems, filteredTins, DatePeriod.PAST) { if (!it.finished) it.cellarDate else null }
            val pastOpened = findDatedTins(filteredItems, filteredTins, DatePeriod.PAST) { if (!it.finished) it.openDate else null }

            val oldestManu = OldestTinsSection("Manufacture", pastManu)
            val oldestCellar = OldestTinsSection("Cellared", pastCellar)
            val oldestOpened = OldestTinsSection("Opened", pastOpened)

            val futureManu = findDatedTins(filteredItems, filteredTins, DatePeriod.FUTURE) { it.manufactureDate }
            val futureCellar = findDatedTins(filteredItems, filteredTins, DatePeriod.FUTURE) { it.cellarDate }
            val futureOpen = findDatedTins(filteredItems, filteredTins, DatePeriod.FUTURE) { it.openDate }

            val futureManuSection = FutureTinsSection("Manufacture", futureManu)
            val futureCellarSection = FutureTinsSection("Cellared", futureCellar)
            val futureOpenSection = FutureTinsSection("Opened", futureOpen)

            DatesUiState(
                items = filteredItems,
                datesExist = tinDates.isNotEmpty(),

                agingSection = listOf(weekSection, monthSection),
                agingExists = agingDue(allItems).first.isNotEmpty() || agingDue(allItems).second.isNotEmpty(),

                averageAgeSection = listOf(manufSection, cellarSection, openSection, waitSection),
                averageAgeExists = averageManuf.isNotBlank() || averageCellar.isNotBlank() || averageOpen.isNotBlank() || averageWait.isNotBlank(),

                oldestTinsSection = listOf(oldestManu, oldestCellar, oldestOpened),
                oldestTinsExists = pastManu.isNotEmpty() || pastCellar.isNotEmpty() || pastOpened.isNotEmpty(),

                futureTinsSection = listOf(futureManuSection, futureCellarSection, futureOpenSection),
                futureTinsExists = futureManu.isNotEmpty() || futureCellar.isNotEmpty() || futureOpen.isNotEmpty(),

                loading = false,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DatesUiState(loading = true)
            )


    init {
        viewModelScope.launch {
            val currentReady = filterViewModel.everythingFlow.first().flatMap { it.tins }
                .filter { tins ->
                    tins.openDate?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() in LocalDate.now()..LocalDate.now().plusDays(7)
                    } ?: false
                }.joinToString(",") { it.tinId.toString() }

            preferencesRepo.setDatesSeen(currentReady)
        }
    }


    fun findDatedTins(
        items: List<ItemsComponentsAndTins>,
        tins: List<Tins>,
        period: DatePeriod,
        dateSelector: (Tins) -> Long?,
        ): List<DateInfoItem> {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val tinIds = tins.map { it.tinId }

        val itemsWithDate: List<Pair<DateInfoItem, Long?>> = items.flatMap{ item ->
            item.tins
                .filter { it.tinId in tinIds }
                .map {
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
        tins: List<Tins>,
        periodSelection: DatePeriod,
        field: (Tins) -> Long?
    ): String {
        val now = LocalDate.now()

        val relevantTins = tins.mapNotNull {
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

    fun agingDue(items: List<ItemsComponentsAndTins>): Pair<List<DateInfoItem>, List<DateInfoItem>> {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val oneWeek = LocalDate.now().plusDays(7).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        val endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        val itemsWithDate: List<Pair<DateInfoItem, Long>> = items.flatMap{ item ->
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
        return tinTime in start..end
    }

    fun resetSelection() {
        _selectionKey.update { it + 1 }
        updateFocused(false)
    }

    fun updateFocused(focused: Boolean) {
        _selectionFocused.update { focused }
    }

}


@Stable
data class DatesUiState(
    val items: List<ItemsComponentsAndTins> = listOf(),
    val datesExist: Boolean = false,
    val loading: Boolean = false,

    val agingSection: List<AgingSection> = emptyList(),
    val agingExists: Boolean = false,

    val averageAgeSection: List<AverageAgeSection> = emptyList(),
    val averageAgeExists: Boolean = false,

    val oldestTinsSection: List<OldestTinsSection> = emptyList(),
    val oldestTinsExists: Boolean = false,

    val futureTinsSection: List<FutureTinsSection> = emptyList(),
    val futureTinsExists: Boolean = false,
)

@Stable
data class AgingSection(
    val exists: String = "",
    val empty: String = "",
    val agingDue: List<DateInfoItem> = emptyList(),
)

@Stable
data class AverageAgeSection(
    val title: String = "",
    val averageAge: String = "",
)

@Stable
data class OldestTinsSection(
    val title: String = "",
    val oldestTins: List<DateInfoItem> = emptyList(),
)

@Stable
data class FutureTinsSection(
    val title: String = "",
    val futureTins: List<DateInfoItem> = emptyList(),
)

@Stable
data class DateInfoItem(
    val id: Int = 0,
    val brand: String = "",
    val blend: String = "",
    val tinLabel: String = "",
    val date: String = "",
    val time: String = "",
)

enum class DatePeriod { PAST, FUTURE }