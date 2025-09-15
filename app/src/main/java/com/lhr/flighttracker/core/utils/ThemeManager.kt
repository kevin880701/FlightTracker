package com.lhr.flighttracker.core.utils

import com.lhr.flighttracker.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val appTheme: Flow<String> = userPreferencesRepository.theme

    suspend fun setTheme(themeCode: String) {
        userPreferencesRepository.setTheme(themeCode)
    }
}