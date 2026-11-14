package com.josholaf.highligher.feature.documents

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.markdown.MarkdownEditor
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.domain.model.Document
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentUiState(
    val document: Document? = null,
    val rawText: String = "",
    val loading: Boolean = true,
    val saving: Boolean = false,
    val dirty: Boolean = false
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: HighlightRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _ui = MutableStateFlow(DocumentUiState())
    val ui: StateFlow<DocumentUiState> = _ui.asStateFlow()
    private var documentId: String? = savedStateHandle["documentId"]
    private var saveJob: Job? = null

    init { documentId?.let { load(it) } }

    fun load(id: String) {
        documentId = id
        viewModelScope.launch {
            val doc = repository.getDocument(id)
            val text = repository.readDocumentText(id)
            _ui.update { it.copy(document = doc, rawText = text, loading = false, dirty = false) }
        }
    }

    fun updateRaw(text: String) {
        _ui.update { it.copy(rawText = text, dirty = true, saving = true) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            val id = documentId ?: return@launch
            repository.writeDocumentText(id, text)
            _ui.update { it.copy(dirty = false, saving = false) }
        }
    }

    fun renameTitle(title: String) {
        val id = documentId ?: return
        viewModelScope.launch {
            repository.renameDocument(id, title)
            _ui.update { s -> s.copy(document = s.document?.copy(title = title.trim())) }
        }
    }

    fun exportText(): String = _ui.value.rawText
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    documentId: String,
    onBack: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current
    var editingTitle by remember { mutableStateOf(false) }
    var titleDraft by remember { mutableStateOf("") }

    LaunchedEffect(documentId) { viewModel.load(documentId) }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.document?.title ?: "Note", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        val status = when {
                            state.dirty || state.saving -> "Saving…"
                            !state.loading -> "Saved"
                            else -> ""
                        }
                        if (status.isNotEmpty()) {
                            Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        titleDraft = state.document?.title.orEmpty()
                        editingTitle = true
                    }) { Text("Rename") }
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, state.document?.title ?: "Note")
                            putExtra(Intent.EXTRA_TEXT, viewModel.exportText())
                        }
                        context.startActivity(Intent.createChooser(send, "Share note"))
                    }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                }
            )
        }
    ) { padding ->
        MarkdownEditor(
            value = state.rawText,
            onValueChange = viewModel::updateRaw,
            modifier = Modifier.fillMaxSize().padding(padding),
            placeholder = "Start writing…\n\nCaptured text is appended into this same note.",
            fillMaxHeight = true
        )
    }

    if (editingTitle) {
        AlertDialog(
            onDismissRequest = { editingTitle = false },
            title = { Text("Rename note") },
            text = {
                OutlinedTextField(
                    value = titleDraft,
                    onValueChange = { titleDraft = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (titleDraft.isNotBlank()) viewModel.renameTitle(titleDraft)
                    editingTitle = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingTitle = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun DocumentEditorScreen(document: Document, onBack: () -> Unit) {
    DocumentScreen(documentId = document.id, onBack = onBack)
}
