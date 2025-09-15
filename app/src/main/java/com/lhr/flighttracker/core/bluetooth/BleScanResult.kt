package com.lhr.flighttracker.core.bluetooth

import android.os.ParcelUuid

/**
 * 藍牙裝置
 */
data class BleScanResult(
    val deviceName: String,
    val address: String,
    val rssi: Int,
    val serviceData: Map<ParcelUuid, ByteArray>
)
