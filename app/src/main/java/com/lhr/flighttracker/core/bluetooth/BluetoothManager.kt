package com.lhr.flighttracker.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 掃描狀態
 */
sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Success(val devices: Set<BleScanResult>) : ScanState()
    data class Error(val message: String) : ScanState()
}

/**
 * 廣播狀態的密封類
 */
sealed class AdvertiseState {
    object Idle : AdvertiseState()
    object Advertising : AdvertiseState()
    data class Error(val message: String) : AdvertiseState()
}



@SuppressLint("MissingPermission")
class BluetoothManager(
    private val bluetoothAdapter: BluetoothAdapter
) {
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter.bluetoothLeAdvertiser
    private val scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var scanJob: Job? = null
    private var advertiseJob: Job? = null

    // 使用 MutableStateFlow 來暴露狀態
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState = _scanState.asStateFlow()

    private val _advertiseState = MutableStateFlow<AdvertiseState>(AdvertiseState.Idle)
    val advertiseState = _advertiseState.asStateFlow()

    private val foundDevices = mutableSetOf<BleScanResult>()

    /**
     * 開始掃描
     * @param scanFilters 過濾條件列表
     * @param durationMillis 掃描持續時間 (毫秒)，若為 null 則會持續掃描直到手動停止
     */
    fun startScanning(scanFilters: List<ScanFilter>, durationMillis: Long? = null) {
        if (_scanState.value is ScanState.Scanning) {
            Log.w("BleManager", "Scan is already in progress.")
            return
        }

        scanner ?: run {
            _scanState.value = ScanState.Error("Scanner not available.")
            return
        }

        foundDevices.clear()
        _scanState.value = ScanState.Scanning

        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(scanFilters, settings, scanCallback)

        // 如果設定了持續時間，則在時間到後自動停止
        durationMillis?.let {
            scanJob = managerScope.launch {
                delay(it)
                stopScanning()
            }
        }
    }

    /**
     * 停止掃描
     */
    fun stopScanning() {
        scanJob?.cancel()
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e("BleManager", "Error stopping scan", e)
        }
        _scanState.value = ScanState.Success(foundDevices.toSet())
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val bleScanResult = BleScanResult(
                deviceName = result.device.name ?: "未知裝置",
                address = result.device.address,
                rssi = result.rssi,
                serviceData = result.scanRecord?.serviceData ?: emptyMap()
            )
            foundDevices.add(bleScanResult)
            // 為了即時更新，也可以在這裡發出一個中間狀態
            // _scanState.value = ScanState.Success(foundDevices.toSet())
        }

        override fun onScanFailed(errorCode: Int) {
            _scanState.value = ScanState.Error("Scan failed with code: $errorCode")
        }
    }

    /**
     * 開始廣播
     * @param advertiseData 主要廣播封包的資料
     * @param scanResponseData (新增) 掃描回應封包的資料
     * @param durationMillis 廣播持續時間 (毫秒)，若為 null 則會持續廣播直到手動停止
     */
    fun startAdvertising(
        advertiseData: AdvertiseData,
        scanResponseData: AdvertiseData? = null, // 【修改點 1】新增 scanResponseData 參數
        durationMillis: Long? = null
    ) {
        if (_advertiseState.value is AdvertiseState.Advertising) {
            // 在分段廣播中，我們會頻繁重啟，所以這裡用 Log.d 即可
            Log.d("BleManager", "Advertise is already in progress, restarting.")
            // 為了能更新分段，需要先停止再開始，但 stopAdvertising 會改變狀態
            // 因此，我們直接呼叫 advertiser 的 API 即可
        }

        advertiser ?: run {
            _advertiseState.value = AdvertiseState.Error("Advertiser not available.")
            return
        }

        _advertiseState.value = AdvertiseState.Advertising
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false).build()

        // 【修改點 2】呼叫包含 scanResponseData 的 API 版本
        advertiser.startAdvertising(settings, advertiseData, scanResponseData, advertiseCallback)

        // 取消上一個計時器
        advertiseJob?.cancel()
        durationMillis?.let {
            advertiseJob = managerScope.launch {
                delay(it)
                stopAdvertising()
            }
        }
    }


    /**
     * 停止廣播
     */
    fun stopAdvertising() {
        advertiseJob?.cancel()
        try {
            advertiser?.stopAdvertising(advertiseCallback)
        } catch (e: Exception) {
            Log.e("BleManager", "Error stopping advertise", e)
        }
        _advertiseState.value = AdvertiseState.Idle
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i("BleManager", "Advertising started successfully.")
        }

        override fun onStartFailure(errorCode: Int) {
            _advertiseState.value = AdvertiseState.Error("Advertise failed with code: $errorCode")
        }
    }

    /**
     * 清理資源，應在 ViewModel 的 onCleared 中呼叫
     */
    fun release() {
        stopScanning()
        stopAdvertising()
        managerScope.cancel()
    }
}