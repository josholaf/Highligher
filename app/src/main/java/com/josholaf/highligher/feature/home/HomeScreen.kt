package com.josholaf.highligher.feature.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.domain.model.Document
import com.josholaf.highligher.feature.documents.DocumentScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val highlightCount: Int = 0,
    val documentCount: Int = 0,
    val folderCount: Int = 0,
    val recentDocuments: List<Document> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HighlightRepository
) : ViewModel() {
    val state: StateFlow<HomeUiState> = combine(
        repository.observeHighlightCount(),
        repository.observeDocuments(),
        repository.observeFolders()
    ) { highlights, docs, folders ->
        HomeUiState(
            highlightCount = highlights,
            documentCount = docs.size,
            folderCount = folders.size,
            recentDocuments = docs.take(8)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch { repository.ensureDefaultLibrary() }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var openDocumentId by remember { mutableStateOf<String?>(null) }

    if (openDocumentId != null) {
        DocumentScreen(
            documentId = openDocumentId!!,
            onBack = { openDocumentId = null }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Highligher", style = MaterialTheme.typography.headlineMedium)
            Text("Your local knowledge library", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Highlights", state.highlightCount.toString(), Modifier.weight(1f))
                StatCard("Documents", state.documentCount.toString(), Modifier.weight(1f))
                StatCard("Folders", state.folderCount.toString(), Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Recent documents", style = MaterialTheme.typography.titleMedium)
        }
        if (state.recentDocuments.isEmpty()) {
            item {
                Text("No documents yet. Capture text from Chrome or any app via the selection menu.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(state.recentDocuments, key = { it.id }) { doc ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { openDocumentId = doc.id }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(doc.title, style = MaterialTheme.typography.titleSmall)
                        Text(doc.filePath.substringAfterLast('/'), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
