package com.nuvio.tv.data.local

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
}
