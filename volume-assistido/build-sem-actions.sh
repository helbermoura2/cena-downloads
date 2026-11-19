#!/usr/bin/env bash
# NIVO Volume: APK externo com Java 17, Gradle 8.9, Android SDK 35
# NÃO executa GitHub Actions.
set -Eeuo pipefail
cd "$(dirname "$0")"
for app in java gradle apksigner zipalign sha256sum; do
  command -v "$app" >/dev/null || { echo "Falta: $app" >&2; exit 2; }
done
if [ -z "$ANDROID_HOME" ]; then echo 'Configure ANDROID_HOME' >&2; exit 2; fi
if [ -z "$NIVO_KEYSTORE_PATH" ]; then echo 'Configure NIVO_KEYSTORE_PATH' >&2; exit 2; fi
if [ -z "$NIVO_KEY_ALIAS" ]; then echo 'Configure NIVO_KEY_ALIAS' >&2; exit 2; fi
if [ -z "$NIVO_STORE_PASS" ]; then echo 'Configure NIVO_STORE_PASS' >&2; exit 2; fi
if [ -z "$NIVO_KEY_PASS" ]; then echo 'Configure NIVO_KEY_PASS' >&2; exit 2; fi
if [ ! -r "$NIVO_KEYSTORE_PATH" ]; then echo 'Chave de assinatura indisponível' >&2; exit 2; fi
gradle --no-daemon :app:assembleRelease
version=$(sed -nE "s/^[[:space:]]*versionName[[:space:]]+'([^']+)'.*/\1/p" app/build.gradle | head -n1)
code=$(sed -nE 's/^[[:space:]]*versionCode[[:space:]]+([0-9]+).*/\1/p' app/build.gradle | head -n1)
test -n "$version" && test -n "$code"
mkdir -p output
input="app/build/outputs/apk/release/app-release-unsigned.apk"
output="output/NIVO-Volume-$version.apk"
test -s "$input"
zipalign -p -f 4 "$input" output/aligned-unsigned.apk
apksigner sign --ks "$NIVO_KEYSTORE_PATH" --ks-key-alias "$NIVO_KEY_ALIAS" --ks-pass env:NIVO_STORE_PASS --key-pass env:NIVO_KEY_PASS --out "$output" output/aligned-unsigned.apk
apksigner verify --verbose "$output"
rm -f output/aligned-unsigned.apk
(cd output && sha256sum "NIVO-Volume-$version.apk" > SHA256SUMS.txt)
printf 'versionCode=%s\nversionName=%s\napk=%s\n' "$code" "$version" "$output" > output/BUILD-INFO.txt
echo "APK gerado: $output"
