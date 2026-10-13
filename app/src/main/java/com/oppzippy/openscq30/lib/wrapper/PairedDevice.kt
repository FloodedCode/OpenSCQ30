package com.oppzippy.openscq30.lib.wrapper

import kotlinx.serialization.Serializable

@Serializable
data class PairedDevice(val macAddress: String, val model: String, val isDemo: Boolean)
