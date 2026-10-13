package com.oppzippy.openscq30.room

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.oppzippy.openscq30.features.equalizer.storage.LegacyEqualizerProfile
import com.oppzippy.openscq30.features.equalizer.storage.LegacyEqualizerProfileDao
import com.oppzippy.openscq30.features.statusnotification.storage.FeaturedSettingSlot
import com.oppzippy.openscq30.features.statusnotification.storage.FeaturedSettingSlotDao
import com.oppzippy.openscq30.features.statusnotification.storage.QuickPresetSlot
import com.oppzippy.openscq30.features.statusnotification.storage.QuickPresetSlotDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

abstract class AppDatabase {
    abstract fun equalizerCustomProfileDao(): LegacyEqualizerProfileDao
    abstract fun legacyEqualizerProfileDao(): LegacyEqualizerProfileDao
    abstract fun quickPresetSlotDao(): QuickPresetSlotDao
    abstract fun featuredSettingSlotDao(): FeaturedSettingSlotDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: SqliteAppDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}

class SqliteAppDatabase(context: Context) : AppDatabase() {

    private val dbHelper = DatabaseHelper(context)

    private val quickPresetSlotsChanged = MutableStateFlow(0)
    private val featuredSettingSlotsChanged = MutableStateFlow(0)
    private val equalizerProfilesChanged = MutableStateFlow(0)

