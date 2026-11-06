# Como apresentar a contribuição ao NuvioTV

## 1. Regra do projeto original

Consulte a [política CONTRIBUTING.md](https://github.com/NuvioMedia/NuvioTV/blob/dev/CONTRIBUTING.md). Ela exige uma feature request com aprovação explícita de mantenedor para funcionalidades maiores, mudanças de arquitetura ou dependências. Correções de bugs precisam de issue reproduzível vinculada. Mudanças visíveis de UI precisam de imagem/vídeo antes e depois.

Essa aprovação é do projeto original. Seu fork pode conservar os experimentos normalmente. Os textos desta pasta foram preparados para você revisar e enviar; nenhuma issue ou PR foi publicada automaticamente.

## 2. Primeiro envio recomendado: atualizar a issue existente

Já existe a [issue #2398 — Audio transcoding selection](https://github.com/NuvioMedia/NuvioTV/issues/2398). Você publicou nela uma proposta em 18/10/2026. Os comentários consultados não contêm aprovação explícita de mantenedor para integrar este protótipo.

1. Entre na conta `HeliumMarcos` e abra a issue #2398.
2. Copie o texto de [COMENTARIO-ISSUE-2398-EN.md](COMENTARIO-ISSUE-2398-EN.md) para o campo de comentário.
3. Revise os resultados e links e clique em **Comment**.
4. Aguarde uma resposta dos mantenedores sobre interesse, aprovação e divisão do escopo.

O comentário apresenta o progresso em relação ao seu pedido anterior e pergunta como os mantenedores preferem avaliar a contribuição. Evita abrir outra issue com a mesma proposta geral. Também existe a [#2610](https://github.com/NuvioMedia/NuvioTV/issues/2610), sobre colocar o controle de transcodificação dentro do player; ela é uma proposta de interface distinta, não um recurso implementado por este trabalho.

### Se pedirem uma feature request separada para E-AC-3/7.1

1. Entre na conta `HeliumMarcos` no GitHub.
2. Abra [New issue](https://github.com/NuvioMedia/NuvioTV/issues/new/choose) no repositório original.
3. Escolha **Feature request** e a área **Audio**.
4. Use o título e os campos do arquivo [PROPOSTA-UPSTREAM-EN.md](PROPOSTA-UPSTREAM-EN.md).
5. No campo de contribuição, selecione a opção correspondente a “Yes, but I understand implementation still needs maintainer approval”.
6. Revise o texto e clique em **Submit new issue**.
7. Vincule a #2398 e aguarde resposta explícita dos mantenedores sobre interesse e escopo. Uma issue aberta ou com label `enhancement` não equivale a aprovação.

O texto informa honestamente que já existe um protótipo no fork, que a reprodução 7.1 foi confirmada em um equipamento e que Atmos na transcodificação ainda não está implementado.

## 3. Bugs de identificação podem ser relatados separadamente

Para o problema TrueHD Atmos, reproduza também na versão oficial atual se possível e registre:

- Versão/commit do Nuvio oficial e método de instalação.
- Modelo da TV, versão do Android e rota de áudio usada.
- Passos exatos: abrir um MKV com TrueHD Atmos confirmado, selecionar a faixa e consultar player/estatísticas.
- Resultado esperado: indicação Atmos quando os dados do bitstream a confirmam.
- Resultado observado: TrueHD 7.1 sem identificação Atmos.
- Perfil FFprobe/MediaInfo e imagens antes/depois, com dados pessoais removidos.
- Uma amostra curta que você possa legalmente compartilhar, preferencialmente um teste livre. Não publique o filme inteiro, links privados de reprodução, IP da sua rede ou logs com tokens.

Escolha **Bug report** em [New issue](https://github.com/NuvioMedia/NuvioTV/issues/new/choose). Abra uma issue por problema. Ao vincular uma correção do fork, explique quais arquivos demonstram a solução e o que foi testado.

## 4. Depois da aprovação: preparar um PR focado

O destino padrão consultado no original é **`dev`**. O fork é `HeliumMarcos/NuvioTV` e a referência completa do trabalho é `heliummarcos/eac3-transcoding-5.1-7.1`.

Antes do PR, prepare uma branch de contribuição a partir do `dev` atualizado, contendo somente o escopo aprovado. Como os commits de prototipagem se sobrepõem, não aplique indiscriminadamente todos por cherry-pick. Podemos selecionar os arquivos/commits pertinentes, adaptar ao upstream atual e validar o resultado quando houver definição dos mantenedores.

Não levar automaticamente:

- Pacote/nome experimental do fork.
- Configuração de assinatura e certificado particular.
- Remoção do gitlink `libmpv-android`.
- Workflows pessoais ou alterações de custo de CI não solicitadas.
- Pesquisa de transcodificação Atmos ainda não implementada.
- Vários bugs ou funcionalidades não relacionados no mesmo PR.

Quando a branch focada estiver pronta e os testes concluídos:

1. Abra **Pull requests → New pull request** no original.
2. Clique em **compare across forks**.
3. Selecione **base repository: `NuvioMedia/NuvioTV`**, **base: `dev`**.
4. Selecione **head repository: `HeliumMarcos/NuvioTV`** e a branch focada que terá sido preparada para aquela contribuição.
5. Confira **Files changed** para garantir que só o problema aprovado aparece.
6. Preencha integralmente o [template do PR](https://github.com/NuvioMedia/NuvioTV/blob/dev/.github/PULL_REQUEST_TEMPLATE.md): resumo, motivo, issue/aprovação, reprodução, impacto, limites, testes, imagens e breaking changes.
7. Vincule a issue com `Fixes #NUMERO` para um bug resolvido, ou `Refs #NUMERO` para a proposta aprovada quando não for adequado fechá-la automaticamente.
8. Marque somente as opções que sejam verdadeiras. Não declare aprovação ou teste em hardware que não aconteceu.
9. Use **Create draft pull request** se ainda faltarem verificações combinadas com os mantenedores; caso esteja revisável, use **Create pull request**.

Não abra um segundo PR integral de `feature/eac3-transcoding`: a branch 5.1/7.1 já inclui seu conteúdo. A preservação das duas é útil como histórico e comparação, não como duas contribuições independentes.

## 5. O que anexar e como acompanhar

Use o relatório técnico e a branch pública como referência. O relatório é extenso; no primeiro contato, envie a proposta curta em inglês e ofereça o detalhamento como contexto. Depois, responda às perguntas dos mantenedores e adapte a contribuição ao escopo aceito.

Não é necessário publicar uma release para abrir uma issue ou PR. O APK de CI ajuda a reproduzir, mas o código-fonte, os testes e a explicação do comportamento são a parte revisável da contribuição. Artefatos de Actions têm retenção limitada; preserve uma cópia local da entrega.

As compilações do fork continuam manuais: nenhuma dessas instruções muda o combinado de compilar somente quando você solicitar ou aprovar.
