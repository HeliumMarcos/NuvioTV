# Experimental AC-3 / E-AC-3 transcoding verification

This branch is experimental. Do not merge into `dev` or distribute an APK as production-ready until the checks below pass.

## Build prerequisites

- Build FFmpeg with `--enable-encoder=ac3` and `--enable-encoder=eac3`.
- Verify that the app links against the newly built native library, not an older prebuilt AAR.
- Compile the FFmpeg decoder module and Android TV application.
- Check `avcodec_find_encoder(AV_CODEC_ID_AC3)` and `avcodec_find_encoder(AV_CODEC_ID_EAC3)` at runtime.
- Confirm the Java native method declaration matches the JNI registration/signature (the final parameter changed from boolean to int).

## Audio playback matrix

| Source | Requested output | Expected result |
| --- | --- | --- |
| TrueHD 5.1 48 kHz | AC-3 | 5.1, 48 kHz, no stalls |
| TrueHD 7.1 48 kHz | AC-3 | 5.1 downmix, 48 kHz |
| TrueHD 5.1 96 kHz | AC-3 | 5.1, resampled to 48 kHz |
| DTS-HD 5.1 48 kHz | AC-3 | 5.1, 48 kHz |
| TrueHD 5.1 48 kHz | E-AC-3 opt-in | E-AC-3 5.1 if sink supports it |
| TrueHD 5.1 48 kHz | E-AC-3 opt-in, unsupported sink | AC-3 fallback |
| Stereo PCM | PCM | Existing PCM behavior unchanged |

## Runtime regression tests

- Start playback, pause/resume, seek backward/forward repeatedly.
- Change audio track, stop/restart playback, and play two titles sequentially.
- Confirm FIFO/encoder state resets on seek and TrueHD decoder reinitialization.
- Confirm audio/video synchronization after long playback and after seeking.
- Check Android logcat for `avcodec_send_frame`, `avcodec_receive_packet`, `swr_convert`, and FIFO errors.
- Confirm E-AC-3 is not selected unless explicitly enabled before renderer initialization.
- Test device audio capability detection and HDMI passthrough with the actual TV/audio equipment.

## Known limitations

- The opt-in E-AC-3 renderer setter is not yet connected to an application setting.
- There is no tested runtime fallback if E-AC-3 encoding itself fails.
- 7.1 E-AC-3 and Dolby Digital Plus Atmos/JOC are not implemented.
- No APK build, automated test result, or device playback validation has been produced yet.

## Local decoder selection (critical)

The app defaults to the prebuilt decoder AAR. **Changing JNI or the FFmpeg build script does not change the APK** unless the local module and newly compiled FFmpeg binaries are used.

Set these in a developer-local `local.properties` (do not commit machine-specific paths):

```properties
USE_LOCAL_FFMPEG_DECODER=true
FFMPEG_SOURCE_DIR=/absolute/path/to/ffmpeg
FFMPEG_BUILD_DIR=/absolute/path/to/compiled/ffmpeg
```

The local module currently specifies NDK `29.0.14206865` and CMake `3.22.1`. Build the required Android ABIs with both encoders enabled and ensure `FFMPEG_BUILD_DIR` contains the native outputs expected by `src/main/jni/CMakeLists.txt`. Run `./gradlew :app:assembleFullDebug` only after those prerequisites are ready.

**Do not claim E-AC-3 support from an APK built with `USE_LOCAL_FFMPEG_DECODER=false`.** The existing `app/libs/lib-decoder-ffmpeg-release.aar` may not contain these experimental changes.

### Pre-release blockers

- Verify the actual JNI method signature and native linkage with the selected AAR/module.
- Verify encoded packet framing and AudioSink behavior on the device.
- Test output timestamps, end-of-stream draining, seek/reset and encoder reconfiguration.
- Confirm 48 kHz resampling with real 96/192 kHz TrueHD samples.
- Wire the opt-in renderer setter into playback settings only after build and playback verification.

## Encoded audio end-of-stream and timestamp audit (open)

The current JNI `decodePacket()` receives input packets, collects decoded frames, resamples them, and emits compressed packets only when the audio FIFO contains at least one full encoder frame. There is **no demonstrated explicit end-of-stream drain** of the decoder, resampler, FIFO, and encoder. The final fractional encoder frame may therefore remain buffered when the track ends. Do not mark this as resolved.

