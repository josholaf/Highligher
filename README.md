# Highligher

Android app for capturing selected text into local Markdown documents, with an Obsidian-style note editor.

Select text in any app, send it to Highligher, and append it to a Markdown file. Edit notes full-screen with autosave. Folder notes hold context for a whole folder. Offline-first; no account.

## Requirements

| Item | Value |
|------|--------|
| Min SDK | 26 |
| Target / compile SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |

## Build

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
```

Open in Android Studio, sync Gradle, run `app` (API 26+).

## Features

- Capture via `ACTION_PROCESS_TEXT`
- Folders + notes (Markdown as source of truth)
- Obsidian-style editor: toolbar, undo/redo, debounced autosave
- Folder note (one shared note per folder)
- New note opens in the editor immediately
- Highlights list per document (edit / delete)
- Search across text, notes, sources
- Theme: system / light / dark
- Export / share via system share sheet

## Storage

```
files/highlight/library/<FolderName>/<DocumentTitle>.md
```

Room indexes metadata for search. Uninstall removes files and DB.

## Stack

Compose, Hilt, Room, DataStore, CommonMark, Kotlin File API.

Package: `com.josholaf.highligher`

## License

MIT
