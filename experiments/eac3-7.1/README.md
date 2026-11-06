# E-AC-3 7.1 encoder proof

> Historical experiment log. The candidate plus the local mapping/surround corrections is now
> integrated into the experimental Android build. The user confirmed all speakers in the TrueHD
> 7.1 channel test. See the [consolidated report](../../docs/contributions/eac3-transcoding/RELATORIO-PT-BR.md)
> for current scope, limitations and upstream contribution guidance. The earlier observations
> below are retained to explain how the channel-loss defect was isolated.

Isolated experiment; not used by the Android app yet. The working 5.1 branch stays unchanged.

- Base: FFmpeg c352a9ab022b945307f1ad5d35575f695a13c2ca (2026-02-06).
- Candidate: FFmpeg PR 21668, author Nomis101/ShortKatz, commit f32ba6711cb3929d07ea7df3382281965352cb1b.
- Source: https://www.mail-archive.com/ffmpeg-devel@ffmpeg.org/msg187572.html
- Original patch endpoint: https://code.ffmpeg.org/FFmpeg/FFmpeg/pulls/21668.patch
- Transport line wrapping was removed from the mailing-list copy. All nine resulting Git blob hashes were checked against the patch index hashes; all match (LF normalization).

The workflow builds a minimal Linux encoder/decoder, generates 7.1 and 5.1 tones, encodes E-AC-3, decodes without forcing channel count, and measures each channel against its distinct frequency. This tests the candidate bitstream and FFmpeg decoding. Passing does not prove Android ARMv7 performance, Media3 framing, HDMI compatibility or Atmos preservation.

No Atmos encoding is claimed. Extra channels in this experiment are discrete speaker channels.

## Measured status (2026-11-05)

- Original candidate builds on Linux, advertises 7.1, and produces a stream recognized as eight channels. The round-trip validation fails: channel index 6 is all zeros; subsequent channel analysis is required. A second, installed FFmpeg decoder reproduces the same decoded RMS values, including the silent channel.
- The independent 5.1 substream mapping uses indices derived from six-channel input. An isolated correction maps those indices back to the eight-channel input; this still fails the channel-6 test. This correction is a hypothesis under test, not an approved patch.
- Decoder inspection shows extended_channel_map initially maps channel indices directly and then replaces dependent-channel destinations. Expanding a side-surround 5.1 base to a 7.1 layout inserts back-surround channels before the side channels. The interaction needs to be tested separately before blaming only the encoder or declaring the bitstream valid.
- Runs: https://github.com/HeliumMarcos/NuvioTV/actions/runs/42082873006 and https://github.com/HeliumMarcos/NuvioTV/actions/runs/42083466185.

The candidate remains excluded from Android builds. Next gate: distinguish encoder channel loss from decoder substream assembly, using all-channel measurements and a 5.1 control, then verify with an independent implementation/device. An advertised channel count alone is insufficient.
