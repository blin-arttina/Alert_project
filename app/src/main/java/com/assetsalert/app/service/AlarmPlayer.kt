package com.assetsalert.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.*

/**
 * Plays the alert sound on a loop, ramping volume up over time so a quiet
 * alert becomes impossible to sleep through — matching the "starts at 5
 * minutes, extends up to 30 minutes if unacknowledged" behavior from the
 * product description. Call stop() from a notification action or when the
 * user opens the app and dismisses the alert.
 */
class AlarmPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var rampJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(customSoundUri: String?) {
        stop()
        val uri: Uri = customSoundUri?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, uri)
            isLooping = true
            setVolume(0.2f, 0.2f)
            prepare()
            start()
        }

        // Ramp from 20% -> 100% volume across the 5-30 minute window.
        rampJob = scope.launch {
            val totalRampMs = 25 * 60 * 1000L // reaches full volume by minute 30
            val steps = 25
            val stepMs = totalRampMs / steps
            for (i in 1..steps) {
                delay(stepMs)
                val v = (0.2f + (0.8f * i / steps)).coerceAtMost(1f)
                player?.setVolume(v, v)
            }
            // Auto stop at the 30 minute ceiling described in the product doc.
            delay(5 * 60 * 1000L)
            withContext(Dispatchers.Main) { stop() }
        }
    }

    fun stop() {
        rampJob?.cancel()
        rampJob = null
        player?.runCatching { stop(); release() }
        player = null
    }
}
