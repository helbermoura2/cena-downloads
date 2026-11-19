# NIVO Volume — controle flutuante Android

Aplicativo Android **nativo**, independente, para controlar volume de mídia, toque, notificações e alarmes com um painel flutuante arrastável. Projeto inicial v0.1.0 — código-fonte, **não é um APK compilado**.

## Design
- OLED preto, cartões com vidro escuro e brilho sutil.
- Três perfis: **Minimal**, **Glass** e **Neon**.
- Acentos ciano, azul, violeta, laranja e verde.
- Expansão/recolhimento do controle, com animações nativas.
- Inspiração de art direction: https://www.tasteskill.dev
- Inspiração de motion: https://transitions.dev
- Inspiração de microinterações: https://reactbits.dev
- Os componentes web NÃO foram copiados: este projeto é Java/Android nativo.

## Recursos na versão inicial
- Overlay via permissão \`SYSTEM_ALERT_WINDOW\` (não usa Accessibility).
- Serviço em primeiro plano com notificação persistente e botão **Parar**.
- Painel arrastável, volume + / - e slider.
- Seleção de mídia/toque/notificações/alarme (alguns canais podem estar vinculados no sistema Samsung).
- Estilos, cores, opacidade, tamanho e recolhimento automático personalizáveis.
- Inicialização no boot somente quando o usuário optar, sujeita às restrições do fabricante/Android.
- Atualização manual nas configurações consultando \`update.json\`; **a instalação de uma nova versão exige confirmação do Android**. Não instala APKs silenciosamente.

## Como compilar, sem GitHub Actions
Abra a pasta \`volume-assistido\` no Android Studio (JDK 17 / Android SDK 35 / Gradle 8.9 ou compatível). Execute \`gradle :app:assembleDebug\` com o Android SDK instalado. O APK debug será criado em \`app/build/outputs/apk/debug/app-debug.apk\`. Para distribuição pública, assine a versão release com uma chave própria, guarde a chave e publique o APK em \`releases/\`. Atualize \`update.json\` com \`versionCode\`, \`versionName\` e a URL HTTPS pública do APK.

**Nota:** Esta entrega não afirma estar compilada ou testada em dispositivo real. O APK e uma assinatura de produção ainda precisam ser gerados em ambiente com Android SDK.

## Segurança e privacidade
O aplicativo não lê conteúdo da tela e não solicita permissões de acessibilidade. O acesso à sobreposição é solicitado de forma explícita. Não coleta dados pessoais. O verificador de atualização só acessa o JSON público do GitHub.

## Estrutura
\`app/src/main/java/br/com/nivo/volume/\` contém Activity, serviço flutuante e receiver de boot.

## Revisão do projeto (19/11/2026)
- Código publicado em pasta isolada; `cena-build-public.yml` permanece intocado.
- Corrigido: após tentativa de volume bloqueada, os valores do painel passam a refletir o volume **real** retornado pelo sistema.
- Corrigido: parada por notificação desliga a preferência; encerramento inesperado do processo não apaga a opção de início automático.
- Corrigido: posição do overlay reconsiderada ao alterar orientação ou tamanho.
- Corrigido: verificador de atualização compara a versão instalada, não um número fixo.
- Prévia web visualmente validada em viewport móvel (412px) com testes de abrir, fechar e ajustar volume. A prévia **não controla o volume do celular**.
- **Pendente:** compilação real com Android SDK, instalação e teste físico em Galaxy S25 Ultra/Android 15 ou superior. Este ambiente não tem Android SDK nem conectividade para baixá-lo.
- **Pendente:** geração e assinatura do APK. Não há APK publicado ainda.
- Futuros incrementos possíveis: perfis por aplicativo, ícones vetoriais mais elaborados, feedback háptico e revisão de acessibilidade.

## Controle rápido (+ / −) — conceito 0.2
- Os botões **+ e − aparecem diretamente na tela** e ajustam imediatamente o tipo de áudio selecionado (mídia por padrão).
- O percentual entre os botões mostra a leitura retornada pelo sistema.
- Toque no percentual para abrir controles completos; arraste o percentual para mudar a posição da coluna.
- Não é preciso abrir o painel para aumentar ou diminuir o som.
- Prévia interativa para celular: [preview.html](preview.html). Esta prévia é HTML, não um APK e não altera o volume real.
