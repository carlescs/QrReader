# QR Reader – Wiki Home

Welcome to the **QR Reader** project wiki. QR Reader is a modern Android application for scanning and generating QR codes and barcodes, featuring on-device AI powered by Google's Gemini Nano.

## About the App

QR Reader is built with Jetpack Compose and follows Clean Architecture principles. It uses CameraX and ML Kit for real-time barcode detection and ML Kit GenAI (Gemini Nano) for on-device AI features.

### Key Highlights

- **QR & Barcode scanning** – Scan QR codes and common barcode formats in real time using CameraX and ML Kit
- **QR code generation** – Create QR codes from custom text
- **Barcode history** – Save, tag, and manage previously scanned barcodes
- **AI tag suggestions** – Gemini Nano automatically suggests relevant tags for each barcode
- **AI descriptions** – Gemini Nano generates concise, human-readable descriptions for scanned barcodes
- **100 % on-device AI** – All AI inference happens locally; no data leaves the device

### Device Requirements

| Requirement | Minimum |
|------------|---------|
| Android version | 8.0 (SDK 26) |
| Target SDK | 36 (Android 15) |
| Camera | Required for scanning |
| AI features | Pixel 9+, Galaxy Z Fold7+, Xiaomi 15, or any device with AICore service |

---

## Wiki Pages

| Page | Description |
|------|-------------|
| [Getting Started](Getting-Started) | How to build and run the project locally |
| [Architecture](Architecture) | Clean Architecture layers, MVVM, design patterns |
| [Features](Features) | In-depth documentation of each app feature |
| [AI Features](AI-Features) | Gemini Nano integration – tag suggestions and descriptions |
| [Database](Database) | Room database schema, entities and migrations |
| [Testing](Testing) | Unit tests, UI tests, coverage |
| [Contributing](Contributing) | Coding standards, PR workflow, adding new features |

---

## Quick Links

- [README](../../README.md)
- [Versioning Guide](../../VERSIONING.md)
- [CI/CD Guide](../../.github/CICD.md)
- [License](../../LICENSE.md)
