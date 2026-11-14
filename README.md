# Highligher

**Capture what you read. Keep it in plain Markdown.**

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://github.com/josholaf/Highligher)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue)](https://github.com/josholaf/Highligher)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-black)](LICENSE)

Highligher is an offline-first Android app for turning highlights from anything you read into organised Markdown notes. Select text in a browser or any other app, send it to Highligher, and it lands in the note you choose. Then edit the note full-screen with an Obsidian-style live editor.

No account. No sync service. Your library is a folder of `.md` files on your device.

## Features

**Capture**
- Send selected text with Android's `ACTION_PROCESS_TEXT` menu from any app
- Pick a folder and note, add a personal note, save
- Last destination is remembered, so repeat captures are two taps
- A home-screen widget shows the last capture and where it went

**Library**
- Folders and notes, with a folder tree and per-folder note counts
- A folder note for context that belongs to the whole folder
- Full-text search across highlight text, notes and sources, with recent searches
- Twelve note colours

**Editor**
- Obsidian-style live preview: Markdown renders inline, and the syntax markers reveal themselves on the line you are editing
- Floating formatting toolbar above the keyboard — bold, italic, heading, lists, quote, code, link, indent
- Full preview mode for the rendered document
- Undo and redo, debounced autosave, rename from the title bar

**Privacy**
- Everything stays on device; capture never touches the network
- Uninstalling removes the library and index

## Markdown support

| Syntax | Live preview |
|---|---|
| `# Heading` … `###### Heading` | Sized and bold, hash marks hidden |
| `**bold**`, `*italic*`, `~~strike~~` | Styled inline |
| `` `code` `` | Monospace with a subtle background |
| `==highlight==` | Highlighted, matching the app's capture style |
| `> quote` | Quoted with a rule marker |
| `- item`, `1. item` | Bullets and numbering |
| `- [ ] task`, `- [x] task` | Checkboxes, completed items struck through |
| `[text](url)` | Text shown, destination hidden until you edit the line |

## Install

Prebuilt APKs are published on the [releases page](https://github.com/josholaf/Highligher/releases).

### Build from source

Requires JDK 17 and Android SDK 35.

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Open the project in Android Studio if you prefer: sync Gradle, then run the `app` configuration on API 26 or newer.

## How it works

Captured text is appended to a Markdown file under the app's private storage:

```
files/highlight/library/<FolderName>/<DocumentTitle>.md
```

Notes start with YAML front matter, and each highlight is written as a block with an HTML comment anchor so it can be edited or deleted later:

```markdown
<!-- highlight:8f21c0a4 -->

### The heading of the highlight

> The selected text.

**Source:** [example.com](https://example.com)

**Captured:** 2026-11-14

**My note:** Why this matters.

---
```

Room indexes note and highlight metadata for search; the Markdown files stay the source of truth.

## Stack

| Layer | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Metadata index | Room (KSP) |
| Preferences | DataStore |
| Markdown render | CommonMark with GFM tables |
| Markdown storage | Plain files via the Kotlin File API |

## Project structure

```
app/src/main/java/com/josholaf/highligher/
├── core/
│   ├── markdown/      editor, live preview, renderer, file engine
│   ├── database/      Room entities, DAOs, database
│   └── datastore/     capture destination and theme preferences
├── data/repository/   library and search
├── domain/model/      Folder, Document, Highlight
└── feature/           capture, home, library, documents, settings, widget
```

## Contributing

Issues and pull requests are welcome. Two rules keep the app what it is:

1. Keep it offline-first. The capture path must never make a network call.
2. Keep Markdown as the source of truth. Anything the UI needs beyond search belongs in a file, not only in the database.

Run the unit tests before opening a pull request:

```bash
./gradlew testDebugUnitTest
```

## License

[MIT](LICENSE)
