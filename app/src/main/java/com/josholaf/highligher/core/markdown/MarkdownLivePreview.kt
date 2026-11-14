package com.josholaf.highligher.core.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Colours and sizes for the live preview. Kept out of the composable so the
 * transformation can be built and tested without a running MaterialTheme.
 */
data class LivePreviewPalette(
    val quoteColor: Color = Color(0xFF6B6B6B),
    val linkColor: Color = Color(0xFF1A73E8),
    val codeBackground: Color = Color(0x14808080),
    val highlightBackground: Color = Color(0xFFFFF176),
    val highlightText: Color = Color(0xFF1B1B1B),
    val markerColor: Color = Color(0x66808080)
) {
    fun headingSize(level: Int): TextUnit = when (level) {
        1 -> 26.sp
        2 -> 22.sp
        3 -> 19.sp
        else -> 17.sp
    }
}

/**
 * A styled view of the Markdown source with the syntax markers hidden on every
 * line except the one the caret sits on, the way Obsidian's live preview
 * behaves.
 */
class LivePreviewDocument internal constructor(
    val text: AnnotatedString,
    private val visibleToSource: IntArray,
    private val sourceToVisible: IntArray
) {
    val mapping: OffsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            sourceToVisible[offset.coerceIn(0, sourceToVisible.lastIndex)]

        override fun transformedToOriginal(offset: Int): Int =
            visibleToSource[offset.coerceIn(0, visibleToSource.lastIndex)]
    }

    val transformation: VisualTransformation = object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText =
            TransformedText(this@LivePreviewDocument.text, mapping)
    }
}

/** Character range of the line that contains [offset], newline excluded. */
fun lineRangeAt(source: String, offset: Int): IntRange {
    if (source.isEmpty()) return 0..0
    val safe = offset.coerceIn(0, source.length)
    val start = if (safe == 0) 0 else source.lastIndexOf('\n', safe - 1).let { if (it < 0) 0 else it + 1 }
    val end = source.indexOf('\n', safe).let { if (it < 0) source.length else it }
    return start until end
}

/** Builds the styled live preview. [activeLine] is the range that stays as raw source. */
fun buildLivePreview(
    source: String,
    activeLine: IntRange,
    palette: LivePreviewPalette = LivePreviewPalette()
): LivePreviewDocument {
    val out = Emitter(source)
    if (source.isEmpty()) return out.finish()

    var lineStart = 0
    while (lineStart <= source.length) {
        val lineEnd = source.indexOf('\n', lineStart).let { if (it < 0) source.length else it }
        val active = lineStart >= activeLine.first && lineStart <= activeLine.last
        if (active) {
            emitInline(out, source, lineStart, lineEnd, SpanStyle(), palette, hideMarkers = false)
        } else {
            emitBlockLine(out, source, lineStart, lineEnd, palette)
        }
        if (lineEnd >= source.length) break
        out.raw(lineEnd, lineEnd + 1)
        lineStart = lineEnd + 1
    }
    return out.finish()
}

private class Emitter(private val source: String) {
    val builder = AnnotatedString.Builder()
    private val anchors = ArrayList<Int>(source.length)

    fun raw(from: Int, to: Int, style: SpanStyle? = null) {
        if (to <= from || from < 0 || from >= source.length) return
        val end = to.coerceAtMost(source.length)
        val start = builder.length
        builder.append(source.substring(from, end))
        for (i in from until end) anchors.add(i)
        if (style != null) builder.addStyle(style, start, builder.length)
    }

    fun synthetic(text: String, anchor: Int, style: SpanStyle? = null) {
        if (text.isEmpty()) return
        val start = builder.length
        builder.append(text)
        repeat(text.length) { anchors.add(anchor.coerceIn(0, source.length)) }
        if (style != null) builder.addStyle(style, start, builder.length)
    }

    fun finish(): LivePreviewDocument {
        val visibleToSource = IntArray(anchors.size + 1)
        for (i in anchors.indices) visibleToSource[i] = anchors[i]
        visibleToSource[visibleToSource.lastIndex] = source.length

        val sourceToVisible = IntArray(source.length + 1)
        var seen = 0
        for (offset in 0..source.length) {
            while (seen < anchors.size && anchors[seen] < offset) seen++
            sourceToVisible[offset] = seen
        }
        return LivePreviewDocument(builder.toAnnotatedString(), visibleToSource, sourceToVisible)
    }
}

