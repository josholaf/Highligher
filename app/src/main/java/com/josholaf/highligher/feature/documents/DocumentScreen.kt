package com.josholaf.highligher.feature.documents

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.design.LocalBottomBarVisible
import com.josholaf.highligher.core.markdown.MarkdownEditor
import com.josholaf.highligher.core.markdown.MarkdownPreview
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
            _ui.update { it.copy(document = doc, rawText = text, loading = false, dirty = false, saving = false) }
        }
    }

    fun updateRaw(text: String) {
        _ui.update { it.copy(rawText = text, dirty = true, saving = true) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            persist(text)
        }
    }

    fun saveNow() {
        val text = _ui.value.rawText
        saveJob?.cancel()
        saveJob = viewModelScope.launch { persist(text) }
    }

    private suspend fun persist(text: String) {
        val id = documentId ?: return
        repository.writeDocumentText(id, text)
        _ui.update { it.copy(dirty = false, saving = false) }
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
    var editing by remember(documentId) { mutableStateOf(false) }
    var editingTitle by remember { mutableStateOf(false) }
    var titleDraft by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    val bottomBarVisible = LocalBottomBarVisible.current
    DisposableEffect(Unit) {
        bottomBarVisible.value = false
        onDispose { bottomBarVisible.value = true }
    }

    LaunchedEffect(documentId) { viewModel.load(documentId) }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (editing) "Editing mode" else (state.document?.title ?: "Note"), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        val status = when {
                            state.saving || state.dirty -> "Saving changes…"
                            !state.loading -> "Saved on this device"
                            else -> "Opening note…"
                        }
                        Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveNow()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editing) {
                        IconButton(onClick = {
                            viewModel.saveNow()
                            editing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Finish editing")
                        }
                    } else {
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit note")
                        }
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename note") },
                            onClick = {
                                menuExpanded = false
                                titleDraft = state.document?.title.orEmpty()
                                editingTitle = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share note") },
                            onClick = {
                                menuExpanded = false
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, state.document?.title ?: "Note")
                                    putExtra(Intent.EXTRA_TEXT, viewModel.exportText())
                                }
                                context.startActivity(Intent.createChooser(send, "Share note"))
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (editing) {
            key(documentId) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    // Note title shown as Heading 1 above the editor, like the design.
                    Text(
                        state.document?.title ?: "Untitled",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                    MarkdownEditor(
                        value = state.rawText,
                        onValueChange = viewModel::updateRaw,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = "Start writing…\n\nCaptured text is added to this note.",
                        fillMaxHeight = true
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                if (state.rawText.isBlank() && !state.loading) {
                    Text("This note is empty.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Tap Edit to start writing, or capture selected text from another app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    MarkdownPreview(state.rawText, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (editingTitle) {
        AlertDialog(
            onDismissRequest = { editingTitle = false },
            title = { Text("Rename note") },
            text = {
                OutlinedTextField(
                    value = titleDraft,
                    onValueChange = { titleDraft = it },
                    label = { Text("Note title") },
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
