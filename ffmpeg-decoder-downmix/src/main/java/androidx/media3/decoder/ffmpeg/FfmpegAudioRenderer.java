/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * This modified decoder module is distributed with NuvioTV under GPL-3.0-only.
 * It is based on AndroidX Media3 decoder_ffmpeg; the original Apache-2.0 notice
 * is preserved below. Additional downmix behavior was adapted from Kodi
 * (GPL-2.0-or-later). See this module's NOTICE.md for provenance details.
 */
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.decoder.ffmpeg;

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.TraceUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.decoder.CryptoConfig;
import androidx.media3.decoder.Decoder;
import androidx.media3.decoder.DecoderInputBuffer;
import androidx.media3.decoder.SimpleDecoderOutputBuffer;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.DecoderAudioRenderer;
import androidx.media3.exoplayer.audio.DefaultAudioSink;

/** Decodes and renders audio using FFmpeg. */
@UnstableApi
public final class FfmpegAudioRenderer extends DecoderAudioRenderer<Decoder<DecoderInputBuffer, SimpleDecoderOutputBuffer, FfmpegDecoderException>> {

  private static final String TAG = "FfmpegAudioRenderer";

  /** The number of input and output buffers. */
  private static final int NUM_BUFFERS = 16;

  /** The default input buffer size. */
  private static final int DEFAULT_INPUT_BUFFER_SIZE = 960 * 6;

  private volatile int userCenterMixLevelDb;
  @Nullable private volatile String requestedOutputLayoutName;
  private volatile int requestedOutputChannelCount;
  private volatile boolean downmixNormalizationEnabled;
  private volatile float lfeMixLevel;
  @Nullable private volatile FfmpegAudioDecoder activeDecoder;
  private volatile boolean rendererEnabled;
  private volatile boolean downmixActive;
  private volatile boolean forceOpticalPassthrough;
  // Experimental; disabled by default until the native E-AC-3 build is validated.
  private volatile boolean preferEac3Transcoding;
  private volatile int transcodingInputMask = 7;

  public FfmpegAudioRenderer() {
    this(/* eventHandler= */ null, /* eventListener= */ null);
  }

  /**
   * Creates a new instance.
   *
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param audioProcessors Optional {@link AudioProcessor}s that will process audio before output.
   */
  public FfmpegAudioRenderer(
      @Nullable Handler eventHandler,
      @Nullable AudioRendererEventListener eventListener,
      AudioProcessor... audioProcessors) {
    this(
        eventHandler,
        eventListener,
        new DefaultAudioSink.Builder().setAudioProcessors(audioProcessors).build());
  }

  /**
   * Creates a new instance.
   *
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param audioSink The sink to which audio will be output.
   */
  public FfmpegAudioRenderer(
      @Nullable Handler eventHandler,
      @Nullable AudioRendererEventListener eventListener,
      AudioSink audioSink) {
    super(eventHandler, eventListener, audioSink);
  }

  @Override
  public String getName() {
    return TAG;
  }

  @Override
  protected void onEnabled(boolean joining, boolean mayRenderStartOfStream)
      throws ExoPlaybackException {
    super.onEnabled(joining, mayRenderStartOfStream);
    rendererEnabled = true;
  }

  @Override
  protected void onDisabled() {
    try {
      super.onDisabled();
    } finally {
      rendererEnabled = false;
      downmixActive = false;
      activeDecoder = null;
    }
  }

