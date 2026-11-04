# Highligher

Offline-first Android app that turns selected text into organized Markdown knowledge documents.

**Capture → annotate → append to a Markdown file.** No account. No cloud required for core use.

## Product principles

1. Capture should take seconds.
2. Organizing should require minimal effort.
3. Multiple highlights accumulate in one Markdown file.
4. Everything works offline.
5. Markdown stays portable and readable outside the app.

## Stack

| Layer | Choice |
| ----- | ------ |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Hilt |
| Metadata | Room |
| Documents | Markdown files on internal storage |
| Capture | `ACTION_PROCESS_TEXT` (text selection menu) |

## Features in this scaffold

- **Capture** from any app via Android’s text-selection toolbar
- **Folders + documents** with Markdown as source of truth
- **Markdown editor** with toolbar: undo / redo · **B** · *I* · underline · lists · indent
- Home, Library, Search, Settings shells
- Safe append of highlight blocks with stable `<!-- highlight:id -->` markers

## Open in Android Studio

1. Clone this repo.
2. Open the root folder in Android Studio.
3. Sync Gradle. Min SDK 26, target 35.
4. Run on a device or emulator.

Capture flow: open Chrome → select text → choose **Highligher** from the selection menu.

## Design system

- Deep forest `#17231D`
- Highlight green `#78B68A`
- Paper `#F3F1E9`
- Warm accent `#C6A66A`

## Important

Data lives in app-private storage. Uninstall removes files. Export/backup is planned.
