package com.josholaf.highligher.feature.library

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Folder
import com.josholaf.highligher.feature.documents.DocumentScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val folders: List<Folder> = emptyList(),
    val documents: List<Document> = emptyList()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: HighlightRepository
) : ViewModel() {
    val state: StateFlow<LibraryUiState> = combine(
        repository.observeFolders(),
        repository.observeDocuments()
    ) { folders, docs -> LibraryUiState(folders, docs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    init { viewModelScope.launch { repository.ensureDefaultLibrary() } }

    fun createFolder(name: String) { viewModelScope.launch { repository.createFolder(name) } }

    fun createDocument(title: String, folderId: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val doc = repository.createBlankNote(title, folderId)
            onCreated(doc.id)
        }
    }

    fun openFolderNote(folderId: String, onOpened: (String) -> Unit) {
        viewModelScope.launch {
            val doc = repository.getOrCreateFolderNote(folderId)
            onOpened(doc.id)
        }
    }

    fun deleteFolder(id: String) { viewModelScope.launch { repository.deleteFolder(id) } }
    fun deleteDocument(id: String) { viewModelScope.launch { repository.deleteDocument(id) } }
    fun renameDocument(id: String, title: String) { viewModelScope.launch { repository.renameDocument(id, title) } }
}

@Composable
fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var openDocumentId by remember { mutableStateOf<String?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }

    if (openDocumentId != null) {
        DocumentScreen(documentId = openDocumentId!!, onBack = { openDocumentId = null })
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewFolder = true }) {
                Icon(Icons.Default.Add, contentDescription = "New folder")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Library", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Folders, notes, and captures — open a note to edit like Obsidian",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            if (state.folders.isEmpty()) {
                item {
                    Text("No folders yet. Create one, then add a note or capture text into it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(state.folders, key = { it.id }) { folder ->
                FolderSection(
                    folder = folder,
                    documents = state.documents.filter { it.folderId == folder.id }.sortedByDescending { it.updatedAt },
                    onOpenDocument = { openDocumentId = it.id },
                    onNewNote = { title -> viewModel.createDocument(title, folder.id) { id -> openDocumentId = id } },
                    onFolderNote = { viewModel.openFolderNote(folder.id) { id -> openDocumentId = id } },
                    onDeleteFolder = { viewModel.deleteFolder(folder.id) },
                    onDeleteDocument = viewModel::deleteDocument,
                    onRenameDocument = viewModel::renameDocument
                )
            }
        }
    }

    if (showNewFolder) {
        NameDialog("New folder", "Name", onDismiss = { showNewFolder = false }) {
            viewModel.createFolder(it)
            showNewFolder = false
        }
    }
}

@Composable
private fun FolderSection(
    folder: Folder,
    documents: List<Document>,
    onOpenDocument: (Document) -> Unit,
    onNewNote: (String) -> Unit,
    onFolderNote: () -> Unit,
    onDeleteFolder: () -> Unit,
    onDeleteDocument: (String) -> Unit,
    onRenameDocument: (String, String) -> Unit
) {
    var showNew by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var renameDoc by remember { mutableStateOf<Document?>(null) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier.padding(6.dp))
                Text(folder.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onFolderNote) {
                    Icon(Icons.Default.StickyNote2, contentDescription = "Folder note")
                }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Folder menu")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Folder note") },
                        onClick = { menuOpen = false; onFolderNote() },
                        leadingIcon = { Icon(Icons.Default.StickyNote2, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New note") },
                        onClick = { menuOpen = false; showNew = true },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete folder") },
                        onClick = { menuOpen = false; onDeleteFolder() },
                        leadingIcon = { Icon(Icons.Default.Delete, null) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            documents.forEach { doc ->
                var docMenu by remember { mutableStateOf(false) }
                val isFolderNote = doc.title.equals("Folder note", ignoreCase = true)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenDocument(doc) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isFolderNote) Icons.Default.StickyNote2 else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.padding(8.dp))
                    Column(Modifier = Modifier.weight(1f)) {
                        Text(doc.title, style = MaterialTheme.typography.bodyLarge)
                        if (isFolderNote) {
                            Text("Folder context", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { docMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Note menu")
                    }
                    DropdownMenu(expanded = docMenu, onDismissRequest = { docMenu = false }) {
                        DropdownMenuItem(text = { Text("Rename") }, onClick = { docMenu = false; renameDoc = doc })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { docMenu = false; onDeleteDocument(doc.id) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { showNew = true }) { Text("+ New note") }
                TextButton(onClick = onFolderNote) { Text("Folder note") }
            }
        }
    }

    if (showNew) {
        NameDialog("New note", "Title", initial = "Untitled", onDismiss = { showNew = false }) {
            onNewNote(it)
            showNew = false
        }
    }
    renameDoc?.let { doc ->
        NameDialog("Rename note", "Title", initial = doc.title, onDismiss = { renameDoc = null }) {
            onRenameDocument(doc.id, it)
            renameDoc = null
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    initial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
