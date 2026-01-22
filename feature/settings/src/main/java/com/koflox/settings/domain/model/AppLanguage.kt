package com.koflox.settings.domain.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    JAPANESE("ja", "日本語"),
}
