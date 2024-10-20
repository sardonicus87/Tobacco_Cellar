package com.example.tobaccocellar.data

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
                val firstRecord =
                    if (records.size > 1) allRecords[1].toList().map { it }
                    else emptyList()
                val columnCount = records.maxOfOrNull { it.size() } ?: 0
                val recordCount = records.size
                CsvResult.Success(header, allRecords, firstRecord, columnCount, recordCount)
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

    fun exportToCsv(data: List<Items>): String {
        val csvWriter = StringWriter()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setQuoteMode(QuoteMode.ALL)
            .setEscape('"')
            .setHeader("Brand","Blend","Type","Quantity","Favorite","Disliked","Notes")
            .build()

        val csvPrinter = CSVPrinter(csvWriter, csvFormat)

        for (item in data) {
            csvPrinter.printRecord(item.brand, item.blend, item.type, item.quantity, item.favorite, item.disliked, item.notes)
        }

        csvPrinter.flush()
        return csvWriter.toString()
    }
}



sealed class CsvResult {
    data class Success(
        val header: List<String>,
        val allRecords: List<List<String>>,
        val firstRecord: List<String>,
        val columnCount: Int,
        val recordCount: Int
    ) : CsvResult()
    object Empty : CsvResult()
    data class Error(val exception: Exception) : CsvResult()
}