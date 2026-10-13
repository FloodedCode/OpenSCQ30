package com.oppzippy.openscq30.features.statusnotification.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class QuickPresetSlot(val deviceModel: String, val slotIndex: Int, val name: String)

abstract class QuickPresetSlotDao {
    abstract fun all(deviceModel: String): Flow<List<QuickPresetSlot>>

    fun allNames(deviceModel: String): Flow<List<String?>> = all(deviceModel).map { slots ->
        val max = slots.maxByOrNull { it.slotIndex }?.slotIndex
        if (max != null) {
            val slotsByIndex = slots.associateBy { it.slotIndex }
            (0..max).map { slotsByIndex[it]?.name }
        } else {
            emptyList()
        }
    }

    abstract suspend fun upsert(quickPresetSlot: QuickPresetSlot)
    abstract suspend fun delete(deviceModel: String, index: Int)
}
