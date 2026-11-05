# Highligher

Android app for capturing selected text into local Markdown documents.

Select text in Chrome (or any app that exposes the system selection menu), send it to Highligher, optionally add a note, and append it to a Markdown file in a folder you choose. No account. No network required for core use.

**Status:** MVP feature set from the product spec is implemented in source. Open in Android Studio, sync Gradle, run on API 26+. Export/backup via share intents works; automated cloud backup does not exist (by design).

## Requirements

| Item | Value |
|------|--------|
| Min SDK | 26 (Android 8.0) |
| Target / compile SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Build | Android Studio (recent AGP) or Gradle |

## Build

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
```

Open the project root in Android Studio, let Gradle sync, run the `app` configuration.

No Gradle Wrapper is committed yet; Studio can generate one on first open.

```bash
./gradlew :app:assembleDebug
```

## Capture flow

1. Install and open Highligher once so `PROCESS_TEXT` is registered.
2. In another app, select text.
3. Choose **Highligher** from the selection toolbar.
4. Optional note and source URL, pick or create folder/document, save.

Last-used folder and document are remembered. First launch creates an **Inbox / Quick captures** document if the library is empty.

## Implemented

- Capture via `ACTION_PROCESS_TEXT`
- Folders and Markdown documents (create, rename document, delete folder/document)
- Append / edit / delete individual highlights (Markdown + Room index)
- Move highlight between documents (repository API)
- Document screen: highlight cards, edit dialog, delete, raw Markdown editor toggle, share/export
- Search across text, notes, and sources; open matching document
- Home stats and recent documents
- Theme: system / light / dark (DataStore)
- Settings: storage size, export all Markdown via share sheet
- Offline-only core path

## Storage

```
files/highlight/library/
  <FolderName>/
    <DocumentTitle>.md
```

Room DB: `highligher.db` (metadata + search). Uninstall removes both.

## Markdown shape

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

### Short heading from quote

> Quoted passage.

**Source:** [https://example.com](https://example.com)

**Captured:** 2026-11-04

**My note:** Optional note.

---
```

## Stack

| Concern | Choice |
|---------|--------|
| UI | Compose, Material 3, Navigation |
| DI | Hilt |
| Metadata | Room |
| Settings / last destination | DataStore |
| Files | Kotlin File API + atomic temp rename |
| Markdown dependency | CommonMark (on classpath) |

Package: `com.josholaf.highligher`.

## Still open / your pass

- Gradle Wrapper + CI
- Automated tests
- SAF folder backup / restore
- Move-highlight UI (API exists)
- Tags
- Accessibility pass and screen-reader labels audit
- Play / F-Droid packaging

## License

MIT
