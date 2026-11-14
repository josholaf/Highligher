package com.josholaf.highligher.core.markdown

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.CharacterStyle
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan

/**
 * Converts platform CharSequence (including Spanned from PROCESS_TEXT) into Markdown
 * so bold, italic, underline, strike, links, and relative heading sizes survive.
 */
object SpannedMarkdown {

    fun toMarkdown(source: CharSequence?): String {
        if (source.isNullOrEmpty()) return ""
        val spanned = source as? Spanned ?: return source.toString().trim()
        return convert(spanned).trim()
    }

    private fun convert(spanned: Spanned): String {
        if (spanned.isEmpty()) return ""
        val out = StringBuilder()
        var i = 0
        val n = spanned.length
        while (i < n) {
            val next = nextBoundary(spanned, i)
            val slice = spanned.subSequence(i, next).toString()
            val styles = spanned.getSpans(i, next, CharacterStyle::class.java)
                .filter { spanCovers(spanned, it, i, next) }
            out.append(applyStyles(slice, styles))
            i = next
        }
        return normalizeHeadings(out.toString())
    }

    private fun nextBoundary(spanned: Spanned, start: Int): Int {
        val n = spanned.length
        var end = n
        spanned.getSpans(start, n, CharacterStyle::class.java).forEach { span ->
            val s = spanned.getSpanStart(span)
            val e = spanned.getSpanEnd(span)
            if (s > start) end = minOf(end, s)
            if (e > start) end = minOf(end, e)
        }
        if (end == start) end = start + 1
        return end
    }

    private fun spanCovers(spanned: Spanned, span: Any, start: Int, end: Int): Boolean {
        val s = spanned.getSpanStart(span)
        val e = spanned.getSpanEnd(span)
        return s <= start && e >= end
    }

    private fun applyStyles(text: String, styles: List<CharacterStyle>): String {
        if (text.isEmpty()) return text
        var result = text
            .replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace("_", "\\_")
            .replace("`", "\\`")

        var bold = false
        var italic = false
        var underline = false
        var strike = false
        var mono = false
        var link: String? = null
        var sizeScale = 1f

        styles.forEach { span ->
            when (span) {
                is StyleSpan -> {
                    if (span.style and Typeface.BOLD != 0) bold = true
                    if (span.style and Typeface.ITALIC != 0) italic = true
                }
                is UnderlineSpan -> underline = true
                is StrikethroughSpan -> strike = true
                is TypefaceSpan -> {
                    if (span.family?.contains("mono", ignoreCase = true) == true) mono = true
                }
                is URLSpan -> link = span.url
                is RelativeSizeSpan -> sizeScale = maxOf(sizeScale, span.sizeChange)
                is AbsoluteSizeSpan -> {
                    val px = if (span.dip) span.size * 3 else span.size
                    if (px >= 22) sizeScale = maxOf(sizeScale, 1.4f)
                    if (px >= 28) sizeScale = maxOf(sizeScale, 1.7f)
                    if (px >= 34) sizeScale = maxOf(sizeScale, 2.0f)
                }
                is SuperscriptSpan, is SubscriptSpan -> { }
            }
        }

        if (mono) result = "`$result`"
        if (bold && italic) result = "***$result***"
        else if (bold) result = "**$result**"
        else if (italic) result = "*$result*"
        if (underline) result = "<u>$result</u>"
        if (strike) result = "~~$result~~"
        if (link != null) result = "[$result]($link)"

        if (sizeScale >= 1.9f) result = "\u0001H1\u0001$result"
        else if (sizeScale >= 1.55f) result = "\u0001H2\u0001$result"
        else if (sizeScale >= 1.25f) result = "\u0001H3\u0001$result"

        return result
    }

    private fun normalizeHeadings(raw: String): String {
        return raw.split('\n').joinToString("\n") { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("\u0001H1\u0001") -> {
                    val body = stripMarkers(trimmed)
                    if (isHeadingCandidate(body)) "# ${plain(body)}" else plain(body)
                }
                trimmed.startsWith("\u0001H2\u0001") -> {
                    val body = stripMarkers(trimmed)
                    if (isHeadingCandidate(body)) "## ${plain(body)}" else "**${plain(body)}**"
                }
                trimmed.startsWith("\u0001H3\u0001") -> {
                    val body = stripMarkers(trimmed)
                    if (isHeadingCandidate(body)) "### ${plain(body)}" else "**${plain(body)}**"
                }
                else -> stripMarkers(line)
            }
        }.replace(Regex("\n{3,}"), "\n\n")
    }

    private fun stripMarkers(s: String) =
        s.replace("\u0001H1\u0001", "")
            .replace("\u0001H2\u0001", "")
            .replace("\u0001H3\u0001", "")

    private fun plain(s: String) = s
        .replace(Regex("\\*\\*\\*(.+?)\\*\\*\\*"), "$1")
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("\\*(.+?)\\*"), "$1")
        .replace(Regex("<u>(.+?)</u>"), "$1")
        .replace(Regex("~~(.+?)~~"), "$1")
        .replace(Regex("`(.+?)`"), "$1")
        .trim()

    private fun isHeadingCandidate(body: String): Boolean {
        val p = plain(body)
        return p.isNotBlank() && p.length <= 120 && !p.contains('\n')
    }
}
