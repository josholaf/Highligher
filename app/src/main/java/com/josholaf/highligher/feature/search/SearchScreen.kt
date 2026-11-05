package com.josholaf.highligher.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.data.repository.HighlightRepository
import com.josholaf.highligher.data.repository.HighlightSearchResult
import com.josholaf.highligher.feature.documents.DocumentScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: HighlightRepository
) : ViewModel() {
    private val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<HighlightSearchResult>> = query
        .flatMapLatest { q -> repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { query.value = q }
}

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    var text by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState()
    var openDocumentId by remember { mutableStateOf<String?>(null) }

    if (openDocumentId != null) {
        DocumentScreen(documentId = openDocumentId!!, onBack = { openDocumentId = null })
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Search", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; viewModel.setQuery(it) },
            label = { Text("Search highlights, notes, sources") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results, key = { it.highlight.id }) { result ->
                Card(modifier = Modifier.fillMaxWidth().clickable { openDocumentId = result.highlight.documentId }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(listOfNotNull(result.folderName, result.documentTitle).joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(result.highlight.selectedText.take(200), style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic))
                        result.highlight.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}
