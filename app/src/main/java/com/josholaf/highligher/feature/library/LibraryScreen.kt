package com.josholaf.highligher.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
    fun deleteFolder(id: String) { viewModelScope.launch { repository.deleteFolder(id) } }
    fun deleteDocument(id: String) { viewModelScope.launch { repository.deleteDocument(id) } }
    fun renameDocument(id: String, title: String) { viewModelScope.launch { repository.renameDocument(id, title) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var openDocumentId by remember { mutableStateOf<String?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showNewNote by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    if (openDocumentId != null) {
        DocumentScreen(documentId = openDocumentId!!, onBack = { openDocumentId = null })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Library", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${state.folders.size} folders · ${state.documents.size} notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showCreateMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                }
                DropdownMenu(
                    expanded = showCreateMenu,
                    onDismissRequest = { showCreateMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("New note") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                        onClick = {
                            showCreateMenu = false
                            if (state.folders.isNotEmpty()) showNewNote = true else showNewFolder = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New folder") },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                        onClick = { showCreateMenu = false; showNewFolder = true }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (state.folders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("Your library starts here", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Create a folder to organise your notes and highlights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showNewFolder = true }) { Text("Create folder") }
                    }
                }
            }
            items(state.folders, key = { it.id }) { folder ->
                val isOpen = expanded[folder.id] ?: true
                val notes = state.documents.filter { it.folderId == folder.id }.sortedBy { it.title.lowercase() }
                FolderTreeRow(
                    folder = folder,
                    expanded = isOpen,
                    noteCount = notes.size,
                    onToggle = { expanded[folder.id] = !isOpen },
                    onDelete = { viewModel.deleteFolder(folder.id) },
                    onNewNote = { title -> viewModel.createDocument(title, folder.id) { id -> openDocumentId = id } }
                )
                AnimatedVisibility(
                    visible = isOpen,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        notes.forEach { doc ->
                            key(doc.id) {
                                NoteTreeRow(
                                    document = doc,
                                    onOpen = { openDocumentId = doc.id },
                                    onDelete = { viewModel.deleteDocument(doc.id) },
                                    onRename = { title -> viewModel.renameDocument(doc.id, title) }
                                )
                            }
                        }
                        if (notes.isEmpty()) {
                            Text(
                                "No notes yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 64.dp, top = 4.dp, bottom = 10.dp)
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }

    if (showNewFolder) {
        NameDialog("New folder", "Folder name", onDismiss = { showNewFolder = false }) {
            viewModel.createFolder(it)
            showNewFolder = false
        }
    }

    if (showNewNote) {
        NewNoteDialog(
            folders = state.folders,
            onDismiss = { showNewNote = false },
            onCreate = { title, folderId ->
                viewModel.createDocument(title, folderId) { id -> openDocumentId = id }
                showNewNote = false
            }
        )
    }
}

@Composable
private fun FolderTreeRow(
    folder: Folder,
    expanded: Boolean,
    noteCount: Int,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onNewNote: (String) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 4.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            if (expanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            if (expanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            folder.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            noteCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Folder menu") }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text("New note") },
                onClick = { menu = false; showNew = true },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, null) }
            )
            DropdownMenuItem(
                text = { Text("Delete folder") },
                onClick = { menu = false; onDelete() },
                leadingIcon = { Icon(Icons.Default.Delete, null) }
            )
        }
    }
    if (showNew) {
        NameDialog("New note", "Note title", initial = "Untitled", onDismiss = { showNew = false }) {
            onNewNote(it)
            showNew = false
        }
    }
}

@Composable
private fun NoteTreeRow(
    document: Document,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(start = 34.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(22.dp))
        Icon(
            Icons.Default.Description,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            document.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { menu = true }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "Note menu", modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; showRename = true })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
        }
    }
    if (showRename) {
        NameDialog("Rename note", "Note title", initial = document.title, onDismiss = { showRename = false }) {
            onRename(it)
            showRename = false
        }
    }
}

@Composable
private fun NewNoteDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("Untitled") }
    var selectedFolderId by remember(folders) { mutableStateOf(folders.firstOrNull()?.id.orEmpty()) }
    var folderMenu by remember { mutableStateOf(false) }
    val selectedFolder = folders.firstOrNull { it.id == selectedFolderId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Box {
                    TextButton(onClick = { folderMenu = true }) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedFolder?.name ?: "Choose folder")
                    }
                    DropdownMenu(expanded = folderMenu, onDismissRequest = { folderMenu = false }) {
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = { selectedFolderId = folder.id; folderMenu = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && selectedFolderId.isNotBlank(),
                onClick = { onCreate(title.trim(), selectedFolderId) }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
            TextButton(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
