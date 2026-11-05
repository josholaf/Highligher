# Highligher

Android app for capturing selected text into local Markdown documents.

Select text in Chrome (or any app that exposes the system selection menu), send it to Highligher, optionally add a note, and append it to a Markdown file in a folder you choose. No account. No network required for core use.

**Status:** early scaffold. Capture, folders/documents, Markdown append, basic library UI, search shell, and theme preference work. Export/backup, highlight edit/delete polish, and accessibility are not done yet.

## Requirements

| Item | Value |
|------|--------|
| Min SDK | 26 (Android 8.0) |
| Target / compile SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Build | Android Studio (Ladybug / recent AGP) or Gradle |

## Build

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
```

Open the project root in Android Studio, let Gradle sync, then run the `app` configuration on a device or emulator (API 26+).

There is no Gradle Wrapper committed yet; Android Studio will offer to generate one on first open. If you prefer the command line after that:

```bash
./gradlew :app:assembleDebug
```

## Capture flow

1. Install and open Highligher once so the `PROCESS_TEXT` activity is registered.
2. In another app (e.g. Chrome), long-press and select text.
3. Tap **Highligher** in the selection toolbar.
4. Add an optional note and source URL, pick or create a folder and document, save.

Selected text is required. Source URL is optional; Android does not always pass it through `ACTION_PROCESS_TEXT`.

## What is implemented

- Text selection capture via `android.intent.action.PROCESS_TEXT`
- Folders and Markdown documents under app-private storage
- Append highlights into `.md` files with stable HTML comment IDs
- Room index for folders, documents, and highlights (search metadata)
- Markdown editor toolbar: undo/redo, bold, italic, underline, lists, indent
- Home, Library, Search, Settings screens
- Theme: system / light / dark (DataStore)

## Storage layout

Files live under the app’s internal storage (removed on uninstall):

```
files/highlight/library/
  <FolderName>/
    <DocumentTitle>.md
```

Room database: `highligher.db` (metadata and search index only).

**Uninstall deletes everything.** Export or copy files out before removing the app. Automatic backup is not implemented.

## Markdown document shape

```markdown
---
id: <document-uuid>
title: Software Architecture
created: 2026-11-04
updated: 2026-11-04
---

# Software Architecture

## Highlights

<!-- highlight:<highlight-uuid> -->

### Architecture principles

> Quoted passage from the source.

**Source:** [https://example.com](https://example.com)

**Captured:** 2026-11-04

**My note:** Optional personal note.

---
```

Highlight blocks are identified by `<!-- highlight:<id> -->` so the app can edit or delete one block without rewriting the whole file blindly.

## Stack

| Concern | Library / approach |
|---------|-------------------|
| UI | Compose, Material 3, Navigation Compose |
| DI | Hilt |
| Metadata DB | Room |
| Settings | DataStore Preferences |
| Markdown files | Kotlin `File` API + custom append/delete |
| Markdown parse (dependency present) | CommonMark |
| Architecture | Feature packages + domain models + repository |

Package root: `com.josholaf.highligher`.

## Project layout

```
app/src/main/java/com/josholaf/highligher/
  core/          database, datastore, design, di, markdown
  data/          repository
  domain/        models
  feature/       capture, documents, home, library, search, settings
  MainActivity.kt
  HighligherApp.kt
```

## Design tokens

| Name | Hex |
|------|-----|
| Deep forest | `#17231D` |
| Highlight green | `#78B68A` |
| Paper | `#F3F1E9` |
| Warm accent | `#C6A66A` |

## Known gaps

- No Gradle Wrapper in the repo yet
- No automated tests
- No export / share / backup UI
- Highlight edit and delete from the document view are incomplete relative to the PRD
- Launcher adaptive icon is minimal; no Play Store assets
- Search UI exists; ranking and navigation into a specific highlight need more work

## Contributing

Issues and PRs are welcome. Prefer small, focused changes. For capture or Markdown write-path changes, say how you tested (device/API level, source app for selection).

## License

MIT
