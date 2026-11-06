# Histórico completo e inventário

## Referências da comparação

Upstream `dev`: `0e320e3c397c51d26923d6fcc7906a4b886e82c5`.

Ancestral comum: `8f3112655d3d128c4888b9c69e37d44f2d001fb2`.

Código de áudio consolidado: `b549315aae8d0764392ed1d6ad6462f0a74396f3`.

Os títulos abaixo são os títulos originais dos commits. Eles incluem passos intermediários substituídos posteriormente; o comportamento final está no relatório técnico.

## feature/eac3-transcoding — 143 commits

- [`8ef62e99b`](https://github.com/HeliumMarcos/NuvioTV/commit/8ef62e99ba05514fc47fa2546cb80bc2ca2d42da) fix(audio): validate AC3 FIFO write and read sample counts
- [`06442ca1b`](https://github.com/HeliumMarcos/NuvioTV/commit/06442ca1b998819e00c79fc2501a578e9b4d633e) fix(audio): validate AC3 conversion buffer dimensions and allocations
- [`9227cd93a`](https://github.com/HeliumMarcos/NuvioTV/commit/9227cd93ad40c0a993b5bf9b515e5281e7c239c5) fix(audio): resample AC3 transcoding to 48 kHz and report encoded sample rate
- [`16c4dbccd`](https://github.com/HeliumMarcos/NuvioTV/commit/16c4dbccd7f43fed8ffffd5de14b66141e97dd8d) fix(audio): negotiate 48 kHz AC3 output with Media3 audio sink
- [`5b523ddfb`](https://github.com/HeliumMarcos/NuvioTV/commit/5b523ddfb180a6407fde9d9014c2c9901d8cd45b) build(ffmpeg): include E-AC-3 encoder in experimental native build
- [`21e83dfad`](https://github.com/HeliumMarcos/NuvioTV/commit/21e83dfadf952266575927568d60bf25de5ed24c) feat(audio): add native PCM AC3 EAC3 transcoding mode selection
- [`75982f03d`](https://github.com/HeliumMarcos/NuvioTV/commit/75982f03d7c6aafad9031b4e1e337f06201ba499) feat(audio): pass AC3 or EAC3 transcoding mode from Java to JNI
- [`3af45fda3`](https://github.com/HeliumMarcos/NuvioTV/commit/3af45fda367e45232a4e2d296f6bdc9ffe207414) fix(audio): preserve EAC3 mode when resetting TrueHD decoder
- [`fd41f724f`](https://github.com/HeliumMarcos/NuvioTV/commit/fd41f724f250d9953ae924100b3d0fb1a2d7e0af) feat(audio): negotiate opt-in EAC3 output with AC3 fallback in Media3 renderer
- [`b2673444a`](https://github.com/HeliumMarcos/NuvioTV/commit/b2673444a495709c16aab172b70bb6d5ea19a644) fix(audio): ensure encoder frame is writable and reject invalid transcoding modes
- [`4aa48b9e1`](https://github.com/HeliumMarcos/NuvioTV/commit/4aa48b9e170570e8e3fe76556adfb508684af236) docs(audio): add experimental transcoding build and playback verification plan
- [`80eb3dd98`](https://github.com/HeliumMarcos/NuvioTV/commit/80eb3dd98501fbc9108bead2adf9bfe424c50e21) fix(audio): avoid unnecessary E-AC-3 to E-AC-3 re-encoding
- [`48712d5ef`](https://github.com/HeliumMarcos/NuvioTV/commit/48712d5ef44853258c86f2faba3cf40049c11fd0) fix(audio): refresh decoder output format after native reset
- [`e0036191a`](https://github.com/HeliumMarcos/NuvioTV/commit/e0036191af96bbe8393aade7830ac99cd67660e5) build(audio): fail fast when local FFmpeg source or build paths are missing
- [`3ecbee202`](https://github.com/HeliumMarcos/NuvioTV/commit/3ecbee202c75523c9c2cde64d73213574fb29cb1) fix(build): retain default prebuilt decoder configuration compatibility
- [`c06031905`](https://github.com/HeliumMarcos/NuvioTV/commit/c060319055db748fa3729001806474b9d26df004) docs(audio): document local FFmpeg build selection and release blockers
- [`dd0168d3d`](https://github.com/HeliumMarcos/NuvioTV/commit/dd0168d3df8b6082000db2fb19d159a0b3d01615) fix(audio): allow zero-output resampler frames during rate conversion
- [`f66bc77af`](https://github.com/HeliumMarcos/NuvioTV/commit/f66bc77afb8f9bfe9ad538998c4d563060bfc74d) fix(audio): clear native encoder pointers on partial initialization failures
- [`e743ef616`](https://github.com/HeliumMarcos/NuvioTV/commit/e743ef616da6694c198877deef8a1275a6257c14) fix(audio): validate direct JNI audio buffers and capacities
- [`8378f4b29`](https://github.com/HeliumMarcos/NuvioTV/commit/8378f4b2925a488f18534406e06f3e6b77cb66c3) fix(audio): prevent signed overflow when growing encoded output buffers
- [`d8accc491`](https://github.com/HeliumMarcos/NuvioTV/commit/d8accc491df1f5c2191ed56ef5f8deb3591701bb) build(audio): include limits header for encoded packet size guard
- [`007854265`](https://github.com/HeliumMarcos/NuvioTV/commit/007854265d7894707b847c4db1c8e8459dac6e65) docs(audio): record unresolved EOS drain and encoded timestamp risks
- [`8123bca6e`](https://github.com/HeliumMarcos/NuvioTV/commit/8123bca6efbc55b171ab09f566626372cc4b179f) fix(audio): validate encoder channel layout and frame size before FIFO setup
- [`9785c637f`](https://github.com/HeliumMarcos/NuvioTV/commit/9785c637f71674e403e81518f9a408e481ef9895) fix(audio): handle encoder frame channel layout allocation failures
- [`2b19bc619`](https://github.com/HeliumMarcos/NuvioTV/commit/2b19bc619da8d86b10e47f2d7dcb7ccbbfd9f75b) fix(audio): zero FFmpeg extradata input padding
- [`3fc9969b9`](https://github.com/HeliumMarcos/NuvioTV/commit/3fc9969b957f033e9ff7416eaa41b0c7ef56e1aa) fix(audio): validate PCM output dimensions before buffer growth
- [`e61a35a58`](https://github.com/HeliumMarcos/NuvioTV/commit/e61a35a580e13fc805db17814dcf0e8d9569d74c) fix(audio): stop decoder initialization on JNI extradata read failure
- [`001552681`](https://github.com/HeliumMarcos/NuvioTV/commit/001552681d2d261135c583eb9b5bce9550ced32f) fix(audio): handle JNI codec-name string allocation failure
- [`543b05eb7`](https://github.com/HeliumMarcos/NuvioTV/commit/543b05eb77f84df91ce2b25deb8e5de463e3d239) fix(audio): never drop decoded frames when resampler predicts zero output
- [`6b822a9cd`](https://github.com/HeliumMarcos/NuvioTV/commit/6b822a9cd82ca5ffd47d3555d211b492e429d6ae) fix(audio): avoid zero-length FIFO writes after resampler consumes input
- [`ae4c67a29`](https://github.com/HeliumMarcos/NuvioTV/commit/ae4c67a293482af28539436dc1600e75c451511d) fix(audio): fall back to PCM when audio sink rejects both AC3 and EAC3
- [`ed8c8ea4c`](https://github.com/HeliumMarcos/NuvioTV/commit/ed8c8ea4c7d9221d06de46eb100bbabaa8c92311) fix(audio): align renderer capability reporting with PCM fallback
- [`e7e38ddb3`](https://github.com/HeliumMarcos/NuvioTV/commit/e7e38ddb3cbe3a54ad53d932b0abd69edec3a4e1) fix(audio): assign monotonic sample-based timestamps to encoded AC3/EAC3 frames
- [`940cc0a8b`](https://github.com/HeliumMarcos/NuvioTV/commit/940cc0a8b20b0983eb3e41df56f94c700a1fa4f3) docs(audio): distinguish encoder frame PTS from Media3 presentation timestamps
- [`029fa2dc3`](https://github.com/HeliumMarcos/NuvioTV/commit/029fa2dc354bd60d6553055c8254a3365cfe67ef) feat(audio): expose first encoded packet PTS in microseconds through JNI
- [`779442332`](https://github.com/HeliumMarcos/NuvioTV/commit/779442332cc7fa539d1540039a751942102afd2d) feat(audio): anchor encoded packet timestamps to Media3 input timeline
- [`86819d81e`](https://github.com/HeliumMarcos/NuvioTV/commit/86819d81e25392c9131b2d5189a4ed8380fd8fca) docs(audio): record Media3 SimpleDecoder EOS bypass and timestamp bridge limitations
- [`e255f4f95`](https://github.com/HeliumMarcos/NuvioTV/commit/e255f4f953f185b40811ba25ea97522f69291805) fix(audio): preserve unknown encoded PTS and use DTS fallback when available
- [`dd48e86be`](https://github.com/HeliumMarcos/NuvioTV/commit/dd48e86be5ea0ef82f88d1c9bee8e7fd6af51385) fix(audio): prevent overflow when mapping encoder PTS to media time
- [`e21cd02e0`](https://github.com/HeliumMarcos/NuvioTV/commit/e21cd02e05ba3000974278028d472c91583996a0) feat(audio): report compressed access-unit count for each JNI decode
- [`d942c5f40`](https://github.com/HeliumMarcos/NuvioTV/commit/d942c5f406a599772287677dc381bd52e187301f) fix(audio): fail safely on multi-access-unit encoded buffers instead of misframing sink
- [`99c7ac804`](https://github.com/HeliumMarcos/NuvioTV/commit/99c7ac804ee0e12acb48c2608a25d33b718b89c7) docs(audio): describe encoded access-unit fail-fast guard and remaining queue requirement
- [`72a8a0569`](https://github.com/HeliumMarcos/NuvioTV/commit/72a8a0569133af943cd27ae648cc6f3f0bac3ff6) feat(audio): queue individual encoded access units and emit one per decoder output
- [`88e267ea5`](https://github.com/HeliumMarcos/NuvioTV/commit/88e267ea5c30f2cc74f6ff17a30572972a40cfb7) fix(audio): derive output PTS only from dequeued encoded access unit
- [`a400c1fd7`](https://github.com/HeliumMarcos/NuvioTV/commit/a400c1fd784b3340a71cfa56f6e5e960b69ba6c7) docs(audio): describe encoded packet queue and unresolved EOS output scheduling
- [`13e66292d`](https://github.com/HeliumMarcos/NuvioTV/commit/13e66292d6a8c56429fbbbec9be7f4ac9296751e) refactor(audio): extract reusable single-access-unit queue dequeue primitive
- [`a03f37def`](https://github.com/HeliumMarcos/NuvioTV/commit/a03f37def2d7744ed3ce54354d3a9e793564793b) feat(audio): expose independent native encoded packet dequeue and pending count
- [`2547f84d5`](https://github.com/HeliumMarcos/NuvioTV/commit/2547f84d5603d3ee62ae7b8a730fc4ed47d4f3e8) feat(audio): declare native packet dequeue interface for future EOS output scheduler
- [`259be59da`](https://github.com/HeliumMarcos/NuvioTV/commit/259be59daf3101fd3fd2e1c75592fd504dfa8f34) docs(audio): record independent packet dequeue API and EOS integration blockers
- [`d32181d27`](https://github.com/HeliumMarcos/NuvioTV/commit/d32181d2778efec059b21b30f2a7e5d59c43a373) feat(audio): add Java access-unit dequeue helper for drain-capable output scheduler
- [`ee0783fa1`](https://github.com/HeliumMarcos/NuvioTV/commit/ee0783fa1c95d36088027358cad9dda1cb97d4c1) docs(native): correct comments for queue dequeue and decode entrypoints
- [`90a7f8978`](https://github.com/HeliumMarcos/NuvioTV/commit/90a7f897851a056a3cf05dadb23d04f032700b64) docs(audio): track Java queue bridge and remaining EOS scheduling work
- [`9076d3be5`](https://github.com/HeliumMarcos/NuvioTV/commit/9076d3be552d5808ae1ab7f9f7567f2625cbcf5d) feat(audio): add explicit final encoder FIFO padding and packet flush primitive
- [`3bf9aebe8`](https://github.com/HeliumMarcos/NuvioTV/commit/3bf9aebe8c738069850a810b7c346bebcb89d772) feat(audio): expose encoder final-frame flush through Java decoder bridge
- [`c005e44cb`](https://github.com/HeliumMarcos/NuvioTV/commit/c005e44cb90e8243936b402fc444d67efa9c1c09) docs(audio): mark encoder flush as partial EOS stage and document ordering constraints
- [`067392e64`](https://github.com/HeliumMarcos/NuvioTV/commit/067392e64707bfa96114ed99c3fd3c503e4b5ece) fix(audio): guard repeated encoder EOS and validate silence padding
- [`6afd3f1e9`](https://github.com/HeliumMarcos/NuvioTV/commit/6afd3f1e9881be382d51b6c6647ae60cffc56b30) docs(audio): document terminal encoder flush guards and unverified status
- [`1f26243e2`](https://github.com/HeliumMarcos/NuvioTV/commit/1f26243e247fcf6e9ce9b0e31223c6de05752d3d) feat(audio): expose delayed FFmpeg decoder frame drain before encoder flush
- [`24a25216f`](https://github.com/HeliumMarcos/NuvioTV/commit/24a25216f2604b4065e26f747c3420301e370410) fix(audio): reject new compressed input after decoder drain until reset
- [`49862fdf2`](https://github.com/HeliumMarcos/NuvioTV/commit/49862fdf233fe0688b075502af1dc7c09d17e57d) feat(audio): expose delayed native decoder drain to Java output scheduler
- [`a147ab99b`](https://github.com/HeliumMarcos/NuvioTV/commit/a147ab99b212211e302b8820b63ea7da97c84441) docs(audio): document delayed decoder drain and remaining resampler EOS blocker
- [`123a5200a`](https://github.com/HeliumMarcos/NuvioTV/commit/123a5200a2920227f2b56ce79eea61452589b7b8) feat(audio): track confirmed FFmpeg decoder EOF separately from empty output
- [`4971596c8`](https://github.com/HeliumMarcos/NuvioTV/commit/4971596c851beb79afd2e44093254e22e6a7187b) feat(audio): expose confirmed decoder drain completion to Java
- [`b94460663`](https://github.com/HeliumMarcos/NuvioTV/commit/b94460663fe972e801b8456bca4033a4b3a6c276) fix(audio): require confirmed decoder EOF before terminal encoder flush
- [`68f39f39c`](https://github.com/HeliumMarcos/NuvioTV/commit/68f39f39c1789f29519c4a835471ea868e251840) docs(audio): record confirmed decoder EOF and encoder flush ordering guard
- [`04109f713`](https://github.com/HeliumMarcos/NuvioTV/commit/04109f713468c73e2780ed08e6f81e8b1b83e207) feat(audio): drain delayed resampler samples into encoded access-unit queue
- [`7666add3c`](https://github.com/HeliumMarcos/NuvioTV/commit/7666add3caa09d1cb19ace2b2888c8f7e9b67e8c) feat(audio): expose resampler drain stage to Java decoder
- [`ad85421e0`](https://github.com/HeliumMarcos/NuvioTV/commit/ad85421e0c0be35f69ea9d8aa829736bbd1d25b4) build(audio): include FFmpeg allocation and sample-format declarations explicitly
- [`fe9c47b0e`](https://github.com/HeliumMarcos/NuvioTV/commit/fe9c47b0e5a1571da58b55b9b940083ff6345923) docs(audio): record resampler drain implementation and remaining Media3 integration
- [`242d30182`](https://github.com/HeliumMarcos/NuvioTV/commit/242d30182d5da1b086500f9e1dd9becaa518ef29) feat(audio): expose explicit resampler drain completion for EOS scheduler
- [`69dec5183`](https://github.com/HeliumMarcos/NuvioTV/commit/69dec518391943e1246bffd608a95d5db1d686cf) feat(audio): expose resampler drain completion to Java scheduler
- [`05e5726e2`](https://github.com/HeliumMarcos/NuvioTV/commit/05e5726e2836a2e269dd7abe438647fe6ebc2cc3) docs(audio): specify Media3 EOS scheduler architecture from upstream SimpleDecoder behavior
- [`381c41394`](https://github.com/HeliumMarcos/NuvioTV/commit/381c4139475787963ca6b6572d7c41c7d47ecc39) fix(audio): bound resampler drain allocation and validate planar buffer dimensions
- [`6d3e0cd5f`](https://github.com/HeliumMarcos/NuvioTV/commit/6d3e0cd5f02b42676d97097dfea18c9cdfc67626) fix(audio): validate JNI grown direct buffers and release temporary local references
- [`9df9c42a0`](https://github.com/HeliumMarcos/NuvioTV/commit/9df9c42a0dc2049cdd92893a3a61c5bc8575030c) docs(audio): document bounded resampler allocation and JNI buffer safety checks
- [`01cf596e1`](https://github.com/HeliumMarcos/NuvioTV/commit/01cf596e10aeffbc6d71d6460dba1e32846e5a9c) fix(audio): handle FFmpeg decoder EOS backpressure without dropping delayed frames
- [`fb4b36c7e`](https://github.com/HeliumMarcos/NuvioTV/commit/fb4b36c7e0e206248a72ce36236e784934395285) fix(audio): pause resampler drain before encoded packet queue saturation
- [`75f07d424`](https://github.com/HeliumMarcos/NuvioTV/commit/75f07d424644988bc87d786e192cddd216e37d1c) docs(audio): describe decoder EOS backpressure and bounded resampler queue handoff
- [`b1d803611`](https://github.com/HeliumMarcos/NuvioTV/commit/b1d803611c39a66c811dd8abcd7dfdeef66a10c5) feat(audio): add ordered encoded EOS drain coordinator for future Media3 output scheduler
- [`6a9a90e15`](https://github.com/HeliumMarcos/NuvioTV/commit/6a9a90e15d0a636d9496d0034f46e21c7b4a2ba9) docs(audio): describe ordered Java encoded EOS drain coordinator and Media3 integration gap
- [`37e872e0b`](https://github.com/HeliumMarcos/NuvioTV/commit/37e872e0b8bccdc754a36883c38f546dcc6c1a6d) fix(audio): track accepted final encoder frame to avoid duplicate silence on EOF retry
- [`6e743af99`](https://github.com/HeliumMarcos/NuvioTV/commit/6e743af997e694df68dea84a6d88e94ca740ff73) docs(audio): record final encoder frame retry state and remaining EOS backpressure risks
- [`be55078ab`](https://github.com/HeliumMarcos/NuvioTV/commit/be55078ab6301b33617160bc3b1a4cabd662a8ec) fix(audio): make encoder EOS flush retryable and track confirmed encoder EOF
- [`e3b5e5f28`](https://github.com/HeliumMarcos/NuvioTV/commit/e3b5e5f2897e0d3a66a8dd2b64de63410e1c703e) feat(audio): expose confirmed encoder EOF state to Java drain coordinator
- [`4739e3422`](https://github.com/HeliumMarcos/NuvioTV/commit/4739e34220ac6bc78371a09fd17a9957ce9554ae) fix(audio): retry encoder EOS drain until native receive_packet confirms completion
- [`d1f012e3e`](https://github.com/HeliumMarcos/NuvioTV/commit/d1f012e3e8a88d88dfa786908eb2b9765f05e429) refactor(audio): remove redundant Java encoder flush state after native EOF confirmation
- [`8262227ce`](https://github.com/HeliumMarcos/NuvioTV/commit/8262227ce8cd10d84e7c4990ef96022efdc43045) docs(audio): explain retryable encoder EOS and explicit completion contract
- [`26b82fa15`](https://github.com/HeliumMarcos/NuvioTV/commit/26b82fa158b95c8e6e26f125e33f42e45522c050) feat(audio): dispatch synthetic EOS drain work on SimpleDecoder worker thread
- [`6bce4dbff`](https://github.com/HeliumMarcos/NuvioTV/commit/6bce4dbff4fed677580a85dc95c9e9c2cb7fe731) feat(audio): signal synthetic EOS drain step completion across decoder and renderer threads
- [`1c77731ae`](https://github.com/HeliumMarcos/NuvioTV/commit/1c77731aea8a50a5480d8258f7b1c8e9fddc6e3d) feat(audio): add Media3 Decoder wrapper to schedule encoded drain buffers before EOS
- [`b33ee988c`](https://github.com/HeliumMarcos/NuvioTV/commit/b33ee988c497730a83e7bdbcb4aa19c028205973) feat(audio): wire encoded EOS scheduler into Media3 FFmpeg audio renderer
- [`33d8d0cef`](https://github.com/HeliumMarcos/NuvioTV/commit/33d8d0cef58c4fe6fa908db132416a47109cc26b) fix(audio): suppress synthetic drain skip discontinuities at Media3 audio sink
- [`40af29375`](https://github.com/HeliumMarcos/NuvioTV/commit/40af2937590b2f41e6fbac4c6454abd361ccad8a) docs(audio): describe integrated Media3 EOS scheduler and required device verification
- [`678c8124c`](https://github.com/HeliumMarcos/NuvioTV/commit/678c8124c2f70edaec22fe79b8de4e7f65626265) fix(audio): wake Media3 renderer after empty synthetic EOS drain steps
- [`b2c085005`](https://github.com/HeliumMarcos/NuvioTV/commit/b2c085005d6ec899727a221b04061a8fbdee380e) fix(audio): register EOS drain wakeups through Media3 renderer callback
- [`a13c0278b`](https://github.com/HeliumMarcos/NuvioTV/commit/a13c0278bec269b541937e550e5302cf51852691) docs(audio): record Media3 EOS wakeup behavior for empty drain steps
- [`0a7053cb2`](https://github.com/HeliumMarcos/NuvioTV/commit/0a7053cb20eeb3b9b78997cdbd22b67e5fc4ebd5) feat(audio): expose worker-thread pending packet snapshot and synthetic packet-only drain
- [`6a2697f65`](https://github.com/HeliumMarcos/NuvioTV/commit/6a2697f65af9cb4a0314c8da855bbd5e070c1364) feat(audio): schedule pending encoded access units during normal Media3 playback
- [`f95d4e563`](https://github.com/HeliumMarcos/NuvioTV/commit/f95d4e56354c8b97878d80cfa944df45537d4223) docs(audio): document continuous encoded packet draining and stress-test requirements
- [`7da9c9fb7`](https://github.com/HeliumMarcos/NuvioTV/commit/7da9c9fb7cf0c204580b9d41886e71404684093c) fix(audio): retain final FIFO samples until encoder accepts padded frame
- [`09578dca1`](https://github.com/HeliumMarcos/NuvioTV/commit/09578dca143aa106f5ef21e7b905603572cef97b) fix(audio): invalidate stale encoded packet scheduling state on seek
- [`0503894f4`](https://github.com/HeliumMarcos/NuvioTV/commit/0503894f4ff9b2d33bbe13d977d65f5bf3b315f4) fix(audio): prevent stale packet-only drain tokens after Media3 flush
- [`c4c4a5b54`](https://github.com/HeliumMarcos/NuvioTV/commit/c4c4a5b54d8df87a89eda1f955ebf118253e4a44) docs(audio): track final-frame EAGAIN retention and seek-safe queue invalidation
- [`995ac46f8`](https://github.com/HeliumMarcos/NuvioTV/commit/995ac46f89b223752b62b265aeaf8b835f4d6265) fix(audio): preserve resampler FIFO frame when encoder applies backpressure
- [`fd1bfcc41`](https://github.com/HeliumMarcos/NuvioTV/commit/fd1bfcc41810793ef32dfd6b21fcff84b90cfc8a) fix(audio): collect encoder output on resampler-drain EAGAIN before retry
- [`cf48c089f`](https://github.com/HeliumMarcos/NuvioTV/commit/cf48c089fa23ab9028f8e4c7e586ab6d6179faf9) fix(audio): drain retained full FIFO frames before declaring resampler EOF
- [`c0073cbb8`](https://github.com/HeliumMarcos/NuvioTV/commit/c0073cbb8c29ced2df48ceb2fca0a392d903b91f) fix(audio): retry retained FIFO frame when resampler reports zero delayed samples
- [`39f95de3a`](https://github.com/HeliumMarcos/NuvioTV/commit/39f95de3a0a5eff10b8df6249cfe96f38bde7b50) docs(audio): add resampler-drain backpressure and FIFO retention verification
- [`1813ccd9f`](https://github.com/HeliumMarcos/NuvioTV/commit/1813ccd9f2296138142eb5201813e93458c32962) fix(audio): invalidate terminal EOS completion on Media3 seek and flush
- [`d234b747b`](https://github.com/HeliumMarcos/NuvioTV/commit/d234b747b6c941015929bd1fca7a7d2fb884cbcf) fix(audio): initialize encoded output format for packets first emitted during EOS drain
- [`8be8a7bc5`](https://github.com/HeliumMarcos/NuvioTV/commit/8be8a7bc5ffe58e0e0c31a4eb6b95900c0ab2469) docs(audio): add short-stream format and EOS seek regression cases
- [`fef889ec3`](https://github.com/HeliumMarcos/NuvioTV/commit/fef889ec3e1081da92f1d1664136476388a55c0d) fix(audio): pause encoder EOF packet reception before pending queue saturation
- [`bd7148a47`](https://github.com/HeliumMarcos/NuvioTV/commit/bd7148a47ee9e67a142a1554dbf3f4bf52921338) fix(audio): reject compressed media input after Media3 EOS request
- [`be40266b1`](https://github.com/HeliumMarcos/NuvioTV/commit/be40266b1271015369a473080844980e9e1712da) docs(audio): add terminal queue saturation and EOS state-machine tests
- [`b93a6f12c`](https://github.com/HeliumMarcos/NuvioTV/commit/b93a6f12c73f67a0152bd4e6018fcadd7de95366) fix(audio): consume normal transcoding FIFO frames only after encoder accepts them
- [`f557ad8c9`](https://github.com/HeliumMarcos/NuvioTV/commit/f557ad8c9d72ae5bf025cf57e2b5e47161c4a37d) fix(audio): update compressed packet validation error after scheduler integration
- [`73ffb44c7`](https://github.com/HeliumMarcos/NuvioTV/commit/73ffb44c7fe956a9b65e28d8da45397131d3394f) docs(audio): record FIFO acceptance invariant and remaining main-path EAGAIN blocker
- [`63f2bbbde`](https://github.com/HeliumMarcos/NuvioTV/commit/63f2bbbde9b1999bfe772b6554c46b0b666b30f7) ci(audio): compile experimental Media3 FFmpeg Java decoder on feature branch
- [`ac4f585b2`](https://github.com/HeliumMarcos/NuvioTV/commit/ac4f585b245a654c3420ecd1efd10b4eb8560018) build(audio): support arm64-only FFmpeg encoder build for CI
- [`30044aafe`](https://github.com/HeliumMarcos/NuvioTV/commit/30044aafe2de0e1d0420b5d30734af71f9568eeb) ci(audio): build pinned FFmpeg arm64, JNI and decoder AAR on experimental branch
- [`0c56101f3`](https://github.com/HeliumMarcos/NuvioTV/commit/0c56101f3d3294082ecc10b8173dea20b2d6b4e4) ci(audio): assemble and publish experimental Android TV debug APK
- [`8944e9546`](https://github.com/HeliumMarcos/NuvioTV/commit/8944e95466a18c6fd24511a76f7ba42102b7e00b) ci(audio): avoid deprecated Android SDK tools package during runner setup
- [`8f276dae9`](https://github.com/HeliumMarcos/NuvioTV/commit/8f276dae96ab24b51d5a32d0b78ad36a9dbeedc2) ci(audio): fix missing sdkmanager using setup-android v4 and validate NDK
- [`9e82c3d14`](https://github.com/HeliumMarcos/NuvioTV/commit/9e82c3d14684b1f35e4f43e6717761d0f9a1f444) fix(audio): resolve Media3 decoder Java compilation errors
- [`00dfd9aea`](https://github.com/HeliumMarcos/NuvioTV/commit/00dfd9aea272d56043274ac24f5f0e9111c926c5) fix(audio): resolve Media3 decoder Java compilation errors
- [`d5ebf6c4d`](https://github.com/HeliumMarcos/NuvioTV/commit/d5ebf6c4db48fd3b1aebc471a84f25337a89cdab) fix(audio): remove callback APIs absent in Media3 1.8.0 decoder
- [`041d56b33`](https://github.com/HeliumMarcos/NuvioTV/commit/041d56b33980205f035b2f215ccbd3e77aa6613d) fix(audio): use Media3 1.8.0 Decoder interface without callbacks
- [`5dbed2ac5`](https://github.com/HeliumMarcos/NuvioTV/commit/5dbed2ac55a13b4b9392e4331025820c6d03714e) ci(audio): sign experimental debug APK with debug key instead of release keystore
- [`3395b8cc2`](https://github.com/HeliumMarcos/NuvioTV/commit/3395b8cc2fe7020178f910fa21816b5bbf663172) ci(audio): enable debug signing and collect flavored APK outputs
- [`b45e4bf11`](https://github.com/HeliumMarcos/NuvioTV/commit/b45e4bf112e92d0bcdedebb8201cc7136e711d41) ci(audio): discover actual APK output and stage full debug artifact
- [`cce54401c`](https://github.com/HeliumMarcos/NuvioTV/commit/cce54401c8448fafb8f329231bbe62b8f888416f) build(ffmpeg): allow isolated armeabi-v7a transcoder compilation
- [`f2c30cf11`](https://github.com/HeliumMarcos/NuvioTV/commit/f2c30cf118d409eee9441dba30587a5df304b9d0) ci(audio): build and publish armeabi-v7a full debug experimental APK
- [`37d2cb547`](https://github.com/HeliumMarcos/NuvioTV/commit/37d2cb5479f1e99b6455d3292dcd9021b0c7f15d) feat(audio): wire experimental E-AC-3 format selection into playback
- [`4c7a90011`](https://github.com/HeliumMarcos/NuvioTV/commit/4c7a900118a040b5dcd378e2ef2bb854269498fc) fix(ci): preserve existing installs with separate EAC3 identity and stable signing
- [`a8d059945`](https://github.com/HeliumMarcos/NuvioTV/commit/a8d0599458f887ac363b969b4da123b9eff8a611) fix(ui): keep experimental launcher label distinct in every locale
- [`7af0e15c5`](https://github.com/HeliumMarcos/NuvioTV/commit/7af0e15c5f8fe1754beb0b5520d63410c3addbee) fix(ui): apply experimental label to launcher activities
- [`008112fd8`](https://github.com/HeliumMarcos/NuvioTV/commit/008112fd851f17a3de72c946d9811f1152f300f3) feat(player): show source and initialized AudioTrack formats in debug statistics
- [`87effb0a4`](https://github.com/HeliumMarcos/NuvioTV/commit/87effb0a45afe9ddf61b7ac858929f0ebcb61308) fix(player): bind debug audio readings to the live AudioTrack and routed device
- [`240f3e2aa`](https://github.com/HeliumMarcos/NuvioTV/commit/240f3e2aa8c76f27ae5818a797c1c520fe85dce3) fix(player): return unavailable explicitly for released debug audio tracks
- [`bea4ab45b`](https://github.com/HeliumMarcos/NuvioTV/commit/bea4ab45bbe6451bfd22f041de91ff19e53012ae) fix(signing): bind CI to explicit keystore and verify pinned certificate before publishing
- [`dbb0bc2a6`](https://github.com/HeliumMarcos/NuvioTV/commit/dbb0bc2a6b17cf4e8ed1827615acac20a6f5c4bd) fix(ci): use supported workspace context for signing key path
- [`fb201e0a3`](https://github.com/HeliumMarcos/NuvioTV/commit/fb201e0a3aaad264456b1d11ecdcd8e5d60dc1f4) feat(audio): select transcoding source codecs and clarify channel statistics
- [`e2840f804`](https://github.com/HeliumMarcos/NuvioTV/commit/e2840f804a6facd49f7dae29098855782b77d226) feat(audio): separate source selections by destination and allow DDP to AC3

## Etapa adicional 5.1/7.1 e Atmos — 18 commits

- [`5c7040639`](https://github.com/HeliumMarcos/NuvioTV/commit/5c704063965fe71a1f12375d0385aac2668efd71) test(audio): add isolated EAC3 7.1 encoder and channel integrity proof
- [`daa9257fd`](https://github.com/HeliumMarcos/NuvioTV/commit/daa9257fd915ce2dc20a85e3feeab0dd72a5f72d) test(audio): investigate independent substream channel mapping for 7.1 input
- [`09625ec39`](https://github.com/HeliumMarcos/NuvioTV/commit/09625ec39243db869c7c7153cba162d77533e7d9) test(audio): report all channel failures and retain 5.1 regression evidence
- [`9816c1490`](https://github.com/HeliumMarcos/NuvioTV/commit/9816c1490832fc279498bff3187eb7ec86a96999) test(audio): isolate decoder mapping when merging EAC3 dependent substreams
- [`fdb4f6be3`](https://github.com/HeliumMarcos/NuvioTV/commit/fdb4f6be305a2614ae25120d974297b29d679ae9) feat(audio): prototype channel-preserving EAC3 5.1 and 7.1 on Android
- [`373db15a4`](https://github.com/HeliumMarcos/NuvioTV/commit/373db15a4188614c922114e59f7f82b6e0a32578) fix(audio): honor transcoding selections and prevent silent PCM or codec fallback
- [`55dcb0893`](https://github.com/HeliumMarcos/NuvioTV/commit/55dcb0893ed6a9ae766de13c2dc0f5b7e78b34de) fix(audio): stop advertising unsupported HDMI codecs and eight-channel capability
- [`d0ed49e08`](https://github.com/HeliumMarcos/NuvioTV/commit/d0ed49e0899714e12bb46294c9a44e72fa1bba16) ci(audio): validate ARMv7 experimental branch and run scoped unit tests separately
- [`0660563e1`](https://github.com/HeliumMarcos/NuvioTV/commit/0660563e169d60b61cc72c4a8b1865519f6617a1) fix(audio): report the JOC bed instead of the passthrough track mask
- [`c992e0cb4`](https://github.com/HeliumMarcos/NuvioTV/commit/c992e0cb44f96542c8320fd2be891e2d0029620c) Encode all four surrounds in the E-AC-3 7.1 extension
- [`46e6ac2cc`](https://github.com/HeliumMarcos/NuvioTV/commit/46e6ac2ccd3f25272b4f7a86c104561831640738) Read discrete E-AC-3 channels and preserve surround signal level
- [`1d7c1a139`](https://github.com/HeliumMarcos/NuvioTV/commit/1d7c1a139e93bd3a9a1f7049adeb9b214c5501af) Accept valid E-AC-3 bitstream IDs in surround detection
- [`9aa374297`](https://github.com/HeliumMarcos/NuvioTV/commit/9aa3742979da6123273cee6719fe8e9059d69076) Run experimental audio workflows on Node 24 actions
- [`65476e45f`](https://github.com/HeliumMarcos/NuvioTV/commit/65476e45f351c18f8154a0817953b9f10bbd405e) Use the correct Matroska lacing modes in audio probing
- [`6b8f68a61`](https://github.com/HeliumMarcos/NuvioTV/commit/6b8f68a619094a1aaa308fdc4bdb3ed5d3a0f140) Add optional 5.1 output for DDP transcoding
- [`fe4342f77`](https://github.com/HeliumMarcos/NuvioTV/commit/fe4342f7704cd6415c19f7d0ac63c2bb3a7fb228) Run experimental audio builds only on explicit dispatch
- [`3405b95ef`](https://github.com/HeliumMarcos/NuvioTV/commit/3405b95ef11a82e1012e2e07e85da3263d78fc9c) Detect Dolby Atmos JOC in Matroska audio frames
- [`b549315aa`](https://github.com/HeliumMarcos/NuvioTV/commit/b549315aae8d0764392ed1d6ad6462f0a74396f3) fix(audio): detect verified TrueHD Atmos spatial presentations

## Inventário do diff acumulado em relação ao ancestral comum

| Arquivo | Linhas adicionadas | Linhas removidas |
| --- | ---: | ---: |
| `.github/workflows/eac3-71-proof.yml` | 44 | 0 |
| `.github/workflows/eac3-java-compile.yml` | 132 | 0 |
| `app/build.gradle.kts` | 25 | 2 |
| `app/proguard-rules.pro` | 9 | 1 |
| `app/src/debug/AndroidManifest.xml` | 10 | 0 |
| `app/src/debug/res/values/strings.xml` | 1 | 1 |
| `app/src/main/java/com/nuvio/tv/core/player/dvmkv/Eac3Surround.java` | 86 | 0 |
| `app/src/main/java/com/nuvio/tv/core/player/dvmkv/MatroskaExtractor.java` | 145 | 42 |
| `app/src/main/java/com/nuvio/tv/core/player/dvmkv/TrueHdAtmos.java` | 83 | 0 |
| `app/src/main/java/com/nuvio/tv/core/sync/ProfileSettingsSyncService.kt` | 3 | 0 |
| `app/src/main/java/com/nuvio/tv/data/local/PlayerSettingsDataStore.kt` | 38 | 1 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/CustomDefaultTrackNameProvider.kt` | 2 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/DebugAudioFormat.kt` | 104 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/ExperimentalEac3Preference.kt` | 18 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerDebugStatsOverlay.kt` | 16 | 2 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerMediaInfo.kt` | 3 | 1 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerRuntimeController.kt` | 1 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerRuntimeControllerAudioDelayRoutes.kt` | 5 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerRuntimeControllerInitialization.kt` | 46 | 25 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerRuntimeControllerLifecycle.kt` | 1 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerViewModel.kt` | 3 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/settings/PlaybackAudioSettings.kt` | 50 | 2 |
| `app/src/main/java/com/nuvio/tv/ui/screens/settings/PlaybackSettingsModel.kt` | 1 | 0 |
| `app/src/main/java/com/nuvio/tv/ui/screens/settings/PlaybackSettingsViewModel.kt` | 12 | 0 |
| `app/src/main/res/values-pt-rBR/strings.xml` | 10 | 0 |
| `app/src/main/res/values/strings.xml` | 10 | 0 |
| `app/src/test/java/com/nuvio/tv/core/player/dvmkv/Eac3SurroundTest.kt` | 76 | 0 |
| `app/src/test/java/com/nuvio/tv/core/player/dvmkv/TrueHdAtmosTest.java` | 63 | 0 |
| `app/src/test/java/com/nuvio/tv/data/local/PlayerSettingsTranscodingTest.kt` | 80 | 0 |
| `app/src/test/java/com/nuvio/tv/ui/screens/player/DebugAudioFormatTest.kt` | 95 | 0 |
| `docs/eac3-transcoding-verification.md` | 219 | 0 |
| `experiments/eac3-7.1/README.md` | 22 | 0 |
| `experiments/eac3-7.1/fix_base_mapping.py` | 29 | 0 |
| `experiments/eac3-7.1/fix_decoder_mapping.py` | 26 | 0 |
| `experiments/eac3-7.1/fix_surround_substream.py` | 98 | 0 |
| `experiments/eac3-7.1/upstream-21668.patch` | 1298 | 0 |
| `experiments/eac3-7.1/verify_channels.py` | 55 | 0 |
| `ffmpeg-decoder-downmix/src/main/java/androidx/media3/decoder/ffmpeg/EncodedEosDecoder.java` | 174 | 0 |
| `ffmpeg-decoder-downmix/src/main/java/androidx/media3/decoder/ffmpeg/FfmpegAudioDecoder.java` | 303 | 2 |
| `ffmpeg-decoder-downmix/src/main/java/androidx/media3/decoder/ffmpeg/FfmpegAudioRenderer.java` | 110 | 28 |
| `ffmpeg-decoder-downmix/src/main/jni/build_ffmpeg.sh` | 12 | 1 |
| `ffmpeg-decoder-downmix/src/main/jni/ffmpeg_jni.cc` | 726 | 52 |
| `libmpv-android` | 0 | 1 |

Este inventário inclui o gitlink removido de `libmpv-android`; essa remoção não faz parte da proposta funcional e deve ser excluída do PR upstream. A documentação de contribuição adicionada depois deste snapshot também não integra essas contagens.
