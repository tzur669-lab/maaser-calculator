package com.maaser.app.ui.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maaser.app.data.model.ExportFormat
import com.maaser.app.data.model.ExportReport
import com.maaser.app.data.repository.ExportFilters
import com.maaser.app.domain.usecase.ExportCsvUseCase
import com.maaser.app.domain.usecase.ExportExcelUseCase
import com.maaser.app.domain.usecase.ExportPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportExcelUseCase: ExportExcelUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val exportPdfUseCase: ExportPdfUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _reportHistory = MutableStateFlow<List<ExportReport>>(emptyList())
    val reportHistory: StateFlow<List<ExportReport>> = _reportHistory.asStateFlow()

    private val historyFile = File(context.filesDir, "export_history.json")
    private val gson = Gson()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        try {
            if (historyFile.exists()) {
                val type = object : TypeToken<List<ExportReport>>() {}.type
                _reportHistory.value = gson.fromJson(historyFile.readText(), type) ?: emptyList()
            }
        } catch (e: Exception) {
            _reportHistory.value = emptyList()
        }
    }

    private fun saveHistory(reports: List<ExportReport>) {
        try {
            historyFile.writeText(gson.toJson(reports))
        } catch (e: Exception) { /* ignore */ }
    }

    fun export(filters: ExportFilters, format: ExportFormat) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val file = when (format) {
                    ExportFormat.EXCEL -> exportExcelUseCase(filters)
                    ExportFormat.CSV -> exportCsvUseCase(filters)
                    ExportFormat.PDF -> exportPdfUseCase(filters)
                }
                val mimeType = when (format) {
                    ExportFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ExportFormat.CSV -> "text/csv"
                    ExportFormat.PDF -> "application/pdf"
                }
                val uri = FileProvider.getUriForFile(context, "com.maaser.app.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, file.name).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                val report = ExportReport(
                    id = UUID.randomUUID().toString(),
                    format = format,
                    fromDate = filters.from,
                    toDate = filters.to,
                    recordCount = 0,
                    createdAt = System.currentTimeMillis()
                )
                val updated = (listOf(report) + _reportHistory.value).take(20)
                _reportHistory.value = updated
                saveHistory(updated)
            } catch (e: Exception) { /* handle silently */ }
            _isExporting.value = false
        }
    }

    fun removeReport(id: String) {
        val updated = _reportHistory.value.filter { it.id != id }
        _reportHistory.value = updated
        saveHistory(updated)
    }
}
