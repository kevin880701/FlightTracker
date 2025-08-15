package com.lhr.flighttracker.features.ble.presentation.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BleDevice(
    val name: String,
    val address: String,
    val uuids: List<String>
)

enum class AppRole { IDLE, MASTER, RESPONDER }

@SuppressLint("MissingPermission")
@HiltViewModel
class BleRollCallViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val bluetoothManager = application.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val _currentRole = MutableStateFlow(AppRole.IDLE)
    val currentRole = _currentRole.asStateFlow()

    private val _statusText = MutableStateFlow("請選擇您的角色")
    val statusText = _statusText.asStateFlow()

    private val _foundDevices = MutableStateFlow<Set<BleDevice>>(emptySet())
    val foundDevices = _foundDevices.asStateFlow()

    private val _receivedPingInfo = MutableStateFlow<BleDevice?>(null)
    val receivedPingInfo = _receivedPingInfo.asStateFlow()

    // 用來存放本機回覆的廣播內容
    private val _myReplyInfo = MutableStateFlow<BleDevice?>(null)
    val myReplyInfo = _myReplyInfo.asStateFlow()

    private var viewModelJob: Job? = null

    // --- UUIDs ---
    private val rollCallServiceUuid: ParcelUuid = ParcelUuid.fromString("0000A001-0000-1000-8000-00805F9B34FB")
    private val rollCallResponseServiceUuid: ParcelUuid = ParcelUuid.fromString("0000A002-0000-1000-8000-00805F9B34FB")

    override fun onCleared() {
        super.onCleared()
        stopAllBleActivities()
    }

    fun stopAllBleActivities() {
        viewModelJob?.cancel()
        stopBroadcasting()
        stopScanning()
        _currentRole.value = AppRole.IDLE
        _statusText.value = "已手動停止，請重新選擇角色"
    }

    // --- Master Role Logic ---
    fun startMasterRole() {
        if (_currentRole.value != AppRole.IDLE) return

        // 開始新任務前，清除上一次的結果
        _foundDevices.value = emptySet()
        _receivedPingInfo.value = null
        _myReplyInfo.value = null // 清除我方回覆資訊

        _currentRole.value = AppRole.MASTER
        viewModelJob = viewModelScope.launch {
            _statusText.value = "主控端：發送點名廣播中 (2秒)..."
            startBroadcastingPing()
            delay(2000)
            stopBroadcasting()
            delay(200)

            _statusText.value = "主控端：掃描回應中 (8秒)..."
            startScanningForPong()
            delay(7800)
            stopScanning()

            _statusText.value = "主控端：點名結束！共收到 ${_foundDevices.value.size} 個回應。"
            _currentRole.value = AppRole.IDLE
        }
    }

    // --- Responder Role Logic ---
    fun startResponderRole() {
        if (_currentRole.value != AppRole.IDLE) return

        // 開始新任務前，清除上一次的結果
        _foundDevices.value = emptySet()
        _receivedPingInfo.value = null
        _myReplyInfo.value = null // 清除我方回覆資訊

        _currentRole.value = AppRole.RESPONDER
        _statusText.value = "回應端：等待點名廣播..."
        startScanningForPing()
    }

    // --- Broadcasting ---
    private fun startBroadcastingPing() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false).build()
        val data = AdvertiseData.Builder().addServiceUuid(rollCallServiceUuid).build()
        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private fun startBroadcastingPong() {
        _statusText.value = "回應端：收到點名！正在廣播回應 (5秒)..."

        // 在廣播前回覆內容前，先建立本機的裝置資訊並更新到 StateFlow
        val myName = bluetoothAdapter?.name ?: "本機裝置"
        val myAddress = bluetoothAdapter?.address ?: "00:00:00:00:00:00"
        val myReplyUuids = listOf(rollCallResponseServiceUuid.uuid.toString())
        _myReplyInfo.value = BleDevice(name = myName, address = myAddress, uuids = myReplyUuids)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false).build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(rollCallResponseServiceUuid)
            .setIncludeDeviceName(true)
            .build()
        advertiser?.startAdvertising(settings, data, advertiseCallback)

        viewModelJob = viewModelScope.launch {
            delay(5000)
            if (_currentRole.value == AppRole.RESPONDER) {
                stopBroadcasting()
                _statusText.value = "回應端：回應廣播結束。"
                _currentRole.value = AppRole.IDLE
            }
        }
    }

    private fun stopBroadcasting() {
        advertiser?.stopAdvertising(advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.i("BleRollCall", "✅ 廣播成功啟動！(Advertise Start Success)")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val reason = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "失敗：廣播封包太大"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "失敗：沒有可用的廣播實例"
                ADVERTISE_FAILED_ALREADY_STARTED -> "失敗：已經在廣播中"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "失敗：系統內部錯誤"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "失敗：不支援此功能"
                else -> "失敗：未知錯誤"
            }
            Log.e("BleRollCall", "❌ 廣播啟動失敗！錯誤碼: $errorCode, 原因: $reason")
            _statusText.value = "錯誤：廣播啟動失敗 ($reason)"
        }
    }

    private fun startScanningForPing() {
        val scanFilter = ScanFilter.Builder().setServiceUuid(rollCallServiceUuid).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner?.startScan(listOf(scanFilter), settings, responderScanCallback)
    }

    private fun startScanningForPong() {
        val scanFilter = ScanFilter.Builder().setServiceUuid(rollCallResponseServiceUuid).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner?.startScan(listOf(scanFilter), settings, masterScanCallback)
    }

    private fun stopScanning() {
        try {
            scanner?.stopScan(masterScanCallback)
            scanner?.stopScan(responderScanCallback)
        } catch (e: Exception) {
            Log.e("BleRollCall", "停止掃描時發生錯誤", e)
        }
    }

    private val responderScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            if (_currentRole.value != AppRole.RESPONDER || _receivedPingInfo.value != null) return

            val deviceName = result.device.name ?: "未知裝置"
            val deviceAddress = result.device.address
            val serviceUuids = result.scanRecord?.serviceUuids?.map { it.uuid.toString() } ?: emptyList()

            val discoveredDevice = BleDevice(name = deviceName, address = deviceAddress, uuids = serviceUuids)
            _receivedPingInfo.value = discoveredDevice

            Log.d("BleRollCall", "Responder: Ping received from $deviceName ($deviceAddress) with UUIDs: $serviceUuids")

            stopScanning()
            startBroadcastingPong()
        }
    }

    private val masterScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val deviceName = result.device.name ?: "未知裝置"
            val deviceAddress = result.device.address
            val serviceUuids = result.scanRecord?.serviceUuids?.map { it.uuid.toString() } ?: emptyList()

            val discoveredDevice = BleDevice(name = deviceName, address = deviceAddress, uuids = serviceUuids)
            _foundDevices.value = _foundDevices.value + discoveredDevice

            Log.d("BleRollCall", "Master: Pong received from $deviceName ($deviceAddress) with UUIDs: $serviceUuids")
        }
    }
}