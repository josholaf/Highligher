package com.josholaf.highligher.core.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Markdown source editor with inline live preview. Syntax markers are hidden on
 * every line except the one being edited, while the formatting toolbar stays
 * accessible above the keyboard.
 */
@Composable
fun MarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing…",
    minLines: Int = 12,
    fillMaxHeight: Boolean = false
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    if (value != fieldValue.text) fieldValue = fieldValue.copy(text = value)

    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    var previewOnly by remember { mutableStateOf(false) }

    val palette = livePreviewPalette()
    val activeLine = lineRangeAt(fieldValue.text, fieldValue.selection.max)
    val liveDocument = remember(fieldValue.text, activeLine, palette) {
        buildLivePreview(fieldValue.text, activeLine, palette)
    }

    fun pushUndo(before: TextFieldValue) {
        if (undoStack.lastOrNull() != before) {
            undoStack.add(before)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
        }
    }

    fun applyChange(newText: String, newSelection: TextRange) {
        pushUndo(fieldValue)
        fieldValue = TextFieldValue(newText, newSelection)
        onValueChange(newText)
    }

    fun wrapSelection(prefix: String, suffix: String = prefix) {
        val text = fieldValue.text
        val sel = fieldValue.selection
        val start = sel.min
        val end = sel.max
        val selected = text.substring(start, end)
        val newText = text.substring(0, start) + prefix + selected + suffix + text.substring(end)
        val cursor = if (selected.isEmpty()) start + prefix.length else start + prefix.length + selected.length + suffix.length
        applyChange(newText, TextRange(cursor))
    }

    fun insertLinePrefix(prefix: String) {
        val text = fieldValue.text
        val cursor = fieldValue.selection.min
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        applyChange(newText, TextRange(cursor + prefix.length))
    }

    fun indent(increase: Boolean) {
        val text = fieldValue.text
        val cursor = fieldValue.selection.min
        val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val newText = if (increase) text.substring(0, lineStart) + "  " + text.substring(lineStart)
        else when {
            text.startsWith("  ", lineStart) -> text.removeRange(lineStart, lineStart + 2)
            text.startsWith("\t", lineStart) -> text.removeRange(lineStart, lineStart + 1)
            else -> text
        }
        val delta = newText.length - text.length
        applyChange(newText, TextRange((cursor + delta).coerceAtLeast(lineStart)))
    }

    Box(
        modifier = if (fillMaxHeight) modifier.fillMaxSize() else modifier.fillMaxWidth().heightIn(min = (minLines * 22).dp)
    ) {
        if (previewOnly) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 76.dp)
            ) {
                MarkdownPreview(fieldValue.text)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 76.dp)
            ) {
                if (fieldValue.text.isEmpty()) {
                    Text(
                        placeholder,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    )
                }
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { next ->
                        if (next.text != fieldValue.text) pushUndo(fieldValue)
                        fieldValue = next
                        onValueChange(next.text)
                    },
                    modifier = if (fillMaxHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    visualTransformation = liveDocument.transformation,
                    minLines = if (fillMaxHeight) 1 else minLines
                )
            }
        }

        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Column {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarIcon(
                        if (previewOnly) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        if (previewOnly) "Edit" else "Preview"
                    ) { previewOnly = !previewOnly }
                    VerticalBar()
                    ToolbarIcon(Icons.Default.Undo, "Undo", enabled = !previewOnly && undoStack.isNotEmpty()) {
                        val prev = undoStack.removeLastOrNull() ?: return@ToolbarIcon
                        redoStack.add(fieldValue)
                        fieldValue = prev
                        onValueChange(prev.text)
                    }
                    ToolbarIcon(Icons.Default.Redo, "Redo", enabled = !previewOnly && redoStack.isNotEmpty()) {
                        val next = redoStack.removeLastOrNull() ?: return@ToolbarIcon
                        undoStack.add(fieldValue)
                        fieldValue = next
                        onValueChange(next.text)
                    }
                    VerticalBar()
                    ToolbarIcon(Icons.Default.Title, "Heading", enabled = !previewOnly) { insertLinePrefix("## ") }
                    ToolbarIcon(Icons.Default.FormatBold, "Bold", enabled = !previewOnly) { wrapSelection("**") }
                    ToolbarIcon(Icons.Default.FormatItalic, "Italic", enabled = !previewOnly) { wrapSelection("*") }
                    ToolbarIcon(Icons.Default.FormatUnderlined, "Underline", enabled = !previewOnly) { wrapSelection("<u>", "</u>") }
                    VerticalBar()
                    ToolbarIcon(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet", enabled = !previewOnly) { insertLinePrefix("- ") }
                    ToolbarIcon(Icons.Default.FormatListNumbered, "Numbered", enabled = !previewOnly) { insertLinePrefix("1. ") }
                    ToolbarIcon(Icons.Default.FormatQuote, "Quote", enabled = !previewOnly) { insertLinePrefix("> ") }
                    ToolbarIcon(Icons.Default.Code, "Code", enabled = !previewOnly) { wrapSelection("`") }
                    ToolbarIcon(Icons.Default.Link, "Link", enabled = !previewOnly) { wrapSelection("[", "](url)") }
                    VerticalBar()
                    ToolbarIcon(Icons.Default.FormatIndentDecrease, "Outdent", enabled = !previewOnly) { indent(false) }
                    ToolbarIcon(Icons.Default.FormatIndentIncrease, "Indent", enabled = !previewOnly) { indent(true) }
                }
            }
        }
    }
}

@Composable
private fun livePreviewPalette(): LivePreviewPalette {
    val colors = MaterialTheme.colorScheme
    return remember(colors) {
        LivePreviewPalette(
            quoteColor = colors.onSurfaceVariant,
            linkColor = colors.primary,
            codeBackground = colors.surfaceVariant,
            highlightBackground = colors.tertiaryContainer,
            highlightText = colors.onTertiaryContainer,
            markerColor = colors.onSurfaceVariant.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun ToolbarIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
        Icon(
            icon,
            contentDescription = desc,
            modifier = Modifier.size(20.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun VerticalBar() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(width = 1.dp, height = 20.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
