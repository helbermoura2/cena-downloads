#!/usr/bin/env bash
# GitHub Codespaces: JDK 17 (devcontainer), Gradle 8.9 e Android SDK API 35.
# Não utiliza GitHub Actions nem dispara qualquer workflow.
set -Eeuo pipefail
SDK_ROOT="$HOME/android-sdk"
GRADLE_ROOT="$HOME/gradle-8.9"
mkdir -p "$SDK_ROOT/cmdline-tools"
if [ ! -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "[NIVO] Baixando ferramentas Android oficiais..."
  tmp="$(mktemp -d)"
  curl -fL --retry 3 --connect-timeout 30 -o "$tmp/tools.zip" \
    "https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp/extracted"
  mv "$tmp/extracted/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"
  rm -rf "$tmp"
fi
if [ ! -x "$GRADLE_ROOT/bin/gradle" ]; then
  echo "[NIVO] Baixando Gradle 8.9..."
  tmp="$(mktemp -d)"
  curl -fL --retry 3 --connect-timeout 30 -o "$tmp/gradle.zip" \
    "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
  unzip -q "$tmp/gradle.zip" -d "$HOME"
  rm -rf "$tmp"
fi
echo "[NIVO] Instalando plataforma Android 35..."
export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
yes | "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null || true
"$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" \
  "platforms;android-35" "build-tools;35.0.0" "platform-tools"
printf 'sdk.dir=%s\n' "$SDK_ROOT" > volume-assistido/local.properties
echo ""
echo "[NIVO] Ambiente pronto. Digite:"
echo "bash volume-assistido/build-codespaces.sh"