  @Override
  protected @C.FormatSupport int supportsFormatInternal(Format format) {
    String mimeType = checkNotNull(format.sampleMimeType);
    if (!FfmpegLibrary.isAvailable() || !MimeTypes.isAudio(mimeType)) {
      return C.FORMAT_UNSUPPORTED_TYPE;
    }
    if (!FfmpegLibrary.supportsFormat(mimeType)) {
      return C.FORMAT_UNSUPPORTED_SUBTYPE;
    }
    if (forceOpticalPassthrough && MimeTypes.AUDIO_AC3.equals(mimeType)) {
      return C.FORMAT_UNSUPPORTED_SUBTYPE;
    }
    boolean transcodeToAc3 = forceOpticalPassthrough && isTranscodingInputSelected(mimeType, preferEac3Transcoding ? transcodingInputMask & 7 : transcodingInputMask) &&
        !MimeTypes.AUDIO_AC3.equals(mimeType);

    if (!transcodeToAc3 && (format.channelCount <= 0 || format.sampleRate <= 0)) {
      return format.cryptoType == C.CRYPTO_TYPE_NONE
          ? C.FORMAT_HANDLED
          : C.FORMAT_UNSUPPORTED_DRM;
    }
    boolean supportsConfiguredOutput;
    if (transcodeToAc3) {
      // The native AC-3 transcoder resamples all input rates to 48 kHz.
      int sampleRate = 48000;
      boolean eac3Supported = preferEac3Transcoding && sinkSupportsFormat(
          new Format.Builder()
              .setSampleMimeType(MimeTypes.AUDIO_E_AC3)
              .setChannelCount(eac3OutputChannels(format.channelCount))
              .setSampleRate(sampleRate)
              .build());
      supportsConfiguredOutput = eac3Supported || sinkSupportsFormat(
          new Format.Builder()
              .setSampleMimeType(MimeTypes.AUDIO_AC3)
              .setChannelCount(6)
              .setSampleRate(sampleRate)
              .build());
    } else {
      supportsConfiguredOutput = false;
    }
    // Match createDecoder(): if compressed output is unavailable, evaluate
    // the ordinary PCM fallback instead of advertising an unsupported format.
    if (!transcodeToAc3 || !supportsConfiguredOutput) {
      int outputChannelCount = resolveOutputChannelCount(format.channelCount);
      boolean shouldRequestDownmix = shouldRequestDownmix(format.channelCount, outputChannelCount);
      @C.PcmEncoding int outputEncoding =
          shouldRequestDownmix ? C.ENCODING_PCM_FLOAT : C.ENCODING_PCM_16BIT;
      supportsConfiguredOutput = sinkSupportsFormat(format, outputEncoding, outputChannelCount);
    }
    if (!supportsConfiguredOutput) {
      return C.FORMAT_UNSUPPORTED_SUBTYPE;
    }
    if (format.cryptoType != C.CRYPTO_TYPE_NONE) {
      return C.FORMAT_UNSUPPORTED_DRM;
    }
    return C.FORMAT_HANDLED;
  }

  @Override
  public @AdaptiveSupport int supportsMixedMimeTypeAdaptation() {
    return ADAPTIVE_NOT_SEAMLESS;
  }

