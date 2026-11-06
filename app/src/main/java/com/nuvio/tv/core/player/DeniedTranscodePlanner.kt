package com.nuvio.tv.core.player

import androidx.media3.common.MimeTypes

object DeniedTranscodePlanner {

    data class Request(val outputMimeType: String, val inputMask: Int, val forceFiveOne: Boolean)

    fun resolvePreferences(
        manualMode: Boolean,
        preferEac3: Boolean,
        ac3InputMask: Int,
        eac3InputMask: Int,
        forceFiveOne: Boolean
    ): Request = if (!manualMode) Request(MimeTypes.AUDIO_AC3, 15, false) else Request(
        if (preferEac3) MimeTypes.AUDIO_E_AC3 else MimeTypes.AUDIO_AC3,
        if (preferEac3) eac3InputMask and 7 else ac3InputMask and 15,
        preferEac3 && forceFiveOne
    )

    private val candidateMimeTypes = listOf(
        MimeTypes.AUDIO_E_AC3,
        MimeTypes.AUDIO_E_AC3_JOC,
        MimeTypes.AUDIO_TRUEHD,
        MimeTypes.AUDIO_DTS,
        MimeTypes.AUDIO_DTS_HD
    )

    fun effectiveTranscodeMimes(
        policy: AudioPassthroughPolicy,
        transcodeDeniedToAc3: Boolean,
        forcePassthroughActive: Boolean,
        outputMimeType: String = MimeTypes.AUDIO_AC3,
        inputMask: Int = 15
    ): Set<String> {
        if (!transcodeDeniedToAc3) return emptySet()
        if (forcePassthroughActive) return emptySet()
        if (!policy.softwareDecodersAvailable) return emptySet()
        if (outputMimeType != MimeTypes.AUDIO_AC3 && outputMimeType != MimeTypes.AUDIO_E_AC3) return emptySet()
        if (policy.deniesPassthrough(outputMimeType)) return emptySet()
        return candidateMimeTypes.filter {
            val flag = when (it) {
                MimeTypes.AUDIO_TRUEHD -> 1
                MimeTypes.AUDIO_DTS -> 2
                MimeTypes.AUDIO_DTS_HD -> 4
                else -> if (outputMimeType == MimeTypes.AUDIO_AC3) 8 else 0
            }
            inputMask and flag != 0 && policy.deniesPassthrough(it)
        }.toSet()
    }
}
