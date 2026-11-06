# Relatório técnico: transcodificação AC-3 / E-AC-3, 5.1 / 7.1 e identificação Atmos

Estado do código de áudio: `b549315aa`, em `heliummarcos/eac3-transcoding-5.1-7.1`.
Este relatório descreve a implementação experimental e a evidência disponível. Ele não declara compatibilidade universal ou certificação Dolby.

## 1. Relação entre as branches

`feature/eac3-transcoding` termina em `e2840f804`. Esse commit é ancestral da branch `heliummarcos/eac3-transcoding-5.1-7.1`: o trabalho da primeira está integralmente incluído na segunda.

Na comparação realizada com `NuvioMedia/NuvioTV:dev` em `0e320e3c397c51d26923d6fcc7906a4b886e82c5`, o ancestral comum é `8f3112655d3d128c4888b9c69e37d44f2d001fb2`. Há 143 commits exclusivos na etapa `feature/eac3-transcoding` e mais 18 até a correção TrueHD Atmos. Esses números incluem documentação, correções intermediárias e CI; não representam 161 funcionalidades.

A lista completa dos commits e arquivos está em [HISTORICO.md](HISTORICO.md). O relatório descreve o comportamento final, identificando também soluções intermediárias que foram substituídas.

`heliummarcos/truehd-eac3-atmos` foi criada separadamente para uma investigação futura de transcodificação com Atmos. Ela não contém uma solução de preservação Atmos e não deve ser apresentada como funcionalidade pronta.

## 2. Resultado para o usuário

O decoder FFmpeg local pode produzir AC-3 ou E-AC-3 comprimido para o AudioSink do Media3. A escolha é opcional e os formatos de entrada são selecionados separadamente para cada destino.

| Destino selecionado | Entradas selecionáveis | Regra |
| --- | --- | --- |
| E-AC-3 / DDP | TrueHD, DTS, DTS-HD | Somente as entradas marcadas entram nessa transcodificação |
| AC-3 / DD | TrueHD, DTS, DTS-HD, DDP | Somente as entradas marcadas entram nessa transcodificação |

AAC não foi incluído na seleção. DDP não é convertido novamente para DDP por essa regra. Uma entrada desmarcada segue a negociação normal do reprodutor; desmarcar não garante passagem direta quando o dispositivo não suporta o formato.

| Entrada | E-AC-3 padrão | E-AC-3 com “fixar em 5.1” | AC-3 multicanal |
| --- | --- | --- | --- |
| 5.1 | 5.1 | 5.1 | 5.1 |
| 7.1 | 7.1 | 5.1 | 5.1 |

A caixa “fixar em 5.1” fica **desativada por padrão**. A regra não faz upmix de 5.1 para 7.1. Limites e downmix solicitados em outras configurações também devem ser considerados na negociação.

As estatísticas agora distinguem entrada, saída inicializada no AudioTrack e rota Android. Exemplo de transcodificação de uma fonte Atmos:

```text
Entrada  TrueHD · 7.1 (8 canais) · 48 kHz · Atmos
Saída    DDP (E-AC-3) · 7.1 (8 canais) · 48 kHz
Rota     HDMI eARC / <dispositivo informado pelo Android>
```

O exemplo indica Atmos na fonte. A transcodificação atual produz E-AC-3 convencional e não transporta os objetos Atmos da fonte.

## 3. Trabalho em feature/eac3-transcoding

### 3.1. Modos de saída e integração Java / JNI / FFmpeg

- Compilação dos encoders `ac3` e `eac3` no FFmpeg usado pelo módulo local.
- Seleção explícita de PCM, AC-3 ou E-AC-3; substituição do parâmetro booleano antigo por um modo de saída na ponte Java/JNI.
- Propagação da escolha do renderer até o decoder nativo, inclusive após reset/reinicialização de TrueHD.
- Negociação do formato comprimido com o AudioSink e publicação de MIME, frequência e canais de saída.
- Conversão da saída transcodificada para 48 kHz, acompanhada de negociação coerente no Media3.
- Atualização do formato Java depois de reset e quando o primeiro pacote codificado aparece somente durante a drenagem final.
- Evita recodificação desnecessária E-AC-3 → E-AC-3.
- Uso do decoder local controlado pela configuração de build. O AAR precompilado padrão não adquire os novos métodos JNI somente porque o código-fonte foi alterado.

