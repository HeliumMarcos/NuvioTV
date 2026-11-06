package com.nuvio.tv.ui.screens.player

import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer

/** Keep stock prebuilt AAR builds valid; E-AC-3 needs the rebuilt decoder module/AAR. */
internal object Eac3TranscodingSupport {
    val available: Boolean by lazy {
        runCatching {
            FfmpegAudioRenderer::class.java.getMethod("setPreferEac3Transcoding", Boolean::class.javaPrimitiveType)
            FfmpegAudioRenderer::class.java.getMethod("setForceEac3FiveOne", Boolean::class.javaPrimitiveType)
            true
        }.getOrDefault(false)
    }
}

internal fun FfmpegAudioRenderer.applyEac3Transcoding(enabled: Boolean, forceFiveOne: Boolean) {
    if (!Eac3TranscodingSupport.available) return
    javaClass.getMethod("setPreferEac3Transcoding", Boolean::class.javaPrimitiveType).invoke(this, enabled)
    javaClass.getMethod("setForceEac3FiveOne", Boolean::class.javaPrimitiveType).invoke(this, forceFiveOne)
}
