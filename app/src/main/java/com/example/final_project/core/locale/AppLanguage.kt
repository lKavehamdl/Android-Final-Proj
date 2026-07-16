package com.example.final_project.core.locale

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    PERSIAN("fa"),
    ;

    companion object {
        fun fromTag(tag: String): AppLanguage =
            entries.find { it.tag == tag } ?: ENGLISH
    }
}
