package com.sardonicus.tobaccocellar.ui.dates

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.addEditItems.formatMediumDate
import com.sardonicus.tobaccocellar.ui.blendDetails.calculateAge
import com.sardonicus.tobaccocellar.ui.blendDetails.formatDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val app: CellarApplication
) : ViewModel() {

    val datesUiState: StateFlow<DatesUiState> =
        combine(
            filterViewModel.unifiedFilteredItems,
            filterViewModel.unifiedFilteredTins,
            filterViewModel.everythingFlow
        ) { filteredItems, filteredTins, allItems ->
            val now = LocalDate.now()
            val nowMillis = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val filteredTinIds = filteredTins.map { it.tinId }.toSet()
            val datedTins = filteredItems.flatMap { item ->
                item.tins.filter { it.tinId in filteredTinIds }.map { tin ->
                    Triple(item, tin, tin.manufactureDate ?: tin.cellarDate ?: tin.openDate)
                }
            }.filter { it.third != null }

            val agingData = agingDue(allItems, now)
            val weekSection = AgingSection(app.getString(R.string.week_section), app.getString(R.string.week_empty), agingData.first)
            val monthSection = AgingSection(app.getString(R.string.month_section), app.getString(R.string.month_empty), agingData.second)

            val filteredItemTins2 = datedTins.map { it.second }
            val averageManuf = calculateAverageDate(filteredItemTins2, DatePeriod.PAST, now) { it.manufactureDate }
            val averageCellar = calculateAverageDate(filteredItemTins2, DatePeriod.PAST, now) { if (!it.finished) it.cellarDate else null }
            val averageOpen = calculateAverageDate(filteredItemTins2, DatePeriod.PAST, now) { if (!it.finished) it.openDate else null }
            val averageWait = calculateAverageDate(filteredItemTins2, DatePeriod.FUTURE, now) { it.openDate }

            val pastManu = findDatedTins(datedTins, DatePeriod.PAST, nowMillis) { it.manufactureDate }
            val pastCellar = findDatedTins(datedTins, DatePeriod.PAST, nowMillis) { if (!it.finished) it.cellarDate else null }
            val pastOpened = findDatedTins(datedTins, DatePeriod.PAST, nowMillis) { if (!it.finished) it.openDate else null }

            val futureManu = findDatedTins(datedTins, DatePeriod.FUTURE, nowMillis) { it.manufactureDate }
            val futureCellar = findDatedTins(datedTins, DatePeriod.FUTURE, nowMillis) { it.cellarDate }
            val futureOpen = findDatedTins(datedTins, DatePeriod.FUTURE, nowMillis) { it.openDate }

            DatesUiState(
                datesExist = datedTins.isNotEmpty(),
                agingSection = listOf(weekSection, monthSection),
                agingExists = agingData.first.isNotEmpty() || agingData.second.isNotEmpty(),
                averageAgeSection = listOf(
                    AverageAgeSection(app.getString(R.string.average_manuf), averageManuf),
                    AverageAgeSection(app.getString(R.string.average_cellar), averageCellar),
                    AverageAgeSection(app.getString(R.string.average_open), averageOpen),
                    AverageAgeSection(app.getString(R.string.average_wait), averageWait)
                ),
                averageAgeExists = averageManuf.isNotBlank() || averageCellar.isNotBlank() || averageOpen.isNotBlank() || averageWait.isNotBlank(),
                oldestTinsSection = listOf(
                    OldestTinsSection(app.getString(R.string.manufacture_date), pastManu),
                    OldestTinsSection(app.getString(R.string.cellared_date), pastCellar),
                    OldestTinsSection(app.getString(R.string.opened_date), pastOpened)
                ),
                oldestTinsExists = pastManu.isNotEmpty() || pastCellar.isNotEmpty() || pastOpened.isNotEmpty(),
                futureTinsSection = listOf(
                    FutureTinsSection(app.getString(R.string.manufacture_date), futureManu),
                    FutureTinsSection(app.getString(R.string.cellared_date), futureCellar),
                    FutureTinsSection(app.getString(R.string.opened_date), futureOpen)
                ),
                futureTinsExists = futureManu.isNotEmpty() || futureCellar.isNotEmpty() || futureOpen.isNotEmpty(),
                loading = false,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DatesUiState(loading = true)
            )


    private var trackingJob: Job? = null
    fun startDatesSeen() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            filterViewModel.everythingFlow.collect { allItems ->
                val currentReady = allItems.flatMap { it.tins }
                    .filter { tins ->
                        tins.openDate?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                                .toLocalDate() in LocalDate.now()..LocalDate.now().plusDays(7)
                        } ?: false
                    }.joinToString(",") { it.tinId.toString() }

                preferencesRepo.setDatesSeen(currentReady)
            }
        }
    }

    fun cancelDatesSeen() { trackingJob?.cancel(); trackingJob = null }


    fun findDatedTins(
        data: List<Triple<ItemsComponentsAndTins, Tins, Long?>>,
        period: DatePeriod,
        now: Long,
        dateSelector: (Tins) -> Long?,
    ): List<DateInfoItem> {
        val itemsWithDate: List<Pair<DateInfoItem, Long?>> = data.mapNotNull { (item, tin, _) ->
            val originalMillis = dateSelector(tin) ?: return@mapNotNull null
            val itemDate = Instant.ofEpochMilli(originalMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val isMatch = if (period == DatePeriod.PAST) itemDate <= now else itemDate > now
            if (!isMatch) return@mapNotNull null

            Pair(
                DateInfoItem(
                    id = item.items.id,
                    brand = item.items.brand,
                    blend = item.items.blend,
                    tinLabel = tin.tinLabel,
                    date = formatMediumDate(originalMillis),
                    timeFrame = calculateAge(originalMillis, app),
                ),
                originalMillis
            )

        }

        return if (period == DatePeriod.PAST) {
            itemsWithDate.sortedBy { it.second }.map { it.first }
        } else {
            itemsWithDate.sortedBy { it.second }.map { it.first }
        }.take(5)
    }


    fun calculateAverageDate(
        tins: List<Tins>,
        periodSelection: DatePeriod,
        now: LocalDate,
        field: (Tins) -> Long?
    ): String {
        val relevantDates = tins.mapNotNull { field(it) }.filter {
            val then = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            if (periodSelection == DatePeriod.PAST) then <= now else then > now
        }

        if (relevantDates.isEmpty()) return ""

        var totalYears = 0.0
        var totalMonths = 0.0
        var totalDays = 0.0
        val count = relevantDates.size

        for (date in relevantDates) {
            val then = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
            val datePeriod = if (periodSelection == DatePeriod.PAST) Period.between(then, now) else Period.between(now, then)
            totalYears += datePeriod.years
            totalMonths += datePeriod.months
            totalDays += datePeriod.days
        }

        val years = floor(totalYears / count).toInt()
        val fractionalYear = (totalYears / count) - years

        val rawMonths = (totalMonths / count) + (fractionalYear * 12.0)
        val months = floor(rawMonths).toInt()
        val fractionalMonth = rawMonths - months

        val days = (totalDays / count) + (fractionalMonth * 30.4375)
        val dayString = formatDecimal(days, if (years == 0 && months == 0) 1 else 0)

        val parts = mutableListOf<String>()

        if (years > 0) { parts.add(app.resources.getQuantityString(R.plurals.years, years, years)) }
        if (months > 0) { parts.add(app.resources.getQuantityString(R.plurals.months, months, months)) }
        if (days > 0.09) {
            val quantity = if (days == 1.0) 1 else 2
            parts.add(app.resources.getQuantityString(R.plurals.days, quantity, dayString))
        }

        return parts.joinToString(", ")
    }

    fun agingDue(items: List<ItemsComponentsAndTins>, now: LocalDate): Pair<List<DateInfoItem>, List<DateInfoItem>> {
        val oneWeek = LocalDate.now().plusDays(7).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        val endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        val nowMillis = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val thisWeekTins = mutableListOf<DateInfoItem>()
        val thisMonthTins = mutableListOf<DateInfoItem>()

        for ((item, _, _, tins) in items) {
            for ((_, _, tinLabel, _, _, _, _, _, openMillis, finished) in tins) {
                if (finished || openMillis == null) continue

                val openDate = Instant.ofEpochMilli(openMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (openDate in nowMillis..oneWeek) {
                    thisWeekTins.add(
                        DateInfoItem(
                            id = item.id,
                            brand = item.brand,
                            blend = item.blend,
                            tinLabel = tinLabel,
                            date = formatMediumDate(openMillis),
                            timeFrame = calculateAge(openMillis, app),
                        )
                    )
                } else if (openDate in (oneWeek + 1)..endOfMonth) {
                    thisMonthTins.add(
                        DateInfoItem(
                            id = item.id,
                            brand = item.brand,
                            blend = item.blend,
                            tinLabel = tinLabel,
                            date = formatMediumDate(openMillis),
                            timeFrame = calculateAge(openMillis, app),
                        )
                    )
                }
            }
        }

        return Pair(thisWeekTins, thisMonthTins)
    }

}


@Stable
data class DatesUiState(
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
    val timeFrame: String = "",
)

enum class DatePeriod { PAST, FUTURE }