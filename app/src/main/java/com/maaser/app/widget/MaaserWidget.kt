package com.maaser.app.widget

import android.content.Context
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.sp
import com.maaser.app.data.repository.MaaserRepository
import com.maaser.app.ui.theme.BalanceNegative
import com.maaser.app.ui.theme.BalancePositive
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import androidx.compose.ui.unit.dp

class MaaserWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun repository(): MaaserRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val balance = entryPoint.repository().getBalance().first()
        val isOwed = balance > 0
        val color = if (isOwed) ColorProvider(BalanceNegative) else ColorProvider(BalancePositive)

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("מחשבון מעשרות", style = TextStyle(fontSize = 11.sp))
                Text("₪${"%.2f".format(abs(balance))}", style = TextStyle(color = color, fontSize = 28.sp))
                Text(if (isOwed) "חייב" else "בפלוס", style = TextStyle(color = color, fontSize = 13.sp))
            }
        }
    }
}
