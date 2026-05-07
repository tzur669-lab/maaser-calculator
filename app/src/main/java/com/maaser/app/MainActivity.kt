package com.maaser.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.maaser.app.data.local.UserSettingsDataStore
import com.maaser.app.data.model.AppLanguage
import com.maaser.app.data.model.UserSettings
import com.maaser.app.ui.addincome.AddIncomeSheet
import com.maaser.app.ui.addpayment.AddPaymentSheet
import com.maaser.app.ui.export.ExportScreen
import com.maaser.app.ui.history.HistoryDrawer
import com.maaser.app.ui.main.MainScreen
import com.maaser.app.ui.settings.SettingsScreen
import com.maaser.app.ui.theme.MaaserTheme
import com.maaser.app.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsDataStore: UserSettingsDataStore

    override fun attachBaseContext(newBase: Context) {
        val language = runBlocking {
            try { settingsDataStore.settings.first().appLanguage }
            catch (e: Exception) { AppLanguage.HEBREW }
        }
        super.attachBaseContext(LocaleHelper.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaaserTheme {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val settings by settingsDataStore.settings.collectAsState(initial = UserSettings())

                var showAddIncome by remember { mutableStateOf(false) }
                var showAddPayment by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                var showExport by remember { mutableStateOf(false) }

                val layoutDirection = if (settings.appLanguage == AppLanguage.HEBREW)
                    LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = { HistoryDrawer() }
                    ) {
                        when {
                            showExport -> {
                                BackHandler { showExport = false }
                                ExportScreen(onBack = { showExport = false })
                            }
                            showSettings -> {
                                BackHandler { showSettings = false }
                                SettingsScreen(
                                    onBack = { showSettings = false },
                                    onOpenExport = { showExport = true }
                                )
                            }
                            else -> {
                                MainScreen(
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onOpenSettings = { showSettings = true },
                                    onAddIncome = { showAddIncome = true },
                                    onAddPayment = { showAddPayment = true }
                                )
                            }
                        }

                        if (showAddIncome) {
                            AddIncomeSheet(onDismiss = { showAddIncome = false })
                        }
                        if (showAddPayment) {
                            AddPaymentSheet(onDismiss = { showAddPayment = false })
                        }
                    }
                }
            }
        }
    }
}