    private val _quickPresetSlotDao = object : QuickPresetSlotDao() {
        override fun all(deviceModel: String): Flow<List<QuickPresetSlot>> =
            quickPresetSlotsChanged.map {
                querySlots(deviceModel)
            }

        private fun querySlots(deviceModel: String): List<QuickPresetSlot> {
            val list = mutableListOf<QuickPresetSlot>()
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT deviceModel, slotIndex, name FROM QuickPresetSlot WHERE deviceModel = ?",
                arrayOf(deviceModel)
            )
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        QuickPresetSlot(
                            deviceModel = it.getString(0),
                            slotIndex = it.getInt(1),
                            name = it.getString(2)
                        )
                    )
                }
            }
            return list
        }

        override suspend fun upsert(quickPresetSlot: QuickPresetSlot) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val cv = ContentValues().apply {
                put("deviceModel", quickPresetSlot.deviceModel)
                put("slotIndex", quickPresetSlot.slotIndex)
                put("name", quickPresetSlot.name)
            }
            db.insertWithOnConflict("QuickPresetSlot", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            quickPresetSlotsChanged.value += 1
        }

        override suspend fun delete(deviceModel: String, index: Int) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("QuickPresetSlot", "deviceModel = ? AND slotIndex = ?", arrayOf(deviceModel, index.toString()))
            quickPresetSlotsChanged.value += 1
        }
    }

    private val _featuredSettingSlotDao = object : FeaturedSettingSlotDao() {
        override fun all(deviceModel: String): Flow<List<FeaturedSettingSlot>> =
            featuredSettingSlotsChanged.map {
                querySlots(deviceModel)
            }

        private fun querySlots(deviceModel: String): List<FeaturedSettingSlot> {
            val list = mutableListOf<FeaturedSettingSlot>()
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT deviceModel, slotIndex, settingId FROM FeaturedSettingSlot WHERE deviceModel = ?",
                arrayOf(deviceModel)
            )
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        FeaturedSettingSlot(
                            deviceModel = it.getString(0),
                            slotIndex = it.getInt(1),
                            settingId = it.getString(2)
                        )
                    )
                }
            }
            return list
        }

        override suspend fun upsert(featuredSettingSlot: FeaturedSettingSlot) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val cv = ContentValues().apply {
                put("deviceModel", featuredSettingSlot.deviceModel)
                put("slotIndex", featuredSettingSlot.slotIndex)
                put("settingId", featuredSettingSlot.settingId)
            }
            db.insertWithOnConflict("FeaturedSettingSlot", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            featuredSettingSlotsChanged.value += 1
        }

        override suspend fun delete(deviceModel: String, index: Int) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("FeaturedSettingSlot", "deviceModel = ? AND slotIndex = ?", arrayOf(deviceModel, index.toString()))
            featuredSettingSlotsChanged.value += 1
        }
    }

    private val _legacyEqualizerProfileDao = object : LegacyEqualizerProfileDao() {
        override suspend fun count(): Int = withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT COUNT(*) FROM custom_equalizer_profile", null)
            cursor.use {
                if (it.moveToNext()) it.getInt(0) else 0
            }
        }

        override suspend fun getAll(): List<LegacyEqualizerProfile> = withContext(Dispatchers.IO) {
            queryProfiles()
        }

        override fun all(): Flow<List<LegacyEqualizerProfile>> =
            equalizerProfilesChanged.map { queryProfiles() }

        private fun queryProfiles(): List<LegacyEqualizerProfile> {
            val list = mutableListOf<LegacyEqualizerProfile>()
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM custom_equalizer_profile", null)
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        LegacyEqualizerProfile(
                            name = it.getString(0),
                            band100 = it.getInt(1),
                            band200 = it.getInt(2),
                            band400 = it.getInt(3),
                            band800 = it.getInt(4),
                            band1600 = it.getInt(5),
                            band3200 = it.getInt(6),
                            band6400 = it.getInt(7),
                            band12800 = it.getInt(8)
                        )
                    )
                }
            }
            return list
        }

        override suspend fun get(name: String): LegacyEqualizerProfile? = withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM custom_equalizer_profile WHERE name = ?", arrayOf(name))
            cursor.use {
                if (it.moveToNext()) {
                    LegacyEqualizerProfile(
                        name = it.getString(0),
                        band100 = it.getInt(1),
                        band200 = it.getInt(2),
                        band400 = it.getInt(3),
                        band800 = it.getInt(4),
                        band1600 = it.getInt(5),
                        band3200 = it.getInt(6),
                        band6400 = it.getInt(7),
                        band12800 = it.getInt(8)
                    )
                } else null
            }
        }

        override suspend fun insert(customProfile: LegacyEqualizerProfile) = withContext(Dispatchers.IO) {
            insertInternal(customProfile, SQLiteDatabase.CONFLICT_ABORT)
        }

        override suspend fun insertIgnoreConflicts(customProfile: LegacyEqualizerProfile) = withContext(Dispatchers.IO) {
            insertInternal(customProfile, SQLiteDatabase.CONFLICT_IGNORE)
        }

        override suspend fun upsert(customProfile: LegacyEqualizerProfile) = withContext(Dispatchers.IO) {
            insertInternal(customProfile, SQLiteDatabase.CONFLICT_REPLACE)
        }

        override suspend fun upsertAll(customProfile: List<LegacyEqualizerProfile>) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                for (p in customProfile) {
                    insertInternal(p, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            equalizerProfilesChanged.value += 1
        }

        private fun insertInternal(p: LegacyEqualizerProfile, conflict: Int) {
            val db = dbHelper.writableDatabase
            val cv = ContentValues().apply {
                put("name", p.name)
                put("band100", p.band100)
                put("band200", p.band200)
                put("band400", p.band400)
                put("band800", p.band800)
                put("band1600", p.band1600)
                put("band3200", p.band3200)
                put("band6400", p.band6400)
                put("band12800", p.band12800)
            }
            db.insertWithOnConflict("custom_equalizer_profile", null, cv, conflict)
            equalizerProfilesChanged.value += 1
        }

        override suspend fun delete(name: String) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("custom_equalizer_profile", "name = ?", arrayOf(name))
            equalizerProfilesChanged.value += 1
        }
    }

    override fun equalizerCustomProfileDao(): LegacyEqualizerProfileDao = _legacyEqualizerProfileDao
    override fun legacyEqualizerProfileDao(): LegacyEqualizerProfileDao = _legacyEqualizerProfileDao
    override fun quickPresetSlotDao(): QuickPresetSlotDao = _quickPresetSlotDao
    override fun featuredSettingSlotDao(): FeaturedSettingSlotDao = _featuredSettingSlotDao

    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, "openscq30_app.db", null, 1) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS QuickPresetSlot (
                    deviceModel TEXT NOT NULL,
                    slotIndex INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(deviceModel, slotIndex)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS FeaturedSettingSlot (
                    deviceModel TEXT NOT NULL,
                    slotIndex INTEGER NOT NULL,
                    settingId TEXT NOT NULL,
                    PRIMARY KEY(deviceModel, slotIndex)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS custom_equalizer_profile (
                    name TEXT PRIMARY KEY NOT NULL,
                    band100 INTEGER NOT NULL,
                    band200 INTEGER NOT NULL,
                    band400 INTEGER NOT NULL,
                    band800 INTEGER NOT NULL,
                    band1600 INTEGER NOT NULL,
                    band3200 INTEGER NOT NULL,
                    band6400 INTEGER NOT NULL,
                    band12800 INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // No migrations needed for v1
        }
    }
}
