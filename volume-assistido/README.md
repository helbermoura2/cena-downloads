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

## Personalização avançada — código-fonte 0.3.0 (19/11/2026)

- **Tamanho dos botões + / −**: 38–92 dp, independente da escala geral (70–150%).
- **Distância entre os botões**: 28–116 dp, incluindo a área do percentual central.
- **Cinco estilos**: Minimal, Glass, Neon, Aurora e RGB ∞, com cores/acabamentos próprios.
- **Animação RGB contínua**: contorno colorido e botões atualizados a cada 100 ms, com ciclo ajustável entre 3 e 20 s; desliga com a tela apagada e pode ser desativada nas configurações.
- **Prévia HTML**: [preview.html](preview.html), que demonstra os novos ajustes sem controlar o áudio real.
- **Observação**: os ajustes são persistidos localmente; não exigem login nem serviço externo.
- O Android pode ajustar dimensões efetivas conforme densidade/escala da tela. Monitoramento de consumo de energia e acessibilidade ainda carecem de teste em aparelho real.
- **APK não compilado nem publicado**, pois não há ambiente Android SDK validado nesta sessão. Nenhum GitHub Actions foi solicitado.

## Temas adicionais — versão 0.4.0 (19/11/2026)

O NIVO agora oferece **12 designs nativos**:
- Originais: Minimal, Glass, Neon, Aurora, RGB ∞.
- Novos: **Liquid Glass**, **Titanium**, **Carbon**, **Synthwave**, **Pearl**, **Pixel** e **Candy**.

Os novos temas alteram não só a cor mas **forma, gradientes, contornos, superfícies e contraste dos dois botões + / −**, mantendo o percentual central, o arraste e a abertura do painel.

**Tecnologia:** desenhos feitos com APIs nativas Android (GradientDrawable), sem WebView ou bibliotecas externas. Liquid Glass é uma aparência de vidro translúcido, não um desfoque real da tela de outros aplicativos. RGB contínuo permanece restrito ao tema RGB ∞ para economizar recursos.

**Assinatura:** o APK v0.3.0 de teste foi assinado com a chave debug temporária do runner anterior. O APK v0.4.0 de teste, se compilado em outro runner, terá outra chave; nesse caso o Android não permite atualizar por cima. É necessário desinstalar a versão de teste anterior antes de instalar a nova, perdendo as configurações locais. Para atualização direta e preservação das preferências nas próximas versões, configurar uma assinatura persistente protegida (GitHub Secrets) antes de distribuir a versão estável.


## Atualização diretamente no aplicativo (preparada para a próxima versão)

Na área **Aplicativo > Atualizar aplicativo**, o NIVO poderá:
1. Consultar `update.json` do repositório público.
2. Pedir aprovação antes de baixar uma nova versão.
3. Baixar o APK internamente em cache privado, sem navegador ou gerenciador de arquivos.
4. Comparar o SHA-256 do arquivo ao valor declarado em `update.json`.
5. Conferir identificador do pacote, versão e **assinatura Android** contra o aplicativo instalado.
6. Solicitar, uma única vez, autorização do Android para "Instalar apps desconhecidos" por essa origem, se necessário.
7. Abrir o instalador Android; o usuário **sempre confirma** a instalação. Não há atualização silenciosa.

Arquivos: `UpdateManager.java`, `UpdateApkProvider.java`, `AndroidManifest.xml` e `MainActivity.java`.

**Importante:** esse recurso já está no código-fonte e passou pela compilação Android, mas NÃO está no APK 0.4.0 anteriormente publicado. Precisa ser entregue numa nova versão.

### Assinatura permanente: requisito para preservar os dados

Os APKs de teste 0.3.0 e 0.4.0 foram assinados por **chaves debug temporárias diferentes**. A chave privada da assinatura 0.4.0 não foi preservada para reutilização. O Android não aceita substituir esse APK por outro assinado por uma chave diferente.

