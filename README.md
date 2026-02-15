# QrReader

A simple Qr code reader for Android. Written with Jetpack Compose and uses CameraX and MlKit for barcode detection and reading.

## Versioning

This project uses Git-based automatic versioning. See [VERSIONING.md](VERSIONING.md) for details.

**Creating a release:**
```bash
git tag v5.2.0
git push origin v5.2.0
```

## CI/CD Setup

For initial CI/CD setup and understanding where version information is stored, see [.github/CICD.md](.github/CICD.md#getting-started-initial-setup).

**Quick setup:**
1. Ensure full Git history (`fetch-depth: 0` in GitHub Actions)
2. Create first version tag: `git tag v5.2.0 && git push origin v5.2.0`
3. Configure GitHub secrets (keystore, Play Store credentials)
4. Manual first upload to Play Console
5. Automated deployments work for subsequent releases

## Third party libraries
Uses https://github.com/g0dkar/qrcode-kotlin : Copyright since 2021 Rafael M. Lins, Licensed under the MIT License.

