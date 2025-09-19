package com.sardonicus.tobaccocellar.data

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import java.io.FileOutputStream
import kotlin.math.roundToInt

class PrintHelper(
    private val context: Context,
    private val documentName: String,
    private val content: String,
    private val fontSize: Float = 12f,
    marginMultiplier: Double = 1.0,
) : PrintDocumentAdapter() {
    private var pageHeight: Int = 0
    private var pageWidth: Int = 0

    private var pageContentHeight: Int = 0
    private var pageContentWidth: Int = 0

    @Volatile
    private var staticLayout: StaticLayout? = null
    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = fontSize
        isAntiAlias = true
    }
    private val margin = (72 * marginMultiplier)

    private var pageLineRanges = mutableListOf<PageLineRange>()
    private var calculatedTotalPages: Int = 0

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        pageHeight = newAttributes.mediaSize!!.heightMils * 72 / 1000
        pageWidth = newAttributes.mediaSize!!.widthMils * 72 / 1000

        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        this.pageContentWidth = pageWidth - (2 * margin).roundToInt()
        this.pageContentHeight = pageHeight - (2 * margin).roundToInt()

        if (this.pageContentWidth <= 0 || this.pageContentHeight <= 0) {
            callback.onLayoutFailed("Page dimensions are invalid")
            return
        }

        staticLayout = StaticLayout.Builder.obtain(
            content, 0, content.length, textPaint, this.pageContentWidth)
            .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(true)
            .build()

        if (staticLayout == null || staticLayout!!.lineCount == 0) {
            calculatedTotalPages = 1
            pageLineRanges.clear()
            pageLineRanges.add(PageLineRange(0, -1))
        } else {
            pageLineRanges.clear()
            var currentLine = 0
            calculatedTotalPages = 0

            while (currentLine < staticLayout!!.lineCount) {
                calculatedTotalPages++
                val pageStartLine = currentLine
                val firstLineTop = staticLayout!!.getLineTop(pageStartLine)
                val heightOfFirst = staticLayout!!.getLineBottom(pageStartLine) - firstLineTop

                if (heightOfFirst > this.pageContentHeight && this.pageContentHeight > 0) {
                    pageLineRanges.add(PageLineRange(pageStartLine, pageStartLine - 1))
                    currentLine++
                    continue
                }

                var pageEndLine = pageStartLine

                for (lineIndex in pageStartLine until staticLayout!!.lineCount) {
                    val currentLineBottom = staticLayout!!.getLineBottom(lineIndex) - firstLineTop
                    if (currentLineBottom <= this.pageContentHeight) {
                        pageEndLine = lineIndex
                    } else {
                        break
                    }
                }

                pageLineRanges.add(PageLineRange(pageStartLine, pageEndLine))
                currentLine = pageEndLine + 1

            }

            if (calculatedTotalPages == 0 && staticLayout!!.lineCount > 0) {
                calculatedTotalPages = 1
                if (this.pageContentHeight > 0 && (staticLayout!!.getLineBottom(0) - staticLayout!!.getLineTop(0)) <= pageContentHeight) {
                    pageLineRanges.add(PageLineRange(0, staticLayout!!.lineCount - 1))
                } else {
                    pageLineRanges.add(PageLineRange(0, -1))
                }
            }
        }

        if (calculatedTotalPages > 0) {
            val info = PrintDocumentInfo.Builder(documentName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(calculatedTotalPages)
                .build()
            val layoutChanged = oldAttributes != newAttributes
            callback.onLayoutFinished(info, layoutChanged)
        } else {
            callback.onLayoutFailed("No content to print")
        }
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        if (staticLayout == null || destination == null || pageLineRanges.isEmpty()) {
            callback.onWriteFailed(null)
            return
        }

        val pdfDocument = PdfDocument()

        try {
            for (pageIndex in 0 until calculatedTotalPages) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    return
                }

                if (pageRangesContainPage(pages, pageIndex)) {
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    val currentRange = pageLineRanges[pageIndex]

                    if (currentRange.startLine <= currentRange.endLine) {
                        val topOfContent = staticLayout!!.getLineTop(currentRange.startLine)

                        canvas.withTranslation(margin.toFloat(), margin.toFloat()) {
                            withClip(0f, 0f, pageContentWidth.toFloat(), pageContentHeight.toFloat()) {
                                withTranslation(y = -topOfContent.toFloat()) {
                                    staticLayout!!.draw(this)
                                }
                            }
                        }
                    }
                    pdfDocument.finishPage(page)
                }
            }

            pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
            callback.onWriteFinished(pages ?: Array(calculatedTotalPages) { PageRange(it, it) })
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        } finally {
            pdfDocument.close()
        }
    }


    private fun pageRangesContainPage(pageRanges: Array<out PageRange>?, page: Int): Boolean {
        if (pageRanges == null) {
            return true
        }
        for (range in pageRanges) {
            if (page >= range.start && page <= range.end) {
                return true
            }
        }
        return false
    }

}

data class PageLineRange(val startLine: Int, val endLine: Int)