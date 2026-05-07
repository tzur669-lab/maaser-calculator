package com.maaser.app.data.remote

import com.google.firebase.database.FirebaseDatabase
import com.maaser.app.data.model.Transaction
import com.maaser.app.data.model.TransactionType
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealtimeRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private fun txRef(userId: String) =
        database.getReference("users/$userId/transactions")

    suspend fun uploadTransactions(userId: String, transactions: List<Transaction>) {
        val ref = txRef(userId)
        transactions.forEach { tx ->
            ref.child(tx.id).setValue(tx.toMap()).await()
        }
    }

    suspend fun getTransactionsSince(userId: String, since: Long): List<Map<String, Any>> {
        val snapshot = txRef(userId)
            .orderByChild("updatedAt")
            .startAt((since + 1).toDouble())
            .get().await()
        return snapshot.children.mapNotNull {
            @Suppress("UNCHECKED_CAST")
            it.value as? Map<String, Any>
        }
    }

    fun toTransaction(map: Map<String, Any>): Transaction? = try {
        Transaction(
            id = map["id"] as? String ?: UUID.randomUUID().toString(),
            type = TransactionType.valueOf(map["type"] as String),
            amount = (map["amount"] as Number).toDouble(),
            maaserAmount = (map["maaserAmount"] as Number).toDouble(),
            source = map["source"] as? String,
            destinationId = map["destinationId"] as? String,
            destinationFreeText = map["destinationFreeText"] as? String,
            note = map["note"] as? String,
            date = (map["date"] as Number).toLong(),
            createdAt = (map["createdAt"] as Number).toLong(),
            updatedAt = (map["updatedAt"] as Number).toLong(),
            isDeleted = map["isDeleted"] as? Boolean ?: false,
            isSynced = true
        )
    } catch (e: Exception) { null }

    private fun Transaction.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "type" to type.name, "amount" to amount,
        "maaserAmount" to maaserAmount, "source" to source,
        "destinationId" to destinationId, "destinationFreeText" to destinationFreeText,
        "note" to note, "date" to date, "createdAt" to createdAt,
        "updatedAt" to updatedAt, "isDeleted" to isDeleted, "isSynced" to true
    )
}