private val thematicBreak = Regex("^\\s{0,3}([-*_])(?:\\s*\\1){2,}\\s*$")
private val headingPrefix = Regex("^(#{1,6})\\s+")
private val quotePrefix = Regex("^\\s{0,3}>\\s?")
private val taskPrefix = Regex("^(\\s*)[-*+]\\s+\\[( xX)]\\s+")
private val bulletPrefix = Regex("^(\\s*)[-*+]\\s+")
private val orderedPrefix = Regex("^(\\s*\\d+[.)])\\s+")

private fun emitBlockLine(
    out: Emitter,
    source: String,
    start: Int,
    end: Int,
    palette: LivePreviewPalette
) {
    if (start >= end) return
    val line = source.substring(start, end)
    if (line.isBlank()) return

    if (line.trimStart().startsWith("```")) {
        out.raw(start, end, SpanStyle(color = palette.quoteColor, fontFamily = FontFamily.Monospace))
        return
    }
    if (thematicBreak.matches(line)) {
        out.synthetic("\u2500".repeat(24), start, SpanStyle(color = palette.markerColor))
        return
    }

    headingPrefix.find(line)?.let { match ->
        val level = match.groupValues[1].length
        val style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = palette.headingSize(level))
        emitInline(out, source, start + match.range.last + 1, end, style, palette, hideMarkers = true)
        return
    }

    quotePrefix.find(line)?.let { match ->
        out.synthetic("\u2502 ", start, SpanStyle(color = palette.quoteColor))
        val style = SpanStyle(color = palette.quoteColor, fontStyle = FontStyle.Italic)
        emitInline(out, source, start + match.range.last + 1, end, style, palette, hideMarkers = true)
        return
    }

    taskPrefix.find(line)?.let { match ->
        val checked = match.groupValues[2].isNotBlank()
        out.raw(start, start + match.groupValues[1].length)
        out.synthetic(
            if (checked) "\u2611 " else "\u2610 ",
            start + match.groupValues[1].length,
            SpanStyle(color = palette.linkColor)
        )
        val strike = if (checked) SpanStyle(textDecoration = TextDecoration.LineThrough, color = palette.quoteColor) else SpanStyle()
        emitInline(out, source, start + match.range.last + 1, end, strike, palette, hideMarkers = true)
        return
    }

    bulletPrefix.find(line)?.let { match ->
        out.raw(start, start + match.groupValues[1].length)
        out.synthetic("\u2022 ", start + match.groupValues[1].length, SpanStyle(color = palette.linkColor))
        emitInline(out, source, start + match.range.last + 1, end, SpanStyle(), palette, hideMarkers = true)
        return
    }

    orderedPrefix.find(line)?.let { match ->
        out.raw(start, start + match.groupValues[1].length)
        out.raw(start + match.range.last, start + match.range.last + 1)
        emitInline(out, source, start + match.range.last + 1, end, SpanStyle(), palette, hideMarkers = true)
        return
    }

    emitInline(out, source, start, end, SpanStyle(), palette, hideMarkers = true)
}

