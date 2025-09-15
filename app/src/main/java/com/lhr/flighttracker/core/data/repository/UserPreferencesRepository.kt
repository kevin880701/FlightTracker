package com.lhr.flighttracker.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 負責所有使用者偏好設定的資料存取。
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val HAS_SHOWN_AUTOSTART_GUIDE = booleanPreferencesKey("has_shown_autostart_guide")
    }

    // --- 主題 Theme ---
    val theme: Flow<String> = dataStore.data.map { it[Keys.APP_THEME] ?: "light" }
    suspend fun setTheme(theme: String) {
        dataStore.edit { it[Keys.APP_THEME] = theme }
    }

    // --- 語言 Language ---
    val language: Flow<String?> = dataStore.data.map { it[Keys.APP_LANGUAGE] }
    suspend fun setLanguage(language: String) {
        dataStore.edit { it[Keys.APP_LANGUAGE] = language }
    }

    // --- 權限引導 Permission Guide ---
    val hasShownAutostartGuide: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_SHOWN_AUTOSTART_GUIDE] ?: false }
    suspend fun setAutostartGuideShown(hasShown: Boolean) {
        dataStore.edit { it[Keys.HAS_SHOWN_AUTOSTART_GUIDE] = hasShown }
    }
}