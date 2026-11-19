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
