Update on my earlier proposal: I now have an experimental implementation in my fork and would like maintainer feedback on whether, and in what scope, it could be useful upstream. I understand a large feature PR still needs explicit maintainer approval.

Implemented behavior:

- Separate source-codec selections for each target: TrueHD/DTS/DTS-HD → E-AC-3, and TrueHD/DTS/DTS-HD/E-AC-3 → AC-3.
- Unselected inputs use normal playback negotiation. The E-AC-3 target does not re-encode native E-AC-3/JOC through this selection rule.
- Channel-preserving E-AC-3: 5.1 → 5.1 and 7.1 → 7.1, with an optional force-5.1 checkbox disabled by default.
- Separate source/output/route diagnostics, including correct JOC base-channel reporting instead of treating the AudioTrack's maximum route mask as the compressed content layout.
- Bitstream-based E-AC-3 JOC and TrueHD Atmos detection in Matroska, independent of filenames/track labels.
- Native FIFO safety checks, 48 kHz output resampling, individual encoded access-unit delivery, timestamps and ordered EOS draining/reset handling.

For 7.1, I used a pinned FFmpeg candidate patch and added fixes to the independent/dependent substream mappings, surround bit allocation and decoder assembly. The initial prototype left both rear surrounds silent; after these corrections, the individual TrueHD 7.1 channel test played through all speakers in Nuvio on my HDMI eARC setup. A native E-AC-3 7.1 control had already confirmed the rear speakers through another player. The previously failing Matroska E-AC-3 Atmos sample is now correctly identified in the player and statistics.

This does **not** preserve Atmos objects when converting TrueHD Atmos to regular E-AC-3. It identifies Atmos on the source and keeps native JOC passthrough separate from ordinary transcoded DDP. It also does not claim universal device compatibility or complete long-duration/backpressure stress validation. The experimental 7.1 FFmpeg dependency needs its own review; the current APK build targets ARMv7.

Code and documentation:

- [Current branch](https://github.com/HeliumMarcos/NuvioTV/tree/heliummarcos/eac3-transcoding-5.1-7.1)
- [Detailed implementation report and test limitations](https://github.com/HeliumMarcos/NuvioTV/blob/heliummarcos/eac3-transcoding-5.1-7.1/docs/contributions/eac3-transcoding/RELATORIO-PT-BR.md)
- [Android build and focused tests for the latest TrueHD Atmos detection fix](https://github.com/HeliumMarcos/NuvioTV/actions/runs/42151624130)

The current branch includes the earlier `feature/eac3-transcoding` work. I would not submit both full branches as separate PRs, and the fork-specific app identity/signing/CI changes would be excluded from an upstream audio contribution.

Would you prefer reviewing this as smaller, separate contributions — for example Matroska Atmos identification fixes first, optional 5.1 transcoding next, and the experimental 7.1 integration separately? Is this direction of interest, and what initial scope would you approve?
