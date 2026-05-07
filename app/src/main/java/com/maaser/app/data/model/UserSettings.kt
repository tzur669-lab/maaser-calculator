package com.maaser.app.data.model

data class UserSettings(
    val maaserPercentage: Double = 10.0,
    val isGoogleSignedIn: Boolean = false,
    val userId: String? = null,
    val userEmail: String? = null,
    val lastSyncAt: Long = 0L,
    val driveLastBackupAt: Long = 0L,
    val appLanguage: AppLanguage = AppLanguage.HEBREW
)

enum class AppLanguage(val code: String) {
    HEBREW("he"),
    ENGLISH("en")
}
