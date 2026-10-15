# BT Remote 📱📺

A lightweight Android app that turns your Android phone into a universal Bluetooth HID remote control (D-pad, navigation, and media keys) for Android TV, Google TV, Fire TV, Apple TV, smart TVs, projectors, and PCs — without needing any companion server software.

## Features

- **Standard Bluetooth HID**: Emulates a native Bluetooth keyboard and consumer media remote.
- **No Companion App Needed**: Pairs directly with TV, PC, or streaming stick via standard Bluetooth settings.
- **Controls Supported**:
  - D-Pad: Up, Down, Left, Right, Select/Enter
  - Navigation: Back, Home, Menu/Esc
  - Volume & Playback: Volume Up, Volume Down, Mute, Play/Pause, Power
- **Zero Latency**: Direct Bluetooth HID packet transmission.
- **Modern Android Support**: Compatible with Android 9.0 (API 28) and above (including Android 14/15 with `BLUETOOTH_CONNECT` runtime permissions).

## Compatibility Table

| Target Device | Phone Model | OS Version | Status | Notes |
|---|---|---|---|---|
| Android TV / Google TV | *e.g. Pixel / Samsung* | Android 14 |  Working | Full D-pad & media control |
| Amazon Fire TV Stick | *e.g. OnePlus* | Android 13 |  Working | Pair via Remotes & Bluetooth |
| Apple TV | *Tested devices* | tvOS |  Working | Keyboard & navigation |
| Windows PC / Mac | *Tested devices* | Win 11 / macOS |  Working | Recognized as Bluetooth keyboard |

## Getting Started

### 1. Build the APK via GitHub Actions
This repository includes a pre-configured GitHub Actions workflow ([`.github/workflows/build.yml`](.github/workflows/build.yml)) to build the debug APK automatically in the cloud.

1. Fork or push this repository to GitHub.
2. Go to the **Actions** tab.
3. Select **Build APK** and click **Run workflow** (or push any commit).
4. When finished, download the `BtRemote-debug-apk` artifact.

### 2. Installation & Pairing
1. Transfer `app-debug.apk` to your phone and install it (allow *Install unknown apps* if prompted).
2. On your TV or streaming device, go to **Settings > Remotes & Accessories > Add Bluetooth Accessory**.
3. Open **BT Remote** on your phone, grant Bluetooth permissions, and tap **Connect**.
4. Select your TV from the paired devices list and start controlling!

## Tech Stack
- **Language**: Java
- **API**: Android Bluetooth HID Device Profile (`BluetoothHidDevice`, API 28+)
- **Build System**: Gradle 8.9 / Android Gradle Plugin 8.5.2
