# Features

This page describes each feature of the QR Reader app in detail.

---

## Camera / Barcode Scanning

**Package:** `features/camera`

The scanning screen uses **CameraX** with an ML Kit barcode analyzer to detect barcodes in real time from the device camera.

### Supported Barcode Formats

- QR Code
- Data Matrix
- PDF417
- Aztec
- EAN-8 / EAN-13
- UPC-A / UPC-E
- Code 39 / 93 / 128
- ITF
- Codabar

### Barcode Content Types

The app recognises and displays specialised UI for:

| Type | Displayed Info |
|------|---------------|
| URL | Link with open-in-browser button |
| Contact (vCard) | Name, phone, email, address |
| WiFi | SSID, security type, connect button |
| Email | Recipient, subject, body |
| Phone | Phone number with call button |
| SMS | Phone number, message body |
| Calendar Event | Event title, start/end date |
| Geographic | Latitude/longitude with map link |
| Other | Raw barcode content |

### Saving Barcodes

A save button appears in the bottom sheet for each detected barcode. After saving, the barcode is stored in the Room database and can be viewed in History.

### Camera Permission

The app requests camera permission at runtime using **Accompanist Permissions**. A dedicated UI is shown when permission is denied, guiding the user to grant it via settings.

---

## QR Code Generation

**Package:** `features/codeCreator`

Users can type any text and generate a QR code rendered on screen. The code can be shared or saved from the result screen.

**Library:** [`qrcode-kotlin`](https://github.com/g0dkar/qrcode-kotlin) (v4.5.0)

A **WiFi Assistant** dialog simplifies creating WiFi QR codes by providing dedicated fields for SSID, password, and security type (WPA/WEP/Open), then generating the `WIFI:T:...` format automatically.

---

## Barcode History

**Package:** `features/history`

The history screen lists all saved barcodes, retrieved from the Room database as a `Flow` via `GetBarcodesWithTagsUseCase`.

### Filtering & Searching

- **Search bar** – Filter barcodes by content or title
- **Tag filter** – Select one or more tags to show only matching barcodes
- **Favorites filter** – Show only starred barcodes

### Barcode Card Actions

Each card in the history list provides the following actions via an overflow menu:

| Action | Description |
|--------|-------------|
| Edit | Change title and description |
| Delete | Remove the barcode from history |
| Toggle Favorite | Star/unstar a barcode |
| Copy | Copy barcode content to clipboard |
| Share | Share the raw content via Android share sheet |

### AI Description

An AI-generated description (from Gemini Nano) can be viewed and regenerated from the barcode card. The description is persisted to the database. See [AI Features](AI-Features) for details.

---

## Tag Management

**Package:** `features/tags`

Tags help organise saved barcodes. Each tag has a **name** and a **color** (chosen from a color picker).

### Tag Operations

| Operation | Description |
|-----------|-------------|
| Create | Add a new named and colored tag |
| Edit | Rename or recolor an existing tag |
| Delete | Remove a tag (removes it from all barcodes) |
| Assign | Tags are assigned to barcodes when saving or via the history screen |

### AI Tag Suggestions

When a barcode is saved from the camera screen, Gemini Nano suggests 1–3 relevant tags. The suggestions are shown as selectable chips below the barcode content. See [AI Features](AI-Features) for details.

---

## Settings

**Package:** `features/settings`

User preferences are stored with **Jetpack DataStore Preferences**.

### Available Settings

| Setting | Description |
|---------|-------------|
| AI Descriptions | Enable/disable AI-generated barcode descriptions |
| Humorous Descriptions | Switch AI descriptions to a funny/witty tone |
| (Future settings) | Additional preferences may be added here |

---

## Analytics

The app integrates **Firebase Analytics** for basic event tracking. All tracking respects user privacy – no barcode content is sent to Firebase.
