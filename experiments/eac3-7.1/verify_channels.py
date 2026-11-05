import array
import math
import subprocess
import sys
import wave
from pathlib import Path

FFMPEG = str(Path(sys.argv[1]).resolve())
out = Path(sys.argv[2]); out.mkdir(parents=True, exist_ok=True)
rate = 48000
freqs = [271, 383, 503, 83, 719, 887, 1061, 1291]

def run(*args):
    subprocess.run([FFMPEG, '-hide_banner', '-nostdin', '-y', *map(str, args)], check=True)

failures = []
for channels, layout in [(8, '7.1'), (6, '5.1')]:
    previous_failures = len(failures)
    src = out / f'input-{channels}.wav'
    pcm = array.array('h')
    for t in range(rate * 3):
        for c in range(channels):
            pcm.append(round(8000 * math.sin(2 * math.pi * freqs[c] * t / rate)))
    with wave.open(str(src), 'wb') as w:
        w.setnchannels(channels); w.setsampwidth(2); w.setframerate(rate); w.writeframes(pcm.tobytes())
    encoded = out / f'encoded-{channels}.eac3'
    decoded = out / f'decoded-{channels}.wav'
    run('-i', src, '-channel_layout', layout, '-c:a', 'eac3', '-b:a', '1024k' if channels == 8 else '640k', encoded)
    # No -ac or layout forcing on decode: the stream must actually describe all channels.
    run('-i', encoded, '-c:a', 'pcm_s16le', decoded)
    with wave.open(str(decoded), 'rb') as w:
        assert w.getnchannels() == channels, (channels, w.getnchannels())
        assert w.getframerate() == rate
        data = array.array('h', w.readframes(w.getnframes()))
    n = min(rate, len(data) // channels - rate // 2)
    assert n > rate // 2, 'truncated decode'
    for c in range(channels):
        signal = [data[(t + rate // 2) * channels + c] for t in range(n)]
        energies = []
        for f in freqs[:channels]:
            re = sum(v * math.cos(2 * math.pi * f * t / rate) for t, v in enumerate(signal))
            im = sum(v * math.sin(2 * math.pi * f * t / rate) for t, v in enumerate(signal))
            energies.append((re * re + im * im) ** 0.5 / n)
        peak = max(range(channels), key=energies.__getitem__)
        if peak != c or energies[c] <= 100:
            failures.append((layout, 'missing or incorrect channel', c, peak, energies))
        leakage = max(v for k, v in enumerate(energies) if k != c)
        if energies[c] <= 5 * max(leakage, 1):
            failures.append((layout, 'excess crosstalk', c, energies))
        print(f'{layout} channel {c}: {freqs[c]} Hz confirmed; amplitude={energies[c]:.1f}; leakage={leakage:.1f}', flush=True)
    print(f'{layout}: {len(failures) - previous_failures} validation failures', flush=True)

for failure in failures:
    print('FAIL:', failure, flush=True)
assert not failures, f'{len(failures)} channel validation failures'
