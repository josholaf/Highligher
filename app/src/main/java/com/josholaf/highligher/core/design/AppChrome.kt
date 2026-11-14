package com.josholaf.highligher.core.design

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * Lets a full-screen surface (the note editor) hide the app's bottom navigation
 * so the editor toolbar can sit directly above the keyboard.
 */
val LocalBottomBarVisible = compositionLocalOf<MutableState<Boolean>> { mutableStateOf(true) }