Para começar a atualizar sem reinstalar:
- Criar e guardar uma **keystore de produção** de propriedade do usuário, com backup privado e seguro.
- Cadastrar quatro GitHub Actions Secrets em `helbermoura2/cena-downloads`: `NIVO_KEYSTORE_B64` (keystore binária codificada em base64), `NIVO_STORE_PASS`, `NIVO_KEY_ALIAS`, `NIVO_KEY_PASS`. **Nunca** publicar keystore ou senhas neste repositório público.
- Preparar uma versão com `versionCode` maior e executar manualmente `.github/workflows/nivo-stable-release.yml`. Ela compila, assina com a chave persistente, publica o APK e atualiza o manifesto com URL e SHA-256.
- **Uma última substituição manual da versão de teste 0.4.0 será necessária** ao mudar para a assinatura permanente. A partir dessa versão estável, atualizar por cima manterá as configurações, desde que a mesma keystore seja usada nos lançamentos seguintes.

A antiga compilação debug agora publica apenas um artefato de teste temporário, nunca um APK no Release público. `.github/workflows/nivo-verify-updater.yml` compila e valida o código sem publicar um novo APK.

O NIVO não pede acesso ao armazenamento externo e não instala aplicativos sem confirmação explícita do Android. Não use `REQUEST_INSTALL_PACKAGES` para arquivos de origem diferente da URL de Releases indicada no projeto.


## Alternativas gratuitas, sem GitHub Actions e sem computador (novembro de 2026)

### 1. GitHub Codespaces — opção de nuvem recomendada

Para contas pessoais GitHub Free, GitHub oferece franquia mensal de Codespaces (120 horas de núcleo, equivalentes a 60h em máquina de 2 núcleos, mais 15 GB-mês de armazenamento; uso extra pode exigir forma de pagamento). O repositório contém:
- [Configuração Codespaces](../.devcontainer/devcontainer.json)
- [Instalação automática do Android SDK e Gradle](../.devcontainer/setup-android.sh)
- [Script de compilação](build-codespaces.sh)

Procedimento pelo navegador do celular:
1. Abra a página deste repositório no GitHub e entre em **Code > Codespaces > Create codespace on main**, escolhendo 2 núcleos se disponível.
2. O devcontainer prepara automaticamente JDK 17, Android SDK 35, Build Tools 35 e Gradle 8.9. A primeira preparação baixa ferramentas e pode demorar; acompanhe no terminal.
3. Abra o terminal da interface web e execute: 
   \`bash volume-assistido/build-codespaces.sh\`
4. Com a compilação aprovada, no Explorador do VS Code web, localize \`volume-assistido/output/NIVO-Volume-0.5.0-teste.apk\` e use **Download**.
5. **Pare ou exclua** o Codespace ao terminar para evitar consumir a franquia gratuita.

**Nenhum GitHub Actions é disparado**. Este é um build em computador Linux temporário no Codespaces controlado pelo titular da conta.

**Assinatura:** o APK de teste tem uma chave debug da máquina; uma atualização sobre NIVO 0.4 de outra máquina não é permitida pelo Android. Para preservação de dados nas próximas versões, uma **keystore estável e privada** deve ser criada e guardada fora do repositório; use a opção de build release assinado com essa keystore. Não salve chaves e senhas no GitHub público.

### 2. Code on the Go — tudo no Android

O aplicativo gratuito e open-source **Code on the Go**, da App Dev for All, pode compilar aplicativos Kotlin/Java diretamente no celular sem PC. Site oficial: https://appdevforall.org/code-on-the-go/. A versão 26.47 foi publicada em 17/11/2026; a página informa suporte a Android Gradle Plugin de 7.3.0 a 9.3.1, portanto a versão AGP 8.7.3 deste projeto está dentro da faixa anunciada, mas o build NIVO ainda deve ser testado.

Abra/importa o subprojeto \`volume-assistido\` (não a raiz \`cena-downloads\`) no CoGo. Após compilar, use **Export** para salvar o APK, pois a versão 26.45+ armazena os resultados no espaço privado do aplicativo.

Estas opções são gratuitas dentro das respectivas condições; nenhuma substitui uma compilação efetiva que produza e verifique um APK antes de distribuí-lo.
