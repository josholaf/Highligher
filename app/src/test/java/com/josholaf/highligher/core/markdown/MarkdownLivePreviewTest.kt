package com.josholaf.highligher.core.markdown

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownLivePreviewTest {

    private val nowhere = 10_000..10_000

    private fun rendered(source: String, activeLine: IntRange = nowhere): LivePreviewDocument =
        buildLivePreview(source, activeLine, LivePreviewPalette())

    @Test
    fun bold_markers_are_hidden_off_the_active_line() {
        val doc = rendered("**bold** text")
        assertEquals("bold text", doc.text.text)
        assertTrue(doc.text.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun active_line_keeps_source_markers() {
        val source = "**bold** text"
        val doc = rendered(source, lineRangeAt(source, 2))
        assertEquals(source, doc.text.text)
    }

    @Test
    fun heading_prefix_is_hidden_and_sized() {
        val doc = rendered("# Title")
        assertEquals("Title", doc.text.text)
        val size = doc.text.spanStyles.firstOrNull { it.item.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified }
        assertTrue(size != null)
    }

    @Test
    fun quote_and_bullet_prefixes_become_markers() {
        assertEquals("\u2502 quoted", rendered("> quoted").text.text)
        assertEquals("\u2022 item", rendered("- item").text.text)
    }

    @Test
    fun task_list_renders_checkbox_and_strikes_done_items() {
        val doc = rendered("- [x] shipped")
        assertEquals("\u2611 shipped", doc.text.text)
        assertTrue(doc.text.spanStyles.any { it.item.textDecoration != null })
        assertEquals("\u2610 shipped", rendered("- [ ] shipped").text.text)
    }

    @Test
    fun inline_code_highlight_and_strikethrough_are_styled() {
        assertEquals("code", rendered("`code`").text.text)
        assertEquals("marked", rendered("==marked==").text.text)
        assertEquals("gone", rendered("~~gone~~").text.text)
    }

    @Test
    fun link_keeps_text_and_hides_destination_off_active_line() {
        assertEquals("site", rendered("[site](https://example.com)").text.text)
    }

    @Test
    fun unpaired_markers_are_kept_literally() {
        assertEquals("5 * 3 = 15", rendered("5 * 3 = 15").text.text)
    }

    @Test
    fun nested_bold_italic_stays_styled() {
        val doc = rendered("**bold and *italic***")
        assertEquals("bold and italic", doc.text.text)
        assertTrue(doc.text.spanStyles.any { it.item.fontStyle != null })
    }

    @Test
    fun offset_mapping_round_trips_for_visible_characters() {
        val source = "**bold** and `code` here"
        val doc = rendered(source)
        for (visible in 0..doc.text.text.length) {
            val original = doc.mapping.transformedToOriginal(visible)
            assertEquals(visible, doc.mapping.originalToTransformed(original))
        }
    }

    @Test
    fun offset_mapping_is_monotonic_across_every_offset() {
        val source = "# Title\n\n- item with **bold**\n> quote\n"
        val doc = rendered(source)
        var previous = -1
        for (visible in 0..doc.text.text.length) {
            val original = doc.mapping.transformedToOriginal(visible)
            assertTrue(original >= previous)
            previous = original
        }
    }

    @Test
    fun line_range_finds_the_line_around_the_caret() {
        val source = "one\ntwo\nthree"
        assertEquals(0 until 3, lineRangeAt(source, 0))
        assertEquals(4 until 7, lineRangeAt(source, 5))
        assertEquals(8 until 13, lineRangeAt(source, 13))
        assertEquals(0..0, lineRangeAt("", 0))
    }
}