Arquivos centrais: `FfmpegAudioRenderer.java`, `FfmpegAudioDecoder.java`, `ffmpeg_jni.cc`, `build_ffmpeg.sh`, configuração Gradle do app e inicialização do player.

### 3.2. Integridade de buffers, memória e FIFO

- Validação das quantidades de amostras escritas/lidas no FIFO.
- Validação de dimensões, número de canais, tamanho do frame e alocações de conversão.
- Verificação de buffers JNI diretos e suas capacidades antes do uso.
- Verificação de leituras de extradata e alocação do nome do codec pela JNI.
- Padding exigido pelo FFmpeg ao copiar extradata.
- Proteções contra overflow ao calcular/grow buffers e mapear timestamps.
- Liberação e limpeza de ponteiros após inicialização parcial malsucedida.
- Garantia de que frames do encoder estejam graváveis antes de reutilizá-los.
- Conversões que consomem entrada e geram zero amostras não são tratadas como perda de frame; não se faz escrita vazia no FIFO.
- Frames completos e o frame final são consultados com `av_audio_fifo_peek`; só são removidos após o encoder aceitá-los. Rejeição/backpressure não pode consumir silenciosamente as amostras.

### 3.3. Pacotes comprimidos e timestamps

- Separação de access units codificadas em uma fila de pacotes; a saída Java entrega um pacote por buffer de saída.
- Interface JNI para retirar um pacote e consultar a quantidade pendente.
- Timestamps do encoder baseados em amostras, com origem na linha do tempo de entrada do Media3.
- PTS do pacote efetivamente retirado da fila determina o timestamp entregue ao player; preservação de timestamp desconhecido e alternativa DTS quando disponível.
- Proteções contra overflow no mapeamento temporal.
- Agendamento de drenagem da fila durante reprodução normal, necessário quando a cadência dos pacotes TrueHD difere da dos frames AC-3/E-AC-3.
- Trabalho sintético executado na thread do decoder para serializar acesso ao estado nativo, sem acesso concorrente indevido pelo renderer.

### 3.4. Fim do fluxo: decoder → resampler → FIFO → encoder → pacotes

- Drenagem explícita dos frames atrasados do decoder FFmpeg.
- Confirmação de EOF por retorno real da API; uma chamada sem bytes não basta para declarar conclusão.
- Drenagem das amostras atrasadas do resampler antes de finalizar o encoder.
- Padding com silêncio apenas no frame parcial final, com verificação do resultado.
- Estado separado para frame final aceito, envio de EOF e EOF confirmado do encoder; evita reenviar o frame final durante tentativas seguintes.
- Limites/backpressure na fila durante drenagem para evitar saturação.
- `EncodedEosDecoder` coordena esses passos antes de entregar o EOS definitivo ao Media3.
- Reagendamento quando um passo de drenagem não gera saída; integração adaptada à interface real do Media3 1.8.0.
- Limpeza de tokens, fila, snapshots e conclusão terminal após seek/flush/reset, evitando levar pacotes da posição anterior para a nova.
- Rejeição de nova entrada comprimida depois do pedido de EOS até ocorrer reset.

Essas proteções estão implementadas. Elas não equivalem a uma campanha completa de testes de stress. O caminho normal ainda trata uma falha de `avcodec_send_frame`, inclusive EAGAIN, como erro preservando as amostras; uma recuperação transparente desse caso não está demonstrada. Testes com injeção de backpressure, EOS curto, seeks repetidos e reprodução longa permanecem relevantes para aprovação upstream.

### 3.5. Configurações, diagnóstico e identidade experimental

