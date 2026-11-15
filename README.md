<p align="center">
  <img src="assets/highligher-icon.svg" alt="Highligher icon" width="180" />
</p>

<h1 align="center">Highligher</h1>

<p align="center">
  <strong>Capture. Organise. Read.</strong><br/>
  A simple, offline-first Android app for saving selected text as organised Markdown notes.
</p>

<p align="center">
  <a href="https://github.com/josholaf/Highligher/releases"><img src="https://img.shields.io/github/v/release/josholaf/Highligher?display_name=tag" alt="Latest release"/></a>
  <a href="https://github.com/josholaf/Highligher/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-black" alt="MIT License"/></a>
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin"/>
</p>

<p align="center">
  <a href="#features">Features</a> ·
  <a href="#install">Install</a> ·
  <a href="#architecture">Architecture</a> ·
  <a href="#contributing">Contributing</a>
</p>

---

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
- Read-first note view renders Markdown cleanly; tap Edit to enter the full editor
- Floating formatting toolbar above the keyboard — heading, bold, italic, underline, lists, quote, code, link, indent, undo and redo
- Inline live preview while editing, with syntax markers shown on the active line
- Debounced autosave, explicit finish-editing action, rename and share from the note menu

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

 capture, style mapping when spans exist, library tree with folder icons, continuous notes with live preview, themes, search, share, widget.

Not built yet: multi-device sync, authenticated backup, OCR, and nested subfolders beyond a parent id.

## Contributing

Issues and pull requests are welcome. Two rules keep the app what it is:

1. Keep the capture path offline. It must not require a network call.
2. Keep Markdown as the source of truth. Anything beyond search belongs in a file, not only in the database.

Run `./gradlew testDebugUnitTest` before opening a pull request, and note any API 26–35 constraints your change touches.

## License

[MIT](LICENSE). Lucide naming is used under the Lucide ISC license for icon identifiers; vector glyphs use Material Icons Extended on device.