  @Override
  protected Decoder<DecoderInputBuffer, SimpleDecoderOutputBuffer, FfmpegDecoderException> createDecoder(Format format, @Nullable CryptoConfig cryptoConfig)
      throws FfmpegDecoderException {
    TraceUtil.beginSection("createFfmpegAudioDecoder");
    String mimeType = checkNotNull(format.sampleMimeType);
    // Do not select a compressed output format that the actual sink rejects.
    // Keep PCM decoding available as the compatibility fallback.
    boolean ac3Supported = sinkSupportsFormat(
        new Format.Builder()
            .setSampleMimeType(MimeTypes.AUDIO_AC3)
            .setChannelCount(6)
            .setSampleRate(48000)
            .build());
    boolean eac3Supported = preferEac3Transcoding && sinkSupportsFormat(
        new Format.Builder()
            .setSampleMimeType(MimeTypes.AUDIO_E_AC3)
            .setChannelCount(eac3OutputChannels(format.channelCount))
            .setSampleRate(48000)
            .build());
    boolean transcodeToAc3 = forceOpticalPassthrough && isTranscodingInputSelected(mimeType, preferEac3Transcoding ? transcodingInputMask & 7 : transcodingInputMask) &&
        (ac3Supported || eac3Supported) &&
        !MimeTypes.AUDIO_AC3.equals(mimeType);
    if (transcodeToAc3 && preferEac3Transcoding && format.channelCount == 8 && !eac3Supported) {
      throw new FfmpegDecoderException("E-AC-3 7.1 output is unsupported by the audio sink; refusing 5.1 conversion.");
    }
    int initialInputBufferSize =
        format.maxInputSize != Format.NO_VALUE ? format.maxInputSize : DEFAULT_INPUT_BUFFER_SIZE;
    @C.PcmEncoding int outputEncoding;
    int nativeOutputChannelCount;
    @Nullable String outputLayoutName;
    if (transcodeToAc3) {
      outputEncoding = eac3Supported ? C.ENCODING_E_AC3 : C.ENCODING_AC3;
      nativeOutputChannelCount = eac3Supported ? eac3OutputChannels(format.channelCount) : 6;
      outputLayoutName = nativeOutputChannelCount == 8 ? "7.1" : nativeOutputChannelCount == 6 ? "5.1" : null;
      downmixActive = false;
    } else {
      int outputChannelCount = resolveOutputChannelCount(format.channelCount);
      boolean shouldRequestDownmix = shouldRequestDownmix(format.channelCount, outputChannelCount);
      outputEncoding = shouldRequestDownmix ? C.ENCODING_PCM_FLOAT : C.ENCODING_PCM_16BIT;
      outputLayoutName = shouldRequestDownmix ? requestedOutputLayoutName : null;
      nativeOutputChannelCount = shouldRequestDownmix ? outputChannelCount : 0;
      downmixActive = shouldRequestDownmix;
    }
    Log.i(TAG, "Transcoding selection: input=" + mimeType
        + ", requestedEac3=" + preferEac3Transcoding
        + ", sinkEac3=" + eac3Supported
        + ", inputChannels=" + format.channelCount
        + ", outputChannels=" + nativeOutputChannelCount
        + ", outputEncoding=" + outputEncoding);
    FfmpegAudioDecoder decoder =
        new FfmpegAudioDecoder(
            format,
            NUM_BUFFERS,
            NUM_BUFFERS,
            initialInputBufferSize,
            nativeOutputChannelCount,
            outputLayoutName,
            outputEncoding);
    decoder.setUserCenterMixLevelDb(userCenterMixLevelDb);
    decoder.setDownmixNormalizationEnabled(downmixNormalizationEnabled);
    decoder.setLfeMixLevel(lfeMixLevel);
    activeDecoder = decoder;
    TraceUtil.endSection();
    // Compressed output can generate several packets after input EOS.
    // Keep the existing SimpleDecoder pipeline unchanged for PCM playback.
    return decoder.isCompressedOutput() ? new EncodedEosDecoder(decoder) : decoder;
  }

  @Override
  protected Format getOutputFormat(
      Decoder<DecoderInputBuffer, SimpleDecoderOutputBuffer, FfmpegDecoderException> decoder) {
    checkNotNull(decoder);
    FfmpegAudioDecoder ffmpegDecoder = decoder instanceof EncodedEosDecoder
        ? ((EncodedEosDecoder) decoder).getUnderlyingDecoder()
        : (FfmpegAudioDecoder) decoder;
    int encoding = ffmpegDecoder.getEncoding();
    if (encoding == C.ENCODING_AC3 || encoding == C.ENCODING_E_AC3) {
      return new Format.Builder()
          .setSampleMimeType(encoding == C.ENCODING_E_AC3
              ? MimeTypes.AUDIO_E_AC3 : MimeTypes.AUDIO_AC3)
          .setChannelCount(ffmpegDecoder.getChannelCount())
          .setSampleRate(ffmpegDecoder.getSampleRate())
          .build();
    }
    return new Format.Builder()
        .setSampleMimeType(MimeTypes.AUDIO_RAW)
        .setChannelCount(ffmpegDecoder.getChannelCount())
        .setSampleRate(ffmpegDecoder.getSampleRate())
        .setPcmEncoding(encoding)
        .build();
  }

  /** Sets the center-mix offset in dB relative to FFmpeg metadata or the default (-3 dB). */
  public void setCenterMixLevelDb(int centerMixLevelDb) {
    userCenterMixLevelDb = centerMixLevelDb;
    @Nullable FfmpegAudioDecoder decoder = activeDecoder;
    if (decoder != null) {
      decoder.setUserCenterMixLevelDb(centerMixLevelDb);
    }
  }

