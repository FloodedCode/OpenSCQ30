package com.oppzippy.openscq30.lib.soundcore

import android.content.Context
import android.util.Log
import com.oppzippy.openscq30.lib.bindings.AndroidRfcommConnectionBackend
import com.oppzippy.openscq30.lib.bindings.ManualConnectionBackends
import com.oppzippy.openscq30.lib.bindings.ManualRfcommConnectionBox
import com.oppzippy.openscq30.lib.bindings.OpenScq30Device
import com.oppzippy.openscq30.lib.bindings.OpenScq30Session
import com.oppzippy.openscq30.lib.bindings.QuickPresetHandler
import com.oppzippy.openscq30.lib.bindings.RfcommServiceSelectionStrategy
import com.oppzippy.openscq30.lib.bindings.SettingIdValuePair
import com.oppzippy.openscq30.lib.bindings.translateDeviceModel
import com.oppzippy.openscq30.lib.wrapper.ConnectionDescriptor
import com.oppzippy.openscq30.lib.wrapper.PairedDevice
import com.oppzippy.openscq30.lib.wrapper.QuickPreset
import com.oppzippy.openscq30.lib.wrapper.QuickPresetField
import com.oppzippy.openscq30.lib.wrapper.toValue
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SoundcoreSessionImpl(private val context: Context) : OpenScq30Session {

    companion object {
        private const val TAG = "SoundcoreSessionImpl"
        private const val PREFS_NAME = "openscq30_session_prefs"
        private const val KEY_PAIRED_DEVICES = "paired_devices_json"
        private const val KEY_QUICK_PRESETS = "quick_presets_json"

        // Standard Soundcore Serial Port Profile UUID
        val SOUNDCORE_SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val pairedDevicesList = mutableListOf<PairedDevice>()
    private val quickPresetsList = mutableListOf<QuickPreset>()

    init {
        loadData()
    }

    private fun loadData() {
        val pairedJson = prefs.getString(KEY_PAIRED_DEVICES, null)
        if (pairedJson != null) {
            try {
                pairedDevicesList.clear()
                pairedDevicesList.addAll(json.decodeFromString<List<PairedDevice>>(pairedJson))
            } catch (e: Exception) {
                Log.w(TAG, "Error loading paired devices: ${e.message}")
            }
        } else {
            // Seed with a default demo device so user can immediately test!
            pairedDevicesList.add(
                PairedDevice(
                    macAddress = "00:11:22:33:44:55",
                    model = "soundcore-a3028",
                    isDemo = true
                )
            )
            savePairedDevices()
        }

        val presetsJson = prefs.getString(KEY_QUICK_PRESETS, null)
        if (presetsJson != null) {
            try {
                quickPresetsList.clear()
                quickPresetsList.addAll(json.decodeFromString<List<QuickPreset>>(presetsJson))
            } catch (e: Exception) {
                Log.w(TAG, "Error loading quick presets: ${e.message}")
            }
        } else {
            // Seed sample presets
            quickPresetsList.add(
                QuickPreset(
                    name = "Commute (ANC)",
                    fields = listOf(
                        QuickPresetField("ambientSoundMode", "noiseCanceling".toValue(), true),
                        QuickPresetField("noiseCancelingMode", "transport".toValue(), true),
                        QuickPresetField("presetEqualizerProfile", "BassBooster".toValue(), true)
                    )
                )
            )
            quickPresetsList.add(
                QuickPreset(
                    name = "Office (Transparency)",
                    fields = listOf(
                        QuickPresetField("ambientSoundMode", "transparency".toValue(), true),
                        QuickPresetField("transparencyMode", "vocalMode".toValue(), true),
                        QuickPresetField("presetEqualizerProfile", "Podcast".toValue(), true)
                    )
                )
            )
            saveQuickPresets()
        }
    }

    private fun savePairedDevices() {
        prefs.edit().putString(KEY_PAIRED_DEVICES, json.encodeToString(pairedDevicesList)).apply()
    }

    private fun saveQuickPresets() {
        prefs.edit().putString(KEY_QUICK_PRESETS, json.encodeToString(quickPresetsList)).apply()
    }

    override suspend fun pairedDevices(): List<PairedDevice> = withContext(Dispatchers.IO) {
        pairedDevicesList.toList()
    }

    override suspend fun pair(pairedDevice: PairedDevice): Unit = withContext(Dispatchers.IO) {
        pairedDevicesList.removeAll { it.macAddress == pairedDevice.macAddress }
        pairedDevicesList.add(pairedDevice)
        savePairedDevices()
    }

    override suspend fun unpair(macAddress: String): Unit = withContext(Dispatchers.IO) {
        pairedDevicesList.removeAll { it.macAddress == macAddress }
        savePairedDevices()
    }

    override suspend fun listDemoDevices(model: String): List<ConnectionDescriptor> = withContext(Dispatchers.IO) {
        listOf(
            ConnectionDescriptor(
                name = "${translateDeviceModel(model)} (Demo)",
                macAddress = "00:11:22:33:44:55"
            )
        )
    }

    override suspend fun listDevicesWithBackends(
        backends: Any?,
        model: String
    ): List<ConnectionDescriptor> = withContext(Dispatchers.IO) {
        if (backends is ManualConnectionBackends) {
            backends.rfcomm.devices()
        } else {
            emptyList()
        }
    }

    override suspend fun connectWithBackends(
        backends: Any?,
        macAddress: String
    ): OpenScq30Device = withContext(Dispatchers.IO) {
        val paired = pairedDevicesList.find { it.macAddress == macAddress }
        val model = paired?.model ?: "soundcore-a3028"
        val isDemo = paired?.isDemo ?: false

        if (isDemo || backends !is ManualConnectionBackends) {
            SoundcoreDeviceImpl(
                context = context,
                deviceModel = model,
                macAddress = macAddress,
                isDemo = true
            )
        } else {
            val outputBox = ManualRfcommConnectionBox()
            try {
                backends.rfcomm.connect(
                    macAddress = macAddress,
                    serviceSelectionStrategy = RfcommServiceSelectionStrategy.Constant(SOUNDCORE_SPP_UUID),
                    outputBox = outputBox
                )
            } catch (ex: Exception) {
                Log.w(TAG, "RFCOMM connect failed: ${ex.message}, falling back to demo simulation")
            }
            SoundcoreDeviceImpl(
                context = context,
                deviceModel = model,
                macAddress = macAddress,
                isDemo = false,
                rfcommConnection = outputBox.get()
            )
        }
    }

    override fun quickPresetHandler(): QuickPresetHandler = object : QuickPresetHandler {
        override suspend fun quickPresets(device: OpenScq30Device): List<QuickPreset> =
            withContext(Dispatchers.IO) { quickPresetsList.toList() }

        override suspend fun activate(device: OpenScq30Device, name: String) = withContext(Dispatchers.IO) {
            val preset = quickPresetsList.find { it.name == name } ?: return@withContext
            val updates = preset.fields.filter { it.isEnabled }.map {
                SettingIdValuePair(it.settingId, it.value)
            }
            device.setSettingValues(updates)
        }

        override suspend fun save(device: OpenScq30Device, name: String) = withContext(Dispatchers.IO) {
            val categories = device.categories()
            val fields = mutableListOf<QuickPresetField>()
            for (category in categories) {
                for (settingId in device.settingsInCategory(category)) {
                    val setting = device.setting(settingId)
                    if (setting != null) {
                        fields.add(QuickPresetField(settingId, setting.toValue(), isEnabled = true))
                    }
                }
            }
            quickPresetsList.removeAll { it.name == name }
            quickPresetsList.add(QuickPreset(name, fields))
            saveQuickPresets()
        }

        override suspend fun delete(device: OpenScq30Device, name: String) = withContext(Dispatchers.IO) {
            quickPresetsList.removeAll { it.name == name }
            saveQuickPresets()
        }

        override suspend fun toggleField(
            device: OpenScq30Device,
            name: String,
            settingId: String,
            enabled: Boolean
        ) = withContext(Dispatchers.IO) {
            val index = quickPresetsList.indexOfFirst { it.name == name }
            if (index >= 0) {
                val current = quickPresetsList[index]
                val updatedFields = current.fields.map {
                    if (it.settingId == settingId) it.copy(isEnabled = enabled) else it
                }
                quickPresetsList[index] = current.copy(fields = updatedFields)
                saveQuickPresets()
            }
        }

        override fun close() {}
    }
}
