package com.maaser.app.domain.usecase

import android.content.Context
import com.maaser.app.data.model.Transaction
import com.maaser.app.data.model.TransactionType
import com.maaser.app.data.repository.ExportFilters
import com.maaser.app.data.repository.MaaserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportExcelUseCase @Inject constructor(
    private val repository: MaaserRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(filters: ExportFilters): File {
        val transactions = repository.getTransactionsForExport(filters)
        val workbook = XSSFWorkbook()
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        buildMainSheet(workbook, transactions, dateFormatter)
        buildSummarySheet(workbook, transactions)
        val file = File(context.cacheDir, "maaser_export_${System.currentTimeMillis()}.xlsx")
        file.outputStream().use { workbook.write(it) }
        workbook.close()
        return file
    }

    private fun buildMainSheet(workbook: XSSFWorkbook, transactions: List<Transaction>, dateFormatter: SimpleDateFormat) {
        val sheet = workbook.createSheet("פירוט").apply { isRightToLeft = true }
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_YELLOW.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
        }
        val headers = listOf("תאריך","סוג","סכום הכנסה","מעשר מחושב","מקור","יעד","הערה","יתרה מצטברת")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title -> headerRow.createCell(i).apply { setCellValue(title); cellStyle = headerStyle } }
        var runningBalance = 0.0
        transactions.forEachIndexed { index, tx ->
            runningBalance += when (tx.type) { TransactionType.INCOME -> tx.maaserAmount; TransactionType.PAYMENT -> -tx.amount }
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormatter.format(Date(tx.date)))
            row.createCell(1).setCellValue(if (tx.type == TransactionType.INCOME) "הכנסה" else "תשלום")
            row.createCell(2).setCellValue(if (tx.type == TransactionType.INCOME) tx.amount else 0.0)
            row.createCell(3).setCellValue(if (tx.type == TransactionType.INCOME) tx.maaserAmount else 0.0)
            row.createCell(4).setCellValue(tx.source ?: "")
            row.createCell(5).setCellValue(tx.destinationFreeText ?: "")
            row.createCell(6).setCellValue(tx.note ?: "")
            row.createCell(7).setCellValue(runningBalance)
        }
        headers.indices.forEach { sheet.autoSizeColumn(it) }
    }

    private fun buildSummarySheet(workbook: XSSFWorkbook, transactions: List<Transaction>) {
        val sheet = workbook.createSheet("סיכום").apply { isRightToLeft = true }
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.maaserAmount }
        val totalPaid = transactions.filter { it.type == TransactionType.PAYMENT }.sumOf { it.amount }
        listOf(listOf("סה\"כ מעשרות לתשלום", totalIncome), listOf("סה\"כ ששולם", totalPaid), listOf("יתרה", totalIncome - totalPaid))
            .forEachIndexed { i, rowData ->
                val row = sheet.createRow(i)
                row.createCell(0).setCellValue(rowData[0] as String)
                row.createCell(1).setCellValue(rowData[1] as Double)
            }
    }
}
