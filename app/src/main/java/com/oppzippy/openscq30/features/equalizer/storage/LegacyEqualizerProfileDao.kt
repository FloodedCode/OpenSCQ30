package com.oppzippy.openscq30.features.equalizer.storage

import kotlinx.coroutines.flow.Flow

abstract class LegacyEqualizerProfileDao {
    abstract suspend fun count(): Int
    abstract suspend fun getAll(): List<LegacyEqualizerProfile>
    abstract fun all(): Flow<List<LegacyEqualizerProfile>>
    abstract suspend fun get(name: String): LegacyEqualizerProfile?
    abstract suspend fun insert(customProfile: LegacyEqualizerProfile)
    abstract suspend fun insertIgnoreConflicts(customProfile: LegacyEqualizerProfile)
    abstract suspend fun upsert(customProfile: LegacyEqualizerProfile)
    abstract suspend fun upsertAll(customProfile: List<LegacyEqualizerProfile>)
    abstract suspend fun delete(name: String)

    open suspend fun insertAndRename(profiles: List<LegacyEqualizerProfile>) {
        profiles.forEach { profile ->
            val name = findRename(profile.name)
            if (name != null) {
                insertIgnoreConflicts(profile.copy(name = name))
            }
        }
    }

    private suspend fun findRename(name: String): String? {
        if (get(name) == null) {
            return name
        }
        for (i in 2..100) {
            val newName = "$name ($i)"
            if (get(newName) == null) {
                return newName
            }
        }
        return null
    }
}
