package com.maaser.app.ui.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maaser.app.R
import com.maaser.app.data.model.ExportFormat
import com.maaser.app.data.repository.ExportFilters
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val isExporting by viewModel.isExporting.collectAsState()
    val reportHistory by viewModel.reportHistory.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val displayFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var selectedFormat by remember { mutableStateOf(ExportFormat.EXCEL) }
    var fromDate by remember {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        mutableStateOf(cal.timeInMillis)
    }
    var toDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var includeIncome by remember { mutableStateOf(true) }
    var includePayments by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.export_date_range), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { fromDate = 0L },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (fromDate == 0L) stringResource(R.string.export_all_time) else dateFormatter.format(Date(fromDate)))
                    }
                    Text("—")
                    OutlinedButton(
                        onClick = { toDate = System.currentTimeMillis() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(dateFormatter.format(Date(toDate)))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = fromDate == 0L,
                        onClick = { fromDate = 0L },
                        label = { Text(stringResource(R.string.export_all_time)) }
                    )
                    FilterChip(
                        selected = run {
                            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                            fromDate > cal.timeInMillis - 86400000 && fromDate < cal.timeInMillis + 86400000
                        },
                        onClick = {
                            fromDate = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
                            toDate = System.currentTimeMillis()
                        },
                        label = { Text(stringResource(R.string.export_last_month)) }
                    )
                    FilterChip(
                        selected = run {
                            val cal = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }
                            fromDate > cal.timeInMillis - 86400000 && fromDate < cal.timeInMillis + 86400000
                        },
                        onClick = {
                            fromDate = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.timeInMillis
                            toDate = System.currentTimeMillis()
                        },
                        label = { Text(stringResource(R.string.export_last_year)) }
                    )
                }
            }
            item {
                Text(stringResource(R.string.export_content), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeIncome, onCheckedChange = { includeIncome = it })
                    Text(stringResource(R.string.export_type_income))
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = includePayments, onCheckedChange = { includePayments = it })
                    Text(stringResource(R.string.export_type_payment))
                }
            }
            item {
                Text(stringResource(R.string.export_format), style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = {
                                Text(
                                    when (format) {
                                        ExportFormat.EXCEL -> stringResource(R.string.export_format_excel)
                                        ExportFormat.CSV -> stringResource(R.string.export_format_csv)
                                        ExportFormat.PDF -> stringResource(R.string.export_format_pdf)
                                    }
                                )
                            }
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val from = if (fromDate == 0L) 0L else fromDate
                        val to = if (toDate == 0L) System.currentTimeMillis() else toDate
                        viewModel.export(
                            ExportFilters(from = from, to = to, includeIncome = includeIncome, includePayments = includePayments),
                            selectedFormat
                        )
                    },
                    enabled = !isExporting && (includeIncome || includePayments),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.export_btn))
                }
            }

            if (reportHistory.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.export_history_title), style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                }
                items(reportHistory, key = { it.id }) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = report.format.name,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = displayFormatter.format(Date(report.createdAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.removeReport(report.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
