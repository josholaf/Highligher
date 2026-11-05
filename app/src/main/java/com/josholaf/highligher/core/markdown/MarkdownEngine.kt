package com.josholaf.highligher.core.markdown

import com.josholaf.highligher.domain.model.Highlight
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownEngine @Inject constructor() {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())

    fun createDocumentFile(file: File, title: String, documentId: String) {
        val now = Instant.now()
        val content = buildString {
            appendLine("---")
            appendLine("id: $documentId")
            appendLine("title: $title")
            appendLine("created: ${dateFmt.format(now)}")
            appendLine("updated: ${dateFmt.format(now)}")
            appendLine("---")
            appendLine()
            appendLine("# $title")
            appendLine()
            appendLine("## Highlights")
            appendLine()
        }
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun appendHighlight(file: File, highlight: Highlight) {
        val block = renderHighlightBlock(highlight)
        val existing = if (file.exists()) file.readText() else ""
        val updated = if (existing.contains("## Highlights")) {
            existing.trimEnd() + "\n\n" + block
        } else {
            existing + "\n\n## Highlights\n\n" + block
        }
        atomicWrite(file, updated)
    }

    fun updateHighlight(file: File, highlight: Highlight) {
        if (!file.exists()) {
            appendHighlight(file, highlight)
            return
        }
        val text = file.readText()
        val pattern = Regex(
            """<!-- highlight:${Regex.escape(highlight.id)} -->[\s\S]*?(?=<!-- highlight:|$)""",
            RegexOption.MULTILINE
        )
        val block = renderHighlightBlock(highlight).trimEnd() + "\n\n"
        val updated = if (pattern.containsMatchIn(text)) {
            pattern.replace(text, block)
        } else {
            text.trimEnd() + "\n\n" + block
        }
        atomicWrite(file, updated.replace(Regex("\n{3,}"), "\n\n").trimEnd() + "\n")
    }

    fun deleteHighlight(file: File, highlightId: String) {
        if (!file.exists()) return
        val text = file.readText()
        val pattern = Regex(
            """<!-- highlight:${Regex.escape(highlightId)} -->[\s\S]*?(?=<!-- highlight:|$)""",
            RegexOption.MULTILINE
        )
        val cleaned = pattern.replace(text, "").replace(Regex("\n{3,}"), "\n\n")
        atomicWrite(file, cleaned.trimEnd() + "\n")
    }

    fun renderHighlightBlock(h: Highlight): String = buildString {
        appendLine("<!-- highlight:${h.id} -->")
        appendLine()
        val heading = h.selectedText.lineSequence().firstOrNull()?.take(60)?.trim()
            ?.takeIf { it.isNotBlank() } ?: "Highlight"
        appendLine("### $heading")
        appendLine()
        appendLine("> ${h.selectedText.replace("\n", "\n> ")}")
        appendLine()
        if (!h.sourceUrl.isNullOrBlank()) {
            appendLine("**Source:** [${h.sourceUrl}](${h.sourceUrl})")
            appendLine()
        }
        appendLine("**Captured:** ${dateFmt.format(h.createdAt)}")
        appendLine()
        if (!h.note.isNullOrBlank()) {
            appendLine("**My note:** ${h.note}")
            appendLine()
        }
        appendLine("---")
    }

    fun touchUpdatedFrontMatter(file: File) {
        if (!file.exists()) return
        val text = file.readText()
        val today = dateFmt.format(Instant.now())
        val updated = if (text.contains(Regex("""^updated:\s*.+$""", RegexOption.MULTILINE))) {
            text.replace(Regex("""^updated:\s*.+$""", RegexOption.MULTILINE), "updated: $today")
        } else text
        if (updated != text) atomicWrite(file, updated)
    }

    private fun atomicWrite(file: File, content: String) {
        file.parentFile?.mkdirs()
        val tmp = File(file.parent, "${file.name}.tmp")
        tmp.writeText(content)
        if (!tmp.renameTo(file)) {
            file.writeText(content)
            tmp.delete()
        }
    }
}
