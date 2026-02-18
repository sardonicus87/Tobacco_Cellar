package com.sardonicus.tobaccocellar.data

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

    private var overflowingLineHeight = -1
    private val additionalPageClipping = mutableMapOf<Int, Int>()

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
            additionalPageClipping.clear()
            pageLineRanges.add(PageLineRange(0, -1))
        } else {
            pageLineRanges.clear()
            additionalPageClipping.clear()
            var currentLine = 0
            calculatedTotalPages = 0

            while (currentLine < staticLayout!!.lineCount) {
                calculatedTotalPages++
                var effectiveStartLine = currentLine
                while (effectiveStartLine < staticLayout!!.lineCount) {
                    val startOffset = staticLayout!!.getLineStart(effectiveStartLine)
                    val endOffset = staticLayout!!.getLineEnd(effectiveStartLine)
                    val lineText = if (startOffset < endOffset) content.substring(startOffset, endOffset) else ""
                    if (lineText.isBlank()) {
                        effectiveStartLine++
                    } else {
                        break
                    }
                }

                currentLine = effectiveStartLine
                if (currentLine >= staticLayout!!.lineCount) {
                    break
                }

                val pageStartLine = currentLine
                val firstLineTop = staticLayout!!.getLineTop(pageStartLine)
                val heightOfFirst = staticLayout!!.getLineBottom(pageStartLine) - firstLineTop

                if (this.pageContentHeight in 1..<heightOfFirst) {
                    pageLineRanges.add(PageLineRange(pageStartLine, pageStartLine - 1))
                    currentLine++
                    continue
                }

                var pageEndLine = pageStartLine
                overflowingLineHeight = -1

                for (lineIndex in pageStartLine until staticLayout!!.lineCount) {
                    val currentLineBottom = staticLayout!!.getLineBottom(lineIndex) - firstLineTop
                    if (currentLineBottom <= this.pageContentHeight) {
                        pageEndLine = lineIndex
                    } else {
                        val overflowingLine = staticLayout!!.getLineTop(lineIndex) - firstLineTop
                        overflowingLineHeight = if (overflowingLine < this.pageContentHeight) {
                            this.pageContentHeight - overflowingLine } else { 0 }

                        break
                    }
                }

                pageLineRanges.add(PageLineRange(pageStartLine, pageEndLine))

                if (overflowingLineHeight > 0) {
                    additionalPageClipping[calculatedTotalPages - 1] = overflowingLineHeight
                } else { additionalPageClipping[calculatedTotalPages - 1] = 0 }

                currentLine = pageEndLine + 1

            }

            if (calculatedTotalPages == 0 && staticLayout!!.lineCount > 0) {
                calculatedTotalPages = 1
                if (this.pageContentHeight > 0 && (staticLayout!!.getLineBottom(0) - staticLayout!!.getLineTop(0)) <= pageContentHeight) {
                    pageLineRanges.add(PageLineRange(0, staticLayout!!.lineCount - 1))
                } else {
                    pageLineRanges.add(PageLineRange(0, -1))
                }
                if (!additionalPageClipping.containsKey(0) && calculatedTotalPages == 1) {
                    additionalPageClipping[0] = 0
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
            repeat(calculatedTotalPages) { pageIndex ->
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

                        val extraClipBottom = additionalPageClipping.getOrDefault(pageIndex, 0)
                        val adjustedContentHeight = (pageContentHeight - extraClipBottom).toFloat()
                        val finalHeight = if (adjustedContentHeight < 0f) 0f else adjustedContentHeight

                        canvas.withTranslation(margin.toFloat(), margin.toFloat()) {
                            withClip(0f, 0f, pageContentWidth.toFloat(), finalHeight) {
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