- Seleção de codecs de entrada armazenada em máscaras independentes para AC-3 e E-AC-3. Alternar destino não apaga a seleção do outro.
- TrueHD, DTS e DTS-HD são opções independentes; DDP é permitido somente para destino AC-3.
- Integração com DataStore, ViewModels, preferências de reprodução e sincronização de configurações de perfil.
- Estatísticas com formato de entrada, formato do AudioTrack inicializado e dispositivo efetivamente roteado.
- Leitura associada ao AudioTrack vivo; referências liberadas retornam indisponível em vez de mostrar informação antiga.
- Pacote experimental final separado (`com.nuvio.tv.eac3.dev`) e nome no launcher distinto do app oficial, inclusive nos recursos de depuração.
- Assinatura experimental estável, segredo de CI e validação do certificado antes de publicar o APK, permitindo atualização sobre a instalação experimental compatível sem apagar seus dados.
- Pipelines de compilação JNI/Java, decoder AAR e APK completo; suporte a compilações isoladas por ABI e correções de integração com SDK/NDK/Media3.

A coexistência é garantida em relação ao pacote oficial. Instalações experimentais históricas com outro pacote/certificado podem exigir migração própria; não se deve prometer atualização automática entre identidades incompatíveis.

## 4. Trabalho adicional em heliummarcos/eac3-transcoding-5.1-7.1

### 4.1. Encoder E-AC-3 7.1 e preservação dos surrounds

O encoder E-AC-3 anterior limitava a saída a 5.1. Foi integrada uma candidata externa para substreams dependentes, fixando a revisão do FFmpeg e aplicando correções locais.

- Base FFmpeg: `c352a9ab022b945307f1ad5d35575f695a13c2ca`.
- Patch candidato: FFmpeg PR 21668, creditado a Nomis101/ShortKatz; cópia e origem documentadas em `experiments/eac3-7.1`.
- Correção do mapeamento entre os canais da entrada 7.1 e o substream independente de base.
- Correção do substream dependente para representar os quatro surrounds, preservando laterais e traseiros.
- Ajuste da alocação de bits/SNR para evitar degradação indevida do sinal surround.
- Montagem da base compatível sem sobrescrever os planos PCM originais usados na extensão.
- Correção do decoder FFmpeg ao combinar layouts independente/dependente: remapeamento pela identidade dos canais, preservando a diferença entre surround lateral e traseiro.
- Saída padrão preserva 5.1 ou 7.1 conforme a entrada; bitrate configurado em 640 kbit/s para seis canais e 1.024 kbit/s para oito canais.

O problema observado era concreto: o teste TrueHD 7.1 deixava os dois traseiros mudos. Após as correções o usuário confirmou som em todas as caixas no mesmo teste pelo Nuvio experimental. Um teste DDP 7.1 nativo fora do Nuvio havia confirmado previamente que a rota/equipamento reproduziam esses traseiros.

Esse é um encoder experimental com alterações fora do FFmpeg de distribuição padrão. Aceitação upstream deve considerar manutenção, atribuição/licença do patch e validação em mais receptores.

### 4.2. Capacidade HDMI, seleção de transcodificação e fallback

- Correções para respeitar as máscaras de entrada na negociação e inicialização do decoder.
- Remoção da declaração artificial de codecs HDMI não reportados pela rota e da capacidade genérica de oito canais.
- Transcodificação forçada desativa tunneling nas condições que impediam o fluxo de funcionar, respeitando a prioridade de decoder configurada.
- Ajustes para que um fallback PCM/codec não invalide silenciosamente a intenção da seleção forçada.
- A preferência de fixar E-AC-3 em 5.1 reduz a entrada 7.1 para 5.1 somente quando ativada; não aumenta canais de fontes menores e inicia desligada.

### 4.3. Leitura real dos canais E-AC-3 em Matroska

