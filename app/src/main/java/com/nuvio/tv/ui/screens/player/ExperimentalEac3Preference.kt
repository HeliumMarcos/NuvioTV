package com.nuvio.tv.ui.screens.player

import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import com.nuvio.tv.BuildConfig

/** The stock prebuilt AAR has no E-AC-3 setter; only local decoder builds expose this option. */
internal fun FfmpegAudioRenderer.applyExperimentalEac3Preference(enabled: Boolean) {
    if (!BuildConfig.LOCAL_FFMPEG_DECODER_ENABLED) return
    javaClass.getMethod("setPreferEac3Transcoding", Boolean::class.javaPrimitiveType)
        .invoke(this, enabled)
}
