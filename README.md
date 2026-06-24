# 🎧 HearBoost — Android Hearing Assistant

[![APK Download](https://img.shields.io/badge/Download-APK-00C9A7?style=for-the-badge&logo=android)](https://github.com/Vikram-Bosak/HearBoost/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Vikram-Bosak/HearBoost/build.yml?branch=main&style=for-the-badge&label=Build)](https://github.com/Vikram-Bosak/HearBoost/actions)



> *Hear Every Word, Clearly*

A low-cost Android hearing assistant app that helps elderly and hearing-impaired users hear conversations more clearly using a smartphone and wired/Bluetooth headphones.

---

## 📱 Features

### MVP (v1.0)
- ✅ **One-Tap Start Listening** — Large, senior-friendly circular button
- ✅ **Real-time Audio Pipeline** — Mic → DSP → Headphones with <15ms latency (wired)
- ✅ **Volume Amplification** — Up to 20x gain with logarithmic curve control
- ✅ **Noise Reduction** — 3-level noise gate (Off/Low/High)
- ✅ **Wired Headphone Support** — 3.5mm, USB-C, any standard audio output
- ✅ **Bluetooth Headphone Support** — A2DP, SCO, LE Audio
- ✅ **Senior-Friendly UI** — 56dp touch targets, 16sp+ text, high contrast dark mode
- ✅ **Battery-Efficient** — Foreground service with wake lock, optimized for 8+ hours

### UI Screens
1. **Splash Screen** — Animated logo with progress bar
2. **Onboarding** — Microphone permission + Headphone connection
3. **Home (Idle)** — Start Listening button, volume slider, status cards
4. **Home (Active)** — Live waveform visualization, listening indicator, pause/stop
5. **Audio Settings** — Volume, Bass Boost, Clarity Boost, Noise Mode, Latency Mode
6. **App Settings** — Accessibility, Safety Limits, Language
7. **Hearing Profiles** — Presets (Dad, Conversation, TV, Outdoors) + Custom
8. **Headphone Manager** — Wired/Bluetooth detection, pairing, device list

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│                    UI Layer                  │
│  Compose Screens + Theme (Deep Ocean)       │
│  HomeViewModel (StateFlow)                  │
├─────────────────────────────────────────────┤
│               Service Layer                 │
│  AudioForegroundService                     │
│  (keeps processing alive in background)     │
├─────────────────────────────────────────────┤
│               Audio Engine                  │
│  AudioRecord → DSP Pipeline → AudioTrack    │
│  NoiseGate → Gain → SoftClip                │
├─────────────────────────────────────────────┤
│              Support Layer                  │
│  HeadphoneManager  SettingsManager          │
│  MicrophoneManager VolumeBooster            │
│  NoiseReductionEngine                       │
└─────────────────────────────────────────────┘
```

### Audio Pipeline
```
Microphone (48kHz/16-bit Mono)
    ↓
AudioRecord API
    ↓
PCM Buffer (40ms chunks)
    ↓
Noise Gate (threshold: 350-700 amplitude)
    ↓
Spectral Shaping (speech frequency boost)
    ↓
Gain Amplification (0.1x - 20x, log curve)
    ↓
Soft Clip (tanh saturation, no harsh distortion)
    ↓
AudioTrack API (Low-Latency Performance Mode)
    ↓
Headphones (Wired ~15ms / BT ~200ms)
```

---

## 🎨 Design System — "Deep Ocean"

Dark theme optimized for aging eyes. High contrast accents on a deep navy background.

| Token | Value | Usage |
|-------|-------|-------|
| Background | `#07111F` | Deepest layer |
| Surface | `#0A1422` | Content containers |
| Primary | `#44E5C2` | HearBoost Teal — actions |
| Primary Container | `#00C9A7` | Buttons, active states |
| Secondary | `#FFB955` | Warm Amber — volume |
| Volume Thumb | `#F5A623` | Slider handles |
| Tertiary | `#57E886` | Success, noise toggle |
| On Surface | `#D9E3F7` | Primary text |

**Typography:** Poppins (headings) + Roboto (body), min 16sp
**Shapes:** Pill-shaped buttons, 24dp card corners, 56dp touch targets

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material3 |
| DI | Hilt (Dagger) |
| Audio Capture | AudioRecord API |
| Audio Playback | AudioTrack (Low-Latency) |
| Bluetooth | BluetoothAdapter + A2DP |
| Background | Foreground Service + WakeLock |
| Persistence | DataStore Preferences |
| Navigation | Navigation Compose |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

---

## 🔧 Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Steps
```bash
# Clone the project
git clone <repo-url>
cd HearBoost

# Build debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

---

## 📂 Project Structure

```
HearBoost/
├── app/src/main/java/com/hearboost/
│   ├── HearBoostApp.kt              # Hilt Application
│   ├── MainActivity.kt              # Compose entry point
│   ├── audio/
│   │   ├── AudioEngine.kt           # Core mic→DSP→speaker pipeline
│   │   ├── AudioProcessingEngine.kt # Advanced DSP (AGC, spectral shaping)
│   │   ├── MicrophoneManager.kt     # Permission & mic detection
│   │   ├── NoiseReductionEngine.kt  # 3-level noise gate
│   │   └── VolumeBooster.kt         # Gain control with safety limits
│   ├── bluetooth/
│   │   └── BluetoothManager.kt      # BT/wired headphone detection
│   ├── service/
│   │   └── AudioForegroundService.kt # Background audio processing
│   ├── settings/
│   │   └── SettingsManager.kt       # DataStore preferences
│   ├── ui/
│   │   ├── theme/                   # Deep Ocean color system
│   │   ├── screens/                 # All 8 screens
│   │   └── navigation/              # NavGraph
│   └── viewmodel/
│       └── HomeViewModel.kt         # Main state management
├── design-reference/                # HTML prototypes + screenshots
└── build.gradle.kts
```

---

## ⚠️ Legal Notice

This app is marketed as a **"Hearing Assistant"** / **"Sound Amplifier"**. It is NOT a medical device. Do not claim it replaces professional hearing aids unless certified per medical regulations.

---

## 📊 Success Metrics

| Metric | Target |
|--------|--------|
| Startup time | < 2 seconds |
| Audio latency (wired) | < 50ms |
| Audio latency (BT) | < 300ms |
| Battery drain | < 10%/hour |
| Stable operation | 8+ hours |
| Min font size | 16sp |
| Min touch target | 56dp |

---

*Made for clear hearing.* 🎧
