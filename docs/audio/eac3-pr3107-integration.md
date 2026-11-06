# E-AC-3 integration with the PR3107 passthrough branch

Base: `halibiram/NuvioTV:fix/passthrough-content-time-clock` at `28626bb1f83993b7028b115973ab5fdb97f2fc6b`.
Source of the transcoding experiment: `HeliumMarcos/NuvioTV:heliummarcos/eac3-transcoding-5.1-7.1` at `772aac3e3`.
Requested integration: https://github.com/NuvioMedia/NuvioTV/issues/2398#issuecomment-6689671248

## User behavior

Automatic surround resolution, learned rejections, IEC/MAT passthrough, PCM and optical AC-3 keep their existing defaults. Saved manual target/masks do not influence Automatic mode.

In Playback > Audio > Surround Sound > Manual, the previous denied-format AC-3 toggle becomes a choice of PCM, Dolby Digital (AC-3), or Dolby Digital Plus (E-AC-3). The E-AC-3 choice is available only when the decoder AAR exposes the new encoder controls. Stock prebuilt AAR builds remain compatible and do not offer unsupported E-AC-3 encoding.

- Allowed formats retain normal passthrough.
- Denied formats are transcoded only when selected in the source mask for the chosen destination.
- Sources selectable for E-AC-3 output: TrueHD, DTS, DTS-HD.
- Sources selectable for AC-3 output: TrueHD, DTS, DTS-HD, E-AC-3/JOC.
- Source selections persist independently for the two compressed destinations.
- AAC is not included. E-AC-3/JOC is never re-encoded to E-AC-3 by this path.
- E-AC-3 preserves six/eight input channels. Optional force-5.1 is off by default and reduces 7.1 to 5.1 without upmixing 5.1.
- The E-AC-3 encoder accepts only supported, known 5.1/7.1 source counts; stereo/unknown counts use the normal decode path.
- Bluetooth and device-only restrictions are retained. Force Optical retains the existing AC-3 behavior.
- AC-3 retains PR3107 sample-rate handling at 32/44.1/48 kHz; other source rates map to 48 kHz. E-AC-3 uses 48 kHz.

The existing IEC/clock/bitrate/underrun diagnostics are retained. Additional Input/Output/Route rows show the source and live AudioTrack configuration. The native IEC path bypasses the stock sink's AudioTrack provider; while IEC is active, these new Output/Route rows are omitted rather than reporting a stale stock AudioTrack. Existing IEC diagnostics remain visible.

## Bitstream metadata

The Matroska extractor refines E-AC-3 JOC and dependent-substream 7.1 channel information and detects TrueHD Atmos from checksum-validated major sync headers. TrueHD retains its MIME/channel count and receives a metadata entry, not an invented codec string.

The early scan is bounded and leaves a per-sample fallback. DTS retains PR3107's sync search. Lace parsing handles fixed, Xiph and all EBML size fields; truncated lace headers retain the late fallback. The player track label/profile and input statistics consume the bitstream evidence.

JOC statistics use the compressed base channel count rather than interpreting the route's maximum AudioTrack mask as content upmix. A source Atmos marker is not copied to ordinary transcoded E-AC-3. TrueHD Atmos object preservation during transcoding is not implemented.

## Encoded decoder pipeline

The local Java/JNI decoder carries the experiment's one-access-unit output queue, sample-based packet timestamps, normal-playback queue servicing, ordered decoder/resampler/FIFO/encoder draining and seek/flush invalidation. It replaces the target branch's earlier concatenated AC-3 drain implementation while preserving its renderer audio-path reporting and AC-3 sample-rate helpers.

Java/JNI must be rebuilt together. Do not combine the new Java decoder with old native libraries. E-AC-3 7.1 also requires the pinned FFmpeg candidate and three mapping/bit-allocation corrections described in [the encoder README](../../experiments/eac3-7.1/README.md). No prebuilt AAR is replaced by this source contribution.

## Validation status

- 94 focused isolated JVM tests passed: passthrough policy, denied-codec planner, surround resolver, AC-3 rate/flush helpers, E-AC-3 layout eligibility, audio descriptions, E-AC-3/JOC probing and Matroska lacing.
- Four TrueHD Atmos parser tests passed, including real Atmos/plain headers, checksum/truncation failures and metadata preservation.
- Java sources for the FFmpeg module and vendored Matroska extractor typechecked against the target AARs, with only decoder/container dependencies supplemented from the pinned Maven version.
- The exact AudioTrack-provider wrapper was separately typechecked against the target AAR API; it uses `DefaultAudioSink.AudioTrackProvider.DEFAULT`.
- JNI native names/parameter arities were statically checked for all 16 Java decoder declarations.
- Independent review found four issues (provider API, Auto/manual preference isolation, stereo eligibility, EBML lace offsets). All four were corrected and re-reviewed with no remaining P1/P2 finding in that review.
- Tests are JVM checks, not a complete Android/native build. They use a small local implementation of Android TextUtils for the isolated JVM environment; it is not part of the app.
- The authorized full local ARMv7 Android/native build passed (`:app:assembleFullDebug`, 10m36s). Native JNI, application Kotlin/Java, DEX and APK packaging completed. The local signing preflight was corrected only through environment variables and a task-local validation key, with no project signing changes.
- The final Gradle audio suite passed: **63 classes, 746 tests, zero failures/errors, one skipped optional live-network AFR test**. This includes the existing IEC/passthrough/tunnel suites and the new codec/metadata/layout/lacing regressions. The final test pass after making the optional-layout tests compatible with old AARs took 22s.
- APK signature, ARMv7 ABI and inclusion of the rebuilt `libffmpegJNI.so` were verified. Validation APK SHA-256: `9117ba43f54ec88fe77381e71faa62bb578794c1e1858e09cc430a62d62e4b99`. It was built from the working tree before the final source commit and was not installed on the TV. No GitHub compilation was dispatched.
- The optional layout test also runs safely against the unchanged stock AAR: its availability guard skips E-AC-3-specific assertions when those APIs are absent.
- Device playback on this integration branch remains pending. The user's prior 7.1 speaker/Atmos confirmations apply to the earlier experimental APK, not this integration.
- Full builds/CI dispatch are intentionally not automatic in this work session and require the user's request.

Until the pending device/UI evidence is available, this contribution should remain a draft. No private fork package/signing configuration, workflows, binary AARs, libmpv gitlink changes or unrelated upstream updates are included.