The native encoder receives monotonic sample-based frame PTS (`time_base = 1/48000`, reset with encoder state). JNI now exposes the first encoded packet PTS in microseconds, and Java anchors it to the first input `timeUs` after reset. **This is a first-packet timestamp implementation, not validated full A/V synchronization**: multiple compressed packets can still be concatenated into one output buffer, and input PTS discontinuities beyond decoder reset are not yet accounted for. When an encoder buffers multiple decoded frames or returns several encoded packets at once, this does **not** establish per-packet presentation timestamps. Before enabling E-AC-3 in the UI, verify that the Media3 encoded audio sink accepts the framing and timestamps emitted by this decoder.

Required implementation review:
1. Determine how `SimpleDecoder` handles EOS input and whether `decode()` is called for EOS buffers.
2. Define a safe JNI drain path (decoder `avcodec_send_packet(NULL)`, `swr_convert` drain, padded final FIFO frame if appropriate, `avcodec_send_frame(NULL)`, and receive remaining packets).
3. Validate the new one-packet-per-output native queue with the selected `AudioSink` and ensure the queue is emptied independently of new input.
4. Verify output timestamps against the encoder sample counter (48 kHz) and actual input PTS, especially after seeks.
5. Reset counters and discard buffered audio on seek/track changes; never emit pre-seek samples after reset.

These items are intentionally **not claimed as implemented**.

### Verified Media3 EOS constraint (upstream `SimpleDecoder.java`)

The upstream `androidx.media3.decoder.SimpleDecoder` worker checks `inputBuffer.isEndOfStream()` **before** calling the subclass's `decode(...)`. For EOS it flags an output buffer directly; the subclass never receives the EOS input. `queueInputBuffer()` and `flush()` are `final`. Therefore adding an EOS branch to `FfmpegAudioDecoder.decode()` would be unreachable and cannot drain the native encoder. A complete solution requires a deliberate decoder/renderer lifecycle change (e.g. a dedicated drain-capable decoder implementation that can emit remaining output buffers before EOS), not a JNI-only flush method. Do not ship a partial native drain that is never invoked. Inspect Media3 version used by this project before changing that lifecycle.

The timestamp bridge is also subject to the multi-packet output framing limitation. Before enabling the feature, implement separate encoded access units and their corresponding timestamps, and test seek, discontinuity, and EOS behavior.

### Encoded access-unit integrity gate

JNI now enqueues encoded AC-3/E-AC-3 packets individually (including their PTS/DTS) and dequeues **at most one** access unit per successful `decodePacket()` call. The Java decoder retains its one-access-unit validation. The native queue is bounded to 256 packets and is discarded on decoder reset/release. **This is not yet a complete output scheduling solution**: a queued packet is emitted only when another input packet reaches the decoder, and upstream `SimpleDecoder` bypasses subclass decoding for EOS input. Pending packets may therefore be lost at EOS, and a backlog can reach the queue limit and fail playback. Proper EOS draining and independent output dequeue remain release blockers. No native build or device test has validated this queue.

### Independent queue-dequeue JNI primitive (not yet wired)

Native `dequeueEncodedPacket()` now handles one access unit independently of decoding input. JNI exposes `ffmpegGetPendingEncodedPacketCount()` and `ffmpegDequeueEncodedPacket()`, with matching Java native declarations. A failed output-buffer grow does not consume the queued packet. These APIs are **not yet invoked** by the current `SimpleDecoder` implementation. The EOS lifecycle bypass still prevents emitting pending packets at track end; the FFmpeg decoder/resampler/encoder itself is also not yet flushed. A drain-capable output scheduler and full codec flush are still required before playback validation.

### Java dequeue bridge

The FFmpeg decoder now exposes package-private `getPendingEncodedPacketCount()` and `dequeuePendingEncodedPacket(SimpleDecoderOutputBuffer)`. The latter initializes a buffer, retrieves one queued compressed packet through JNI, and assigns its encoder-derived media timestamp. These helpers are **not invoked by `SimpleDecoder`**, whose EOS path still bypasses subclass decoding. Do not report EOS drain as complete until an output scheduler consumes the queued packets and explicitly flushes the FFmpeg decoder, resampler, FIFO and encoder before emitting EOS. A native build and playback tests remain outstanding.

### Partial encoder flush primitive (not wired to EOS)

