# E-AC-3 7.1 encoder proof

Isolated experiment; not used by the Android app yet. The working 5.1 branch stays unchanged.

- Base: FFmpeg c352a9ab022b945307f1ad5d35575f695a13c2ca (2026-02-06).
- Candidate: FFmpeg PR 21668, author Nomis101/ShortKatz, commit f32ba6711cb3929d07ea7df3382281965352cb1b.
- Source: https://www.mail-archive.com/ffmpeg-devel@ffmpeg.org/msg187572.html
- Original patch endpoint: https://code.ffmpeg.org/FFmpeg/FFmpeg/pulls/21668.patch
- Transport line wrapping was removed from the mailing-list copy. All nine resulting Git blob hashes were checked against the patch index hashes; all match (LF normalization).

The workflow builds a minimal Linux encoder/decoder, generates 7.1 and 5.1 tones, encodes E-AC-3, decodes without forcing channel count, and measures each channel against its distinct frequency. This tests the candidate bitstream and FFmpeg decoding. Passing does not prove Android ARMv7 performance, Media3 framing, HDMI compatibility or Atmos preservation.

No Atmos encoding is claimed. Extra channels in this experiment are discrete speaker channels.
