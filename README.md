# PrivacyShield 🛡️
> **Protect what you share.**  
> An on-device privacy gateway Android application that sanitizes sensitive data (Aadhaar, PAN, Phone, Email, UPI, Bank Accounts, Facial Biometrics), scrubs EXIF metadata, and shares safely with zero cloud footprint.

---

## 📲 Direct APK Download

You can download and install the latest **PrivacyShield APK** directly on your Android device:

1. Go to the [**PrivacyShield GitHub Releases**](https://github.com/ARYAN-WASEKAR/PrivacyShield/releases) page.
2. Tap on **`app-debug.apk`** under the latest release assets to download.
3. Open the downloaded `.apk` file on your phone and tap **Install** (allow "Install from unknown sources" if prompted).

---

## ✨ Features

- 🔒 **100% On-Device Processing**: Powered by Google ML Kit Text & Face Detection running entirely offline on your phone. Zero cloud upload.
- 🔴 **Privacy Check Risk Classification**:
  - **HIGH RISK**: Aadhaar (with Verhoeff checksum validation), PAN Card, Bank Account Numbers, Facial Biometrics.
  - **MEDIUM RISK**: Phone Numbers (`+91`), Email Addresses, UPI Handles (`@okhdfcbank`, `@oksbi`, `@paytm`).
- ❓ **Fact-Based Explanations**: Tap any detected item to view detection confidence and exact pattern/checksum validation rules.
- 🎨 **Multi-Style Redaction Engine**: Blur, Blackout, or Pixelate sensitive bounding boxes.
- 🧹 **EXIF Metadata Cleanser**: Removes GPS coordinates, camera model info, timestamps, and device details using `ExifInterface`.
- 📲 **Android Share Gateway (`ACTION_SEND`)**: Tap "Share" in WhatsApp, Gallery, or Gmail → Select "Shield & Protect" → Review & Share safely via `FileProvider` `content://` URI.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material 3 Theme
- **ML / AI**: Google ML Kit Text Recognition & ML Kit Face Detection (Bundled On-Device)
- **Camera**: CameraX (Lifecycle, View, Camera2)
- **Image Processing**: Android Canvas, Coil, ExifInterface
- **Architecture**: MVVM, Kotlin Coroutines, StateFlow, Navigation Compose

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 35 (Minimum SDK 26)
- JDK 17

### Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/ARYAN-WASEKAR/PrivacyShield.git
   cd PrivacyShield
   ```
2. Open the project in Android Studio.
3. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🤝 Contributing & License
Distributed under the MIT License. Contributions and feedback are welcome!
