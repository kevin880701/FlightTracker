package com.lhr.flighttracker.core.room.convert

import androidx.room.TypeConverter
import com.lhr.flighttracker.features.flightScheduled.domain.entity.LocalizedString
import com.squareup.moshi.Moshi

class Converters {

    private val moshi = Moshi.Builder().build()
    private val localizedStringAdapter = moshi.adapter(LocalizedString::class.java)

    @TypeConverter
    fun fromLocalizedString(value: LocalizedString?): String? {
        return value?.let { localizedStringAdapter.toJson(it) }
    }

    @TypeConverter
    fun toLocalizedString(json: String?): LocalizedString? {
        return json?.let { localizedStringAdapter.fromJson(it) }
    }
}