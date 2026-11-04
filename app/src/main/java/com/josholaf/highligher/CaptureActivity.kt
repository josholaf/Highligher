package com.josholaf.highligher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.josholaf.highligher.ui.theme.HighligherTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.Instant
import java.util.UUID

@AndroidEntryPoint
class CaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        var note by mutableStateOf("")
        var saved by mutableStateOf(false)

        setContent {
            HighligherTheme {
                Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                    Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                        Text(
                            text = "Highlight",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Note (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                appendHighlight(selectedText, note.ifBlank { null })
                                saved = true
                                setResult(Activity.RESULT_OK)
                                finish()
                            },
                            enabled = !saved && selectedText.isNotBlank(),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(if (saved) "Saved" else "Save highlight")
                        }
                    }
                }
            }
        }
    }

    private fun appendHighlight(text: String, note: String?) {
        val dir = File(filesDir, "documents").apply { mkdirs() }
        val file = File(dir, "highlights.md")
        val id = UUID.randomUUID().toString()
        val block = buildString {
            append("\n<!-- highlight:").append(id).append(" -->\n")
            append("> ").append(text.replace("\n", "\n> ")).append("\n")
            note?.let { append("\n").append(it).append("\n") }
            append("_Captured ").append(Instant.now()).append("_\n")
        }
        file.appendText(block)
    }
}
