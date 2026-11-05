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
