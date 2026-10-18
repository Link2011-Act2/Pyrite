#!/bin/sh
set -eu

# ANDROID_HOME is provided by JitPack. No developer-specific local.properties is needed.
sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$sdk_root" ]; then
    echo "ANDROID_HOME or ANDROID_SDK_ROOT must point to the Android SDK." >&2
    exit 1
fi
sdk_manager="$(command -v sdkmanager || true)"
if [ -z "$sdk_manager" ]; then
    for candidate in "$sdk_root/cmdline-tools/latest/bin/sdkmanager" "$sdk_root/tools/bin/sdkmanager"; do
        if [ -x "$candidate" ]; then
            sdk_manager="$candidate"
            break
        fi
    done
fi
if [ -z "$sdk_manager" ]; then
    echo "Android SDK command-line tools (sdkmanager) are required." >&2
    exit 1
fi
# Accept the Android SDK package licenses on the ephemeral builder.
yes | "$sdk_manager" --sdk_root="$sdk_root" --licenses >/dev/null
# Build Tools 36.0.0 is the existing AGP build's default; compileSdk remains 37.
"$sdk_manager" --sdk_root="$sdk_root" "platforms;android-37" "build-tools;36.0.0"
