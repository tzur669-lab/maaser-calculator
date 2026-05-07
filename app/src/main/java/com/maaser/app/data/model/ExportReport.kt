package com.maaser.app.data.model

data class ExportReport(
    val id: String,
    val format: ExportFormat,
    val fromDate: Long,
    val toDate: Long,
    val recordCount: Int,
    val createdAt: Long
)

enum class ExportFormat { EXCEL, CSV, PDF }
