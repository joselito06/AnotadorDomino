package com.jbncode.anotadordomino.domain.model

data class AppLanguage(val tag: String, val nativeName: String)

val supportedLanguages = listOf(
    AppLanguage("en", "English"),
    AppLanguage("es", "Español"),
    AppLanguage("fr", "Français"),
    AppLanguage("pt", "Português")
    // ¡Solo tendrás que agregar una línea aquí en el futuro!
)
