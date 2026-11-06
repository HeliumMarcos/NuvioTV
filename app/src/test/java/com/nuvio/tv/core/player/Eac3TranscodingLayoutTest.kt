package com.nuvio.tv.core.player

import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import java.lang.reflect.Method
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

class Eac3TranscodingLayoutTest {
    private lateinit var eligibility: Method
    private lateinit var layout: Method

    @Before fun requiresRebuiltEac3Decoder() {
        val methods = runCatching {
            val renderer = FfmpegAudioRenderer::class.java
            renderer.getMethod("canEncodeEac3", Int::class.javaPrimitiveType) to
                renderer.getMethod("eac3OutputChannels", Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
        }.getOrNull()
        assumeTrue("The stock prebuilt decoder has no E-AC-3 encoder controls", methods != null)
        eligibility = methods!!.first
        layout = methods.second
    }

    @Test fun onlySupportedMultichannelLayoutsEnterTheEncoder() {
        for (channels in listOf(-1, 0, 1, 2, 3, 4, 5, 7, 10)) {
            assertEquals("Unsupported channels: $channels", false, eligibility.invoke(null, channels))
        }
        assertEquals(true, eligibility.invoke(null, 6))
        assertEquals(true, eligibility.invoke(null, 8))
    }
    @Test fun fiveOneAndSevenOneArePreservedUnlessExplicitlyLimited() {
        assertEquals(6, layout.invoke(null, 6, false))
        assertEquals(8, layout.invoke(null, 8, false))
        assertEquals(6, layout.invoke(null, 8, true))
        assertEquals(6, layout.invoke(null, 6, true))
    }
}
