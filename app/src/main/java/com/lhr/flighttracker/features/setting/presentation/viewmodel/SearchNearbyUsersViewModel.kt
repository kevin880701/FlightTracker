package com.lhr.flighttracker.features.setting.presentation.viewmodel

import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lhr.flighttracker.core.bluetooth.BleScanResult
import com.lhr.flighttracker.core.bluetooth.ScanState
import com.lhr.flighttracker.features.setting.data.repositories.FakeUserRepository
import com.lhr.flighttracker.features.setting.domain.entity.FriendshipStatus
import com.lhr.flighttracker.features.setting.domain.entity.UserProfile
import com.lhr.flighttracker.features.setting.domain.entity.UserProfileFaker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.SortedMap
import javax.inject.Inject
import kotlin.random.Random

/**
 * 用於暫存單一訊息的所有分段資料。
 * @param totalChunks 此訊息預期的總段數。
 * @param chunks 已收到的分段資料，Key為分段索引，Value為分段內容。
 */
data class MessageChunks(
    var totalChunks: Int = -1, // 初始值設為-1，代表尚未從任何分段中獲知總數
    val chunks: SortedMap<Int, ByteArray> = sortedMapOf()
)

@HiltViewModel
class SearchNearbyUsersViewModel @Inject constructor(
    private val application: Application,
    private val userRepository: FakeUserRepository
) : ViewModel() {

    private val bluetoothManager = application.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleManager = bluetoothAdapter?.let {
        com.lhr.flighttracker.core.bluetooth.BluetoothManager(it)
    }

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _foundUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val foundUsers = _foundUsers.asStateFlow()

    /**
     * 自訂的服務 UUID (Service UUID)。
     * 掃描端會使用此 UUID 來過濾，只接收本 App 發出的廣播。
     */
    private val nearbyShareServiceUuid: ParcelUuid =
        ParcelUuid.fromString("0000B001-0000-1000-8000-00805F9B34FB")

    // --- 分段傳輸協定相關 ---

    /**
     * 用於暫存所有接收到的訊息分段。
     * Key: 訊息 ID (Byte) - 用來區分不同裝置或不同次的廣播。
     * Value: MessageChunks - 包含該訊息的總段數和已收到的分段資料。
     */
    private val chunkBuffer = mutableMapOf<Byte, MessageChunks>()

    /**
     * 用於管理廣播的 Coroutine Job。
     * 可以在開始新的廣播或 ViewModel 銷毀時，取消正在進行的廣播任務。
     */
    private var broadcastJob: Job? = null

    /**
     * 定義每個「資料分段」的最大容量 (Bytes)。
     * 計算公式: 總容量 31 - 欄位標頭 2 bytes - Service UUID 16 bytes - 訊息ID 1 byte - 總段數 0.5 byte - 當前索引 0.5 byte = 11 bytes。
     */
    private val MAX_CHUNK_SIZE = 11

    init {
        // 持續監聽掃描狀態的變化，以便在掃描結束時觸發重組。
        viewModelScope.launch {
            bleManager?.scanState?.collect { state ->
                when (state) {
                    is ScanState.Success -> {
                        // 掃描成功結束，將期間內掃描到的所有裝置結果進行解析。
                        state.devices.forEach { parseChunkFromScanResult(it) }
                        // 將已接收完整的分段拼湊成使用者id。
                        reassembleAndProcessUsers()
                    }
                    is ScanState.Error -> {
                        _isSearching.value = false
                        Log.e("SearchNearbyUsersVM", "Scan failed: ${state.message}")
                    }
                    else -> { /* 處理 Idle, Scanning 等其他狀態 */ }
                }
            }
        }
    }

    /**
     * 開始為時 8 秒的搜尋任務。
     * 此任務包含兩個並行操作：廣播自身資訊 和 掃描他人資訊。
     */
    fun startSearching() {
        if (_isSearching.value || bleManager == null) return

        _isSearching.value = true
        _foundUsers.value = emptyList()
        chunkBuffer.clear()    // 清除上一次掃描的緩衝資料。
        broadcastJob?.cancel() // 取消上一次可能還在進行的廣播任務。

        // --- 1. 準備並開始廣播自身資訊 ---
        val myProfile = UserProfileFaker.create()
        // 只廣播 UUID 字串本身，以節省空間。
        val fullPayload = myProfile.id.toByteArray(StandardCharsets.UTF_8)

        // 建立信標。
        val beaconAdvertiseData = AdvertiseData.Builder()
            .addServiceUuid(nearbyShareServiceUuid)
            .setIncludeDeviceName(false) // 不包含裝置名稱以節省廣播空間。
            .build()

        // 啟動一個新的協程，在背景執行分段廣播的任務。
        broadcastJob = viewModelScope.launch {
            broadcastChunks(beaconAdvertiseData, fullPayload, 8000L)
        }

        // --- 2. 同時開始掃描他人資訊 ---
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(nearbyShareServiceUuid) // 設定過濾器，只接收與我們使用相同 UUID 的廣播。
            .build()
        bleManager.startScanning(listOf(scanFilter), 8000L)
    }

    /**
     * 核心廣播函式：將完整資料分段，並透過 Scan Response 依序廣播出去。
     * @param advertiseData 作為「信標」的主要廣播封包。
     * @param fullPayload 完整的原始資料 (使用者ID)。
     * @param totalDuration 整個廣播過程的總時長。
     */
    private suspend fun broadcastChunks(
        advertiseData: AdvertiseData,
        fullPayload: ByteArray,
        totalDuration: Long
    ) {
        // 為本次廣播產生一個隨機的「訊息 ID」，如同這次貨運的總運單號。
        val messageId = Random.nextInt(256).toByte()
        val chunks = fullPayload.asSequence().chunked(MAX_CHUNK_SIZE).toList()
        val totalChunks = chunks.size

        if (totalChunks == 0) return
        if (totalChunks > 16) {
            Log.e("BleChunkSender", "Data too large, exceeds 16 chunks limit.")
            return
        }

        // 均分總時長，計算每個分段可以廣播多久。
        val durationPerChunk = totalDuration / totalChunks

        for (index in chunks.indices) {
            val chunkData = chunks[index].toByteArray()

            // 組合「分段資訊」位元組：
            // 高4位元存放「總段數」，低4位元存放「當前索引」。
            val chunkInfo = (((totalChunks - 1) and 0x0F) shl 4) or (index and 0x0F)

            // 組裝自訂協定 payload。
            // 格式: [訊息ID (1 byte)] [分段資訊 (1 byte)] [資料分段 (N bytes)]
            val scanResponsePayload = byteArrayOf(messageId, chunkInfo.toByte()) + chunkData

            val scanResponseData = AdvertiseData.Builder()
                .addServiceData(nearbyShareServiceUuid, scanResponsePayload)
                .build()

            // 開始廣播
            bleManager?.startAdvertising(advertiseData, scanResponseData)

            Log.d("BleChunkSender", "Broadcasting chunk for msg $messageId: ${index + 1}/$totalChunks")

            delay(durationPerChunk)

            // 停止當前分段的廣播，以便在下一個循環中廣播新的分段。
            bleManager?.stopAdvertising()
        }
    }

    /**
     * 解析掃描到的單一廣播封包，將分段資料存入緩衝區。
     * @param scanResult 掃描器回傳的單一裝置結果。
     */
    private fun parseChunkFromScanResult(scanResult: BleScanResult) {
        // 根據 Service UUID 取出我們自訂的 Service Data。
        val serviceData = scanResult.serviceData[nearbyShareServiceUuid] ?: return

        // 驗證資料長度是否至少包含我們的 2-byte 標頭。
        if (serviceData.size < 2) return

        // 第1個位元組：訊息 ID
        val messageId = serviceData[0]
        // 第2個位元組：分段資訊。
        val chunkInfo = serviceData[1].toInt() and 0xFF // 轉為 0-255 的 Int

        // 從分段資訊中解包出「總段數」和「當前索引」。
        val totalChunks = (chunkInfo shr 4) + 1 // 右移4位取得高4位的數值。
        val chunkIndex = chunkInfo and 0x0F      // 與 0x0F (00001111) 做 AND 運算，取得低4位的數值。

        // 資料分段。
        val dataChunk = serviceData.copyOfRange(2, serviceData.size)

        // 將解析出的分段存入緩衝區。
        val messageChunks = chunkBuffer.getOrPut(messageId) { MessageChunks() }
        messageChunks.totalChunks = totalChunks // 更新/記錄此訊息的總段數。
        if (!messageChunks.chunks.containsKey(chunkIndex)) {
            messageChunks.chunks[chunkIndex] = dataChunk
            Log.d("BleChunkReceiver", "Received chunk for msg $messageId: index ${chunkIndex}/${totalChunks - 1}")
        }
    }

    /**
     * 在掃描結束後，檢查緩衝區並重組所有已接收完整的訊息。
     */
    private fun reassembleAndProcessUsers() {
        val completeUserIds = mutableListOf<String>()
        val bufferIterator = chunkBuffer.iterator()

        while (bufferIterator.hasNext()) {
            val (messageId, messageData) = bufferIterator.next()
            if (messageData.totalChunks > 0 && messageData.chunks.size == messageData.totalChunks) {
                val fullPayloadBytes = messageData.chunks.values.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
                val userId = String(fullPayloadBytes, StandardCharsets.UTF_8)
                if (userId != UserProfileFaker.create().id) {
                    completeUserIds.add(userId)
                }
                bufferIterator.remove()
            }
        }

        // 如果有找到 User ID，就去打 API
        if (completeUserIds.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    // 呼叫 repository 來獲取資料
                    val profiles = userRepository.fetchUserProfiles(completeUserIds.distinct())
                    _foundUsers.value = profiles
                } catch (e: Exception) {
                    // 處理 API 錯誤
                    Log.e("SearchNearbyUsersVM", "Failed to fetch user profiles", e)
                    // _foundUsers.value = emptyList() // 或顯示錯誤狀態
                } finally {
                    // 無論成功或失敗，都停止顯示搜尋中的 UI
                    _isSearching.value = false
                }
            }
        } else {
            // 如果沒有找到任何 User ID，直接結束搜尋
            _isSearching.value = false
            _foundUsers.value = emptyList()
        }
    }

    /**
     * 處理新增好友的點擊事件
     */
    fun addFriend(userId: String) {
        viewModelScope.launch {
            // 步驟 1: 立即更新 UI 為 "處理中" 狀態
            _foundUsers.update { currentList ->
                currentList.map {
                    if (it.id == userId) it.copy(friendshipStatus = FriendshipStatus.PENDING)
                    else it
                }
            }

            // 步驟 2: 模擬 API 請求
            delay(1000) // 模擬網路延遲

            // 步驟 3: 更新 UI 為最終狀態 "已是好友"
            _foundUsers.update { currentList ->
                currentList.map {
                    if (it.id == userId) it.copy(friendshipStatus = FriendshipStatus.ALREADY_FRIEND)
                    else it
                }
            }
        }
    }

    /**
     * 處理取消好友邀請的事件
     */
    fun cancelFriendRequest(userId: String) {
        viewModelScope.launch {
            delay(500)

            _foundUsers.update { currentList ->
                currentList.map {
                    if (it.id == userId) it.copy(friendshipStatus = FriendshipStatus.NOT_FRIEND)
                    else it
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        broadcastJob?.cancel()
        bleManager?.release()
    }
}