- Análise limitada dos blocos de áudio antes de publicar os formatos, com fallback durante leitura normal.
- Reconhecimento dos canais adicionais em substreams dependentes, corrigindo casos de arquivo E-AC-3 7.1 declarado como 5.1 no container/player.
- Aceitação dos IDs de bitstream E-AC-3 válidos, evitando rejeitar quadros legítimos por uma restrição excessiva.
- Correção das constantes de lacing Matroska utilizadas na análise.
- A análise antecipada preserva a posição de leitura; falhas mantêm o formato provisório, sem bloquear `endTracks()` esperando dados de áudio.

### 4.4. Estatísticas: base Atmos e limite do AudioTrack

Foi corrigido o caso em que a entrada DDP Atmos 5.1 aparecia com saída 7.1 somente porque o AudioTrack tinha sido aberto com a máscara máxima de canais da rota. Essa máscara não comprova upmix ou oito canais codificados em JOC.

Para JOC, os canais exibidos vêm da base comprimida identificada da fonte. O texto mostra `5.1 (6 canais) · Atmos` quando essa é a base. Não se inventa uma disposição de altura `7.1.4` a partir da presença de Atmos. Quando Atmos não foi identificado, o texto simplesmente omite esse sufixo.

As estatísticas observam a fronteira app → Android. Elas não medem diretamente os canais físicos recebidos/processados pela soundbar depois de eventuais conversões do firmware da TV.

### 4.5. Atmos E-AC-3 JOC em MKV

- Um MKV real possuía sinalização JOC válida, mas aparecia somente como E-AC-3. Um MP4 equivalente já era reconhecido por outro caminho do extractor.
- `Eac3Surround.refineFormat()` consulta a estrutura do quadro com o parser AC-3/E-AC-3 do Media3.
- Quando JOC é confirmado, publica `audio/eac3-joc`, preservando os canais da base correta.
- Integração no scan antecipado e no fallback por amostra, com suporte à leitura da amostra descomprimida no caminho zlib.
- Rejeição segura de quadros inválidos/truncados; condições protegidas para conteúdo criptografado/header stripping.
- Usuário confirmou a identificação correta no player e nas estatísticas após atualização anterior.

### 4.6. Atmos TrueHD em MKV — correção atual

O arquivo relatado é identificado pelo FFprobe como `Dolby TrueHD + Dolby Atmos`, 48 kHz, oito canais, layout 7.1. O título interno é apenas `Surround 7.1`; por isso depender do nome da faixa não resolve o problema.

- Leitura do major sync TrueHD `f8 72 6f ba`, com comprimento variável e verificação do checksum.
- Mesma condição de identificação usada no decoder FFmpeg: quatro substreams e bit de apresentação espacial no `substream_info`.
- Varredura limitada que tolera quadros minor sync antes de encontrar o cabeçalho completo; ausência momentânea de major sync não é tratada como prova de ausência de Atmos.
- Marker `TrueHdAtmos` em `Format.metadata` preserva MIME TrueHD, canais, sample rate e metadados já existentes; não cria um codec Android fictício.
- Uso da confirmação no nome da faixa, no perfil de áudio do player e nas estatísticas de entrada.
- Atmos na estatística de saída somente se a saída real for TrueHD com essa fonte confirmada, ou E-AC-3 JOC. Uma saída E-AC-3 comum transcodificada não herda o marker.
- Testes com cabeçalhos reais Atmos e sem Atmos, offset, truncamento, corrupção/checksum, falso MLP e preservação dos metadados.

### 4.7. CI, atualizações e controle de custo

- Actions atualizadas para versões com runtime Node.js 24, corrigindo os avisos de Node 20.
- Runner fixado em `ubuntu-26.04`, em vez de acompanhar mudanças futuras de `ubuntu-latest`.
- Checkout sem submodules para evitar falha de inicialização de submódulo no pipeline experimental.
- Fonte FFmpeg fixada e aplicação reprodutível dos patches antes da compilação.
- Separação/execução de testes de configurações, apresentação de áudio e parsers no workflow.
- Conferência de pacote, ABI, assinatura e ausência de `testOnly` antes de entregar o APK.
- Remoção dos gatilhos `push` dos dois workflows experimentais; eles executam somente com `workflow_dispatch`. Um push de correção ou documentação não dispara nova compilação.

