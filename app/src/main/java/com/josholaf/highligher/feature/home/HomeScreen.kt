package com.josholaf.highligher.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.datastore.CapturePreferences
import com.josholaf.highligher.core.icons.IconPickerDialog
import com.josholaf.highligher.core.icons.rememberFolderIcon
import com.josholaf.highligher.data.repository.FolderHit
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.data.repository.LibrarySearch
import com.josholaf.highligher.data.repository.NoteHit
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Folder
import com.josholaf.highligher.feature.documents.DocumentScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val folders: List<Folder> = emptyList(),
    val documents: List<Document> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HighlightRepository,
    private val preferences: CapturePreferences
) : ViewModel() {
    private val query = MutableStateFlow("")

    val recentSearches: StateFlow<List<String>> = preferences.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<HomeUiState> = combine(
        repository.observeFolders(),
        repository.observeDocuments()
    ) { folders, docs -> HomeUiState(folders, docs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<LibrarySearch> = query
        .flatMapLatest { q -> repository.searchAll(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibrarySearch())

    fun setQuery(q: String) { query.value = q.trim() }

    fun recordSearch(term: String) {
        if (term.isBlank()) return
        viewModelScope.launch { preferences.addRecentSearch(term) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { preferences.clearRecentSearches() }
    }

    fun openFolder(folderId: String, onOpened: (String) -> Unit) {
        viewModelScope.launch {
            val folderNote = repository.getOrCreateFolderNote(folderId)
            onOpened(folderNote.id)
        }
    }

    fun createFolder(onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val folder = repository.createFolder("Untitled")
            onCreated(folder.id)
        }
    }

    fun createNote(folderId: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val doc = repository.createBlankNote("Untitled", folderId)
            onCreated(doc.id)
        }
    }

    fun createNoteAnywhere(onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val folder = repository.observeFolders().first().firstOrNull()
                ?: repository.createFolder("Untitled")
            val doc = repository.createBlankNote("Untitled", folder.id)
            onCreated(doc.id)
        }
    }

    fun deleteFolder(id: String) { viewModelScope.launch { repository.deleteFolder(id) } }
    fun deleteDocument(id: String) { viewModelScope.launch { repository.deleteDocument(id) } }
    fun renameDocument(id: String, title: String) { viewModelScope.launch { repository.renameDocument(id, title) } }
    fun renameFolder(id: String, name: String) { viewModelScope.launch { repository.renameFolder(id, name) } }
    fun setFolderIcon(id: String, iconName: String?) { viewModelScope.launch { repository.setFolderIcon(id, iconName) } }

    init {
        viewModelScope.launch { repository.ensureDefaultLibrary() }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val results by viewModel.results.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var openDocumentId by remember { mutableStateOf<String?>(null) }
    var showCreateMenu by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    var renameFolder by remember { mutableStateOf<Folder?>(null) }
    var iconFolder by remember { mutableStateOf<Folder?>(null) }
    var renameDoc by remember { mutableStateOf<Document?>(null) }

    if (openDocumentId != null) {
        DocumentScreen(
            documentId = openDocumentId!!,
            onBack = { openDocumentId = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Highligher", style = MaterialTheme.typography.titleLarge)
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
                DropdownMenu(expanded = showCreateMenu, onDismissRequest = { showCreateMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("New note") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                        onClick = {
                            showCreateMenu = false
                            viewModel.createNoteAnywhere { id -> openDocumentId = id }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New folder") },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                        onClick = { showCreateMenu = false; viewModel.createFolder() }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.setQuery(it)
                },
                label = { Text("Search folders, notes and highlights") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.recordSearch(searchText) })
            )

            if (searchText.isNotBlank()) {
                SearchResults(
                    results = results,
                    searchText = searchText,
                    onRecordSearch = { viewModel.recordSearch(searchText) },
                    onOpenFolder = { folderId -> viewModel.openFolder(folderId) { id -> openDocumentId = id } },
                    onOpenNote = { id -> openDocumentId = id }
                )
            } else {
                if (recentSearches.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { term ->
                            AssistChip(
                                onClick = { searchText = term; viewModel.setQuery(term) },
                                label = { Text(term) }
                            )
                        }
                        AssistChip(onClick = { viewModel.clearRecentSearches() }, label = { Text("Clear") })
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (state.folders.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Text("Your library starts here", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Create a folder to organise your notes and highlights.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            onRename = { renameFolder = folder },
                            onEditIcon = { iconFolder = folder },
                            onDelete = { viewModel.deleteFolder(folder.id) },
                            onNewNote = { viewModel.createNote(folder.id) { id -> openDocumentId = id } }
                        )
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isOpen,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Column {
                                notes.forEach { doc ->
                                    key(doc.id) {
                                        NoteTreeRow(
                                            document = doc,
                                            onOpen = { openDocumentId = doc.id },
                                            onDelete = { viewModel.deleteDocument(doc.id) },
                                            onRename = { renameDoc = doc }
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
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }

    renameFolder?.let { folder ->
        NameDialog("Rename folder", "Folder name", initial = folder.name, onDismiss = { renameFolder = null }) {
            viewModel.renameFolder(folder.id, it)
            renameFolder = null
        }
    }

    renameDoc?.let { doc ->
        NameDialog("Rename note", "Note title", initial = doc.title, onDismiss = { renameDoc = null }) {
            viewModel.renameDocument(doc.id, it)
            renameDoc = null
        }
    }

    iconFolder?.let { folder ->
        IconPickerDialog(
            currentIconName = folder.iconName,
            onDismiss = { iconFolder = null },
            onPick = { name ->
                viewModel.setFolderIcon(folder.id, name)
                iconFolder = null
            }
        )
    }
}

@Composable
private fun SearchResults(
    results: LibrarySearch,
    searchText: String,
    onRecordSearch: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenNote: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (results.isEmpty) {
            item { Text("No matches found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (results.folders.isNotEmpty()) {
            item { Text("Folders", style = MaterialTheme.typography.titleSmall) }
            items(results.folders, key = { "folder-" + it.folder.id }) { hit ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenFolder(hit.folder.id) }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(rememberFolderIcon(hit.folder.iconName), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(hit.folder.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(hit.noteCount.toString() + " notes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (results.notes.isNotEmpty()) {
            item { Text("Notes", style = MaterialTheme.typography.titleSmall) }
            items(results.notes, key = { "note-" + it.document.id }) { hit ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenNote(hit.document.id) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(hit.document.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            hit.folderName?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        hit.snippet?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        if (results.highlights.isNotEmpty()) {
            item { Text("Highlights", style = MaterialTheme.typography.titleSmall) }
            items(results.highlights, key = { "hl-" + it.highlight.id }) { result ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable {
                        onRecordSearch()
                        onOpenNote(result.highlight.documentId)
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            listOfNotNull(result.folderName, result.documentTitle).joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(result.highlight.selectedText.take(200), style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic))
                        result.highlight.note?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderTreeRow(
    folder: Folder,
    expanded: Boolean,
    noteCount: Int,
    onToggle: () -> Unit,
    onRename: () -> Unit,
    onEditIcon: () -> Unit,
    onDelete: () -> Unit,
    onNewNote: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggle, onLongClick = { menu = true })
            .padding(horizontal = 4.dp, vertical = 7.dp),
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
            if (expanded) Icons.Default.FolderOpen else rememberFolderIcon(folder.iconName),
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
                text = { Text("Rename") },
                onClick = { menu = false; onRename() },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
            DropdownMenuItem(
                text = { Text("Edit icon") },
                onClick = { menu = false; onEditIcon() },
                leadingIcon = { Icon(Icons.Default.Brush, null) }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { menu = false; onDelete() },
                leadingIcon = { Icon(Icons.Default.Delete, null) }
            )
        }
    }
    if (showNew) {
        NameDialog("New note", "Note title", initial = "Untitled", onDismiss = { showNew = false }) {
            onNewNote()
            showNew = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteTreeRow(
    document: Document,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpen, onLongClick = { menu = true })
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
            DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; onRename() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
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
            TextButton(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
