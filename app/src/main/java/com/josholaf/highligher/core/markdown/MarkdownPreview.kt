package com.josholaf.highligher.core.markdown

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlInline
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

private val parser = Parser.builder().build()

/** ==text== is not standard markdown, so it is converted to <mark> HTML before parsing. */
private val markRegex = Regex("==([^=\n\r]+)==")

private val markStyle = SpanStyle(background = Color(0xFFFFF176), color = Color(0xFF1B1B1B))

fun renderMarkdown(markdown: String): AnnotatedString {
    if (markdown.isBlank()) return AnnotatedString("")
    val prepared = markRegex.replace(markdown) { m -> "<mark>" + m.groupValues[1] + "</mark>" }
    val root = parser.parse(prepared)
    return buildAnnotatedString { Renderer(this).children(root) }
}

private class Renderer(private val b: AnnotatedString.Builder) {
    var inMark = false

    fun children(parent: Node) {
        var child = parent.firstChild
        while (child != null) {
            node(child)
            child = child.next
        }
    }

    fun node(node: Node) {
        when (node) {
            is Heading -> {
                b.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = headingSize(node.level))) { children(node) }
                b.appendLine()
            }
            is Paragraph -> {
                children(node)
                b.appendLine()
            }
            is BlockQuote -> {
                b.withStyle(SpanStyle(color = Color(0xFF6B6B6B), fontStyle = FontStyle.Italic)) { children(node) }
            }
            is BulletList -> {
                var item = node.firstChild
                while (item != null) {
                    b.append("\u2022  ")
                    children(item)
                    b.appendLine()
                    item = item.next
                }
            }
            is OrderedList -> {
                var item = node.firstChild
                var n = 1
                while (item != null) {
                    b.append("$n.  ")
                    children(item)
                    b.appendLine()
                    n++
                    item = item.next
                }
            }
            is FencedCodeBlock -> {
                b.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    b.appendLine(node.literal.trimEnd())
                }
            }
            is IndentedCodeBlock -> {
                b.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    b.appendLine(node.literal.trimEnd())
                }
            }
            is ThematicBreak -> b.appendLine("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500")
            is StrongEmphasis -> {
                b.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { children(node) }
            }
            is Emphasis -> {
                b.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { children(node) }
            }
            is Code -> {
                b.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                    if (inMark) b.withStyle(markStyle) { b.append(node.literal) } else b.append(node.literal)
                }
            }
            is HtmlInline -> when (node.literal) {
                "mark" -> inMark = true
                "/mark" -> inMark = false
                else -> {}
            }
            is Link -> {
                children(node)
                b.append(" (" + node.destination + ")")
            }
            is Text -> {
                if (inMark) b.withStyle(markStyle) { b.append(node.literal) } else b.append(node.literal)
            }
            is SoftLineBreak -> b.append(' ')
            is HardLineBreak -> b.appendLine()
            else -> children(node)
        }
    }
}

private fun headingSize(level: Int): androidx.compose.ui.unit.TextUnit = when (level) {
    1 -> 24.sp
    2 -> 20.sp
    3 -> 18.sp
    else -> 16.sp
}

@Composable
fun MarkdownPreview(markdown: String, modifier: Modifier = Modifier) {
    val rendered = remember(markdown) { renderMarkdown(markdown) }
    if (rendered.text.isEmpty()) {
        Text(
            "Nothing to preview yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    } else {
        Text(
            rendered,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier
        )
    }
}
