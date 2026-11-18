package com.josholaf.highligher.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.josholaf.highligher.feature.widget.HighlightWidgetUpdater

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val folderName = state.folders.firstOrNull { it.id == state.selectedFolderId }?.name
    val noteTitle = state.documents.firstOrNull { it.id == state.selectedDocumentId }?.title

    LaunchedEffect(state.saved) {
        if (state.saved) {
            HighlightWidgetUpdater.update(
                context,
                state.selectedText,
                folderName ?: "",
                noteTitle ?: ""
            )
            kotlinx.coroutines.delay(1100)
            onSaved()
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .fillMaxHeight(0.9f)
            ) {
                if (state.saved) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "\u2713",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Saved",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (folderName != null && noteTitle != null)
                                "Added to $folderName / $noteTitle"
                            else
                                "Saved on this device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Save selection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }

                    HighlightSaveWidget(
                        highlightedText = state.selectedText,
                        folders = state.folders,
                        documents = state.documents,
                        selectedFolderId = state.selectedFolderId,
                        selectedDocumentId = state.selectedDocumentId,
                        onSelectFolder = viewModel::selectFolder,
                        onSelectDocument = viewModel::selectDocument,
                        onCreateFolder = viewModel::createFolderAndSelect,
                        onCreateNote = viewModel::createDocumentAndSelect
                    )

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text("Personal note") },
                        placeholder = { Text("Add a personal note\u2026") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = state.sourceUrl,
                        onValueChange = viewModel::updateSource,
                        label = { Text("Source (optional)") },
                        placeholder = { Text("https://\u2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving && state.selectedDocumentId != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            val folder = state.folders.firstOrNull { it.id == state.selectedFolderId }?.name
                            val note = state.documents.firstOrNull { it.id == state.selectedDocumentId }?.title
                            Text(
                                if (folder != null && note != null) "Save to $folder / $note"
                                else "Save"
                            )
                        }
                    }
                }
                }
            }
        }
    }
}
