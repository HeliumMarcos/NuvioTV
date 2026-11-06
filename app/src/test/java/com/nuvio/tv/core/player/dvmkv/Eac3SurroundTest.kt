package com.nuvio.tv.core.player.dvmkv

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Eac3SurroundTest {
    @Test
    fun detectsSurroundExtensionAfterAc3OrEac3Base() {
        val hybrid = accessUnit(2560, "0b77486b2430e1fffdcdd924", 4096, "0b7747ff3a87fff1a0008200")
        val eac3 = accessUnit(1720, "0b77035b3f67ff810420568000020d00", 1352, "0b7742a33a67e011a000021000000021")
        assertTrue(Eac3Surround.has71Extension(hybrid, 0, hybrid.size))
        assertTrue(Eac3Surround.has71Extension(eac3, 0, eac3.size))
        assertFalse(Eac3Surround.has71Extension(eac3, 0, 1720))
        assertFalse(Eac3Surround.has71Extension(eac3, 0, eac3.size - 1))
    }

    private fun accessUnit(baseSize: Int, baseHeader: String, extensionSize: Int, extensionHeader: String) =
        ByteArray(baseSize + extensionSize).also { data ->
            baseHeader.chunked(2).forEachIndexed { index, value ->
                data[index] = value.toInt(16).toByte()
            }
            extensionHeader.chunked(2).forEachIndexed { index, value ->
                data[baseSize + index] = value.toInt(16).toByte()
            }
        }
}
