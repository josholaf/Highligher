# Highligher

**Capture what you read. Keep it in plain Markdown.**

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://github.com/josholaf/Highligher)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue)](https://github.com/josholaf/Highligher)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-black)](LICENSE)

Highligher is an offline-first Android app that turns selections from anything you read into organised Markdown notes. It registers for the system **Process Text** action: select text in a browser or another app, send it to Highligher, choose a folder and note, and the selection is appended to a Markdown file on your device. Then edit that note full-screen in an Obsidian-style editor with live preview.

No account. No sync service. Your library is a folder of `.md` files.

## Features

**Capture**
- System capture through `ACTION_PROCESS_TEXT`, from any app that exposes a selection menu
- Style-aware capture — when the source provides a `Spanned` selection, bold, italic, underline, strikethrough, links and heading-sized runs become Markdown automatically
- Destination picker with remembered folder and note, so repeat captures are two taps
- Home-screen widget showing the last capture and where it went

**Library**
- Collapsible folder tree with nested notes and per-folder counts
- Searchable Lucide-named folder icons, shown in the library and in the capture picker
- Full-text search across highlight text, notes and sources, with persisted recent searches
- Twelve note colours, reminders, themes (system / light / dark)

**Editor**
- Obsidian-style live preview: Markdown renders inline, and the syntax markers reveal themselves on the line you are editing
- Floating formatting toolbar above the keyboard — heading, bold, italic, underline, lists, quote, code, link, indent, undo and redo
- Full preview mode for the rendered document
- Debounced autosave, rename from the title bar, share a note as text

**Privacy**
- Capture and editing never require the network
- No analytics or account backend in the core path
- Uninstalling removes the library and its index

## Style-aware capture

Android's Process Text extra is a `CharSequence`. When the providing app attaches style spans, Highligher maps them to Markdown:

| Source span | Markdown |
|---|---|
| Bold | `**text**` |
| Italic | `*text*` |
| Underline | `<u>text</u>` |
| Strikethrough | `~~text~~` |
| Heading-sized run on a short line | `#` / `##` / `###` |
| URL span | `[text](url)` |

Many mobile browsers still pass plain text only. In that case Highligher cannot invent structure, and the selection lands as plain prose in a blockquote.

## Live preview

The editor renders Markdown as you type. Everything except the line holding the caret is displayed without its syntax markers; tap a line and its raw source appears.

| Syntax | Rendered as |
|---|---|
| `# Heading` … `###### Heading` | Sized heading, hash marks hidden |
| `**bold**`, `*italic*`, `~~strike~~` | Styled inline |
| `` `code` `` | Monospace with a subtle background |
| `==highlight==` | Highlighted, matching the capture block style |
| `> quote` | Quoted with a rule marker |
| `- item`, `1. item` | Bullets and numbering |
| `- [ ] task`, `- [x] task` | Checkboxes, completed items struck through |
| `[text](url)` | Link text, destination hidden until you edit the line |

## Requirements

| | |
|---|---|
| Min SDK | 26 |
| Target / compile SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Persistence | Room + DataStore + Markdown files |

## Install

Prebuilt debug APKs are attached to [releases](https://github.com/josholaf/Highligher/releases) by CI. They are not signed for distribution.

### Build from source

Requires JDK 17 and Android SDK 35.

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio, sync Gradle, and run the `app` configuration on API 26 or newer.

If the Gradle Wrapper is missing from the checkout, generate it once with `gradle wrapper --gradle-version 8.11.1` or let Android Studio create it.

Run the unit tests:

```bash
./gradlew testDebugUnitTest
```

## Capture flow

1. Launch Highligher once so the Process Text target is registered.
2. Select text in another app.
3. Choose **Highligher** from the selection toolbar.
4. Review the text, add an optional note and source, and pick a folder and note.
5. Save — the selection is appended to the chosen Markdown file.

First launch seeds an **Inbox / Quick captures** note, and the last destination is remembered.

## Storage

Markdown files are the source of truth:

```
files/highlight/library/
  <FolderName>/
    <NoteTitle>.md
```

Notes carry YAML front matter, and each highlight is a block anchored by an HTML comment so it can be edited or deleted later:

```markdown
<!-- highlight:8f21c0a4 -->

### The heading of the highlight

> The selected text.

**Source:** [example.com](https://example.com)

**Captured:** 2026-11-14

**My note:** Why this matters.

---
```

Room (`highligher.db`) indexes folders, documents and highlights for search. Folder icons are stored as Lucide-style name strings on the folder row; glyphs render from Material Icons Extended on device.

## Architecture

```
feature/capture    PROCESS_TEXT UI + destination picker
feature/library    folder tree + navigation into notes
feature/documents  continuous Markdown editor
core/markdown      Spanned -> Markdown, live preview, file engine
core/icons         searchable Lucide-named icon catalog
core/database      Room entities, DAOs, database
core/datastore     capture destination, theme, recent searches
data/repository    Room + filesystem coordination
```

MVVM with Hilt. Markdown files hold the content; Room is the index.

## Project status

Working offline MVP: capture, style mapping when spans exist, library tree with folder icons, continuous notes with live preview, themes, search, share, widget.

Not built yet: multi-device sync, authenticated backup, OCR, and nested subfolders beyond a parent id.

## Contributing

Issues and pull requests are welcome. Two rules keep the app what it is:

1. Keep the capture path offline. It must not require a network call.
2. Keep Markdown as the source of truth. Anything beyond search belongs in a file, not only in the database.

Run `./gradlew testDebugUnitTest` before opening a pull request, and note any API 26–35 constraints your change touches.

## License

[MIT](LICENSE). Lucide naming is used under the Lucide ISC license for icon identifiers; vector glyphs use Material Icons Extended on device.
