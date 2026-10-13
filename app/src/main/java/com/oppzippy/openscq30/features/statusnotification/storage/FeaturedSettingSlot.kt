package com.oppzippy.openscq30.features.statusnotification.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FeaturedSettingSlot(val deviceModel: String, val slotIndex: Int, val settingId: String)

abstract class FeaturedSettingSlotDao {
    abstract fun all(deviceModel: String): Flow<List<FeaturedSettingSlot>>

    fun allSettingIds(deviceModel: String): Flow<List<String?>> = all(deviceModel).map { slots ->
        val max = slots.maxByOrNull { it.slotIndex }?.slotIndex
        if (max != null) {
            val slotsByIndex = slots.associateBy { it.slotIndex }
            (0..max).map { slotsByIndex[it]?.settingId }
        } else {
            emptyList()
        }
    }

    abstract suspend fun upsert(featuredSettingSlot: FeaturedSettingSlot)
    abstract suspend fun delete(deviceModel: String, index: Int)
}
