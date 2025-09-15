package com.lhr.flighttracker.core.location.domain.model

/**
 * 用於儲存裝置方向感測器數據的資料類別。
 */
data class OrientationData(
    /**
     * 方位角 (Azimuth)。
     * 表示裝置 Y 軸（頂部）所指向的羅盤方向。
     * 單位為度，範圍從 0 到 360。
     * 0 = 正北方, 90 = 正東方, 180 = 正南方, 270 = 正西方。
     */
    val azimuth: Float,

    /**
     * 俯仰角 (Pitch)。
     * 表示裝置 X 軸的旋轉角度。
     * 當裝置平放時為 0 度。當裝置頂部向上翹起時為正值，
     * 頂部向下俯時為負值。範圍通常在 -90 到 90 度之間。
     */
    val pitch: Float,

    /**
     * 翻滾角 (Roll)。
     * 表示裝置 Z 軸的旋轉角度。
     * 當裝置平放時為 0 度。當裝置向左側翻滾時為正值，
     * 向右側翻滾時為負值。範圍通常在 -180 到 180 度之間。
     */
    val roll: Float,

    /**
     * 此方向數據被記錄時的時間戳記。
     * 單位為毫秒 (milliseconds)，表示從 UTC 1970年1月1日 00:00:00 以來的時間。
     */
    val timestamp: Long = System.currentTimeMillis()
)