@file:Suppress("unused")

package com.sardonicus.tobaccocellar.data

import com.sardonicus.tobaccocellar.ui.settings.exportRatingString
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.QuoteMode
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.Charset
import java.text.ParseException

class CsvHelper {

    fun csvFileReader(inputStream: InputStream): CsvResult {
        return try {
            val defaultParser = CSVParser.parse(inputStream, Charset.defaultCharset(),
                CSVFormat.DEFAULT)
            val records = defaultParser.records

            if (records.isNotEmpty()) {
                val header = records.first().toList().map { it.toString() }
                val allRecords = records.map { record -> record.toList().map { it.toString() }}
                val columnCount = records.maxOfOrNull { it.size() } ?: 0
                val firstFullRecord =
                    allRecords.drop(1).firstOrNull { it.size == columnCount } ?: emptyList()
                val recordCount = records.size
                CsvResult.Success(header, allRecords, firstFullRecord, columnCount, recordCount)
            } else {
                CsvResult.Empty
            }
        } catch (e: ParseException) {
            CsvResult.Error(e)
        } catch (e: Exception) {
            CsvResult.Error(e)
        }
    }

    fun exportToCsv(data: List<ItemsComponentsAndTins>, maxRating: Int, rounding: Int): String {
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader(
                "Brand", "Blend", "Type", "Subgenre", "Cut", "Components", "Flavoring", "No. of Tins",
                "Rating", "Favorite", "Disliked", "Production Status", "Notes"
            )
            .get()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for (item in data) {
            val componentsString = item.components.joinToString(", ") { it.componentName }
            val flavoringString = item.flavoring.joinToString(", ") { it.flavoringName }
            val ratingString = exportRatingString(item.items.rating, maxRating, rounding)

            csvPrinter.printRecord(
                item.items.brand,
                item.items.blend,
                item.items.type,
                item.items.subGenre,
                item.items.cut,
                componentsString,
                flavoringString,
                item.items.quantity,
                ratingString,
                item.items.favorite,
                item.items.disliked,
                item.items.inProduction,
                item.items.notes,
            )
        }

        csvPrinter.flush()
        return csvWriter.toString()
    }

    fun exportTinsToCsv(data: List<TinExportData>): String {
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader(
                "Brand", "Blend", "Type", "Subgenre", "Cut", "Components", "Flavoring",
                "No. of Tins", "Rating", "Favorite", "Disliked", "Production Status", "Notes", "Container",
                "Quantity", "Manufacture Date", "Cellar Date", "Open Date", "Finished"
            )
            .get()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for (tin in data) {
            csvPrinter.printRecord(
                tin.brand,
                tin.blend,
                tin.type,
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
        val allRecords: List<List<String>>,
        val firstFullRecord: List<String>,
        val columnCount: Int,
        val recordCount: Int
    ) : CsvResult()
    object Empty : CsvResult()
    data class Error(val exception: Exception) : CsvResult()
}