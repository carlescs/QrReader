# QrReader

A simple Qr code reader for Android. Written with Jetpack Compose and uses CameraX and MlKit for barcode detection and reading.

## Versioning

This project uses **Google Play API-based versioning** as the primary method, with automatic fallback to git-based versioning. 

**Version Code:** Fetched from Google Play Store (latest + 1)  
**Version Name:** From Git tags (semantic versioning)

See [VERSIONING.md](VERSIONING.md) and [Google Play Versioning Guide](docs/GOOGLE_PLAY_VERSIONING.md) for details.

**Creating a release:**
```bash
git tag v5.2.0
git push origin v5.2.0
```

## Third party libraries
Uses https://github.com/g0dkar/qrcode-kotlin : Copyright since 2021 Rafael M. Lins, Licensed under the MIT License.

