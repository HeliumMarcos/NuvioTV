from pathlib import Path
import sys


def replace_once(source, old, new):
    assert source.count(old) == 1, old
    return source.replace(old, new, 1)


root = Path(sys.argv[1]) / "libavcodec"
encoder = root / "ac3enc.c"
source = encoder.read_text()
source = replace_once(
    source,
    "} eac3_dep_channel_map[] = {",
    """} eac3_dep_channel_map[] = {
    { AV_CH_SIDE_LEFT,                                          3, 1 },
    { AV_CH_SIDE_RIGHT,                                         4, 1 },""",
)
source = replace_once(
    source,
    "        uint64_t dep_mask = mask & ~base_mask;",
    """        const int surround_71 = mask == AV_CH_LAYOUT_7POINT1;
        uint64_t dep_mask = mask & ~base_mask;
        if (surround_71)
            dep_mask |= AV_CH_SIDE_LEFT | AV_CH_SIDE_RIGHT;""",
)
source = replace_once(
    source,
    "                mask &= ~ch_mask;",
    "                mask &= ~(ch_mask & ~base_mask);",
)
source = replace_once(
    source,
    "            channels = avctx->ch_layout.nb_channels - dep_ch_idx;",
    "            channels = surround_71 ? 6 : avctx->ch_layout.nb_channels - dep_ch_idx;",
)
source = replace_once(
    source,
    """    int snr_offset, snr_incr;
    int bits_left, mantissa_bits;""",
    """    int snr_offset;
    int bits_left;""",
)
start = source.index("    /* Try bit allocation at max SNR offset first.")
end = source.index("    /* Final pass: calculate bap values", start)
source = source[:start] + """    /* Higher SNR offsets allocate more bits. Keep the highest offset
     * that fits instead of minimizing it and starving the surround channels. */
    int low = 0;
    int high = 1023;
    while (low < high) {
        int candidate = low + (high - low + 1) / 2;
        if (dep_count_mantissa_bits(s, (candidate - 240) * 4) <= bits_left)
            low = candidate;
        else
            high = candidate - 1;
    }
    snr_offset = low;

""" + source[end:]
source = replace_once(
    source,
    """    s->encode_frame(s, frame->extended_data);

    ac3_apply_rematrixing(s);""",
    """    if (s->eac3 && avctx->ch_layout.u.mask == AV_CH_LAYOUT_7POINT1) {
        float mixed_surround[2][AC3_FRAME_SIZE];
        uint8_t *base_samples[8];

        memcpy(base_samples, frame->extended_data, sizeof(base_samples));
        /* The dependent stream replaces the two downmixed surrounds and
         * adds the original rear channels. Keep the source planes intact. */
        for (int i = 0; i < 2; i++) {
            int side_index = s->channel_map_dep[i];
            const float *side = (const float *)frame->extended_data[side_index];
            const float *rear = (const float *)frame->extended_data[s->channel_map_dep[i + 2]];
            for (int sample = 0; sample < frame->nb_samples; sample++)
                mixed_surround[i][sample] = 0.5f * (side[sample] + rear[sample]);
            base_samples[side_index] = (uint8_t *)mixed_surround[i];
        }
        s->encode_frame(s, base_samples);
    } else {
        s->encode_frame(s, frame->extended_data);
    }

    ac3_apply_rematrixing(s);""",
)
encoder.write_text(source)

header = root / "eac3enc.c"
source = header.read_text()
source = replace_once(
    source,
    "    int dep_acmod = get_dep_acmod(s->dependent_channels);",
    """    int dep_acmod = s->dependent_channel_map == 0x1a00
        ? AC3_CHMODE_3F1R : get_dep_acmod(s->dependent_channels);""",
)
header.write_text(source)
