package com.oppzippy.openscq30.lib.soundcore

import android.content.Context
import android.util.Log
import com.oppzippy.openscq30.lib.bindings.ConnectionStatus
import com.oppzippy.openscq30.lib.bindings.ConnectionStatusCallback
import com.oppzippy.openscq30.lib.bindings.ManualRfcommConnection
import com.oppzippy.openscq30.lib.bindings.NotificationCallback
import com.oppzippy.openscq30.lib.bindings.OpenScq30Device
import com.oppzippy.openscq30.lib.bindings.SettingIdValuePair
import com.oppzippy.openscq30.lib.wrapper.Equalizer
import com.oppzippy.openscq30.lib.wrapper.Range
import com.oppzippy.openscq30.lib.wrapper.Select
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SoundcoreDeviceImpl(
    private val context: Context,
    private val deviceModel: String,
    private val macAddress: String,
    private val isDemo: Boolean,
    private val rfcommConnection: ManualRfcommConnection? = null,
) : OpenScq30Device {

    companion object {
        private const val TAG = "SoundcoreDeviceImpl"

        val EQUALIZER_BANDS = listOf<UShort>(100u, 200u, 400u, 800u, 1600u, 3200u, 6400u, 12800u)
        val PRESET_NAMES = listOf(
            "SoundcoreSignature", "Acoustic", "BassBooster", "BassReducer",
            "Classical", "Podcast", "Dance", "Deep", "Electronic", "Flat",
            "HipHop", "Jazz", "Latin", "Lounge", "Piano", "Pop", "RnB",
            "Rock", "SmallSpeakers", "SpokenWord", "TrebleBooster", "TrebleReducer"
        )
        val PRESET_VALUES: Map<String, List<Short>> = mapOf(
            "SoundcoreSignature" to listOf(0, 0, 0, 0, 0, 0, 0, 0),
            "Acoustic" to listOf(40, 10, 20, 20, 40, 40, 40, 20),
            "BassBooster" to listOf(40, 30, 10, 0, 0, 0, 0, 0),
            "BassReducer" to listOf(-40, -30, -10, 0, 0, 0, 0, 0),
            "Classical" to listOf(30, 30, -20, -20, 0, 20, 30, 40),
            "Podcast" to listOf(-30, 20, 40, 40, 30, 20, 0, -20),
            "Dance" to listOf(20, -30, -10, 10, 20, 20, 10, -30),
            "Deep" to listOf(20, 10, 30, 30, 20, -20, -40, -50),
            "Electronic" to listOf(30, 20, -20, 20, 10, 20, 30, 30),
            "Flat" to listOf(-20, -20, -10, 0, 0, 0, -20, -20),
            "HipHop" to listOf(20, 30, -10, -10, 20, -10, 20, 30),
            "Jazz" to listOf(20, 20, -20, -20, 0, 20, 30, 40),
            "Latin" to listOf(0, 0, -20, -20, -20, 0, 30, 50),
            "Lounge" to listOf(-10, 20, 40, 30, 0, -20, 20, 10),
            "Piano" to listOf(0, 30, 30, 20, 40, 50, 30, 40),
            "Pop" to listOf(-10, 10, 30, 30, 10, -10, -20, -30),
            "RnB" to listOf(60, 20, -20, -20, 20, 30, 30, 40),
            "Rock" to listOf(30, 20, -10, -10, 10, 30, 30, 30),
            "SmallSpeakers" to listOf(40, 30, 10, 0, -20, -30, -40, -40),
            "SpokenWord" to listOf(-30, -20, 10, 20, 20, 10, 0, -30),
            "TrebleBooster" to listOf(-20, -20, -20, -10, 10, 20, 20, 40),
            "TrebleReducer" to listOf(0, 0, 0, -20, -30, -40, -40, -60)
        )
    }

    private var isClosed = false
    private var connectionStatusCallback: ConnectionStatusCallback? = null
    private var watchForChangesCallback: NotificationCallback? = null

    // Setting values
    var ambientSoundMode: String = "noiseCanceling"
    var noiseCancelingMode: String = "transport"
    var transparencyMode: String = "fullyTransparent"
    var customNoiseCanceling: Int = 3
    var windNoiseSuppression: Boolean = true

    var selectedPresetProfile: String? = "SoundcoreSignature"
    var selectedCustomProfile: String? = null
    var customProfilesList: MutableList<String> = mutableListOf("My EQ 1", "Bass Heavy")
    var volumeAdjustments: List<Short> = PRESET_VALUES["SoundcoreSignature"] ?: List(8) { 0.toShort() }

    var autoPowerOff: String = "30min"
    var limitHighVolume: Boolean = false

    var leftSinglePress: String = "playPause"
    var leftDoublePress: String = "nextTrack"
    var leftLongPress: String = "ambientSoundModeCycle"
    var rightSinglePress: String = "volumeUp"
    var rightDoublePress: String = "volumeDown"
    var rightLongPress: String = "voiceAssistant"

    init {
        rfcommConnection?.let { conn ->
            conn.onStatusChange { status ->
                if (status == com.oppzippy.openscq30.lib.wrapper.ConnectionStatus.Disconnected) {
                    connectionStatusCallback?.onChange(ConnectionStatus.Disconnected)
                }
            }
            conn.onPacket { data ->
                parseInboundPacket(data)
            }
            // Request device initial state
            CoroutineScope(Dispatchers.IO).launch {
                sendOutboundPacket(byteArrayOf(0x01, 0x01, 0x01))
            }
        }
    }

    override fun model(): String = deviceModel

    override fun categories(): List<String> = listOf(
        "soundModes",
        "equalizer",
        "buttonConfiguration",
        "general",
        "deviceInformation"
    )

    override fun settingsInCategory(categoryId: String): List<String> = when (categoryId) {
        "soundModes" -> listOf(
            "ambientSoundMode",
            "noiseCancelingMode",
            "transparencyMode",
            "customNoiseCanceling",
            "windNoiseSuppression"
        )
        "equalizer" -> listOf(
            "presetEqualizerProfile",
            "customEqualizerProfile",
            "volumeAdjustments"
        )
        "buttonConfiguration" -> listOf(
            "leftSinglePress",
            "leftDoublePress",
            "leftLongPress",
            "rightSinglePress",
            "rightDoublePress",
            "rightLongPress"
        )
        "general" -> listOf(
            "autoPowerOff",
            "limitHighVolume"
        )
        "deviceInformation" -> listOf(
            "batteryLevel",
            "firmwareVersion",
            "serialNumber"
        )
        else -> emptyList()
    }

    override fun setting(settingId: String): Setting? {
        if (isClosed) return null
        return when (settingId) {
            "ambientSoundMode" -> Setting.SelectSetting(
                setting = Select(
                    options = listOf("noiseCanceling", "transparency", "normal"),
                    localizedOptions = listOf("Noise Canceling", "Transparency", "Normal")
                ),
                value = ambientSoundMode
            )
            "noiseCancelingMode" -> Setting.SelectSetting(
                setting = Select(
                    options = listOf("transport", "indoor", "outdoor", "custom"),
                    localizedOptions = listOf("Transport", "Indoor", "Outdoor", "Custom")
                ),
                value = noiseCancelingMode
            )
            "transparencyMode" -> Setting.SelectSetting(
                setting = Select(
                    options = listOf("fullyTransparent", "vocalMode"),
                    localizedOptions = listOf("Fully Transparent", "Vocal Mode")
                ),
                value = transparencyMode
            )
            "customNoiseCanceling" -> Setting.I32RangeSetting(
                setting = Range(start = 1, end = 5, step = 1),
                value = customNoiseCanceling
            )
            "windNoiseSuppression" -> Setting.ToggleSetting(value = windNoiseSuppression)

            "presetEqualizerProfile" -> Setting.PresetEqualizerProfileSelect(
                equalizer = Equalizer(
                    bandHz = EQUALIZER_BANDS,
                    fractionDigits = 1,
                    min = -120,
                    max = 135
                ),
                select = Select(
                    options = PRESET_NAMES,
                    localizedOptions = PRESET_NAMES.map { it.replace(Regex("([a-z])([A-Z])"), "$1 $2") }
                ),
                presets = PRESET_NAMES.map { PRESET_VALUES[it] ?: List(8) { 0.toShort() } },
                value = selectedPresetProfile
            )
            "customEqualizerProfile" -> Setting.ModifiableSelectSetting(
                setting = Select(
                    options = customProfilesList,
                    localizedOptions = customProfilesList
                ),
                value = selectedCustomProfile
            )
            "volumeAdjustments" -> Setting.EqualizerSetting(
                setting = Equalizer(
                    bandHz = EQUALIZER_BANDS,
                    fractionDigits = 1,
                    min = -120,
                    max = 135
                ),
                readOnly = false,
                value = volumeAdjustments
            )

            "leftSinglePress" -> Setting.SelectSetting(
                setting = buttonActionSelect(),
                value = leftSinglePress
            )
            "leftDoublePress" -> Setting.SelectSetting(
                setting = buttonActionSelect(),
                value = leftDoublePress
            )
            "leftLongPress" -> Setting.SelectSetting(
                setting = buttonActionSelect(),
                value = leftLongPress
            )
            "rightSinglePress" -> Setting.SelectSetting(
                setting = buttonActionSelect(),
                value = rightSinglePress
            )
            "rightDoublePress" -> Setting.SelectSetting(
                setting = buttonActionSelect(),
                value = rightDoublePress
            )
            "rightLongPress" -> Setting.SelectSetting(
                setting = buttonActionSelect(),
                value = rightLongPress
            )

            "autoPowerOff" -> Setting.SelectSetting(
                setting = Select(
                    options = listOf("never", "30min", "60min", "90min", "120min"),
                    localizedOptions = listOf("Never", "30 Minutes", "60 Minutes", "90 Minutes", "120 Minutes")
                ),
                value = autoPowerOff
            )
            "limitHighVolume" -> Setting.ToggleSetting(value = limitHighVolume)

            "batteryLevel" -> Setting.InformationSetting(
                value = "90%",
                translatedValue = "90% (Charging)"
            )
            "firmwareVersion" -> Setting.InformationSetting(
                value = "03.22",
                translatedValue = "v03.22"
            )
            "serialNumber" -> Setting.InformationSetting(
                value = macAddress.replace(":", "").take(14).uppercase(),
                translatedValue = macAddress.replace(":", "").take(14).uppercase()
            )
            else -> null
        }
    }

    private fun buttonActionSelect() = Select(
        options = listOf(
            "playPause", "volumeUp", "volumeDown", "nextTrack",
            "previousTrack", "ambientSoundModeCycle", "voiceAssistant"
        ),
        localizedOptions = listOf(
            "Play / Pause", "Volume Up", "Volume Down", "Next Track",
            "Previous Track", "Ambient Sound Mode", "Voice Assistant"
        )
    )

    override suspend fun setSettingValues(values: List<SettingIdValuePair>) {
        if (isClosed) throw IllegalStateException("Device is closed")

        for (item in values) {
            when (item.settingId) {
                "ambientSoundMode" -> if (item.value is Value.StringValue) ambientSoundMode = item.value.value
                "noiseCancelingMode" -> if (item.value is Value.StringValue) noiseCancelingMode = item.value.value
                "transparencyMode" -> if (item.value is Value.StringValue) transparencyMode = item.value.value
                "customNoiseCanceling" -> if (item.value is Value.I32Value) customNoiseCanceling = item.value.value
                "windNoiseSuppression" -> if (item.value is Value.BoolValue) windNoiseSuppression = item.value.value

                "presetEqualizerProfile" -> {
                    if (item.value is Value.OptionalStringValue) {
                        selectedPresetProfile = item.value.value
                        selectedCustomProfile = null
                        item.value.value?.let { preset ->
                            PRESET_VALUES[preset]?.let { volumeAdjustments = it }
                        }
                    }
                }
                "customEqualizerProfile" -> {
                    when (item.value) {
                        is Value.OptionalStringValue -> {
                            selectedCustomProfile = item.value.value
                            selectedPresetProfile = null
                        }
                        is Value.ModifiableSelectCommand -> {
                            when (val cmd = item.value.value) {
                                is com.oppzippy.openscq30.lib.wrapper.ModifiableSelectCommandInner.Add -> {
                                    if (!customProfilesList.contains(cmd.name)) customProfilesList.add(cmd.name)
                                    selectedCustomProfile = cmd.name
                                    selectedPresetProfile = null
                                }
                                is com.oppzippy.openscq30.lib.wrapper.ModifiableSelectCommandInner.Remove -> {
                                    customProfilesList.remove(cmd.name)
                                    if (selectedCustomProfile == cmd.name) selectedCustomProfile = null
                                }
                            }
                        }
                        else -> Unit
                    }
                }
                "volumeAdjustments" -> {
                    if (item.value is Value.I16VecValue) {
                        volumeAdjustments = item.value.value
                    }
                }

                "leftSinglePress" -> if (item.value is Value.StringValue) leftSinglePress = item.value.value
                "leftDoublePress" -> if (item.value is Value.StringValue) leftDoublePress = item.value.value
                "leftLongPress" -> if (item.value is Value.StringValue) leftLongPress = item.value.value
                "rightSinglePress" -> if (item.value is Value.StringValue) rightSinglePress = item.value.value
                "rightDoublePress" -> if (item.value is Value.StringValue) rightDoublePress = item.value.value
                "rightLongPress" -> if (item.value is Value.StringValue) rightLongPress = item.value.value

                "autoPowerOff" -> if (item.value is Value.StringValue) autoPowerOff = item.value.value
                "limitHighVolume" -> if (item.value is Value.BoolValue) limitHighVolume = item.value.value
            }
        }

        // Write to Bluetooth RFCOMM if connected
        if (!isDemo && rfcommConnection != null) {
            sendStateUpdatePacket(values)
        }

        // Notify watchers
        withContext(Dispatchers.Main) {
            watchForChangesCallback?.onNotify()
        }
    }

    private suspend fun sendStateUpdatePacket(values: List<SettingIdValuePair>) {
        try {
            // Soundcore RFCOMM command framing: 0x08, 0xEE, 0x00, 0x00, 0x00, <cmd_high>, <cmd_low>, <len>, <payload>, <checksum>
            for (v in values) {
                when (v.settingId) {
                    "ambientSoundMode", "noiseCancelingMode", "transparencyMode" -> {
                        val modeByte: Byte = when (ambientSoundMode) {
                            "noiseCanceling" -> 0x00
                            "transparency" -> 0x01
                            else -> 0x02
                        }
                        val subModeByte: Byte = when (ambientSoundMode) {
                            "noiseCanceling" -> when (noiseCancelingMode) {
                                "transport" -> 0x00
                                "outdoor" -> 0x01
                                "indoor" -> 0x02
                                else -> 0x03
                            }
                            "transparency" -> when (transparencyMode) {
                                "fullyTransparent" -> 0x00
                                else -> 0x01
                            }
                            else -> 0x00
                        }
                        sendOutboundPacket(byteArrayOf(0x08, 0x01, modeByte, subModeByte))
                    }
                    "volumeAdjustments" -> {
                        val bytes = ByteArray(volumeAdjustments.size) { i ->
                            ((volumeAdjustments[i] + 120) / 10).toByte()
                        }
                        sendOutboundPacket(byteArrayOf(0x08, 0x03) + bytes)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.w(TAG, "Failed to send packet to RFCOMM: ${ex.message}")
        }
    }

    private suspend fun sendOutboundPacket(payload: ByteArray) {
        val header = byteArrayOf(0x08.toByte(), 0xEE.toByte(), 0x00, 0x00, 0x00)
        val full = header + payload
        val checksum = (full.sumOf { it.toInt() and 0xFF } and 0xFF).toByte()
        rfcommConnection?.writer?.write(full + checksum)
    }

    private fun parseInboundPacket(data: ByteArray) {
        if (data.size < 7) return
        // Handle packet updates from headphone
        watchForChangesCallback?.onNotify()
    }

    override fun setConnectionStatusCallback(callback: ConnectionStatusCallback) {
        this.connectionStatusCallback = callback
    }

    override fun setWatchForChangesCallback(callback: NotificationCallback) {
        this.watchForChangesCallback = callback
    }

    override fun close() {
        isClosed = true
        rfcommConnection?.writer?.closeSocket()
    }
}
