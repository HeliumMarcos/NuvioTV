package com.nuvio.tv.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSettingsTranscodingTest {
    @Test
    fun `existing users keep transcoding and EAC3 disabled by default`() {
        val settings = PlayerSettings()
        assertFalse(settings.forceOpticalPassthrough)
        assertFalse(settings.preferEac3Transcoding)
    }

    @Test
    fun `forced transcoding disables tunneling even with prefer device decoder`() {
        val settings = PlayerSettings(
            decoderPriority = 1,
            tunnelingEnabled = true,
            forceOpticalPassthrough = true,
            preferEac3Transcoding = true
        )
        assertFalse(settings.effectiveTunnelingEnabled)
        assertFalse(settings.copy(preferEac3Transcoding = false).effectiveTunnelingEnabled)
        assertTrue(settings.copy(forceOpticalPassthrough = false).effectiveTunnelingEnabled)
    }

    @Test
    fun `inactive transcoding does not block device only tunneling`() {
        val settings = PlayerSettings(
            decoderPriority = 0,
            tunnelingEnabled = true,
            forceOpticalPassthrough = true,
            preferEac3Transcoding = true
        )
        assertTrue(settings.effectiveTunnelingEnabled)
    }
    @Test
    fun `input selection treats DTS and DTS HD independently and excludes AAC`() {
        val renderer = Class.forName("androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer")
        val method = renderer.getDeclaredMethod("isTranscodingInputSelected", String::class.java, Int::class.javaPrimitiveType)
        method.isAccessible = true
        fun selected(mime: String, mask: Int) = method.invoke(null, mime, mask) as Boolean
        assertTrue(selected("audio/true-hd", 1))
        assertFalse(selected("audio/true-hd", 6))
        assertTrue(selected("audio/vnd.dts", 2))
        assertFalse(selected("audio/vnd.dts.hd", 2))
        assertTrue(selected("audio/vnd.dts.hd", 4))
        assertFalse(selected("audio/vnd.dts", 4))
        assertTrue(selected("audio/eac3", 8))
        assertTrue(selected("audio/eac3-joc", 8))
        assertFalse(selected("audio/eac3", 7))
        assertFalse(selected("audio/mp4a-latm", 15))
        assertFalse(selected("audio/mp4a-latm", 7))
        assertFalse(selected("audio/ac3", 7))
        assertFalse(selected("audio/true-hd", 0))
    }
    @Test
    fun `destination selections remain independent when switching output`() {
        val settings = PlayerSettings(ac3TranscodingInputMask = 8, eac3TranscodingInputMask = 1)
        assertEquals(8, settings.transcodingInputMask)
        assertEquals(1, settings.copy(preferEac3Transcoding = true).transcodingInputMask)
        assertEquals(8, settings.copy(preferEac3Transcoding = true).copy(preferEac3Transcoding = false).transcodingInputMask)
    }
    @Test
    fun `EAC3 layout negotiation preserves both six and eight source channels`() {
        val renderer = Class.forName("androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer")
        val method = renderer.getDeclaredMethod("eac3OutputChannels", Int::class.javaPrimitiveType)
        method.isAccessible = true
        assertEquals(6, method.invoke(null, 6))
        assertEquals(8, method.invoke(null, 8))
    }
}
