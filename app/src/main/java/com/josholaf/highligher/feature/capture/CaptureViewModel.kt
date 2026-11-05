package com.josholaf.highligher.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.datastore.CapturePreferences
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Folder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CaptureUiState(
    val selectedText: String = "",
    val note: String = "",
    val sourceUrl: String = "",
    val folders: List<Folder> = emptyList(),
    val documents: List<Document> = emptyList(),
    val selectedFolderId: String? = null,
    val selectedDocumentId: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val repository: HighlightRepository,
    private val capturePreferences: CapturePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState())

    val state: StateFlow<CaptureUiState> = combine(
        _state,
        repository.observeFolders(),
        repository.observeDocuments(),
        capturePreferences.lastDestination
    ) { s, folders, docs, last ->
        val folderId = s.selectedFolderId
            ?: last.folderId?.takeIf { id -> folders.any { it.id == id } }
            ?: folders.firstOrNull()?.id
        val documentId = s.selectedDocumentId
            ?: last.documentId?.takeIf { id -> docs.any { it.id == id && it.folderId == folderId } }
            ?: docs.firstOrNull { it.folderId == folderId }?.id
        s.copy(
            folders = folders,
            documents = docs,
            selectedFolderId = folderId,
            selectedDocumentId = documentId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaptureUiState())

    init {
        viewModelScope.launch { repository.ensureDefaultLibrary() }
    }

    fun setSelectedText(text: String) { _state.update { it.copy(selectedText = text) } }
    fun updateNote(note: String) { _state.update { it.copy(note = note) } }
    fun updateSource(url: String) { _state.update { it.copy(sourceUrl = url) } }
    fun selectFolder(id: String) { _state.update { it.copy(selectedFolderId = id, selectedDocumentId = null) } }
    fun selectDocument(id: String) { _state.update { it.copy(selectedDocumentId = id) } }

    fun createFolderAndSelect(name: String) {
        viewModelScope.launch {
            val folder = repository.createFolder(name)
            _state.update { it.copy(selectedFolderId = folder.id, selectedDocumentId = null) }
        }
    }

    fun createDocumentAndSelect(title: String) {
        val folderId = state.value.selectedFolderId ?: return
        viewModelScope.launch {
            val doc = repository.createDocument(title, folderId)
            _state.update { it.copy(selectedDocumentId = doc.id) }
        }
    }

    fun save() {
        val s = state.value
        val docId = s.selectedDocumentId
        if (docId == null || s.selectedText.isBlank()) {
            _state.update { it.copy(error = "Choose a document first") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                repository.saveHighlight(
                    selectedText = s.selectedText,
                    documentId = docId,
                    note = s.note.ifBlank { null },
                    sourceUrl = s.sourceUrl.ifBlank { null }
                )
                capturePreferences.setLastDestination(s.selectedFolderId, docId)
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Save failed") }
            }
        }
    }
}
