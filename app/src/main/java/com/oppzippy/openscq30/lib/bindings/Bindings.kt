package com.oppzippy.openscq30.lib.bindings

import com.oppzippy.openscq30.lib.wrapper.ConnectionDescriptor
import com.oppzippy.openscq30.lib.wrapper.PairedDevice
import com.oppzippy.openscq30.lib.wrapper.QuickPreset
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import java.util.UUID

// Basic exceptions
class OpenScq30Exception(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

sealed class AndroidException(message: String? = null) : Exception(message) {
    class Other(message: String) : AndroidException(message)
}

// Key-Value data model
data class SettingIdValuePair(val settingId: String, val value: Value)

typealias ConnectionStatus = com.oppzippy.openscq30.lib.wrapper.ConnectionStatus

// Callbacks
interface ConnectionStatusCallback {
    fun onChange(connectionStatus: com.oppzippy.openscq30.lib.wrapper.ConnectionStatus)
}

interface NotificationCallback {
    fun onNotify()
}

// Localization and logging stubs
data class LanguageIdentifier(
    val language: String,
    val script: String? = null,
    val region: String? = null,
    val variants: List<String> = emptyList(),
)

fun initNativeI18n(languageIdentifiers: List<LanguageIdentifier>) {
    // Pure Kotlin implementation - Android string resources handle localization
}

fun initNativeLogging() {
    // Pure Kotlin implementation - Logcat is used
}

// RFCOMM connection types
typealias MacAddr6 = String

sealed class RfcommServiceSelectionStrategy {
    data class Constant(val uuid: UUID) : RfcommServiceSelectionStrategy()
    data class Dynamic(val selectService: RfcommDynamicServiceSelector) : RfcommServiceSelectionStrategy()
}

fun interface RfcommDynamicServiceSelector {
    fun select(uuids: List<UUID>): UUID
}

interface AndroidRfcommConnectionWriter {
    suspend fun write(data: ByteArray)
    fun closeSocket()
}

class ManualRfcommConnection(val writer: AndroidRfcommConnectionWriter) {
    private var statusCallback: ((com.oppzippy.openscq30.lib.wrapper.ConnectionStatus) -> Unit)? = null
    private var packetCallback: ((ByteArray) -> Unit)? = null

    fun setConnectionStatus(status: com.oppzippy.openscq30.lib.wrapper.ConnectionStatus) {
        statusCallback?.invoke(status)
    }

    fun addInboundPacket(data: ByteArray) {
        packetCallback?.invoke(data)
    }

    fun onStatusChange(callback: (com.oppzippy.openscq30.lib.wrapper.ConnectionStatus) -> Unit) {
        statusCallback = callback
    }

    fun onPacket(callback: (ByteArray) -> Unit) {
        packetCallback = callback
    }
}

class ManualRfcommConnectionBox {
    private var connection: ManualRfcommConnection? = null

    fun set(connection: ManualRfcommConnection) {
        this.connection = connection
    }

    fun get(): ManualRfcommConnection? = connection
}

interface AndroidRfcommConnectionBackend {
    suspend fun devices(): List<ConnectionDescriptor>
    suspend fun connect(
        macAddress: MacAddr6,
        serviceSelectionStrategy: RfcommServiceSelectionStrategy,
        outputBox: ManualRfcommConnectionBox,
    )
}

data class ManualConnectionBackends(val rfcomm: AndroidRfcommConnectionBackend)

// Core OpenSCQ30 Device and Session Interfaces
interface OpenScq30Device : AutoCloseable {
    fun model(): String
    fun categories(): List<String>
    fun settingsInCategory(categoryId: String): List<String>
    fun setting(settingId: String): Setting?
    suspend fun setSettingValues(values: List<SettingIdValuePair>)
    fun setConnectionStatusCallback(callback: ConnectionStatusCallback)
    fun setWatchForChangesCallback(callback: NotificationCallback)
    override fun close()
}

interface QuickPresetHandler : AutoCloseable {
    suspend fun quickPresets(device: OpenScq30Device): List<QuickPreset>
    suspend fun activate(device: OpenScq30Device, name: String)
    suspend fun save(device: OpenScq30Device, name: String)
    suspend fun delete(device: OpenScq30Device, name: String)
    suspend fun toggleField(device: OpenScq30Device, name: String, settingId: String, enabled: Boolean)
    override fun close()
}

interface OpenScq30Session {
    suspend fun pairedDevices(): List<PairedDevice>
    suspend fun pair(pairedDevice: PairedDevice)
    suspend fun unpair(macAddress: String)
    suspend fun listDemoDevices(model: String): List<ConnectionDescriptor>
    suspend fun listDevicesWithBackends(backends: Any?, model: String): List<ConnectionDescriptor>
    suspend fun connectWithBackends(backends: Any?, macAddress: String): OpenScq30Device
    fun quickPresetHandler(): QuickPresetHandler
}

suspend fun newSession(sqlitePath: String): OpenScq30Session =
    com.oppzippy.openscq30.lib.soundcore.SoundcoreSessionImpl(com.oppzippy.openscq30.OpenSCQ30Application.instance)

// Translations and model registry
private val DEVICE_MODELS = listOf(
    "soundcore-a3028" to "Soundcore Q30 / Life Q30",
    "soundcore-a3027" to "Soundcore Life Q35",
    "soundcore-a3040" to "Soundcore Space Q45",
    "soundcore-a3035" to "Soundcore Space One",
    "soundcore-a3062" to "Soundcore Space One Pro",
    "soundcore-a3004" to "Soundcore Q20i",
    "soundcore-a3005" to "Soundcore Q11i",
    "soundcore-a3029" to "Soundcore Life Tune",
    "soundcore-a3030" to "Soundcore Life Tune Pro",
    "soundcore-a3031" to "Soundcore Vortex",
    "soundcore-a3033" to "Soundcore Life 2 Neo",
    "soundcore-a3947" to "Soundcore Liberty 4 NC",
    "soundcore-a3952" to "Soundcore Liberty 3 Pro",
    "soundcore-a3909" to "Soundcore Liberty 2 Pro",
    "soundcore-a3930" to "Soundcore Liberty 2 Pro+",
    "soundcore-a3951" to "Soundcore Liberty Air 2 Pro",
    "soundcore-a3936" to "Soundcore Space A40",
    "soundcore-a3939" to "Soundcore Life P3",
    "soundcore-a3949" to "Soundcore P20i / P25i / R50i",
    "soundcore-a3959" to "Soundcore P30i / R50i NC",
    "soundcore-a3955" to "Soundcore P40i",
    "soundcore-a3954" to "Soundcore Liberty 4 Pro",
    "soundcore-a3957" to "Soundcore Liberty 5",
    "soundcore-a3968" to "Soundcore Sport X20",
    "soundcore-a3948" to "Soundcore A20i",
    "soundcore-a3944" to "Soundcore Life P2 Mini",
    "soundcore-a3945" to "Soundcore Life Note 3S",
    "soundcore-a3931" to "Soundcore Life Dot 2 NC",
    "soundcore-a3933" to "Soundcore Life Note 3",
    "soundcore-a3926" to "Soundcore Life Dot 2S",
    "soundcore-a3116" to "Soundcore Motion+",
    "soundcore-a3330" to "Soundcore C30i",
    "soundcore-a3388" to "Soundcore AeroClip",
    "soundcore-a3876" to "Soundcore V20i",
    "soundcore-d1101" to "Soundcore C50i",
    "soundcore-d1202" to "Soundcore P31i",
    "soundcore-d1202c" to "Soundcore R60i NC",
    "soundcore-d1301" to "Soundcore Sleep A30",
    "soundcore-development" to "Soundcore Development Device",
)

private val CATEGORY_NAMES = mapOf(
    "general" to "General",
    "soundModes" to "Sound Modes",
    "equalizer" to "Equalizer",
    "buttonConfiguration" to "Button Configuration",
    "deviceInformation" to "Device Information",
    "equalizerImportExport" to "Equalizer Import/Export",
    "dualConnections" to "Dual Connections",
    "limitHighVolume" to "Limit High Volume",
    "case" to "Case",
    "lights" to "Lights",
)

private val SETTING_NAMES = mapOf(
    "ambientSoundMode" to "Ambient Sound Mode",
    "transparencyMode" to "Transparency Mode",
    "noiseCancelingMode" to "Noise Canceling Mode",
    "customNoiseCanceling" to "Custom Noise Canceling",
    "windNoiseSuppression" to "Wind Noise Suppression",
    "presetEqualizerProfile" to "Preset Equalizer Profile",
    "customEqualizerProfile" to "Custom Equalizer Profile",
    "volumeAdjustments" to "Volume Adjustments",
    "autoPowerOff" to "Auto Power Off",
    "limitHighVolume" to "Limit High Volume",
    "batteryLevel" to "Battery Level",
    "leftBatteryLevel" to "Left Earbud Battery",
    "rightBatteryLevel" to "Right Earbud Battery",
    "caseBatteryLevel" to "Case Battery",
    "firmwareVersion" to "Firmware Version",
    "serialNumber" to "Serial Number",
    "leftSinglePress" to "Left Single Press",
    "leftDoublePress" to "Left Double Press",
    "leftLongPress" to "Left Long Press",
    "rightSinglePress" to "Right Single Press",
    "rightDoublePress" to "Right Double Press",
    "rightLongPress" to "Right Long Press",
)

private val VALUE_NAMES = mapOf(
    "noiseCanceling" to "Noise Canceling",
    "transparency" to "Transparency",
    "normal" to "Normal",
    "transport" to "Transport",
    "indoor" to "Indoor",
    "outdoor" to "Outdoor",
    "custom" to "Custom",
    "vocalMode" to "Vocal Mode",
    "fullyTransparent" to "Fully Transparent",
    "never" to "Never",
    "30min" to "30 Minutes",
    "60min" to "60 Minutes",
    "90min" to "90 Minutes",
    "120min" to "120 Minutes",
    "volumeUp" to "Volume Up",
    "volumeDown" to "Volume Down",
    "nextTrack" to "Next Track",
    "previousTrack" to "Previous Track",
    "playPause" to "Play / Pause",
    "ambientSoundModeCycle" to "Ambient Sound Mode",
    "voiceAssistant" to "Voice Assistant",
)

fun deviceModels(): List<String> = DEVICE_MODELS.map { it.first }

fun translateDeviceModel(model: String): String =
    DEVICE_MODELS.find { it.first == model }?.second ?: model

fun translateCategoryId(categoryId: String): String =
    CATEGORY_NAMES[categoryId] ?: categoryId.replaceFirstChar { it.uppercase() }

fun translateSettingId(settingId: String): String =
    SETTING_NAMES[settingId] ?: settingId.replaceFirstChar { it.uppercase() }

fun translateValue(setting: Setting?, value: Value): String = translateValue(value)

fun translateValue(setting: Setting?): String = setting?.let { translateValue(it.toValue()) } ?: ""

fun translateValue(value: Value): String = when (value) {
    is Value.StringValue -> VALUE_NAMES[value.value] ?: value.value
    is Value.OptionalStringValue -> value.value?.let { VALUE_NAMES[it] ?: it } ?: "None"
    is Value.BoolValue -> if (value.value) "Enabled" else "Disabled"
    is Value.I32Value -> value.value.toString()
    is Value.F32Value -> value.value.toString()
    is Value.U16Value -> value.value.toString()
    is Value.OptionalU16Value -> value.value?.toString() ?: "None"
    is Value.I16VecValue -> value.value.joinToString(", ")
    is Value.U16VecValue -> value.value.joinToString(", ")
    is Value.StringVecValue -> value.value.joinToString(", ") { VALUE_NAMES[it] ?: it }
    is Value.ModifiableSelectCommand -> when (val inner = value.value) {
        is com.oppzippy.openscq30.lib.wrapper.ModifiableSelectCommandInner.Add -> "Add ${inner.name}"
        is com.oppzippy.openscq30.lib.wrapper.ModifiableSelectCommandInner.Remove -> "Remove ${inner.name}"
    }
    is Value.MultiSelectWithRemoveCommand -> when (val inner = value.value) {
        is com.oppzippy.openscq30.lib.wrapper.MultiSelectWithRemoveCommandInner.Remove -> "Remove ${inner.name}"
    }
}
