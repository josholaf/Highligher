package com.josholaf.highligher.feature.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.datastore.ThemeMode
import com.josholaf.highligher.core.datastore.ThemePreferences
import com.josholaf.highligher.data.repository.HighlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val repository: HighlightRepository
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePreferences.setThemeMode(mode) }
    }

    fun storageBytes(): Long = repository.storageUsageBytes()

    suspend fun exportAllMarkdown(): String = withContext(Dispatchers.IO) {
        val root = repository.libraryRoot
        if (!root.exists()) return@withContext ""
        buildString {
            root.walkTopDown()
                .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
                .sortedBy { it.absolutePath }
                .forEach { file ->
                    appendLine()
                    appendLine("<!-- file: ${file.relativeTo(root).path} -->")
                    appendLine()
                    appendLine(file.readText().trimEnd())
                    appendLine()
                    appendLine("---")
                }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storage = remember { viewModel.storageBytes() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            listOf(
                ThemeMode.SYSTEM to "System",
                ThemeMode.LIGHT to "Light",
                ThemeMode.DARK to "Dark"
            ).forEach { (mode, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) }
                    )
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Text("Storage", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Local library ≈ %.1f KB".format(storage / 1024.0),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                scope.launch {
                    val md = viewModel.exportAllMarkdown()
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Highligher export")
                        putExtra(Intent.EXTRA_TEXT, md.ifBlank { "(empty library)" })
                    }
                    context.startActivity(Intent.createChooser(send, "Export Markdown"))
                }
            }
        ) { Text("Export all Markdown") }

        Spacer(Modifier.height(24.dp))
        Text(
            "Highligher is offline-first. Core capture never needs the network.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
