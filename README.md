# Highligher

Offline-first Android reader for capturing selections into local Markdown notes.

Highligher registers for the system **Process Text** action. Select text in another app, send it to Highligher, choose a folder and note, and the selection is appended to a Markdown file on device. No account, no required network path for capture or editing.

Repository: https://github.com/josholaf/Highligher

## Features

- **System capture** via `ACTION_PROCESS_TEXT` (Chrome, apps that expose the selection menu)
- **Style-aware capture** — when the source provides a `Spanned` selection, bold, italic, underline, strike, links, and relative size (treated as headings) are converted to Markdown automatically
- **Obsidian-style library** — collapsible folder tree, nested notes, single continuous note editor
- **Folder icons** — searchable Lucide-named icon set assigned per folder; icons appear in the library and in the capture destination picker
- **Markdown as source of truth** under app private storage; Room indexes metadata for search
- **Debounced autosave** in the note editor; formatting toolbar above the soft keyboard
- **Themes** — system / light / dark
- **Share** individual notes or export the library via the system share sheet
- **Home-screen widget** shows the last capture and save path

## Style preservation (important)

Android’s Process Text extra is a `CharSequence`. When the providing app includes style spans (`StyleSpan`, `RelativeSizeSpan`, `URLSpan`, …), Highligher maps them:

| Source span | Markdown |
|-------------|----------|
| Bold | `**text**` |
| Italic | `*text*` |
| Underline | `<u>text</u>` |
| Strikethrough | `~~text~~` |
| Large relative/absolute size (short line) | `#` / `##` / `###` heading |
| URL span | `[text](url)` |

If you highlight a heading-sized run and the browser or reader supplies size spans, the note receives a heading without manual reformatting.

**Limitation:** many mobile browsers still pass **plain text only**. In that case Highligher cannot invent heading structure. Style-aware capture works when the host app attaches spans (some readers, editors, and accessibility-friendly sources do). Plain selections remain plain prose in a blockquote-style block.

## Requirements

| | |
|--|--|
| Min SDK | 26 |
| Target / compile SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Persistence | Room + DataStore + Markdown files |

## Build

```bash
git clone https://github.com/josholaf/Highligher.git
cd Highligher
```

Open the project in Android Studio, sync Gradle, run the `app` configuration on a device or emulator (API 26+).

```bash
./gradlew :app:assembleDebug
```

If the Gradle Wrapper is missing, use Android Studio’s “Gradle Wrapper” generation or install a matching Gradle distribution locally.

## Capture flow

1. Launch Highligher once so the Process Text target is registered.
2. Select text in another app.
3. Choose **Highligher** from the selection toolbar.
4. Review text (already Markdown if styles were present), optional note and source, pick folder (with icon) and note.
5. Save — content is appended into the chosen Markdown file.

Last folder/note destination is remembered. First launch seeds an **Inbox / Quick captures** note.

## Storage layout

```
files/highlight/library/
  <FolderName>/
    <NoteTitle>.md
```

Room database: `highligher.db` (folders, documents, highlight index). Uninstall removes both. Folder icons are stored as Lucide-style name strings on the folder row.

## Architecture (brief)

```
feature/capture   PROCESS_TEXT UI + destination widget
feature/library   folder tree + navigation into notes
feature/documents continuous Markdown editor
core/markdown     Spanned → Markdown, file append/update/delete
core/icons        searchable Lucide-named icon catalog
data/repository   Room + filesystem coordination
```

MVVM + Hilt. Markdown files remain the durable content store; Room is the index.

## Project status

Working offline MVP: capture, library tree, continuous notes, style mapping when spans exist, folder icons, themes, search, share, widget.

Not in scope yet: multi-device sync, authenticated backup, full CommonMark live preview renderer, nested subfolders beyond parent id, OCR.

## Contributing

Issues and PRs are welcome. Prefer small, focused changes with a short description of behaviour and any API 26–35 constraints. Keep the offline capture path free of network requirements.

## License

Unless otherwise noted in-repo, treat the project as source available under the terms the author publishes in a root `LICENSE` file when added. Lucide naming is used under the Lucide ISC license for icon identifiers; vector glyphs use Material Icons Extended on device.

## Privacy

Capture and notes stay on the device. No analytics or account backend is bundled in the core path. Share/export only runs when you invoke a system share intent.
