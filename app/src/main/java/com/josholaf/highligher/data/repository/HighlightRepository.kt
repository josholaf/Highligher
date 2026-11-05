package com.josholaf.highligher.data.repository

import android.content.Context
import com.josholaf.highligher.core.database.AppDatabase
import com.josholaf.highligher.core.database.DocumentEntity
import com.josholaf.highligher.core.database.FolderEntity
import com.josholaf.highligher.core.database.HighlightEntity
import com.josholaf.highligher.core.markdown.MarkdownEngine
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Folder
import com.josholaf.highligher.domain.model.Highlight
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class HighlightSearchResult(
    val highlight: Highlight,
    val documentTitle: String,
    val folderName: String?
)

@Singleton
class HighlightRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val markdown: MarkdownEngine
) {
    private val rootDir: File
        get() = File(context.filesDir, "highlight/library").also { it.mkdirs() }

    val libraryRoot: File get() = rootDir

    fun observeFolders(): Flow<List<Folder>> =
        db.folderDao().observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getFolder(id: String): Folder? =
        db.folderDao().getById(id)?.toDomain()

    suspend fun createFolder(name: String, parentId: String? = null): Folder {
        val id = UUID.randomUUID().toString()
        val entity = FolderEntity(id, name.trim(), parentId, Instant.now().toEpochMilli())
        db.folderDao().upsert(entity)
        File(rootDir, sanitize(entity.name)).mkdirs()
        return entity.toDomain()
    }

    suspend fun renameFolder(id: String, newName: String) {
        val existing = db.folderDao().getById(id) ?: return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val oldDir = File(rootDir, sanitize(existing.name))
        val newDir = File(rootDir, sanitize(trimmed))
        if (oldDir.exists() && oldDir.absolutePath != newDir.absolutePath) oldDir.renameTo(newDir)
        val docs = db.documentDao().getByFolder(id)
        docs.forEach { doc ->
            val newPath = File(newDir, File(doc.filePath).name).absolutePath
            if (File(doc.filePath).exists() && doc.filePath != newPath) File(doc.filePath).renameTo(File(newPath))
            db.documentDao().update(doc.copy(filePath = newPath))
        }
        db.folderDao().upsert(existing.copy(name = trimmed))
    }

    suspend fun deleteFolder(id: String) {
        val folder = db.folderDao().getById(id) ?: return
        db.documentDao().getByFolder(id).forEach { deleteDocument(it.id) }
        File(rootDir, sanitize(folder.name)).deleteRecursively()
        db.folderDao().delete(id)
    }

    fun observeDocuments(): Flow<List<Document>> =
        db.documentDao().observeAll().map { list -> list.map { it.toDomain() } }

    fun observeDocumentsInFolder(folderId: String): Flow<List<Document>> =
        db.documentDao().observeByFolder(folderId).map { list -> list.map { it.toDomain() } }

    suspend fun getDocument(id: String): Document? = db.documentDao().getById(id)?.toDomain()

    suspend fun createDocument(title: String, folderId: String): Document {
        val id = UUID.randomUUID().toString()
        val folder = db.folderDao().getById(folderId) ?: throw IllegalArgumentException("Folder not found")
        val dir = File(rootDir, sanitize(folder.name)).also { it.mkdirs() }
        val file = File(dir, "${sanitize(title)}.md")
        markdown.createDocumentFile(file, title.trim(), id)
        val entity = DocumentEntity(id, title.trim(), folderId, file.absolutePath, Instant.now().toEpochMilli())
        db.documentDao().upsert(entity)
        return entity.toDomain()
    }

    suspend fun renameDocument(id: String, newTitle: String) {
        val doc = db.documentDao().getById(id) ?: return
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        val folder = db.folderDao().getById(doc.folderId)
        val dir = if (folder != null) File(rootDir, sanitize(folder.name)) else File(doc.filePath).parentFile
        dir?.mkdirs()
        val newFile = File(dir, "${sanitize(trimmed)}.md")
        val oldFile = File(doc.filePath)
        if (oldFile.exists() && oldFile.absolutePath != newFile.absolutePath) oldFile.renameTo(newFile)
        db.documentDao().update(doc.copy(title = trimmed, filePath = newFile.absolutePath, updatedAt = Instant.now().toEpochMilli()))
    }

    suspend fun deleteDocument(id: String) {
        val doc = db.documentDao().getById(id) ?: return
        File(doc.filePath).delete()
        db.documentDao().delete(id)
    }

    suspend fun readDocumentText(id: String): String {
        val doc = db.documentDao().getById(id) ?: return ""
        val file = File(doc.filePath)
        return if (file.exists()) file.readText() else ""
    }

    suspend fun writeDocumentText(id: String, content: String) {
        val doc = db.documentDao().getById(id) ?: return
        val file = File(doc.filePath)
        file.parentFile?.mkdirs()
        val tmp = File(file.parent, "${file.name}.tmp")
        tmp.writeText(content)
        if (!tmp.renameTo(file)) { file.writeText(content); tmp.delete() }
        db.documentDao().update(doc.copy(updatedAt = Instant.now().toEpochMilli()))
    }

    fun observeHighlights(documentId: String): Flow<List<Highlight>> =
        db.highlightDao().observeByDocument(documentId).map { list -> list.map { it.toDomain() } }

    fun observeAllHighlights(): Flow<List<Highlight>> =
        db.highlightDao().observeAll().map { list -> list.map { it.toDomain() } }

    fun search(query: String): Flow<List<HighlightSearchResult>> {
        val q = query.trim()
        if (q.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return combine(db.highlightDao().search(q), db.documentDao().observeAll(), db.folderDao().observeAll()) { highlights, docs, folders ->
            val docMap = docs.associateBy { it.id }
            val folderMap = folders.associateBy { it.id }
            highlights.map { h ->
                val doc = docMap[h.documentId]
                HighlightSearchResult(h.toDomain(), doc?.title ?: "Unknown", doc?.folderId?.let { folderMap[it]?.name })
            }
        }
    }

    fun observeHighlightCount(): Flow<Int> = db.highlightDao().observeCount()
    suspend fun getHighlight(id: String): Highlight? = db.highlightDao().getById(id)?.toDomain()

    suspend fun saveHighlight(selectedText: String, documentId: String, note: String? = null, sourceUrl: String? = null): Highlight {
        val doc = db.documentDao().getById(documentId) ?: throw IllegalArgumentException("Document not found")
        val id = UUID.randomUUID().toString()
        val highlight = Highlight(id, documentId, selectedText.trim(), sourceUrl?.takeIf { it.isNotBlank() }, note?.takeIf { it.isNotBlank() }, Instant.now())
        val file = File(doc.filePath)
        markdown.appendHighlight(file, highlight)
        markdown.touchUpdatedFrontMatter(file)
        db.highlightDao().upsert(highlight.toEntity())
        db.documentDao().update(doc.copy(updatedAt = Instant.now().toEpochMilli()))
        return highlight
    }

    suspend fun updateHighlight(highlightId: String, selectedText: String? = null, note: String? = null, sourceUrl: String? = null, clearNote: Boolean = false, clearSource: Boolean = false): Highlight? {
        val existing = db.highlightDao().getById(highlightId) ?: return null
        val doc = db.documentDao().getById(existing.documentId) ?: return null
        val updated = existing.copy(
            selectedText = selectedText?.trim()?.takeIf { it.isNotEmpty() } ?: existing.selectedText,
            note = when { clearNote -> null; note != null -> note.trim().ifBlank { null }; else -> existing.note },
            sourceUrl = when { clearSource -> null; sourceUrl != null -> sourceUrl.trim().ifBlank { null }; else -> existing.sourceUrl }
        )
        val domain = updated.toDomain()
        markdown.updateHighlight(File(doc.filePath), domain)
        markdown.touchUpdatedFrontMatter(File(doc.filePath))
        db.highlightDao().upsert(updated)
        db.documentDao().update(doc.copy(updatedAt = Instant.now().toEpochMilli()))
        return domain
    }

    suspend fun deleteHighlight(highlightId: String) {
        val h = db.highlightDao().getById(highlightId) ?: return
        val doc = db.documentDao().getById(h.documentId) ?: return
        markdown.deleteHighlight(File(doc.filePath), highlightId)
        db.highlightDao().delete(highlightId)
        db.documentDao().update(doc.copy(updatedAt = Instant.now().toEpochMilli()))
    }

    suspend fun moveHighlight(highlightId: String, targetDocumentId: String) {
        val h = db.highlightDao().getById(highlightId) ?: return
        if (h.documentId == targetDocumentId) return
        val sourceDoc = db.documentDao().getById(h.documentId) ?: return
        val targetDoc = db.documentDao().getById(targetDocumentId) ?: return
        val domain = h.toDomain().copy(documentId = targetDocumentId)
        markdown.deleteHighlight(File(sourceDoc.filePath), highlightId)
        markdown.appendHighlight(File(targetDoc.filePath), domain)
        markdown.touchUpdatedFrontMatter(File(targetDoc.filePath))
        db.highlightDao().upsert(domain.toEntity())
        val now = Instant.now().toEpochMilli()
        db.documentDao().update(sourceDoc.copy(updatedAt = now))
        db.documentDao().update(targetDoc.copy(updatedAt = now))
    }

    suspend fun getOrCreateFolderNote(folderId: String): Document {
        val existing = db.documentDao().getByFolder(folderId)
            .firstOrNull { it.title.equals("Folder note", ignoreCase = true) }
        if (existing != null) return existing.toDomain()
        return createDocument("Folder note", folderId)
    }

    suspend fun createBlankNote(title: String, folderId: String): Document {
        val doc = createDocument(title.ifBlank { "Untitled" }, folderId)
        val file = File(doc.filePath)
        if (file.exists()) {
            file.writeText(file.readText().trimEnd() + "\n\n")
        }
        return doc
    }

    suspend fun ensureDefaultLibrary() {
        if (db.folderDao().getAllOnce().isEmpty()) {
            val folder = createFolder("Inbox")
            createDocument("Quick captures", folder.id)
        }
    }

    fun storageUsageBytes(): Long = rootDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()

    private fun FolderEntity.toDomain() = Folder(id, name, parentId, Instant.ofEpochMilli(createdAt))
    private fun DocumentEntity.toDomain() = Document(id, title, folderId, filePath, Instant.ofEpochMilli(updatedAt))
    private fun HighlightEntity.toDomain() = Highlight(id, documentId, selectedText, sourceUrl, note, Instant.ofEpochMilli(createdAt))
    private fun Highlight.toEntity() = HighlightEntity(id, documentId, selectedText, sourceUrl, note, createdAt.toEpochMilli())
    private fun sanitize(name: String): String = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "untitled" }
}
