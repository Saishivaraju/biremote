# BT Remote 📱📺

A lightweight, serverless Android app that turns your Android phone into a universal **3-in-1 Bluetooth HID Remote, Touchpad Mouse, and Keyboard** for Android TV, Google TV, Fire TV, Apple TV, Smart TVs, Projectors, and PCs/Macs — without requiring any companion software or server.

## Features

- **📺 TV / Media Remote**:
  - D-Pad: Up, Down, Left, Right, Select/Enter
  - Navigation: Back, Home, Menu/Esc
  - Media: Volume Up, Volume Down, Mute, Play/Pause, Power
- **🖱️ Touchscreen Trackpad (Mouse)**:
  - Swipe surface for smooth mouse cursor movement
  - Tap-to-click (Left Click)
  - Dedicated Left Click and Right Click buttons
  - Scroll Up and Scroll Down controls
- **⌨️ Keyboard & Text Typing**:
  - Input field to type or paste search queries, passwords, and URLs from your phone keyboard (with full voice typing / Gboard support!)
  - One-tap "Send Text to TV" with smooth keystroke streaming
  - Quick action keys: Enter, Backspace, Space, Tab, Delete, Esc
- **Standard Bluetooth HID**: Emulates native USB HID keyboard, mouse, and consumer devices.
- **Zero Latency**: Direct Bluetooth HID packet transmission.
- **Modern Android Support**: Full compatibility with Android 9.0 (API 28) through Android 14/15 (with `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`, and `BLUETOOTH_SCAN` runtime permissions).

## Compatibility Table

| Target Device | Phone Model | OS Version | Status | Notes |
|---|---|---|---|---|
| Android TV / Google TV | *e.g. Pixel / Samsung / Xiaomi* | Android 14 |  Working | D-pad, media, mouse, keyboard |
| Amazon Fire TV Stick | *e.g. OnePlus* | Android 13 |  Working | Pair via Remotes & Bluetooth |
| Apple TV | *Tested devices* | tvOS |  Working | Keyboard & navigation |
| Windows PC / Mac | *Tested devices* | Win 11 / macOS |  Working | Recognized as Bluetooth keyboard/mouse |

## Getting Started

### 1. Build the APK via GitHub Actions
This repository includes a pre-configured GitHub Actions workflow ([`.github/workflows/build.yml`](.github/workflows/build.yml)) to build the debug APK automatically in the cloud.

1. Go to the **Actions** tab on GitHub.
2. Select **Build APK** and view the latest build.
3. Download the `BtRemote-debug-apk` artifact from the bottom of the completed run.

### 2. Installation & Pairing
1. Transfer `app-debug.apk` to your phone and install it (allow *Install unknown apps* if prompted).
2. On your TV or streaming device, go to **Settings > Remotes & Accessories > Add Bluetooth Accessory**.
3. Open **BT Remote** on your phone, grant Bluetooth permissions, and tap **Connect**.
4. Select your TV from the paired devices list and start controlling!

## Tech Stack
- **Language**: Java
- **API**: Android Bluetooth HID Device Profile (`BluetoothHidDevice`, API 28+)
- **Build System**: Gradle 8.9 / Android Gradle Plugin 8.5.2
