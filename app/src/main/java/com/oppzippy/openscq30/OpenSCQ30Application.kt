package com.oppzippy.openscq30

import android.app.Application
import android.content.Context
import android.content.Intent
import com.oppzippy.openscq30.android.AndroidIntentFactory
import com.oppzippy.openscq30.features.autoconnect.AutoConnectService
import com.oppzippy.openscq30.features.equalizer.storage.LegacyEqualizerProfileDao
import com.oppzippy.openscq30.features.preferences.Preferences
import com.oppzippy.openscq30.features.statusnotification.storage.FeaturedSettingSlotDao
import com.oppzippy.openscq30.features.statusnotification.storage.QuickPresetSlotDao
import com.oppzippy.openscq30.features.whatsnew.Version2BreakingChangesMessage
import com.oppzippy.openscq30.lib.bindings.OpenScq30Session
import com.oppzippy.openscq30.lib.soundcore.SoundcoreSessionImpl
import com.oppzippy.openscq30.room.AppDatabase
import com.oppzippy.openscq30.ui.utils.ToastHandler

class AppContainer(val context: Context) {
    val preferences: Preferences by lazy { Preferences(context) }
    val appDatabase: AppDatabase by lazy { AppDatabase.getInstance(context) }
    val quickPresetSlotDao: QuickPresetSlotDao by lazy { appDatabase.quickPresetSlotDao() }
    val featuredSettingSlotDao: FeaturedSettingSlotDao by lazy { appDatabase.featuredSettingSlotDao() }
    val legacyEqualizerProfileDao: LegacyEqualizerProfileDao by lazy { appDatabase.legacyEqualizerProfileDao() }
    val session: OpenScq30Session by lazy { SoundcoreSessionImpl(context) }
    val intentFactory: AndroidIntentFactory by lazy { AndroidIntentFactory() }
    val toastHandler: ToastHandler by lazy { ToastHandler() }
    val version2BreakingChangesMessage: Version2BreakingChangesMessage by lazy { Version2BreakingChangesMessage(context) }
}

class OpenSCQ30Application : Application() {

    companion object {
        lateinit var instance: OpenSCQ30Application
            private set
        val container: AppContainer get() = instance.appContainer
    }

    lateinit var appContainer: AppContainer
        private set

    val preferences: Preferences get() = appContainer.preferences

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContainer = AppContainer(this)
        Native.initialize()

        if (preferences.autoConnect) {
            startService(Intent(this, AutoConnectService::class.java))
        }
    }
}
