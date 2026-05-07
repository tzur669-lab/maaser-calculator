package com.maaser.app.domain.usecase

import android.content.Context
import com.maaser.app.data.model.TransactionType
import com.maaser.app.data.repository.ExportFilters
import com.maaser.app.data.repository.MaaserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportCsvUseCase @Inject constructor(
    private val repository: MaaserRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(filters: ExportFilters): File {
        val transactions = repository.getTransactionsForExport(filters)
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val csv = buildString {
            append("תאריך,סוג,סכום הכנסה,מעשר מחושב,סכום תשלום,מקור,יעד,הערה\n")
            transactions.forEach { tx ->
                val date = dateFormatter.format(Date(tx.date))
                val type = if (tx.type == TransactionType.INCOME) "הכנסה" else "תשלום"
                val incomeAmount = if (tx.type == TransactionType.INCOME) tx.amount else 0.0
                val maaserAmount = if (tx.type == TransactionType.INCOME) tx.maaserAmount else 0.0
                val paymentAmount = if (tx.type == TransactionType.PAYMENT) tx.amount else 0.0
                val source = tx.source?.replace(",", ";") ?: ""
                val dest = (tx.destinationFreeText ?: "").replace(",", ";")
                val note = (tx.note ?: "").replace(",", ";")
                append("$date,$type,$incomeAmount,$maaserAmount,$paymentAmount,$source,$dest,$note\n")
            }
        }
        val file = File(context.cacheDir, "maaser_export_${System.currentTimeMillis()}.csv")
        file.writeText(csv, Charsets.UTF_8)
        return file
    }
}
