# Entrega: identificação TrueHD Atmos em Matroska

## Código e compilação

- Branch: `heliummarcos/eac3-transcoding-5.1-7.1`.
- Commit do código compilado: `b549315aae8d0764392ed1d6ad6462f0a74396f3`.
- [Execução GitHub Actions 42151624130](https://github.com/HeliumMarcos/NuvioTV/actions/runs/42151624130): concluída com sucesso.
- Job Android: 18 min 43 s; uma única execução de compilação foi acionada nesta rodada.
- Compilações do FFmpeg, JNI/Java e APK concluídas.
- `:app:testFullDebugUnitTest` concluído com os filtros `PlayerSettingsTranscodingTest`, `DebugAudioFormatTest`, `Eac3SurroundTest` e `TrueHdAtmosTest`.
- Antes do build Android, os quatro testes isolados JUnit do parser TrueHD passaram. A fase inicial do parser reproduziu a falha antes da implementação da leitura do cabeçalho.

Commits posteriores de documentação não alteram o código do APK e não disparam outra compilação.

## APK

- Arquivo: `app-full-armeabi-v7a-debug.apk`.
- Tamanho: 73.529.037 bytes.
- Pacote: `com.nuvio.tv.eac3.dev`.
- Nome no launcher: `NuvioTV E-AC-3 Dev`.
- Versão: `1.1.0-beta.10`, versionCode `1073`.
- ABI: `armeabi-v7a`.
- SHA-256 do APK: `56878929c3f20ecef59caa99c1a6b24dad3e60909b11b4812df517577da88645`.
- SHA-256 do certificado: `f03c16a4865e3b93f7c1faf9ee75f7885b972b6b65c10ddbce5db1a1d5ffea22`.
- Assinatura verificada com `apksigner`; pacote e ABI conferidos com `aapt`.
- [Artefato APK no GitHub](https://github.com/HeliumMarcos/NuvioTV/actions/runs/42151624130/artifacts/13473586787). O acesso pode exigir login e está sujeito à retenção de artefatos do Actions.

## Instalação e verificação no aparelho

A atualização foi aplicada na instalação experimental existente com `adb install -r`, retornando `Success`. Não foi feita desinstalação ou limpeza de dados. O pacote oficial não foi alterado. O Android passou a informar `lastUpdateTime=2026-11-06 00:03:25` para a experimental.

A versão e o versionCode permanecem os mesmos da base experimental; para distinguir esta entrega, use o commit/artefato/hash acima e o horário de atualização.

A confirmação visual do arquivo TrueHD Atmos relatado ainda depende do teste no player atualizado. O resultado esperado para a fonte é:

```text
Entrada  TrueHD · 7.1 (8 canais) · 48 kHz · Atmos
```

Quando ela for transcodificada para E-AC-3 convencional, a linha Saída continua sem o sufixo Atmos. Se a opção de fixar 5.1 estiver desligada e a negociação permitir 7.1, espera-se:

```text
Saída    DDP (E-AC-3) · 7.1 (8 canais) · 48 kHz
```

Isso identifica corretamente a presença de Atmos na entrada e não promete preservar seus objetos durante a conversão. A investigação dessa transcodificação permanece separada.

O [relatório completo](RELATORIO-PT-BR.md), o [histórico](HISTORICO.md) e o [guia de envio upstream](COMO-ENVIAR-PT-BR.md) acompanham esta entrega.
