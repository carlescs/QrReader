# QrReader

A simple Qr code reader for Android. Written with Jetpack Compose and uses CameraX and MlKit for barcode detection and reading. Features AI-powered tag suggestions and barcode descriptions using Google's Gemini Nano on-device AI model.

## Features

- **QR Code & Barcode Scanning**: Scan QR codes and various barcode formats using CameraX and ML Kit
- **QR Code Generation**: Create QR codes from text
- **AI Tag Suggestions**: Automatically suggests relevant tags for organizing scanned barcodes using Gemini Nano
- **AI Descriptions**: Generates helpful, human-readable descriptions explaining what barcodes represent
- **Barcode History**: Save and manage your scanned barcodes with tags
- **On-Device AI**: All AI processing happens locally using ML Kit GenAI (Gemini Nano) - no cloud API calls

## AI Features (Gemini Nano)

This app uses **Google ML Kit GenAI Prompt API** with **Gemini Nano**, a lightweight on-device large language model (LLM), to provide intelligent features:

### 1. Smart Tag Suggestions
When you scan a barcode, the app automatically suggests 1-3 relevant tags to help categorize it. For example:
- A product barcode might suggest "shopping", "retail", "product"
- A URL might suggest "website", "link", "bookmark"

### 2. Contextual Descriptions
The app generates short (1-2 sentence) descriptions explaining what the barcode is and what it's used for. For example:
- A product barcode: "This is a UPC product barcode commonly found on retail items for inventory and checkout."
- A WiFi QR code: "This QR code contains WiFi network credentials for easy connection."

### Device Requirements for AI Features
- **Android 10+** (SDK 29) for the app
- **Gemini Nano support** requires: Pixel 9+, Galaxy Z Fold7+, Xiaomi 15, or other devices with AICore service
- **First-time download**: ~150-200MB for the Gemini Nano model
- **Graceful degradation**: AI features are optional and the app works fully on unsupported devices

### Privacy & Security
- **100% on-device**: All AI processing runs locally with Gemini Nano
- **No cloud APIs**: Your barcode data never leaves your device
- **User control**: All AI-generated content can be edited or deleted

## Versioning

This project uses Git-based automatic versioning. See [VERSIONING.md](VERSIONING.md) for details.

**Creating a release:**
```bash
git tag v5.2.0
git push origin v5.2.0
```

## Third Party Libraries

- **qrcode-kotlin** (https://github.com/g0dkar/qrcode-kotlin): QR code generation - Copyright since 2021 Rafael M. Lins, Licensed under the MIT License
- **Google ML Kit**: Barcode scanning and GenAI (Gemini Nano) for on-device AI features
- **CameraX**: Camera functionality for barcode scanning
- **Jetpack Compose**: Modern UI toolkit

