package com.lhr.flighttracker.core.utils

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * 定義一個 enum 來表示不同的裝置類型，方便類型安全檢查。
 */
enum class DeviceType {
    PHONE,
    TABLET,
    TV,
    WEAR_OS,
    AUTOMOTIVE,
    DESKTOP // Chrome OS
}

/**
 * 用於確認並記住當前的裝置類型。
 *
 * @return 回傳當前的 [DeviceType]。
 */
@Composable
fun rememberDeviceType(): DeviceType {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    return remember {
        // 使用 UiModeManager 檢查 TV, Wear, Automotive
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        when (uiModeManager.currentModeType) {
            Configuration.UI_MODE_TYPE_TELEVISION -> return@remember DeviceType.TV
            Configuration.UI_MODE_TYPE_WATCH -> return@remember DeviceType.WEAR_OS
            Configuration.UI_MODE_TYPE_CAR -> return@remember DeviceType.AUTOMOTIVE
        }

        // 檢查是否為桌面環境 (主要是 Chrome OS)
        if (context.packageManager.hasSystemFeature("org.chromium.arc")) {
            return@remember DeviceType.DESKTOP
        }

        // 根據螢幕最小寬度來判斷是手機還是平板
        if (configuration.smallestScreenWidthDp >= 600) {
            DeviceType.TABLET
        } else {
            DeviceType.PHONE
        }
    }
}