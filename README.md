# Highligher

[![Android](https://img.shields.io/badge/platform-Android-green)](https://github.com/josholaf/Highligher)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue)](https://github.com/josholaf/Highligher)
[![License: MIT](https://img.shields.io/badge/License-MIT-black.svg)](LICENSE)

Android app for capturing selected text into local Markdown documents, with an Obsidian-style note editor.

Select text in any app, send it to Highligher, and append it to a Markdown file. Edit notes full-screen with autosave. Folder notes hold context for a whole folder. Offline-first; no account.

## Requirements

| Item | Value |
|------|--------|
| Min SDK | 26 |
| Target / compile SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |

## Install

Prebuilt APKs are published from GitHub Actions on the [releases page](https://github.com/josholaf/Highligher/releases) when available.

Or build from source:

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
```

Open in Android Studio, sync Gradle, run `app` (API 26+).

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Features

- **Capture** via `ACTION_PROCESS_TEXT` from any app
- **Folders + notes** — Markdown files as the source of truth
- **Obsidian-style editor** — toolbar, undo/redo, debounced autosave
- **Folder note** — one shared note per folder
- **New note** opens in the editor immediately
- **Highlights list** per document (edit / delete)
- **Search** across text, notes, and sources from Home
- **Theme** — system / light / dark
- **Export / share** via the system share sheet
- **Home screen widget** — last capture at a glance

## Storage

```
files/highlight/library/<FolderName>/<DocumentTitle>.md
```

Room indexes metadata for search. Uninstall removes files and the database.

## Stack

Compose · Hilt · Room · DataStore · CommonMark · Kotlin File API

Package: `com.josholaf.highligher`

## Contributing

Bug reports and PRs are welcome. Keep the app offline-first and avoid network calls in the capture path.

## License

MIT
