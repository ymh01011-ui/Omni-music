package com.omnimusic.player.util

import android.os.Build
import android.Manifest

/**
 * Returns the correct runtime permission to request for reading the audio
 * library, accounting for the READ_EXTERNAL_STORAGE -> READ_MEDIA_AUDIO
 * split introduced in Android 13 (API 33).
 */
object AudioPermission {
    val permissionString: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}
