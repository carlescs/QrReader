# Database

QR Reader uses **Room** (v2.8.4) as its local database. This page documents the schema, entities, relationships, and migration history.

---

## Database Configuration

| Property | Value |
|----------|-------|
| Class | `db/BarcodesDb.kt` |
| Current version | **6** |
| Entities | `SavedBarcode`, `Tag`, `BarcodeTagCrossRef` |
| Type converters | `db/converters/Converters.kt` |
| DAOs | `SavedBarcodeDao`, `TagDao` |

---

## Entities

### `saved_barcodes`

Stores each scanned and saved barcode.

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER (PK, auto) | Primary key |
| `date` | INTEGER | Scan timestamp (stored as Unix epoch via `Converters`) |
| `type` | INTEGER | ML Kit barcode type constant |
| `format` | INTEGER | ML Kit barcode format constant |
| `title` | TEXT | User-provided title (nullable) |
| `description` | TEXT | User-provided description (nullable) |
| `barcode` | TEXT | Raw barcode content |
| `aiGeneratedDescription` | TEXT | AI-generated description (nullable) |
| `is_favorite` | INTEGER | Boolean – 1 if starred, 0 otherwise |

### `tags`

Stores user-defined tags.

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER (PK, auto) | Primary key |
| `name` | TEXT | Tag name |
| `color` | TEXT | Hex color string (e.g. `#FF5733`) |

### `barcode_tag_cross_ref`

Join table for the many-to-many relationship between barcodes and tags.

| Column | Type | Description |
|--------|------|-------------|
| `barcodeId` | INTEGER (FK → `saved_barcodes.id`) | Barcode reference |
| `tagId` | INTEGER (FK → `tags.id`) | Tag reference |

Both columns form a composite primary key. Cascade-delete rules ensure that removing a barcode or a tag also removes the corresponding cross-reference rows.

---

## Relationships

```
saved_barcodes  ────────────  barcode_tag_cross_ref  ────────────  tags
     (1)                              (N:M)                          (1)
```

Room compound queries (via `@Relation`) return `BarcodeWithTags` objects that include the full tag list for each barcode.

---

## Migrations

All migrations are defined in `db/Migrations.kt`.

| Migration | Change |
|-----------|--------|
| 1 → 2 | Added `format` column to `saved_barcodes` |
| 2 → 3 | Added `title` and `description` columns to `saved_barcodes` |
| 3 → 4 | Created `tags` and `barcode_tag_cross_ref` tables |
| 4 → 5 | Added `aiGeneratedDescription` column to `saved_barcodes` |
| 5 → 6 | Added `is_favorite` column to `saved_barcodes` |

### Adding a Migration

1. Update the entity in `db/entities/`.
2. Increment `version` in `@Database(version = ...)` inside `BarcodesDb`.
3. Add a new `Migration(from, to)` object in `Migrations.kt`.
4. Register the migration in `BarcodesDb` (via `.addMigrations(...)` in the Room builder in `di/AppModule.kt`).
5. Write a migration test in the `androidTest` source set.

> **Warning:** Skipping migrations or clearing the database on upgrade causes data loss. Always provide a proper migration.

---

## DAOs

### `SavedBarcodeDao`

Key queries:

| Method | Description |
|--------|-------------|
| `getBarcodesWithTags()` | `Flow` of all barcodes with their associated tags |
| `insertBarcode(barcode)` | Insert a new barcode; returns the new row ID |
| `updateBarcode(barcode)` | Update an existing barcode |
| `deleteBarcode(barcode)` | Delete a barcode (also deletes cross-refs via cascade) |

### `TagDao`

Key queries:

| Method | Description |
|--------|-------------|
| `getAllTags()` | `Flow` of all tags |
| `insertAll(tags)` | Insert one or more tags |
| `updateItem(tag)` | Update an existing tag |
| `deleteItem(tag)` | Delete a tag (also deletes cross-refs via cascade) |
| `insertBarcodeTagCrossRef(ref)` | Associate a tag with a barcode |

---

## Type Converters

`db/converters/Converters.kt` handles `Date` ↔ `Long` conversion so that timestamps are stored as integers in SQLite.