  /** Sets the desired FFmpeg output layout used for explicit downmix decisions. */
  public void setAudioOutputChannels(@Nullable String outputLayoutName, int outputChannelCount) {
    requestedOutputLayoutName = outputLayoutName;
    requestedOutputChannelCount = outputChannelCount;
  }

  /** Sets whether downmix normalization should be enabled. */
  public void setDownmixNormalizationEnabled(boolean downmixNormalizationEnabled) {
    this.downmixNormalizationEnabled = downmixNormalizationEnabled;
    @Nullable FfmpegAudioDecoder decoder = activeDecoder;
    if (decoder != null) {
      decoder.setDownmixNormalizationEnabled(downmixNormalizationEnabled);
    }
  }

  /** Sets the LFE mix level used when the downmix layout has no LFE channel (0 to 1). */
  public void setLfeMixLevel(float lfeMixLevel) {
    this.lfeMixLevel = lfeMixLevel;
    @Nullable FfmpegAudioDecoder decoder = activeDecoder;
    if (decoder != null) {
      decoder.setLfeMixLevel(lfeMixLevel);
    }
  }

  public void setForceOpticalPassthrough(boolean enabled) {
    this.forceOpticalPassthrough = enabled;
  }

  /**
   * Enables experimental E-AC-3 encoding when the sink supports it.
   * Disabled by default; the existing AC-3 path remains the fallback.
   * Set before preparing playback so format negotiation and decoder agree.
   */
  public void setTranscodingInputMask(int mask) {
    transcodingInputMask = mask & 15;
  }

  private static int eac3OutputChannels(int inputChannels) {
    return inputChannels == 8 ? 8 : inputChannels == 6 ? 6 : inputChannels;
  }

  private static boolean isTranscodingInputSelected(String mimeType, int mask) {
    int flag = MimeTypes.AUDIO_TRUEHD.equals(mimeType) ? 1
        : MimeTypes.AUDIO_DTS.equals(mimeType) ? 2
        : MimeTypes.AUDIO_DTS_HD.equals(mimeType) ? 4
        : (MimeTypes.AUDIO_E_AC3.equals(mimeType) || MimeTypes.AUDIO_E_AC3_JOC.equals(mimeType)) ? 8 : 0;
    return (mask & flag) != 0;
  }

  public void setPreferEac3Transcoding(boolean enabled) {
    this.preferEac3Transcoding = enabled;
  }

  /** Returns whether this renderer is the active playback path for FFmpeg downmix + center mix. */
  public boolean isCenterMixActive() {
    return rendererEnabled && activeDecoder != null && downmixActive;
  }

  /** Returns whether this renderer is the active playback path for FFmpeg audio decoding. */
  public boolean isAudioPathActive() {
    return rendererEnabled && activeDecoder != null;
  }

  /**
   * Returns whether the renderer's {@link AudioSink} supports the PCM format that will be output
   * from the decoder for the given input format and requested output encoding.
   */
  private boolean sinkSupportsFormat(
      Format inputFormat, @C.PcmEncoding int pcmEncoding, int channelCount) {
    if (channelCount <= 0 || inputFormat.sampleRate <= 0) {
      return false;
    }
    return sinkSupportsFormat(Util.getPcmFormat(pcmEncoding, channelCount, inputFormat.sampleRate));
  }

  private int resolveOutputChannelCount(int inputChannelCount) {
    if (inputChannelCount <= 0) {
      return inputChannelCount;
    }
    int configuredChannelCount = requestedOutputChannelCount;
    if (configuredChannelCount <= 0 || configuredChannelCount >= inputChannelCount) {
      return inputChannelCount;
    }
    return configuredChannelCount;
  }

  private boolean shouldRequestDownmix(int inputChannelCount, int outputChannelCount) {
    return requestedOutputChannelCount > 0 && outputChannelCount < inputChannelCount;
  }
}
