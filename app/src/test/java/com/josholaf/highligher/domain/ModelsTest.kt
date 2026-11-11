package com.josholaf.highligher.domain

import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Folder
import com.josholaf.highligher.domain.model.Highlight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ModelsTest {

    @Test
    fun folder_holds_name_and_id() {
        val now = Instant.parse("2026-11-11T12:00:00Z")
        val folder = Folder(id = "f1", name = "Inbox", parentId = null, createdAt = now)
        assertEquals("f1", folder.id)
        assertEquals("Inbox", folder.name)
        assertNull(folder.parentId)
        assertEquals(now, folder.createdAt)
    }

    @Test
    fun document_links_to_folder() {
        val now = Instant.parse("2026-11-11T12:00:00Z")
        val doc = Document(
            id = "d1",
            title = "Quick captures",
            folderId = "f1",
            filePath = "/tmp/Quick captures.md",
            updatedAt = now
        )
        assertEquals("d1", doc.id)
        assertEquals("f1", doc.folderId)
        assertEquals("Quick captures", doc.title)
    }

    @Test
    fun highlight_optional_note_and_source() {
        val now = Instant.parse("2026-11-11T12:00:00Z")
        val h = Highlight(
            id = "h1",
            documentId = "d1",
            selectedText = "Hello world",
            sourceUrl = null,
            note = "remember this",
            createdAt = now
        )
        assertEquals("Hello world", h.selectedText)
        assertNull(h.sourceUrl)
        assertEquals("remember this", h.note)
    }
}
