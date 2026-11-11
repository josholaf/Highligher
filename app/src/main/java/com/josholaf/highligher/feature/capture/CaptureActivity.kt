package com.josholaf.highligher.feature.capture

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.josholaf.highligher.R
import com.josholaf.highligher.core.design.HighligherTheme
import com.josholaf.highligher.core.design.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CaptureActivity : ComponentActivity() {

    private val viewModel: CaptureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedText = intent
            ?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            ?.toString()
            ?.trim()
            .orEmpty()

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
                        onSaved = {
                            Toast.makeText(
                                this,
                                getString(R.string.capture_saved_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}