private fun emitInline(
    out: Emitter,
    source: String,
    from: Int,
    to: Int,
    style: SpanStyle,
    palette: LivePreviewPalette,
    hideMarkers: Boolean
) {
    var i = from
    while (i < to) {
        val c = source[i]

        if (c == '\\' && i + 1 < to && source[i + 1] in "\\`*_{}[]()#+-.!>~=") {
            if (hideMarkers) out.raw(i + 1, i + 2, style) else out.raw(i, i + 2, style.merge(SpanStyle(color = palette.markerColor)))
            i += 2
            continue
        }

        when {
            source.startsWith("**", i) || source.startsWith("__", i) -> {
                val delim = source.substring(i, i + 2)
                val close = closingMarker(source, i + 2, to, delim)
                if (close > i + 2) {
                    emitWrapped(out, source, i, i + 2, close, close + 2, style.merge(SpanStyle(fontWeight = FontWeight.Bold)), palette, hideMarkers)
                    i = close + 2
                } else {
                    i = emitChar(out, source, i, to, style, palette, hideMarkers)
                }
            }

            source.startsWith("~~", i) -> {
                val close = closingMarker(source, i + 2, to, "~~")
                if (close > i + 2) {
                    val strike = SpanStyle(textDecoration = TextDecoration.LineThrough)
                    emitWrapped(out, source, i, i + 2, close, close + 2, style.merge(strike), palette, hideMarkers)
                    i = close + 2
                } else {
                    i = emitChar(out, source, i, to, style, palette, hideMarkers)
                }
            }

            source.startsWith("==", i) -> {
                val close = closingMarker(source, i + 2, to, "==")
                if (close > i + 2) {
                    val mark = SpanStyle(background = palette.highlightBackground, color = palette.highlightText)
                    emitWrapped(out, source, i, i + 2, close, close + 2, style.merge(mark), palette, hideMarkers)
                    i = close + 2
                } else {
                    i = emitChar(out, source, i, to, style, palette, hideMarkers)
                }
            }

            c == '`' -> {
                val close = closingMarker(source, i + 1, to, "`")
                if (close > i + 1) {
                    val code = SpanStyle(fontFamily = FontFamily.Monospace, background = palette.codeBackground)
                    emitWrapped(out, source, i, i + 1, close, close + 1, style.merge(code), palette, hideMarkers)
                    i = close + 1
                } else {
                    i = emitChar(out, source, i, to, style, palette, hideMarkers)
                }
            }

            c == '*' || c == '_' -> {
                val delim = c.toString()
                val close = closingMarker(source, i + 1, to, delim)
                if (close > i + 1) {
                    val italic = SpanStyle(fontStyle = FontStyle.Italic)
                    emitWrapped(out, source, i, i + 1, close, close + 1, style.merge(italic), palette, hideMarkers)
                    i = close + 1
                } else {
                    i = emitChar(out, source, i, to, style, palette, hideMarkers)
                }
            }

            c == '[' -> {
                val closeBracket = source.indexOf(']', i + 1)
                val closeParen = if (closeBracket in (i + 1) until to && closeBracket + 1 < to && source[closeBracket + 1] == '(') {
                    source.indexOf(')', closeBracket + 2)
                } else -1
                if (closeBracket in (i + 1) until to && closeParen in (closeBracket + 1) until to) {
                    val link = SpanStyle(color = palette.linkColor, textDecoration = TextDecoration.Underline)
                    emitInline(out, source, i + 1, closeBracket, style.merge(link), palette, hideMarkers)
                    if (!hideMarkers) {
                        out.raw(i, i + 1, style.merge(SpanStyle(color = palette.markerColor)))
                        out.raw(closeBracket, closeParen + 1, style.merge(SpanStyle(color = palette.markerColor)))
                    }
                    i = closeParen + 1
                } else {
                    i = emitChar(out, source, i, to, style, palette, hideMarkers)
                }
            }

            else -> i = emitChar(out, source, i, to, style, palette, hideMarkers)
        }
    }
}

private fun emitChar(
    out: Emitter,
    source: String,
    index: Int,
    to: Int,
    style: SpanStyle,
    palette: LivePreviewPalette,
    hideMarkers: Boolean
): Int {
    out.raw(index, index + 1, style)
    return index + 1
}

private fun emitWrapped(
    out: Emitter,
    source: String,
    openStart: Int,
    openEnd: Int,
    closeStart: Int,
    closeEnd: Int,
    inner: SpanStyle,
    palette: LivePreviewPalette,
    hideMarkers: Boolean
) {
    if (!hideMarkers) {
        out.raw(openStart, openEnd, SpanStyle(color = palette.markerColor))
    }
    emitInline(out, source, openEnd, closeStart, inner, palette, hideMarkers)
    if (!hideMarkers) {
        out.raw(closeStart, closeEnd, SpanStyle(color = palette.markerColor))
    }
}

/** Finds the closing delimiter within [to], or -1 when the marker is unpaired. */
private fun closingMarker(source: String, from: Int, to: Int, marker: String): Int {
    val ch = marker[0]
    var index = source.indexOf(marker, from)
    while (index >= 0 && index < to) {
        var runEnd = index
        while (runEnd < to && source[runEnd] == ch) runEnd++
        val start = runEnd - marker.length
        if (start >= from && start + marker.length <= to) return start
        index = source.indexOf(marker, runEnd)
    }
    return -1
}
