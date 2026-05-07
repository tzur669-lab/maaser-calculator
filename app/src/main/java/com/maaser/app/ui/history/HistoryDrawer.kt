package com.maaser.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaser.app.R
import com.maaser.app.data.model.Transaction
import com.maaser.app.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryDrawer(viewModel: HistoryViewModel = hiltViewModel()) {
    val transactionsByMonth by viewModel.transactionsByMonth.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var deletingTransaction by remember { mutableStateOf<Transaction?>(null) }

    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { editingTransaction = null },
            onSave = { updated ->
                viewModel.updateTransaction(updated)
                editingTransaction = null
            }
        )
    }

    deletingTransaction?.let { tx ->
        AlertDialog(
            onDismissRequest = { deletingTransaction = null },
            title = { Text(stringResource(R.string.btn_confirm_delete)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteTransaction(tx.id); deletingTransaction = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.btn_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingTransaction = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        if (transactionsByMonth.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                transactionsByMonth.forEach { (monthKey, transactions) ->
                    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.maaserAmount }
                    val totalPaid = transactions.filter { it.type == TransactionType.PAYMENT }.sumOf { it.amount }
                    item(key = "header_$monthKey") {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(text = monthKey, style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        "${stringResource(R.string.history_total_income)}: ₪${"%.2f".format(totalIncome)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "${stringResource(R.string.history_total_paid)}: ₪${"%.2f".format(totalPaid)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (transaction.type == TransactionType.INCOME)
                                            "${stringResource(R.string.history_income_label)}: ₪${"%.2f".format(transaction.amount)}"
                                        else
                                            "${stringResource(R.string.history_payment_label)}: ₪${"%.2f".format(transaction.amount)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(transaction.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    transaction.note?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                IconButton(onClick = { editingTransaction = transaction }) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                                IconButton(onClick = { deletingTransaction = transaction }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
