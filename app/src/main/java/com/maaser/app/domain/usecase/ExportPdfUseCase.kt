package com.maaser.app.domain.usecase

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.maaser.app.data.model.TransactionType
import com.maaser.app.data.repository.ExportFilters
import com.maaser.app.data.repository.MaaserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportPdfUseCase @Inject constructor(
    private val repository: MaaserRepository,
    @ApplicationContext private val context: Context
) {
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val MARGIN = 40f
    private val ROW_HEIGHT = 22f
    private val ROWS_PER_PAGE = 30

    suspend operator fun invoke(filters: ExportFilters): File {
        val transactions = repository.getTransactionsForExport(filters)
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val document = PdfDocument()

        val titlePaint = Paint().apply { textSize = 16f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val headerPaint = Paint().apply { textSize = 11f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val bodyPaint = Paint().apply { textSize = 10f; isAntiAlias = true }
        val linePaint = Paint().apply { strokeWidth = 0.5f }

        val chunks = transactions.chunked(ROWS_PER_PAGE)
        val pageCount = maxOf(1, chunks.size)

        for (pageIndex in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN + 20f

            if (pageIndex == 0) {
                canvas.drawText("דוח מעשרות", PAGE_WIDTH - MARGIN, y, titlePaint)
                y += 30f
                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.maaserAmount }
                val totalPaid = transactions.filter { it.type == TransactionType.PAYMENT }.sumOf { it.amount }
                canvas.drawText("סה\"כ מעשר: ₪${"%.2f".format(totalIncome)}   |   שולם: ₪${"%.2f".format(totalPaid)}   |   יתרה: ₪${"%.2f".format(totalIncome - totalPaid)}", PAGE_WIDTH - MARGIN, y, bodyPaint)
                y += 25f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN.toFloat(), y, linePaint)
                y += 10f
            }

            drawRow(canvas, y, "תאריך", "סוג", "סכום", "מעשר", "יעד", headerPaint)
            y += ROW_HEIGHT
            canvas.drawLine(MARGIN, y - 2f, PAGE_WIDTH - MARGIN.toFloat(), y - 2f, linePaint)

            val pageTransactions = if (pageIndex < chunks.size) chunks[pageIndex] else emptyList()
            pageTransactions.forEach { tx ->
                val date = dateFormatter.format(Date(tx.date))
                val type = if (tx.type == TransactionType.INCOME) "הכנסה" else "תשלום"
                val amount = "₪${"%.2f".format(tx.amount)}"
                val maaser = if (tx.type == TransactionType.INCOME) "₪${"%.2f".format(tx.maaserAmount)}" else "-"
                val dest = tx.destinationFreeText ?: tx.source ?: ""
                drawRow(canvas, y, date, type, amount, maaser, dest, bodyPaint)
                y += ROW_HEIGHT
            }

            document.finishPage(page)
        }

        val file = File(context.cacheDir, "maaser_export_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawRow(canvas: Canvas, y: Float, col1: String, col2: String, col3: String, col4: String, col5: String, paint: Paint) {
        val right = PAGE_WIDTH - MARGIN
        canvas.drawText(col1, right, y, paint)
        canvas.drawText(col2, right - 80f, y, paint)
        canvas.drawText(col3, right - 160f, y, paint)
        canvas.drawText(col4, right - 240f, y, paint)
        canvas.drawText(col5, right - 320f, y, paint)
    }
}