The native `ffmpegFlushEncodedFrames()` JNI entry point and Java `flushEncodedFramesAfterUpstreamDrain()` bridge now exist. The native routine pads a partial AC-3/E-AC-3 FIFO frame with silence, sends the final frame, sends encoder EOF, and queues packets returned by the encoder. **This is only the encoder stage of EOS draining.** It must not be called until the FFmpeg decoder and `SwrContext` have been drained, otherwise buffered audio is lost. It is not called by the current `SimpleDecoder` EOS path; the remaining work is decoder/resampler drain, independent output scheduling, and emitting all queued access units before Media3 EOS. Encoder flush is terminal for the current encoder context; seek/reset must recreate it before decoding resumes. Validate exact final-frame duration/padding and packet count in native tests before release.

### Encoder flush safety guards

The experimental encoder-finalization entry point now rejects repeated EOF submission for the same encoder context and checks the FFmpeg silence-padding return value before consuming the final FIFO samples. The guard is cleared when `clearResampler()` resets encoder state. This does **not** complete decoder/resampler draining or Media3 EOS integration. Native compilation, unit tests, and playback validation have not yet been performed.

### Delayed decoder-frame drain primitive

Native `ffmpegDrainDecoder()` now passes a null input packet to `decodePacket()` and accepts FFmpeg decoder EOF as a normal drain result. Its Java bridge `drainDelayedDecoderFrames(SimpleDecoderOutputBuffer)` can return an encoded access unit and its media timestamp. The native decoder rejects new input after draining starts until reset. **Neither bridge is invoked by the existing `SimpleDecoder` EOS path.** A successful decoder drain does not imply the resampler is drained; `swr_convert(..., nullptr, 0)` or equivalent must process buffered samples before calling the terminal encoder flush. An output scheduler must also emit every queued access unit before signaling Media3 EOS. These APIs have not been compiled or tested on Android.

### Confirmed decoder EOF guard

`ffmpegDrainDecoder()` now records `decoder_drain_completed` only after `avcodec_receive_frame()` returns `AVERROR_EOF`; Java can query it with `isDecoderDrainComplete()`. A zero-byte drain response is **not** proof of EOF. Terminal `ffmpegFlushEncodedFrames()` now refuses to run before confirmed decoder EOF. **This guard does not confirm SwrContext is empty:** resampler drain and Media3 output scheduling are still unimplemented. The guard is reset on decoder reset. No native build or device tests have been performed.

### SwrContext delayed-sample drain (experimental)

The JNI `ffmpegDrainResampler()` and Java `drainResamplerAfterDecoderEof()` are now available. After confirmed decoder EOF, the native routine repeatedly calls `swr_convert(..., nullptr, 0)`, writes produced planar-float samples to the encoder FIFO, encodes full frames and queues individual access units. It marks `resampler_drain_completed` when no more samples are produced; terminal encoder flush now requires both decoder and resampler completion. This path is **not wired into Media3 EOS**. The native loop, buffer sizing, delayed-sample accounting, and encoder behavior still require native compilation and tests, especially for sample-rate conversion and seeks. The Java drain calls must be sequenced by an output scheduler that can emit additional buffers; until then, this is not an end-to-end working feature.

### Media3 EOS scheduling constraint — verified against upstream source

The upstream AndroidX Media3 `SimpleDecoder.decode()` implementation handles EOS internally: for an EOS input it marks the output EOS without calling the subclass's `decode(input, output, reset)` method. Its `dequeueOutputBuffer()` and `queueInputBuffer()` methods are final, so overriding those methods in `FfmpegAudioDecoder` cannot inject additional encoded access units. Upstream `DecoderAudioRenderer.feedInputBuffer()` queues EOS directly, and `drainOutputBuffer()` handles the first EOS output as end-of-stream, rather than asking the decoder to flush FFmpeg.

**Required integration design:** create a drain-aware implementation of Media3's `Decoder<DecoderInputBuffer, SimpleDecoderOutputBuffer, FfmpegDecoderException>` interface for compressed transcoding, or a compatible independently owned decoding queue that can produce multiple outputs after the last input and before EOS. Keep the existing `SimpleDecoder` path for PCM decoding. The drain-aware implementation must:

1. Decode all queued compressed inputs, retaining one complete AC-3/E-AC-3 access unit per output buffer.
2. On EOS, call native decoder drain until `isDecoderDrainComplete()` is true, emitting each returned access unit.
3. Call `drainResamplerAfterDecoderEof()` and check `isResamplerDrainComplete()`; emit queued access units, respecting the queue capacity.
4. Call `flushEncodedFramesAfterUpstreamDrain()` once and dequeue all remaining access units.
5. Only then enqueue a Media3 EOS output buffer; never send encoded output after EOS.
6. On seek, flush, format change, release or decoder reinitialization, discard pending packets and reset all native drain flags and timestamps. Ensure the audio renderer cannot observe stale outputs from the previous generation.

