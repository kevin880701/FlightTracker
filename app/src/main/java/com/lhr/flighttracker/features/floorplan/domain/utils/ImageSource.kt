package com.lhr.flighttracker.features.floorplan.domain.utils

import android.net.Uri
import androidx.annotation.DrawableRes

/**
 * 圖片來源的類別
 */
sealed class ImageSource {
    data class FromAsset(val assetName: String) : ImageSource()
    data class FromResource(@DrawableRes val resourceId: Int) : ImageSource()
    data class FromUri(val uri: Uri) : ImageSource()
}