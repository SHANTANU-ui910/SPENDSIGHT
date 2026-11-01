# SpendSight // HUD

> **Autonomous Offline-First Payment Telemetry & Expense Intelligence Engine**

SpendSight is an ultra-fast, zero-friction expense tracking and financial telemetry Progressive Web App (PWA) & native Android app built with a cyber-morphic dark HUD aesthetic.

---

## ⚡ Features

- **Cyber-Morphic Dark HUD**: 120 FPS high-refresh reactive telemetry with real-time burn velocity tracking (`₹/HR`).
- **100% Offline-First Architecture**: Runs fully in-browser with zero telemetry leak, zero cloud dependencies, and instant local state persistence.
- **Fast Radial Action**: Instant manual expense capture (`+` button), category tagger, and keyboard navigation.
- **PWA & Mobile Ready**: Installable on iOS (Safari Add to Home Screen) and Android (Chrome / WebAPK) with full service worker offline caching.
- **Native Android Companion**: Jetpack Compose, Room DB, and background notification listener for automated UPI/SMS payment tracking.

---

## 🚀 One-Click Deploy on Vercel

1. Push this repository to GitHub: `https://github.com/SHANTANU-ui910/SPENDSIGHT`
2. Go to [Vercel](https://vercel.com/new).
3. Import the `SPENDSIGHT` repository.
4. Keep the Framework Preset as **Other** (Static Site).
5. Click **Deploy**.

Vercel will immediately build and serve the app with root static assets and optimized caching headers configured in `vercel.json`.

---

## 📱 Mobile Installation

### iOS (Safari)
1. Open the deployed Vercel URL in Safari.
2. Tap the **Share** button (box with upward arrow).
3. Tap **Add to Home Screen**.

### Android (Chrome)
1. Open the deployed Vercel URL in Chrome.
2. Tap the three dots menu `⋮` or the install banner.
3. Tap **Install App** / **Add to Home screen**.

---

## 🤖 Android Native App Build

The native Kotlin codebase is located in `/app`:
- Open the root folder in **Android Studio Ladybug (2024.2+)**.
- Sync Gradle (`build.gradle.kts`).
- Run on any physical Android device or emulator running Android 8.0+ (API 26+).

