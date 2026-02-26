# AI Features

QR Reader uses **Google ML Kit GenAI Prompt API** with **Gemini Nano**, a lightweight on-device large language model, to provide two intelligent features: tag suggestions and barcode descriptions.

---

## Overview

| Property | Details |
|----------|---------|
| Model | Gemini Nano (on-device LLM) |
| API | ML Kit GenAI Prompt API `1.0.0-beta1` |
| Model size | ~150–200 MB (downloaded once, on first use) |
| Privacy | 100% on-device – no data leaves the device |
| Required devices | Pixel 9+, Galaxy Z Fold7+, Xiaomi 15, or any device with AICore service |
| Graceful degradation | AI features are optional; the app works fully on unsupported devices |

---

## Feature 1 – AI Tag Suggestions

**Use case:** `domain/usecase/tags/GenerateTagSuggestionsUseCase`

When a barcode is detected on the camera screen, the app calls Gemini Nano to suggest 1–3 relevant tags for categorising it.

### How It Works

1. The use case builds a prompt containing the barcode content, type, format, and existing tag names.
2. Gemini Nano generates a comma-separated list of tag names.
3. Suggestions are surfaced as selectable `FilterChip` components in the camera bottom sheet.
4. **Tags are pre-selected by default** – the user can deselect unwanted tags before saving.
5. When the barcode is saved, selected tags are stored in the database.

### Prompt Configuration

| Parameter | Value |
|-----------|-------|
| Temperature | 0.3 (low, for consistency) |
| Top-K | 20 |
| Max output tokens | 60 |
| Candidate count | 1 |

---

## Feature 2 – AI Barcode Descriptions

**Use case:** `domain/usecase/barcode/GenerateBarcodeDescriptionUseCase`

The app generates a short (1–2 sentence, max 200 characters) human-readable description that explains what a barcode is and what it is used for.

### How It Works

1. The use case builds a prompt with the barcode content, type, and format.
2. Gemini Nano generates a description.
3. The description is truncated to 200 characters if necessary.
4. The result is persisted to the `aiGeneratedDescription` column of the `saved_barcodes` table.
5. Users can regenerate or delete descriptions from the **AI Description** dialog in the history screen.

### Humorous Mode

When **Humorous Descriptions** is enabled in Settings, the prompt instructs the model to use a funny/witty tone instead of a factual one.

### Prompt Configuration

| Parameter | Value |
|-----------|-------|
| Temperature | 0.5 (moderate, for creative but accurate text) |
| Top-K | 20 |
| Max output tokens | 100 |
| Candidate count | 1 |

---

## Implementation Details

### Model Lifecycle

```kotlin
// Lazy initialisation
private var model: GenerativeModel? = null

// In invoke():
if (model == null) {
    model = Generation.getClient()
}
```

The ViewModel calls `downloadModelIfNeeded()` in its `init` block so the model is ready before the user scans their first barcode. The model is cleaned up in `ViewModel.onCleared()`.

### Status Checking

Before generating content, the use case checks model availability:

```kotlin
when (model?.checkStatus()) {
    FeatureStatus.UNAVAILABLE  -> // Device not supported
    FeatureStatus.DOWNLOADABLE -> // Model needs to be downloaded
    FeatureStatus.DOWNLOADING  -> // Download in progress
    FeatureStatus.AVAILABLE    -> // Ready to use
}
```

### Error Handling

AI use cases return `Result<T>`. Errors are mapped to user-friendly string resources in the UI:

| Error message | String resource |
|---------------|----------------|
| "not available on this device" | `R.string.not_available_on_device` |
| "downloading" | `R.string.ai_model_downloading` |
| "temporarily unavailable" | `R.string.temporarily_unavailable` |
| Other | `R.string.could_not_generate` |

---

## Testing AI Features

Gemini Nano is unavailable in the test environment. The recommended approach is to create fake use-case subclasses:

```kotlin
class FakeGenerateTagSuggestionsUseCase : GenerateTagSuggestionsUseCase() {
    override suspend fun invoke(...): Result<List<SuggestedTagModel>> =
        Result.success(emptyList())
    override suspend fun downloadModelIfNeeded() { /* no-op */ }
    override fun cleanup() { /* no-op */ }
}
```

Use these fakes in ViewModel unit tests to exercise the non-AI logic paths. Mark tests that require a real model with `@Ignore`.
