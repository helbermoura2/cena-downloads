#!/usr/bin/env bash
# Build manual em Codespaces. NÃO usa GitHub Actions.
set -Eeuo pipefail
cd "$(dirname "$0")"
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
GRADLE="$HOME/gradle-8.9/bin/gradle"
if [ ! -x "$GRADLE" ]; then
  echo "Ambiente incompleto; execute bash .devcontainer/setup-android.sh na raiz do repositório." >&2
  exit 2
fi
"$GRADLE" --no-daemon :app:assembleDebug
APK="app/build/outputs/apk/debug/app-debug.apk"
test -s "$APK"
unzip -l "$APK" | grep -q 'classes.dex'
unzip -l "$APK" | grep -q 'AndroidManifest.xml'
VERSION=$(sed -nE "s/^[[:space:]]*versionName[[:space:]]+'([^']+)'.*/\1/p" app/build.gradle | head -n1)
mkdir -p output
cp "$APK" "output/NIVO-Volume-$VERSION-teste.apk"
(cd output && sha256sum "NIVO-Volume-$VERSION-teste.apk" > SHA256SUMS.txt)
echo ""
echo "APK PRONTO: volume-assistido/output/NIVO-Volume-$VERSION-teste.apk"
echo "No explorador de arquivos do Codespaces, escolha Download para salvar no celular."
echo "ATENÇÃO: APK debug de outro ambiente é assinado por chave diferente."
echo "Para atualizar sem desinstalar, use uma keystore privada permanente."
