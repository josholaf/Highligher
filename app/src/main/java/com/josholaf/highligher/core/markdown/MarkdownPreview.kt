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

fun renderMarkdown(markdown: String): AnnotatedString {
    if (markdown.isBlank()) return AnnotatedString("")
    val root = parser.parse(markdown)
    return buildAnnotatedString { renderChildren(root) }
}

private fun AnnotatedString.Builder.renderChildren(parent: Node) {
    var child = parent.firstChild
    while (child != null) {
        renderNode(child)
        child = child.next
    }
}

private fun headingSize(level: Int): androidx.compose.ui.unit.TextUnit = when (level) {
    1 -> 24.sp
    2 -> 20.sp
    3 -> 18.sp
    else -> 16.sp
}

private fun AnnotatedString.Builder.renderNode(node: Node) {
    when (node) {
        is Heading -> {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = headingSize(node.level))) {
                renderChildren(node)
            }
            appendLine()
        }
        is Paragraph -> {
            renderChildren(node)
            appendLine()
        }
        is BlockQuote -> {
            withStyle(SpanStyle(color = Color(0xFF6B6B6B), fontStyle = FontStyle.Italic)) {
                renderChildren(node)
            }
        }
        is BulletList -> {
            var item = node.firstChild
            while (item != null) {
                append("\u2022  ")
                renderChildren(item)
                appendLine()
                item = item.next
            }
        }
        is OrderedList -> {
            var item = node.firstChild
            var n = 1
            while (item != null) {
                append("$n.  ")
                renderChildren(item)
                appendLine()
                n++
                item = item.next
            }
        }
        is FencedCodeBlock -> {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                appendLine(node.literal.trimEnd())
            }
        }
        is IndentedCodeBlock -> {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                appendLine(node.literal.trimEnd())
            }
        }
        is ThematicBreak -> appendLine("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500")
        is StrongEmphasis -> {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                renderChildren(node)
            }
        }
        is Emphasis -> {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                renderChildren(node)
            }
        }
        is Code -> {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                append(node.literal)
            }
        }
        is Link -> {
            renderChildren(node)
            append(" (" + node.destination + ")")
        }
        is Text -> append(node.literal)
        is SoftLineBreak -> append(' ')
        is HardLineBreak -> appendLine()
        else -> renderChildren(node)
    }
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
