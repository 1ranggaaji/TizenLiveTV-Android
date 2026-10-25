#!/usr/bin/env bash
set -e

# ==============================================================================
# Script Otomatis Build APK Samsung Tizen Live TV
# Menggunakan CDN Cloudflare & Auto-Resume untuk keandalan maksimal
# ==============================================================================

PROJECT_DIR="/Users/ranggaaji/Documents/TizenLiveTV"
TOOLS_DIR="/Users/ranggaaji/.tizen_build_tools"
mkdir -p "$TOOLS_DIR"

echo "=========================================================="
echo "🚀 Memulai Proses Pembuatan File APK Tizen Live TV..."
echo "=========================================================="

# 1. Unduh & Siapkan Zulu OpenJDK 17 (via Cloudflare CDN)
JDK_DIR="$TOOLS_DIR/zulu17"
JAVA_BIN="$JDK_DIR/zulu-17.jdk/Contents/Home/bin/java"

if [ ! -f "$JAVA_BIN" ]; then
    echo "⬇️  Mengunduh OpenJDK 17 via CDN cepat (Cloudflare)..."
    JDK_TAR="$TOOLS_DIR/zulu17.tar.gz"
    curl -L --http1.1 -C - --retry 10 --retry-delay 2 --retry-connrefused \
        -o "$JDK_TAR" \
        "https://cdn.azul.com/zulu/bin/zulu17.48.15-ca-jdk17.0.10-macosx_x64.tar.gz"
    
    echo "📦 Mengekstrak JDK 17..."
    rm -rf "$JDK_DIR"
    mkdir -p "$JDK_DIR"
    tar -xzf "$JDK_TAR" -C "$JDK_DIR" --strip-components=1
    rm -f "$JDK_TAR"
fi

export JAVA_HOME="$JDK_DIR/zulu-17.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
echo "✓ Java aktif: $($JAVA_HOME/bin/java -version 2>&1 | head -n 1)"

# 2. Siapkan direktori Android SDK dan Lisensi
SDK_DIR="$TOOLS_DIR/android-sdk"
mkdir -p "$SDK_DIR/licenses"

# Tulis hash lisensi Android SDK resmi agar Gradle dapat otomatis mengunduh platform 34
echo -e "\n24333f8a63061b914de0fa814170c4e85df97528\nd56f5187479451eabf01fb78ba6ed6ee32403e84\n8933bad161af4178b1185d1a37fbf41ea5269c55" > "$SDK_DIR/licenses/android-sdk-license"
echo -e "\n84831b9409646a20101311422345610584da4767" > "$SDK_DIR/licenses/android-sdk-preview-license"

export ANDROID_HOME="$SDK_DIR"
export ANDROID_SDK_ROOT="$SDK_DIR"
echo "sdk.dir=$SDK_DIR" > "$PROJECT_DIR/local.properties"
echo "✓ Android SDK siap di $SDK_DIR"

# 3. Kompilasi APK menggunakan Gradle
echo "🔨 Membangun APK (Gradle assembleDebug)..."
cd "$PROJECT_DIR"
chmod +x gradlew
./gradlew assembleDebug --no-daemon --stacktrace

OUTPUT_APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
FINAL_DOCS_APK="/Users/ranggaaji/Documents/TizenLiveTV.apk"
FINAL_LOCAL_APK="$PROJECT_DIR/TizenLiveTV.apk"

if [ -f "$OUTPUT_APK" ]; then
    cp "$OUTPUT_APK" "$FINAL_DOCS_APK"
    cp "$OUTPUT_APK" "$FINAL_LOCAL_APK"
    echo ""
    echo "=========================================================="
    echo "🎉 BERHASIL! File APK telah dibuat di:"
    echo "👉 $FINAL_DOCS_APK"
    echo "👉 $FINAL_LOCAL_APK"
    echo "=========================================================="
else
    echo "❌ Terjadi kendala saat proses kompilasi APK."
    exit 1
fi
