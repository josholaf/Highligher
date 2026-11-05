package com.josholaf.highligher.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.domain.model.Folder

/** Shows highlighted text (with copy) and save path: Folder → Note. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightSaveWidget(
    highlightedText: String,
    folders: List<Folder>,
    documents: List<Document>,
    selectedFolderId: String?,
    selectedDocumentId: String?,
    onSelectFolder: (String) -> Unit,
    onSelectDocument: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onCreateNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedFolder = folders.firstOrNull { it.id == selectedFolderId }
    val folderDocs = documents.filter { it.folderId == selectedFolderId }
    val selectedDoc = folderDocs.firstOrNull { it.id == selectedDocumentId }
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    var folderExpanded by remember { mutableStateOf(false) }
    var noteExpanded by remember { mutableStateOf(false) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showNewNote by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }

    fun copyText() {
        if (highlightedText.isNotBlank()) {
            clipboard.setText(AnnotatedString(highlightedText))
            copied = true
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Highlighted text",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = ::copyText, enabled = highlightedText.isNotBlank()) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (copied) "Copied" else "Copy")
            }
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Spacer(
                    Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    highlightedText.ifBlank { "(no text)" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = ::copyText, enabled = highlightedText.isNotBlank()) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy highlighted text")
                }
            }
        }

        Text("Save to", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        selectedFolder?.name ?: "Folder",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        selectedDoc?.title ?: "Note",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text("Folder", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = folderExpanded, onExpandedChange = { folderExpanded = it }) {
                    OutlinedTextField(
                        value = selectedFolder?.name ?: "Choose folder",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Folder, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(folderExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = folderExpanded, onDismissRequest = { folderExpanded = false }) {
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = { onSelectFolder(folder.id); folderExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Folder, null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("New folder…") },
                            onClick = { folderExpanded = false; nameDraft = ""; showNewFolder = true },
                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, null) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Note", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(expanded = noteExpanded, onExpandedChange = { noteExpanded = it }) {
                    OutlinedTextField(
                        value = selectedDoc?.title ?: "Choose note",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(noteExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        singleLine = true,
                        enabled = selectedFolderId != null
                    )
                    ExposedDropdownMenu(expanded = noteExpanded, onDismissRequest = { noteExpanded = false }) {
                        if (folderDocs.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No notes in this folder yet", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { noteExpanded = false },
                                enabled = false
                            )
                        }
                        folderDocs.forEach { doc ->
                            DropdownMenuItem(
                                text = { Text(doc.title) },
                                onClick = { onSelectDocument(doc.id); noteExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Description, null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("New note…") },
                            onClick = { noteExpanded = false; nameDraft = ""; showNewNote = true },
                            leadingIcon = { Icon(Icons.Default.NoteAdd, null) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { nameDraft = ""; showNewFolder = true }) {
                        Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New folder")
                    }
                    TextButton(onClick = { nameDraft = ""; showNewNote = true }, enabled = selectedFolderId != null) {
                        Icon(Icons.Default.NoteAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New note")
                    }
                }
            }
        }
    }

    if (showNewFolder) {
        NameDialog("New folder", "Folder name", nameDraft, { nameDraft = it }, { showNewFolder = false }) {
            if (nameDraft.isNotBlank()) onCreateFolder(nameDraft.trim())
            showNewFolder = false
        }
    }
    if (showNewNote) {
        NameDialog("New note", "Note title", nameDraft, { nameDraft = it }, { showNewNote = false }) {
            if (nameDraft.isNotBlank()) onCreateNote(nameDraft.trim())
            showNewNote = false
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
