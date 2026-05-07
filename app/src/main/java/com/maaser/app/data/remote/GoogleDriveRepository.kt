package com.maaser.app.data.remote

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.Gson
import com.maaser.app.data.model.PaymentDestination
import com.maaser.app.data.model.Transaction
import com.maaser.app.data.repository.MaaserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class DriveBackupData(
    val version: Int = 1,
    val exportedAt: Long,
    val maaserPercentage: Double,
    val transactions: List<Transaction>,
    val destinations: List<PaymentDestination>
)

@Singleton
class GoogleDriveRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MaaserRepository
) {
    private val BACKUP_FILE_NAME = "maaser_backup.json"
    private val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.appdata"
    private val gson = Gson()

    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
            GoogleAuthUtil.getToken(context, account.account!!, DRIVE_SCOPE)
        } catch (e: Exception) { null }
    }

    suspend fun backup(maaserPercentage: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = getAccessToken() ?: return@withContext false
            val transactions = repository.getAllTransactions()
            val destinations = repository.getAllDestinations()
            val data = DriveBackupData(
                exportedAt = System.currentTimeMillis(),
                maaserPercentage = maaserPercentage,
                transactions = transactions,
                destinations = destinations
            )
            val json = gson.toJson(data)
            val existingFileId = findBackupFileId(token)
            if (existingFileId != null) {
                updateDriveFile(token, existingFileId, json)
            } else {
                createDriveFile(token, json)
            }
            true
        } catch (e: Exception) { false }
    }

    suspend fun restore(): DriveBackupData? = withContext(Dispatchers.IO) {
        try {
            val token = getAccessToken() ?: return@withContext null
            val fileId = findBackupFileId(token) ?: return@withContext null
            val json = downloadDriveFile(token, fileId) ?: return@withContext null
            gson.fromJson(json, DriveBackupData::class.java)
        } catch (e: Exception) { null }
    }

    private fun findBackupFileId(token: String): String? {
        val url = URL("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&fields=files(id,name)&q=name='$BACKUP_FILE_NAME'")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        if (conn.responseCode != 200) return null
        val response = conn.inputStream.bufferedReader().readText()
        val files = JSONObject(response).getJSONArray("files")
        return if (files.length() > 0) files.getJSONObject(0).getString("id") else null
    }

    private fun createDriveFile(token: String, json: String) {
        val boundary = "boundary_maaser"
        val url = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.doOutput = true
        val metadata = """{"name":"$BACKUP_FILE_NAME","parents":["appDataFolder"]}"""
        val body = "--$boundary\r\nContent-Type: application/json\r\n\r\n$metadata\r\n--$boundary\r\nContent-Type: application/json\r\n\r\n$json\r\n--$boundary--"
        OutputStreamWriter(conn.outputStream).use { it.write(body) }
        conn.responseCode
    }

    private fun updateDriveFile(token: String, fileId: String, json: String) {
        val url = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PATCH"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        OutputStreamWriter(conn.outputStream).use { it.write(json) }
        conn.responseCode
    }

    private fun downloadDriveFile(token: String, fileId: String): String? {
        val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        if (conn.responseCode != 200) return null
        return BufferedReader(InputStreamReader(conn.inputStream)).readText()
    }
}
