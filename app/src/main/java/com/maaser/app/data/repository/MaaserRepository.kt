package com.maaser.app.data.repository

import com.maaser.app.data.local.dao.PaymentDestinationDao
import com.maaser.app.data.local.dao.TransactionDao
import com.maaser.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class ExportFilters(
    val from: Long,
    val to: Long,
    val includeIncome: Boolean = true,
    val includePayments: Boolean = true,
    val destinationId: String? = null
)

@Singleton
class MaaserRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val destinationDao: PaymentDestinationDao
) {

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction.toEntity().copy(isSynced = false))
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(
            transaction.toEntity().copy(updatedAt = System.currentTimeMillis(), isSynced = false)
        )
    }

    suspend fun softDeleteTransaction(id: String) {
        transactionDao.softDelete(id, updatedAt = System.currentTimeMillis())
    }

    fun getBalance(): Flow<Double> = transactionDao.getBalance()

    fun getTransactionsByMonth(): Flow<Map<String, List<Transaction>>> =
        transactionDao.getAllActive().map { entities ->
            entities
                .map { it.toDomain() }
                .groupBy { transaction ->
                    val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
                    "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                }
                .toSortedMap(compareByDescending { it })
        }

    suspend fun getAllTransactions(): List<Transaction> =
        transactionDao.getAllActiveList().map { it.toDomain() }

    suspend fun getAllDestinations(): List<PaymentDestination> =
        destinationDao.getAllActiveList().map { it.toDomain() }

    suspend fun getUnsyncedTransactions(): List<Transaction> =
        transactionDao.getUnsynced().map { it.toDomain() }

    suspend fun markAsSynced(ids: List<String>) {
        transactionDao.markAsSynced(ids)
    }

    suspend fun getTransactionsForExport(filters: ExportFilters): List<Transaction> {
        return transactionDao.getForExport(filters.from, filters.to)
            .map { it.toDomain() }
            .filter { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> filters.includeIncome
                    TransactionType.PAYMENT -> filters.includePayments
                }
            }
            .filter { tx -> filters.destinationId == null || tx.destinationId == filters.destinationId }
    }

    fun getPaymentDestinations(): Flow<List<PaymentDestination>> =
        destinationDao.getAllActive().map { list -> list.map { it.toDomain() } }

    suspend fun insertPaymentDestination(destination: PaymentDestination) {
        destinationDao.insert(destination.toEntity())
    }

    suspend fun softDeletePaymentDestination(id: String) {
        destinationDao.softDelete(id)
    }
}
