package com.josholaf.highligher.domain.model

import java.time.Instant

data class Folder(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: Instant = Instant.now()
)

data class Document(
    val id: String,
    val title: String,
    val folderId: String,
    val filePath: String,
    val updatedAt: Instant = Instant.now()
)

data class Highlight(
    val id: String,
    val documentId: String,
    val selectedText: String,
    val sourceUrl: String? = null,
    val note: String? = null,
    val createdAt: Instant = Instant.now()
)

data class Tag(
    val id: String,
    val name: String
)

data class CaptureRequest(
    val selectedText: String,
    val sourceUrl: String? = null,
    val pageTitle: String? = null
)
