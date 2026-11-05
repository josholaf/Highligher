package com.josholaf.highligher.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.captureDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "highligher_capture"
)

data class LastDestination(
    val folderId: String? = null,
    val documentId: String? = null
)

@Singleton
class CapturePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val folderKey = stringPreferencesKey("last_folder_id")
    private val documentKey = stringPreferencesKey("last_document_id")

    val lastDestination: Flow<LastDestination> = context.captureDataStore.data.map { prefs ->
        LastDestination(
            folderId = prefs[folderKey],
            documentId = prefs[documentKey]
        )
    }

    suspend fun setLastDestination(folderId: String?, documentId: String?) {
        context.captureDataStore.edit { prefs ->
            if (folderId != null) prefs[folderKey] = folderId else prefs.remove(folderKey)
            if (documentId != null) prefs[documentKey] = documentId else prefs.remove(documentKey)
        }
    }
}
