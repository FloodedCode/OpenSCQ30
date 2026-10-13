package com.oppzippy.openscq30.features.whatsnew

import android.content.Context
import com.oppzippy.openscq30.features.equalizer.storage.LegacyEqualizerProfileDao
import com.oppzippy.openscq30.room.AppDatabase
import kotlinx.coroutines.flow.map

class Version2BreakingChangesMessage(
    val store: WhatsNewStore,
    legacyEqualizerProfileDao: LegacyEqualizerProfileDao,
) {
    constructor(context: Context) : this(
        store = WhatsNewStore(context),
        legacyEqualizerProfileDao = AppDatabase.getInstance(context).legacyEqualizerProfileDao()
    )

    val shouldShow =
        store.version2BreakingChangesMessageShown.map { shown -> !shown && legacyEqualizerProfileDao.count() != 0 }

    fun setShown() = store.setVersion2BreakingChangesMessageShown(true)
}
