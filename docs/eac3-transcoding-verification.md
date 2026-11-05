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
