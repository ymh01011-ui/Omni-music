package com.omnimusic.player.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Placeholder home screen widget. Will be expanded in Phase 4 to show
 * artwork + playback controls bound to the live PlaybackService state.
 */
class OmniMusicWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: android.content.Context,
        id: androidx.glance.GlanceId
    ) {
        // Intentionally minimal for now; real composable content (artwork,
        // play/pause, next/previous bound to MediaController state) is
        // implemented in the Phase 4 widget step.
        provideContent {
            androidx.glance.text.Text(text = "Omni Music")
        }
    }
}

class OmniMusicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OmniMusicWidget()
}
