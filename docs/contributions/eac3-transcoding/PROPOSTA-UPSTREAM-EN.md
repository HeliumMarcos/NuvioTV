# Suggested feature request

Copy the following sections into the matching fields in the upstream **Feature request** form. Area: **Audio**.

## Title

[Feature]: Optional AC-3 / E-AC-3 transcoding with codec selection and channel-preserving 5.1/7.1 output

## Problem statement

Some Android TV / HDMI audio setups cannot pass through or decode every TrueHD/DTS/DTS-HD track, while accepting Dolby Digital or Dolby Digital Plus. A user-controlled conversion path would help play those sources through compatible soundbars/receivers. It should respect the user's codec selections and avoid re-encoding an already compatible E-AC-3 track.

Audio diagnostics also need to distinguish the source track from the AudioTrack output. For E-AC-3 JOC, an eight-channel AudioTrack configuration can represent the route's maximum capacity rather than an eight-channel compressed bed. Reporting that mask as the output content can misleadingly suggest that 5.1 audio was upmixed.

## Proposed solution

I have an experimental implementation in my fork and would like maintainer feedback before proposing a large PR. I understand that the existing prototype does not imply upstream approval.

Proposed user behavior:

- Opt-in AC-3 or E-AC-3 output through the local FFmpeg decoder and Media3 AudioSink.
- Separate input selections for each target: TrueHD/DTS/DTS-HD for E-AC-3; TrueHD/DTS/DTS-HD/E-AC-3 for AC-3. Unselected formats use the normal playback negotiation.
- Preserve 5.1 → 5.1 and 7.1 → 7.1 for E-AC-3; offer a force-5.1 checkbox disabled by default.
- Do not add AAC to these forced-transcoding selections or re-encode E-AC-3 into E-AC-3 through this rule.
- Report source format, initialized AudioTrack output and routed Android device separately.

The implementation includes native FIFO validation, 48 kHz resampling, individual encoded access-unit delivery, timestamps, ordered decoder/resampler/encoder EOS draining and seek/flush invalidation. Its 7.1 path uses a pinned external FFmpeg candidate patch plus local channel-mapping and dependent-substream fixes, so that part requires a separate maintenance/compatibility discussion.

I would be happy to split the work into smaller, approved contributions. Bitstream-based Matroska E-AC-3 JOC/TrueHD Atmos identification fixes could be reviewed independently as documented bug fixes.

## Contribution plan

Yes, but I understand implementation still needs maintainer approval.

## Alternatives considered

- Native passthrough where the device and HDMI route support the source format.
- PCM output/downmix, depending on device and receiver capabilities.
- Keeping E-AC-3 transcoding at 5.1 only for a smaller initial upstream scope.
- Separating the 7.1 FFmpeg experiment from the general transcoding settings and renderer integration.

## Additional context

Source branches:

- [Current 5.1/7.1 implementation](https://github.com/HeliumMarcos/NuvioTV/tree/heliummarcos/eac3-transcoding-5.1-7.1)
- [Earlier transcoding implementation](https://github.com/HeliumMarcos/NuvioTV/tree/feature/eac3-transcoding), fully included in the current branch
- [Detailed implementation report in Portuguese](https://github.com/HeliumMarcos/NuvioTV/blob/heliummarcos/eac3-transcoding-5.1-7.1/docs/contributions/eac3-transcoding/RELATORIO-PT-BR.md)

Device evidence: the setup described in my earlier [issue #2398 comment](https://github.com/NuvioMedia/NuvioTV/issues/2398#issuecomment-6253587332) is a TCL 75C7K with an LG S95TR soundbar over HDMI eARC. Android currently reports version 14 and the generic device name “Smart TV Pro”. After the surround-channel fixes, the user confirmed that the individual TrueHD 7.1 channel test played through all speakers in the experimental Nuvio player. A native E-AC-3 7.1 control had also played both rear surrounds through the same audio setup using another player. E-AC-3 JOC detection in the previously failing MKV was confirmed in both player and debug statistics.

The latest TrueHD Atmos detection correction validates major-sync headers and checksum and uses the same spatial-presentation condition as FFmpeg. It marks the source metadata without changing the TrueHD MIME/channel count or falsely marking ordinary transcoded E-AC-3 as Atmos. Isolated parser tests cover real Atmos/plain headers, corrupt/truncated data and metadata preservation. The Android build and focused tests are available in [this Actions run](https://github.com/HeliumMarcos/NuvioTV/actions/runs/42151624130); final device verification of this latest correction should be reported separately.

Limitations: TrueHD Atmos → E-AC-3 Atmos preservation is **not implemented**. This prototype does not claim Dolby certification, universal device compatibility or complete long-duration/EOS/backpressure stress coverage. The current APK build targets ARMv7. The fork's experimental application ID/signing and personal CI workflows would not be copied into an upstream audio PR.

Would you be interested in this direction, and which initial scope would you prefer: metadata/diagnostic bug fixes, optional 5.1 transcoding, or a separate review of the experimental 7.1 encoder integration?
