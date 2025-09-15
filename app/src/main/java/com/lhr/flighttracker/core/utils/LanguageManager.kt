package com.lhr.flighttracker.core.utils

import android.content.Context
import com.lhr.flighttracker.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    // 改為 suspend 函式，避免阻塞
    suspend fun getLanguageCode(): String {
        return userPreferencesRepository.language.first() ?: "zh-rTW"
    }

    fun getLanguageCodeFlow(): Flow<String> {
        return userPreferencesRepository.language.map { it ?: "zh-rTW" }
    }

    suspend fun setLanguage(languageCode: String) {
        userPreferencesRepository.setLanguage(languageCode)
    }

    fun applyLanguage(context: Context): Context {
        val languageCode = runBlocking { getLanguageCode() }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}