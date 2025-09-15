package com.lhr.flighttracker.features.flightScheduled.domain.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocalizedString(
    val chinese: String,
    val english: String,
    val japanese: String?,
    val korean: String?
) {
    /**
     *
     * @param languageCode 語言代碼，例如 "zh-rTW", "en", "ja", "ko"。
     * @return 對應語言的字串。如果指定語言的翻譯不存在，會優先使用英文作為預設值。
     */
    fun get(languageCode: String): String {
        return when {
            languageCode.startsWith("zh") -> chinese
            languageCode.startsWith("ja") -> japanese ?: english
            languageCode.startsWith("ko") -> korean ?: english
            languageCode.startsWith("en") -> english
            else -> english
        }
    }
}
