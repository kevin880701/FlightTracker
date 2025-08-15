package com.lhr.flighttracker.core.permission

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import java.util.Locale

/**
 * 檢查是否為已知有嚴格後台限制的製造商。
 */
fun isProblematicManufacturer(): Boolean {
    val problematicBrands = listOf("xiaomi", "oppo", "vivo", "huawei", "meizu", "oneplus", "samsung")
    return problematicBrands.contains(Build.MANUFACTURER.lowercase(Locale.ROOT))
}

/**
 * 盡最大努力嘗試開啟各家廠商的「自動啟動」或「應用程式」設定頁面。
 */
fun openAutostartSettings(context: Context) {
    val intents = listOf(
        // 小米
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        // 華為
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
        // OPPO
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
        // vivo
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        // 三星 (跳轉到電池設定)
        Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
        // 一加
        Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"))
    )

    var didLaunch = false
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            didLaunch = true
            break
        } catch (e: ActivityNotFoundException) {
            // 當前裝置上沒有這個 Intent，繼續嘗試下一個
        }
    }

    // 如果所有特定 Intent 都失敗了，則跳轉到通用的應用程式詳情頁作為備案
    if (!didLaunch) {
        Toast.makeText(context, "無法自動跳轉，請手動前往設定", Toast.LENGTH_SHORT).show()
        try {
            val genericIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            genericIntent.data = Uri.fromParts("package", context.packageName, null)
            genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(genericIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}