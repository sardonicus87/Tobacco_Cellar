package com.sardonicus.tobaccocellar.data

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
            println("Error reading CSV: ${e.message}")
            CsvResult.Error(e)
        } catch (e: Exception) {
            println("Error reading CSV: ${e.message}")
            CsvResult.Error(e)
        }
    }

    fun exportToCsv(data: List<ItemsWithComponents>): String {
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader(
                "Brand", "Blend", "Type", "Sub-Genre", "Cut", "No. of Tins", "Favorite",
                "Disliked", "Production Status", "Notes", "Components"
            )
            .build()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for (itemWithComponents in data) {
            val componentsString = itemWithComponents.components.joinToString(", ") { it.componentName }

            csvPrinter.printRecord(
                itemWithComponents.item.brand,
                itemWithComponents.item.blend,
                itemWithComponents.item.type,
                itemWithComponents.item.subGenre,
                itemWithComponents.item.cut,
                itemWithComponents.item.quantity,
                itemWithComponents.item.favorite,
                itemWithComponents.item.disliked,
                itemWithComponents.item.inProduction,
                itemWithComponents.item.notes,
                componentsString
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
                "Brand", "Blend", "Type", "Sub-Genre", "Cut", "Favorite", "Disliked",
                "Production Status", "Notes", "Components", "Container", "Quantity",
                "Manufacture Date", "Cellar Date", "Open Date"
            )
            .build()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for (tinData in data) {
            csvPrinter.printRecord(
                tinData.brand,
                tinData.blend,
                tinData.type,
                tinData.subGenre,
                tinData.cut,
                tinData.favorite,
                tinData.disliked,
                tinData.inProduction,
                tinData.notes,
                tinData.components,
                tinData.container,
                tinData.quantity,
                tinData.manufactureDate,
                tinData.cellarDate,
                tinData.openDate
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