The new JNI/Java `isResamplerDrainComplete()` query is available to distinguish completion from a zero-packet result. This plan is not yet implemented. No build or device validation has been performed. Sources inspected: `androidx/media` upstream `libraries/decoder/src/main/java/androidx/media3/decoder/SimpleDecoder.java` and `libraries/exoplayer/src/main/java/androidx/media3/exoplayer/audio/DecoderAudioRenderer.java`.

### Native memory and JNI growth checks

The experimental `ffmpegDrainResampler()` now limits each planar-float allocation to 8,192 output samples, iterating for larger delayed buffers, and validates aggregate channel/sample dimensions against integer overflow. `GrowOutputBufferCallback` checks that Java returns a direct buffer of sufficient capacity and releases the JNI local reference created by `CallObjectMethod`. These are static code hardening changes; native compilation and runtime behavior remain unverified. **The Media3 EOS scheduler is still missing**; no claim of gapless or complete end-of-stream output is made.

### EOS backpressure and bounded queue behavior

Decoder EOF submission may return `AVERROR(EAGAIN)` while delayed decoded frames remain. The native drain now receives those frames and allows EOS submission to be retried on a later call, rather than treating EAGAIN as fatal. The resampler drain also pauses before the pending encoded-packet queue approaches its 256-unit limit (threshold 248); the future output scheduler must dequeue packets and call it again until `isResamplerDrainComplete()` is true. **A positive queued-packet count does not imply completion.** The threshold assumes the current fixed 1,536-sample AC-3/E-AC-3 frame and 8,192-sample drain batch; verify these assumptions during native tests. Neither change is connected to the Media3 EOS scheduler, and neither has been compiled or exercised on a device.

### Java encoded EOS drain coordinator

`FfmpegAudioDecoder.drainNextEncodedEndOfStreamBuffer(SimpleDecoderOutputBuffer)` now coordinates the existing native stages for a future drain-aware output scheduler: dequeue pending access units first; drain delayed decoder frames until confirmed EOF; drain resampler output and dequeue its encoded packets; flush encoder once; finally dequeue remaining access units and mark completion. The coordinator produces **at most one access unit per call** and `isEncodedEndOfStreamComplete()` distinguishes a no-output step from true completion. Its state is reset on the existing decoder reset path. **This method is not called by Media3 SimpleDecoder**, which still converts EOS inputs directly into EOS outputs. A custom scheduler or decoder integration is necessary before playback can use the coordinator. Keep all coordinator and native decode/reset calls serialized on the same thread. No native build, unit tests, or TCL C7K tests have been run.

### Terminal encoder frame retry state

The native encoder now tracks `encoder_final_frame_submitted` separately from `encoder_flush_started`. Once the padded final FIFO frame has been accepted, a later retry of encoder EOF submission must not send that frame again. Both flags are cleared by `clearResampler()` on decoder reset/reconfiguration. This guards against duplicate final audio if the terminal EOF submission fails after accepting the final frame. **Still unresolved:** robust handling of encoder `AVERROR(EAGAIN)`, terminal packet queue backpressure, and complete Media3 output scheduling. Validate that final silence padding, packet timestamps and reset/seek transitions behave correctly before enabling transcoding. This change has not been compiled or tested on device.

### Retryable encoder EOS and confirmed completion

`ffmpegFlushEncodedFrames()` now distinguishes terminal EOF submission (`encoder_flush_started`) from confirmed encoder packet exhaustion (`encoder_flush_completed`, set on `avcodec_receive_packet() == AVERROR_EOF`). The native method permits retries after `AVERROR(EAGAIN)`, protects against resending the final padded frame, and pauses when the pending queue is near capacity. `ffmpegIsEncoderFlushComplete()` exposes confirmed EOF to Java. `drainNextEncodedEndOfStreamBuffer()` retries encoder flushing only after draining pending packets and does not signal complete until native encoder EOF is confirmed and the queue is empty. **This is not yet called by Media3's current SimpleDecoder EOS path**; the separate output scheduler and native compilation/testing remain outstanding. Test FFmpeg encoder backpressure and terminal packet production explicitly.
