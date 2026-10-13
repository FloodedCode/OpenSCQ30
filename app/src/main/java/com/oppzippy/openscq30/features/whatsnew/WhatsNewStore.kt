package com.oppzippy.openscq30.features.whatsnew

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhatsNewStore(context: Context) {
    private val preferences = context.getSharedPreferences("whats-new", Context.MODE_PRIVATE)

    private val _version2BreakingChangesMessageShown =
        MutableStateFlow(preferences.getBoolean("version2BreakingChangesMessageShown", true))
    val version2BreakingChangesMessageShown = _version2BreakingChangesMessageShown.asStateFlow()

    fun setVersion2BreakingChangesMessageShown(shown: Boolean) {
        _version2BreakingChangesMessageShown.value = shown
        preferences.edit { putBoolean("version2BreakingChangesMessageShown", shown) }
    }
}
