package com.josholaf.highligher.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    var folderExpanded by remember { mutableStateOf(false) }
    var documentExpanded by remember { mutableStateOf(false) }
    var newFolder by remember { mutableStateOf(false) }
    var newDoc by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }

    val selectedFolder = state.folders.firstOrNull { it.id == state.selectedFolderId }
    val folderDocs = state.documents.filter { it.folderId == state.selectedFolderId }
    val selectedDoc = folderDocs.firstOrNull { it.id == state.selectedDocumentId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capture") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Selected text", style = MaterialTheme.typography.labelLarge)
            Text(
                state.selectedText,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Your note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = state.sourceUrl,
                onValueChange = viewModel::updateSource,
                label = { Text("Source URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Folder", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(expanded = folderExpanded, onExpandedChange = { folderExpanded = it }) {
                OutlinedTextField(
                    value = selectedFolder?.name ?: "Choose folder",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(folderExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = folderExpanded, onDismissRequest = { folderExpanded = false }) {
                    state.folders.forEach { f ->
                        DropdownMenuItem(text = { Text(f.name) }, onClick = {
                            viewModel.selectFolder(f.id)
                            folderExpanded = false
                        })
                    }
                    DropdownMenuItem(text = { Text("+ New folder") }, onClick = {
                        folderExpanded = false
                        nameDraft = ""
                        newFolder = true
                    })
                }
            }

            Text("Document", style = MaterialTheme.typography.labelLarge)
            ExposedDropdownMenuBox(expanded = documentExpanded, onExpandedChange = { documentExpanded = it }) {
                OutlinedTextField(
                    value = selectedDoc?.title ?: "Choose document",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(documentExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = documentExpanded, onDismissRequest = { documentExpanded = false }) {
                    folderDocs.forEach { d ->
                        DropdownMenuItem(text = { Text(d.title) }, onClick = {
                            viewModel.selectDocument(d.id)
                            documentExpanded = false
                        })
                    }
                    DropdownMenuItem(text = { Text("+ New document") }, onClick = {
                        documentExpanded = false
                        nameDraft = ""
                        newDoc = true
                    })
                }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving && state.selectedDocumentId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.height(20.dp))
                else Text("Save highlight")
            }
        }
    }

    if (newFolder) {
        NamePrompt("New folder", nameDraft, { nameDraft = it }, { newFolder = false }) {
            if (nameDraft.isNotBlank()) viewModel.createFolderAndSelect(nameDraft.trim())
            newFolder = false
        }
    }
    if (newDoc) {
        NamePrompt("New document", nameDraft, { nameDraft = it }, { newDoc = false }) {
            if (nameDraft.isNotBlank()) viewModel.createDocumentAndSelect(nameDraft.trim())
            newDoc = false
        }
    }
}

@Composable
private fun NamePrompt(
    title: String,
    value: String,
    onChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = value, onValueChange = onChange, singleLine = true, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
