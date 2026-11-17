package com.sardonicus.tobaccocellar.data

import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.collator
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.formatLocalizedList
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.getLocalizedContext
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.getLocalizedType
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.rtlIsolate
import com.sardonicus.tobaccocellar.ui.utilities.localizationUtil.safeString
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.QuoteMode
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.Charset

class CsvHelper {

    fun csvFileReader(inputStream: InputStream): CsvResult {
        return try {
            val defaultParser = CSVParser.parse(inputStream, Charset.defaultCharset(),
                CSVFormat.DEFAULT)
            val iterator = defaultParser.iterator()

            if (iterator.hasNext()) {
                val headerRecord = iterator.next()
                val header = headerRecord.toList().map { it.toString() }
                var firstFullRecord = emptyList<String>()
                var recordCount = 1
                var columnCount = header.size

                while (iterator.hasNext()) {
                    val record = iterator.next()
                    val recordList = record.toList().map { it.toString() }

                    if (firstFullRecord.isEmpty() && recordList.size >= header.size) {
                        firstFullRecord = recordList
                    }
                    if (recordList.size > columnCount) columnCount = recordList.size
                    recordCount++
                }
                CsvResult.Success(header, firstFullRecord, columnCount, recordCount)
            } else CsvResult.Empty
        } catch (e: Exception) { CsvResult.Error(e) }
    }

    fun exportToCsv(data: List<ItemsComponentsAndTins>, maxRating: Int, rounding: Int): String {
        val context = getLocalizedContext(CellarApplication.instance)
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader(
                context.safeString(R.string.brand_v2), context.safeString(R.string.blend_v2),
                context.safeString(R.string.type_v2), context.safeString(R.string.subgenre_v2),
                context.safeString(R.string.cut_v2), context.safeString(R.string.components_v2),
                context.safeString(R.string.flavoring_v2), context.safeString(R.string.no_of_tins_v2),
                context.safeString(R.string.rating_v2), context.safeString(R.string.favorite_v2),
                context.safeString(R.string.disliked_v2), context.safeString(R.string.production_status_v2),
                context.safeString(R.string.notes_v2)
            )
            .get()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for ((items, components, flavoring) in data) {
            val componentsString = formatLocalizedList(components.map { it.componentName.rtlIsolate() }.sortedWith(collator))
            val flavoringString = formatLocalizedList(flavoring.map { it.flavoringName.rtlIsolate() }.sortedWith(collator))
            val ratingString = exportRatingString(items.rating, maxRating, rounding)

            csvPrinter.printRecord(
                items.brand,
                items.blend,
                getLocalizedType(items.type, false),
                items.subGenre,
                items.cut,
                componentsString,
                flavoringString,
                items.quantity,
                ratingString,
                items.favorite,
                items.disliked,
                items.inProduction,
                items.notes,
            )
        }

        csvPrinter.flush()
        return csvWriter.toString()
    }

    fun exportTinsToCsv(data: List<TinExportData>): String {
        val context = getLocalizedContext(CellarApplication.instance)
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader(
                context.safeString(R.string.brand_v2), context.safeString(R.string.blend_v2),
                context.safeString(R.string.type_v2), context.safeString(R.string.subgenre_v2),
                context.safeString(R.string.cut_v2), context.safeString(R.string.components_v2),
                context.safeString(R.string.flavoring_v2), context.safeString(R.string.no_of_tins_v2),
                context.safeString(R.string.rating_v2), context.safeString(R.string.favorite_v2),
                context.safeString(R.string.disliked_v2), context.safeString(R.string.production_status_v2),
                context.safeString(R.string.notes_v2), context.safeString(R.string.container_v2),
                context.safeString(R.string.quantity_v2), context.safeString(R.string.manufacture_date_v2),
                context.safeString(R.string.cellar_date_v2), context.safeString(R.string.open_date_v2),
                context.safeString(R.string.finished_v2)
            )
            .get()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for (tin in data) {
            csvPrinter.printRecord(
                tin.brand,
                tin.blend,
                getLocalizedType(tin.type),
                tin.subGenre,
                tin.cut,
                tin.components,
                tin.flavoring,
                tin.quantity,
                tin.rating,
                tin.favorite,
                tin.disliked,
                tin.inProduction,
                tin.notes,
                tin.container,
                tin.tinQuantity,
                tin.manufactureDate,
                tin.cellarDate,
                tin.openDate,
                tin.finished
            )
        }

        csvPrinter.flush()
        return csvWriter.toString()
    }


}


sealed class CsvResult {
    data class Success(
        val header: List<String>,
        val firstFullRecord: List<String>,
        val columnCount: Int,
        val recordCount: Int
    ) : CsvResult()
    object Empty : CsvResult()
    data class Error(val exception: Exception) : CsvResult()
}