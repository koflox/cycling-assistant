package com.koflox.locale.domain.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    JAPANESE("ja", "日本語"),
    ;

    companion object {
        val DEFAULT = ENGLISH
    }

}
