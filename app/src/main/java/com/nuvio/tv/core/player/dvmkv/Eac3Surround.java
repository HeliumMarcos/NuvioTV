package com.nuvio.tv.core.player.dvmkv;

import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.ParsableBitArray;
import androidx.media3.extractor.Ac3Util;
import java.util.Arrays;

/** Recognizes a 7.1 extension in a Matroska E-AC-3 access unit. */
final class Eac3Surround {
  private Eac3Surround() {}

  static boolean has71Extension(byte[] data, int offset, int limit) {
    if (limit - offset < 6 || !hasSyncword(data, offset)) {
      return false;
    }
    int baseSize = Ac3Util.parseAc3SyncframeSize(Arrays.copyOfRange(data, offset, offset + 6));
    if (baseSize < 6 || baseSize > limit - offset - 10) {
      return false;
    }
    int extension = offset + baseSize;
    int bitstreamId = (data[extension + 5] & 0xff) >> 3;
    if (!hasSyncword(data, extension)
        || ((data[extension + 2] & 0xff) >> 6) != 1
        || bitstreamId < 11 || bitstreamId > 16) {
      return false;
    }
    int extensionSize = Ac3Util.parseAc3SyncframeSize(
        Arrays.copyOfRange(data, extension, extension + 6));
    if (extensionSize < 10 || extensionSize > limit - extension) {
      return false;
    }
    try {
      Ac3Util.SyncFrameInfo base = Ac3Util.parseAc3SyncframeInfo(
          new ParsableBitArray(Arrays.copyOfRange(data, offset, extension)));
      if (base.channelCount != 6 || MimeTypes.AUDIO_E_AC3_JOC.equals(base.mimeType)) {
        return false;
      }
    } catch (IllegalStateException | IndexOutOfBoundsException e) {
      return false;
    }
    ParsableBitArray header = new ParsableBitArray(data, extension + extensionSize);
    header.setPosition(extension * 8 + 50); // After bsid and dialnorm.
    if (header.readBit()) {
      header.skipBits(8); // Compression gain.
    }
    if (!header.readBit()) {
      return false;
    }
    int channelMap = header.readBits(16);
    int channelMode = (data[extension + 4] >> 1) & 7;
    return ((channelMode == 5 || channelMode == 6) && channelMap == 0x1a00)
        || (channelMode == 2 && channelMap == 0x0200);
  }

  private static boolean hasSyncword(byte[] data, int offset) {
    return data[offset] == 0x0b && data[offset + 1] == 0x77;
  }
}
