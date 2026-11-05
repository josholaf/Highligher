package com.josholaf.highligher.feature.documents

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.markdown.MarkdownEditor
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Highlight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class DocumentTab { Editor, Highlights }

data class DocumentUiState(
    val document: Document? = null,
    val highlights: List<Highlight> = emptyList(),
    val rawText: String = "",
    val tab: DocumentTab = DocumentTab.Editor,
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
    private var highlightsJob: Job? = null
    private var saveJob: Job? = null

    init { documentId?.let { load(it) } }

    fun load(id: String) {
        documentId = id
        highlightsJob?.cancel()
        viewModelScope.launch {
            val doc = repository.getDocument(id)
            val text = repository.readDocumentText(id)
            _ui.update { it.copy(document = doc, rawText = text, loading = false, dirty = false) }
        }
        highlightsJob = viewModelScope.launch {
            repository.observeHighlights(id).collect { list ->
                _ui.update { it.copy(highlights = list) }
            }
        }
    }

    fun setTab(tab: DocumentTab) { _ui.update { it.copy(tab = tab) } }

    fun updateRaw(text: String) {
        _ui.update { it.copy(rawText = text, dirty = true, saving = true) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(600)
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

    fun deleteHighlight(id: String) { viewModelScope.launch { repository.deleteHighlight(id) } }

    fun updateHighlight(id: String, text: String, note: String?, source: String?) {
        viewModelScope.launch {
            repository.updateHighlight(id, text, note, source, note.isNullOrBlank(), source.isNullOrBlank())
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
    var editing by remember { mutableStateOf<Highlight?>(null) }
    var editingTitle by remember { mutableStateOf(false) }
    var titleDraft by remember { mutableStateOf("") }

    LaunchedEffect(documentId) { viewModel.load(documentId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.document?.title ?: "Note", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(
                            when {
                                state.dirty || state.saving -> "Saving…"
                                !state.loading -> "Saved"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        Column(Modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = state.tab == DocumentTab.Editor,
                    onClick = { viewModel.setTab(DocumentTab.Editor) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Editor") }
                SegmentedButton(
                    selected = state.tab == DocumentTab.Highlights,
                    onClick = { viewModel.setTab(DocumentTab.Highlights) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Highlights (${state.highlights.size})") }
            }
            when (state.tab) {
                DocumentTab.Editor -> MarkdownEditor(
                    value = state.rawText,
                    onValueChange = viewModel::updateRaw,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = "Write in Markdown…",
                    fillMaxHeight = true
                )
                DocumentTab.Highlights -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!state.loading && state.highlights.isEmpty()) {
                        item {
                            Text(
                                "No captured highlights yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(state.highlights, key = { it.id }) { h ->
                        HighlightCard(h, onEdit = { editing = h }, onDelete = { viewModel.deleteHighlight(h.id) })
                    }
                    item { Spacer(Modifier.height(24.dp)) }
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

    editing?.let { h ->
        var text by remember { mutableStateOf(h.selectedText) }
        var note by remember { mutableStateOf(h.note.orEmpty()) }
        var source by remember { mutableStateOf(h.sourceUrl.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit highlight") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Text") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Source URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) {
                        viewModel.updateHighlight(h.id, text.trim(), note.trim().ifBlank { null }, source.trim().ifBlank { null })
                        editing = null
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HighlightCard(highlight: Highlight, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()) }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(highlight.selectedText, style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic))
            Spacer(Modifier.height(8.dp))
            highlight.sourceUrl?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            Text("Captured ${dateFmt.format(highlight.createdAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            highlight.note?.let { Spacer(Modifier.height(6.dp)); Text(it, style = MaterialTheme.typography.bodyMedium) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
        }
    }
}

@Composable
fun DocumentEditorScreen(document: Document, onBack: () -> Unit) {
    DocumentScreen(documentId = document.id, onBack = onBack)
}
