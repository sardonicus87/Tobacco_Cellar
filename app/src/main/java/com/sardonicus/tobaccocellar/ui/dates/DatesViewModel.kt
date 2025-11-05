package com.sardonicus.tobaccocellar.ui.dates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.details.calculateAge
import com.sardonicus.tobaccocellar.ui.details.formatDecimal
import com.sardonicus.tobaccocellar.ui.items.formatMediumDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val filterViewModel: FilterViewModel,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val datesUiState: StateFlow<DatesUiState> =
        combine(
            filterViewModel.unifiedFilteredItems,
            filterViewModel.unifiedFilteredTins,
            filterViewModel.everythingFlow
        ) { filteredItems, filteredTins, allItems ->

            val filteredItemTins = filteredItems.flatMap { it.tins }.filter { it in filteredTins }
            val tinDates = filteredItemTins.mapNotNull { it.manufactureDate } + filteredItemTins.mapNotNull { it.cellarDate } + filteredItemTins.mapNotNull { it.openDate }

            DatesUiState(
                items = filteredItems,
                datesExist = tinDates.isNotEmpty(),

                agedDueThisWeek = agingDue(allItems).first,
                agedDueThisMonth = agingDue(allItems).second,

                averageAgeManufacture = calculateAverageDate(filteredItemTins, DatePeriod.PAST) { it.manufactureDate },
                averageAgeCellar = calculateAverageDate(filteredItemTins, DatePeriod.PAST) { if (!it.finished) it.cellarDate else null },
                averageAgeOpen = calculateAverageDate(filteredItemTins, DatePeriod.PAST) { if (!it.finished) it.openDate else null },
                averageWaitTime = calculateAverageDate(filteredItemTins, DatePeriod.FUTURE) { it.openDate },

                pastManufacture = findDatedTins(filteredItems, filteredTins, DatePeriod.PAST) { it.manufactureDate },
                pastCellared = findDatedTins(filteredItems, filteredTins, DatePeriod.PAST) { if (!it.finished) it.cellarDate else null },
                pastOpened = findDatedTins(filteredItems, filteredTins, DatePeriod.PAST) { if (!it.finished) it.openDate else null },
                futureManufacture = findDatedTins(filteredItems, filteredTins, DatePeriod.FUTURE) { it.manufactureDate },
                futureCellared = findDatedTins(filteredItems, filteredTins, DatePeriod.FUTURE) { it.cellarDate },
                futureOpened = findDatedTins(filteredItems, filteredTins, DatePeriod.FUTURE) { it.openDate },

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