package com.josholaf.highligher.feature.capture

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.josholaf.highligher.core.design.HighligherTheme
import com.josholaf.highligher.core.design.ThemeViewModel
import com.josholaf.highligher.core.markdown.SpannedMarkdown
import dagger.hilt.android.AndroidEntryPoint

/**
 * Entry point for ACTION_PROCESS_TEXT.
 * Converts Spanned selection (when the source provides style spans) to Markdown
 * so headings/bold/italic land in the note without extra user action.
 */
@AndroidEntryPoint
class CaptureActivity : ComponentActivity() {

    private val viewModel: CaptureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val raw = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        val selectedText = SpannedMarkdown.toMarkdown(raw)

        if (selectedText.isBlank()) {
            finish()
            return
        }

        viewModel.setSelectedText(selectedText)

        setContent {
            val themeVm: ThemeViewModel = hiltViewModel()
            val themeMode by themeVm.themeMode.collectAsState()
            HighligherTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CaptureScreen(
                        viewModel = viewModel,
                        onSaved = { finish() },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}
