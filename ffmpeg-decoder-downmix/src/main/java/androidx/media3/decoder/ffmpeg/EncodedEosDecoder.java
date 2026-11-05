/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Experimental compressed-output EOS scheduler for NuvioTV.
 */
package androidx.media3.decoder.ffmpeg;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.decoder.Decoder;
import androidx.media3.decoder.DecoderInputBuffer;
import androidx.media3.decoder.SimpleDecoderOutputBuffer;
import java.util.concurrent.Executor;

/**
 * Bridges Media3's single-output-per-input SimpleDecoder to FFmpeg's multi-packet
 * terminal drain. Only used for AC-3/E-AC-3 transcoding; PCM uses the original
 * decoder directly.
 *
 * <p>All public Decoder methods are called by the Media3 renderer thread.
 * The underlying SimpleDecoder owns native FFmpeg calls on its worker thread.
 * EOS is intercepted and replaced with synthetic drain inputs until the native
 * decoder, resampler, encoder and packet queue have all reached completion.
 */
final class EncodedEosDecoder
    implements Decoder<DecoderInputBuffer, SimpleDecoderOutputBuffer, FfmpegDecoderException> {

  private final FfmpegAudioDecoder delegate;
  private boolean eosRequested;
  private boolean drainStepQueued;
  private boolean eosQueued;
  private boolean released;

  EncodedEosDecoder(FfmpegAudioDecoder delegate) {
    if (!delegate.isCompressedOutput()) {
      throw new IllegalArgumentException("Encoded EOS scheduler requires compressed output");
    }
    this.delegate = delegate;
  }

  FfmpegAudioDecoder getUnderlyingDecoder() {
    return delegate;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public void setOutputStartTimeUs(long outputStartTimeUs) {
    delegate.setOutputStartTimeUs(outputStartTimeUs);
  }

  @Override
  @Nullable
  public DecoderInputBuffer dequeueInputBuffer() throws FfmpegDecoderException {
    if (eosRequested) {
      // Once source EOS is received, only the wrapper may queue drain inputs.
      return null;
    }
    return delegate.dequeueInputBuffer();
  }

  @Override
  public void queueInputBuffer(DecoderInputBuffer inputBuffer) throws FfmpegDecoderException {
    if (!inputBuffer.isEndOfStream()) {
      delegate.queueInputBuffer(inputBuffer);
      return;
    }
    if (eosRequested) {
      throw new IllegalStateException("EOS already requested");
    }
    eosRequested = true;
    // The buffer is still the one dequeued from SimpleDecoder, so it can
    // safely be repurposed as the first drain token.
    queueDrainToken(inputBuffer);
  }

  @Override
  @Nullable
  public SimpleDecoderOutputBuffer dequeueOutputBuffer() throws FfmpegDecoderException {
    if (eosRequested && !eosQueued) {
      advanceDrain();
    }
    @Nullable SimpleDecoderOutputBuffer output = delegate.dequeueOutputBuffer();
    if (eosRequested && output != null && !output.isEndOfStream()) {
      // SimpleDecoder counts empty synthetic drain steps as skipped output.
      // Those are internal scheduler ticks, not media discontinuities.
      // Do not ask AudioSink to reset its clock for each such tick.
      output.skippedOutputBufferCount = 0;
    }
    return output;
  }

  private void advanceDrain() throws FfmpegDecoderException {
    if (drainStepQueued) {
      if (!delegate.hasFinishedSyntheticDrainStep()) {
        return;
      }
      drainStepQueued = false;
    }
    @Nullable DecoderInputBuffer input = delegate.dequeueInputBuffer();
    if (input == null) {
      return;
    }
    if (delegate.isEncodedEndOfStreamComplete()) {
      input.clear();
      input.setFlags(C.BUFFER_FLAG_END_OF_STREAM);
      delegate.queueInputBuffer(input);
      eosQueued = true;
    } else {
      queueDrainToken(input);
    }
  }

  private void queueDrainToken(DecoderInputBuffer input) throws FfmpegDecoderException {
    input.clear();
    input.timeUs = FfmpegAudioDecoder.ENCODED_DRAIN_INPUT_TIME_US;
    delegate.prepareSyntheticDrainStep();
    delegate.queueInputBuffer(input);
    drainStepQueued = true;
  }

  @Override
  public void flush() {
    eosRequested = false;
    drainStepQueued = false;
    eosQueued = false;
    delegate.flush();
  }

  @Override
  public void release() {
    if (!released) {
      released = true;
      delegate.setDrainWakeupCallback(null, null);
      delegate.release();
    }
  }

  @Override
  public void setCallback(Callback callback, Executor executor) {
    delegate.setDrainWakeupCallback(callback, executor);
    delegate.setCallback(callback, executor);
  }
}
