
package com.lhr.flighttracker.features.floorplan.presentation.state

import android.graphics.PointF
import androidx.compose.ui.unit.IntOffset
import com.lhr.flighttracker.features.floorplan.domain.entity.MapDefinition
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute

/**
 * 平面圖 UI 狀態
 *
 * 包含所有與 UI 顯示相關的狀態資料，
 * 使用 data class 確保狀態的不可變性
 */
data class FloorPlanState(

    /** 完整的地圖定義，包含圖片來源、標記、路徑網絡、地圖邊界等 */
    val mapDefinition: MapDefinition? = null,

    /** 當前位置標記，來自 GPS 或其他位置服務 */
    val currentLocationMarker: MapMarker? = null,

    /** 所有要顯示的標記列表，包含固定標記和當前位置 */
    val allMarkers: List<MapMarker> = emptyList(),

    /** 當前的導航路線，null 表示沒有導航 */
    val navigationRoute: NavigationRoute? = null,

    /** 用戶選中的標記 */
    val selectedMarker: MapMarker? = null,

    /** 設備方位角，用於指北針和方向指示 */
    val deviceAzimuth: Float = 0f,

    /** 位置服務是否已啟用 */
    val isLocationEnabled: Boolean = false,

    /** 載入狀態，用於顯示載入指示器 */
    val isLoading: Boolean = false,

    /** 錯誤訊息，用於顯示錯誤提示 */
    val error: String? = null,

    /**
     * 當前縮放比例
     *
     * 範圍通常在 0.1 到 5.0 之間，1.0 表示原始大小
     */
    val scale: Float = 1.0f,

    /**
     * 當前地圖旋轉角度（度）
     *
     * 0 表示正北方向，順時針為正值
     * 範圍：0-360 度
     */
    val rotation: Float = 0f,

    /**
     * 地圖視窗中心點在原始圖片中的座標
     *
     * 座標系統：以圖片左上角為原點 (0,0)
     * 當用戶拖動地圖時會改變此值
     */
    val center: PointF = PointF(0f, 0f),

    /**
     * 所有標記在螢幕上的實際顯示位置
     *
     * Key: 標記的唯一 ID
     * Value: 標記在螢幕上的像素座標 (IntOffset)
     * 會隨縮放、旋轉、平移自動更新
     */
    val markerScreenPositions: Map<Int, IntOffset> = emptyMap(),

    /**
     * 導航路線在螢幕上的顯示位置列表
     *
     * 每個 PointF 代表路線上一個點的螢幕座標
     * 用於繪製導航路線覆蓋層
     */
    val navigationRouteScreenPositions: List<PointF> = emptyList(),

    /** 標記地圖視圖是否已連接 */
    val isMapViewConnected: Boolean = false
)
