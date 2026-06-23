package com.omnimusic.player.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.text.Text

/**
 * Placeholder home screen widget. Will be expanded in Phase 4 to show
 * artwork + playback controls bound to the live PlaybackService state.
 */
class OmniMusicWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Intentionally minimal for now; real composable content (artwork,
        // play/pause, next/previous bound to MediaController state) is
        // implemented in the Phase 4 widget step.
        provideContent {
            Text(text = "Omni Music")
        }
    }
}

class OmniMusicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OmniMusicWidget()
}
