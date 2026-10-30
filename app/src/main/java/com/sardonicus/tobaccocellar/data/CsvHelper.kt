package com.sardonicus.tobaccocellar.data

import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.R
import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
import com.sardonicus.tobaccocellar.ui.utilities.getLocalizedContext
import com.sardonicus.tobaccocellar.ui.utilities.getLocalizedType
import com.sardonicus.tobaccocellar.ui.utilities.safeString
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
        } catch (e: Exception) {
            CsvResult.Error(e)
        }
    }

    fun exportToCsv(data: List<ItemsComponentsAndTins>, maxRating: Int, rounding: Int): String {
        val context = getLocalizedContext(CellarApplication.instance)
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader(
                context.safeString(R.string.brand), context.safeString(R.string.blend),
                context.safeString(R.string.type), context.safeString(R.string.subgenre),
                context.safeString(R.string.cut), context.safeString(R.string.components),
                context.safeString(R.string.flavoring), context.safeString(R.string.no_of_tins),
                context.safeString(R.string.rating), context.safeString(R.string.favorite),
                context.safeString(R.string.disliked), context.safeString(R.string.production_status),
                context.safeString(R.string.notes)
            )
            .get()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for ((items, components, flavoring) in data) {
            val componentsString = components.joinToString(", ") { it.componentName }
            val flavoringString = flavoring.joinToString(", ") { it.flavoringName }
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
                context.safeString(R.string.brand), context.safeString(R.string.blend),
                context.safeString(R.string.type), context.safeString(R.string.subgenre),
                context.safeString(R.string.cut), context.safeString(R.string.components),
                context.safeString(R.string.flavoring), context.safeString(R.string.no_of_tins),
                context.safeString(R.string.rating), context.safeString(R.string.favorite),
                context.safeString(R.string.disliked), context.safeString(R.string.production_status),
                context.safeString(R.string.notes), context.safeString(R.string.container),
                context.safeString(R.string.quantity), context.safeString(R.string.manufacture_date),
                context.safeString(R.string.cellar_date), context.safeString(R.string.open_date),
                context.safeString(R.string.finished)
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