package com.sardonicus.tobaccocellar.ui.dates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sardonicus.tobaccocellar.data.ItemsComponentsAndTins
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.data.Tins
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.home.calculateAge
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

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
        }

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
            filterViewModel.selectedHasTins,
            filterViewModel.selectedNoTins
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
                        (!hasTins || items.tins.isNotEmpty()) &&
                        (!noTins || items.tins.isEmpty())
            }

            val averageDateManufacture = calculateAverageDate(filteredItems, DatePeriod.PAST) { it.manufactureDate }
            val averageDateCellar = calculateAverageDate(filteredItems, DatePeriod.PAST) { it.cellarDate }
            val averageDateOpen = calculateAverageDate(filteredItems, DatePeriod.PAST) { if (it.finished == false) it.openDate else null }
            val averageFutureOpen = calculateAverageDate(filteredItems, DatePeriod.FUTURE) { it.openDate }


            flow {
                emit(
                    DatesUiState(
                        items = filteredItems,
                        loading = false,

                        agedDueThisWeek = agingDue(filteredItems).first,
                        agedDueThisMonth = agingDue(filteredItems).second,

                        averageAgeManufacture = calculateAge(averageDateManufacture, ""),
                        averageAgeCellar = calculateAge(averageDateCellar, ""),
                        averageAgeOpen = calculateAge(averageDateOpen, ""),
                        averageWaitTime = calculateAge(averageFutureOpen, ""),

                        pastManufacture = findDatedTins(filteredItems, DatePeriod.PAST) { it.manufactureDate },
                        pastCellared = findDatedTins(filteredItems, DatePeriod.PAST) { it.cellarDate },
                        pastOpened = findDatedTins(filteredItems, DatePeriod.PAST) { if (it.finished == false) it.openDate else null },
                        futureManufacture = findDatedTins(filteredItems, DatePeriod.FUTURE) { it.manufactureDate },
                        futureCellared = findDatedTins(filteredItems, DatePeriod.FUTURE) { it.cellarDate },
                        futureOpened = findDatedTins(filteredItems, DatePeriod.FUTURE) { it.openDate },
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


    fun findDatedTins(
        items: List<ItemsComponentsAndTins>,
        period: DatePeriod,
        dateSelector: (Tins) -> Long?,
        ): List<DateInfoItem> {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalDateTime.now().hour, LocalDateTime.now().minute).toInstant(ZoneOffset.UTC).toEpochMilli()

        return items.flatMap { item ->
            item.tins.map { tin ->
                DateInfoItem(
                    id = item.items.id,
                    brand = item.items.brand,
                    blend = item.items.blend,
                    tinLabel = tin.tinLabel,
                    date = formatMediumDate(dateSelector(tin)),
                    time = calculateAge(dateSelector(tin), ""),
                )
            }
        }
            .filter { it.date.isNotBlank() }
            .let {
                when (period) {
                    DatePeriod.PAST -> it
                        .filter {
                            val millis = items.flatMap { it.tins }.find { tin -> it.tinLabel == tin.tinLabel }?.let { dateSelector(it) }
                            val localMillis = Instant.ofEpochMilli(millis!!).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            localMillis < now
                        }
                        .sortedBy {
                            items.flatMap { it.tins }.find { tin -> it.tinLabel == tin.tinLabel }?.let { dateSelector(it) } ?: Long.MAX_VALUE
                        }

                    DatePeriod.FUTURE -> it
                        .filter {
                            val millis = items.flatMap { it.tins }.find { tin -> it.tinLabel == tin.tinLabel }?.let { dateSelector(it) }
                            val localMillis = Instant.ofEpochMilli(millis!!).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            localMillis > now
                        }
                        .sortedBy {
                            items.flatMap { it.tins }.find { tin -> it.tinLabel == tin.tinLabel }?.let { dateSelector(it) } ?: Long.MAX_VALUE
                        }
                }
            }
            .take(5)
    }


    fun calculateAverageDate(
        filteredItems: List<ItemsComponentsAndTins>,
        period: DatePeriod,
        field: (Tins) -> Long?
    ): Long? {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalDateTime.now().hour, LocalDateTime.now().minute).toInstant(ZoneOffset.UTC).toEpochMilli()

        val sumDate = when (period) {
            DatePeriod.PAST -> {
                filteredItems.flatMap { it.tins }.filter {
                    field(it) != null &&
                            (Instant.ofEpochMilli(field(it)!!).atZone(ZoneId.systemDefault())
                                .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
                                .toEpochMilli() < now)
                }.sumOf { field(it)!! }
            }
            DatePeriod.FUTURE -> {
                filteredItems.flatMap { it.tins }.filter {
                    field(it) != null &&
                            (Instant.ofEpochMilli(field(it)!!).atZone(ZoneId.systemDefault())
                                .toLocalDate().atTime(23, 59).toInstant(ZoneOffset.UTC)
                                .toEpochMilli() > now)
                }.sumOf { field(it)!! }
            }
        }
        val averageDate = when (period) {
            DatePeriod.PAST -> {
                if (sumDate > 0) sumDate / filteredItems.flatMap { it.tins }.filter {
                    field(it) != null &&
                            (Instant.ofEpochMilli(field(it)!!).atZone(ZoneId.systemDefault())
                                .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
                                .toEpochMilli() < now)
                }.size else null
            }
            DatePeriod.FUTURE -> {
                if (sumDate > 0) sumDate / filteredItems.flatMap { it.tins }.filter {
                    field(it) != null &&
                            (Instant.ofEpochMilli(field(it)!!).atZone(ZoneId.systemDefault())
                                .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
                                .toEpochMilli() > now)
                }.size else null
            }
        }
        return averageDate
    }


    fun agingDue(filteredItems: List<ItemsComponentsAndTins>): Pair<List<DateInfoItem>, List<DateInfoItem>> {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val oneWeek =
            LocalDate.now().plusDays(7).atTime(23, 59).toInstant(ZoneOffset.UTC).toEpochMilli()
        val endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59)
            .toInstant(ZoneOffset.UTC).toEpochMilli()

        val thisWeekTins = filteredItems.flatMap { item ->
            item.tins.map { tin ->
                DateInfoItem(
                    id = item.items.id,
                    brand = item.items.brand,
                    blend = item.items.blend,
                    tinLabel = tin.tinLabel,
                    date = formatMediumDate(tin.openDate),
                    time = calculateAge(tin.openDate, ""),
                )
            }.filter {
                val openDate = if (it.date.isNotBlank()) Instant.ofEpochMilli(filteredItems.flatMap { it.tins }.find { tin -> it.tinLabel == tin.tinLabel }?.openDate!!).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() else null
                it.date.isNotBlank() &&
                        tinTimeInPeriod(openDate!!, now, oneWeek)
            }
        }

        val thisMonthTins = filteredItems.flatMap { item ->
            item.tins.map { tin ->
                DateInfoItem(
                    id = item.items.id,
                    brand = item.items.brand,
                    blend = item.items.blend,
                    tinLabel = tin.tinLabel,
                    date = formatMediumDate(tin.openDate),
                    time = calculateAge(tin.openDate, ""),
                )
            }.filter {
                val openDate = if (it.date.isNotBlank()) Instant.ofEpochMilli(filteredItems.flatMap { it.tins }.find { tin -> it.tinLabel == tin.tinLabel }?.openDate!!).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() else null
                it.date.isNotBlank() &&
                        tinTimeInPeriod(openDate!!, now, endOfMonth) && !thisWeekTins.contains(it)
            }
        }
        return Pair(thisWeekTins, thisMonthTins)
    }

    fun tinTimeInPeriod(tinTime: Long, start: Long, end: Long): Boolean {
        return tinTime >= start && tinTime <= end
    }






}


data class DatesUiState(
    val items: List<ItemsComponentsAndTins> = listOf(),
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

enum class DatePeriod {
    PAST, FUTURE
}