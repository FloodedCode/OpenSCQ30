package com.oppzippy.openscq30

import com.oppzippy.openscq30.lib.bindings.LanguageIdentifier
import com.oppzippy.openscq30.lib.bindings.initNativeI18n
import com.oppzippy.openscq30.lib.bindings.initNativeLogging

object Native {
    fun initialize() {
        initNativeLogging()
    }

    fun initializeI18n(languageIdentifiers: List<LanguageIdentifier>) {
        initNativeI18n(languageIdentifiers)
    }
}
