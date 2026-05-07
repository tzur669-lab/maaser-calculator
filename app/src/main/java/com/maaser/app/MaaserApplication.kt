package com.maaser.app

import android.app.Application
import android.content.Context
import com.maaser.app.data.model.AppLanguage
import com.maaser.app.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MaaserApplication : Application() {

    @Inject
    lateinit var userSettingsDataStore: com.maaser.app.data.local.UserSettingsDataStore

    override fun attachBaseContext(base: Context) {
        val language = runBlocking {
            try {
                userSettingsDataStore.settings.first().appLanguage
            } catch (e: Exception) {
                AppLanguage.HEBREW
            }
        }
        super.attachBaseContext(LocaleHelper.wrap(base, language))
    }
}
