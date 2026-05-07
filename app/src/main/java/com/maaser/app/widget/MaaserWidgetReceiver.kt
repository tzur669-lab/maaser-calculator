package com.maaser.app.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class MaaserWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MaaserWidget()
}