Há também uma remoção de gitlink `libmpv-android` no histórico de manutenção do fork. Ela não é necessária à proposta de transcodificação e deve ser excluída de um PR de áudio para upstream. Da mesma forma, identidade experimental, assinatura privada e política pessoal de builds não devem ser transplantadas automaticamente para o app oficial.

## 5. Evidências e limites

O equipamento descrito pelo usuário em seu [comentário anterior na issue #2398](https://github.com/NuvioMedia/NuvioTV/issues/2398#issuecomment-6253587332) é uma TCL 75C7K com soundbar LG S95TR via HDMI eARC. Na consulta desta entrega, o Android informou modelo genérico `Smart TV Pro` e versão 14. Esses dados descrevem uma configuração de teste, não uma matriz de aparelhos homologados.

| Verificação | Estado/evidência |
| --- | --- |
| Relação de ancestralidade das branches | Confirmada por Git |
| FFprobe do MKV TrueHD relatado | Perfil TrueHD + Atmos, 8 canais, 48 kHz |
| Parser TrueHD Atmos isolado | 4 testes JUnit passaram antes da compilação Android atual |
| DDP 7.1 nativo fora do Nuvio | Usuário confirmou os dois surrounds traseiros |
| TrueHD 7.1 → DDP pelo Nuvio após correção | Usuário confirmou som em todas as caixas |
| Canais, passagem direta e demais correções anteriores | Usuário informou comportamento correto após os testes |
| MKV E-AC-3 JOC | Usuário confirmou Atmos correto no player e nas estatísticas |
| Build Android anterior com JOC corrigido | [42147132779](https://github.com/HeliumMarcos/NuvioTV/actions/runs/42147132779), concluído e instalado |
| Build Android da correção TrueHD | [42151624130](https://github.com/HeliumMarcos/NuvioTV/actions/runs/42151624130), concluído com sucesso; APK e testes de configurações, estatísticas e parsers E-AC-3/TrueHD passaram; [dados da entrega e instalação](ENTREGA-TRUEHD-ATMOS.md) |
| Reconhecimento TrueHD Atmos na TV com APK atual | Depende de validação após atualização; o teste isolado não substitui esse teste |
| Preservação TrueHD Atmos → E-AC-3 Atmos | Não implementada; pesquisa futura em branch separada |
| Todos os ABIs e receptores | Não validados; APK desta etapa é ARMv7 |
| Stress completo de seek/EOS/backpressure e sincronismo longo | Não demonstrado por esta rodada |

Os arquivos `docs/eac3-transcoding-verification.md` e `experiments/eac3-7.1/README.md` registram estágios anteriores e hipóteses daquele momento. Afirmações antigas de “ainda não compilado” ou “fora do app” não descrevem o estado final aqui consolidado; não devem ser copiadas isoladamente para a proposta.

## 6. Como dividir para avaliação upstream

1. **Bugs de identificação:** E-AC-3 JOC em MKV, TrueHD Atmos em MKV e correção da contagem aparente de JOC. Preferir issues e PRs pequenos por problema, com provas antes/depois.
2. **Transcodificação opcional AC-3 / E-AC-3:** propor a funcionalidade, regras de entrada, defaults e integração decoder/renderer. Aguardar aprovação explícita dos mantenedores antes de apresentar PR amplo.
3. **E-AC-3 7.1 experimental:** tratar separadamente a dependência do patch FFmpeg, as correções de canais e a campanha de compatibilidade.
4. **CI e identidade do fork:** manter fora dos PRs de produto, salvo se os mantenedores solicitarem adaptação específica.

Não enviar as duas branches como dois PRs integrais: a segunda já contém a primeira. Não abrir um PR com todos os commits de desenvolvimento apenas por eles estarem preservados no histórico.

Veja [COMO-ENVIAR-PT-BR.md](COMO-ENVIAR-PT-BR.md) para o procedimento e [PROPOSTA-UPSTREAM-EN.md](PROPOSTA-UPSTREAM-EN.md) para o texto inicial em inglês.
