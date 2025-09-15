package com.lhr.flighttracker.features.floorplan.domain.entity

/**
 * 地圖初始縮放類型的內部定義
 */
enum class MapInitialScaleType {
    /** 居中顯示，確保圖片完整可見 */
    CENTER_INSIDE,
    /** 居中裁剪，填滿視圖 */
    CENTER_CROP,
    /** 使用自訂的最小縮放比例 */
    CUSTOM,
    /** 從視圖起始點開始顯示 */
    START
}