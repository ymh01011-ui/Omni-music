package com.omnimusic.player

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Annotated for Hilt dependency injection so that
 * ViewModels, repositories, and the playback service can receive their
 * dependencies (MediaStore scanner, Room DB, network clients) without
 * manual wiring.
 */
@HiltAndroidApp
class OmniMusicApp : Application()
