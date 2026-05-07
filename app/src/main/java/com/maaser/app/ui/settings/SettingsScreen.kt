package com.maaser.app.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.maaser.app.R
import com.maaser.app.data.model.AppLanguage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenExport: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val destinations by viewModel.destinations.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var newDestinationName by remember { mutableStateOf("") }
    var customPercentage by remember(settings.maaserPercentage) {
        mutableStateOf(settings.maaserPercentage.toString())
    }

    val signInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.handleSignInResult(task)
        }
    }

    syncMessage?.let { msg ->
        val text = when (msg) {
            "sync_success" -> stringResource(R.string.settings_sync_success)
            "backup_success" -> stringResource(R.string.settings_drive_backup_success)
            "restore_success" -> stringResource(R.string.settings_drive_restore_success)
            else -> null
        }
        if (text != null) {
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Maaser percentage
            Text(stringResource(R.string.settings_maaser_percentage), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            listOf(10.0, 15.0, 20.0).forEach { pct ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.maaserPercentage == pct,
                        onClick = { viewModel.setMaaserPercentage(pct) }
                    )
                    Text("${pct.toInt()}%")
                }
            }
            OutlinedTextField(
                value = customPercentage,
                onValueChange = { customPercentage = it; it.toDoubleOrNull()?.let { v -> viewModel.setMaaserPercentage(v) } },
                label = { Text(stringResource(R.string.settings_custom_percentage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Payment destinations
            Text(stringResource(R.string.settings_destinations), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            destinations.forEach { dest ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(dest.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteDestination(dest.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newDestinationName,
                    onValueChange = { newDestinationName = it },
                    label = { Text(stringResource(R.string.settings_add_destination)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = {
                    if (newDestinationName.isNotBlank()) {
                        viewModel.addDestination(newDestinationName)
                        newDestinationName = ""
                    }
                }) { Icon(Icons.Default.Add, contentDescription = null) }
            }

            // Account & Sync
            Text(stringResource(R.string.settings_account), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            if (settings.isGoogleSignedIn) {
                settings.userEmail?.let {
                    Text(
                        "${stringResource(R.string.settings_signed_in_as)}: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (settings.lastSyncAt > 0L) {
                    Text(
                        "${stringResource(R.string.settings_last_sync)}: ${dateFormatter.format(Date(settings.lastSyncAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.syncNow() },
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.settings_sync_now))
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.signOut(context as Activity) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_sign_out)) }
                }

                // Drive backup
                Text(stringResource(R.string.settings_drive_backup_title), style = MaterialTheme.typography.titleSmall)
                if (settings.driveLastBackupAt > 0L) {
                    Text(
                        "${stringResource(R.string.settings_drive_last_backup)}: ${dateFormatter.format(Date(settings.driveLastBackupAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.driveBackup() },
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_drive_backup)) }
                    OutlinedButton(
                        onClick = { viewModel.driveRestore() },
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_drive_restore)) }
                }

                syncMessage?.let {
                    val msg = when (it) {
                        "sync_success" -> stringResource(R.string.settings_sync_success)
                        "backup_success" -> stringResource(R.string.settings_drive_backup_success)
                        "restore_success" -> stringResource(R.string.settings_drive_restore_success)
                        "sync_failed", "backup_failed", "restore_failed" -> stringResource(R.string.settings_action_failed)
                        else -> null
                    }
                    msg?.let { text ->
                        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Button(
                    onClick = { signInLauncher.launch(signInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.settings_sign_in_google)) }
            }

            // Export
            Text(stringResource(R.string.settings_export), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            Button(onClick = onOpenExport, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.export_title))
            }

            // Language
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLanguage.entries.forEach { lang ->
                    val label = if (lang == AppLanguage.HEBREW)
                        stringResource(R.string.language_hebrew)
                    else
                        stringResource(R.string.language_english)
                    FilterChip(
                        selected = settings.appLanguage == lang,
                        onClick = { viewModel.setLanguage(lang, context as Activity) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
