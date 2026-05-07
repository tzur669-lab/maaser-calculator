package com.maaser.app.util

import android.content.Context
import android.content.res.Configuration
import com.maaser.app.data.model.AppLanguage
import java.util.Locale

object LocaleHelper {
    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(config)
    }
}
