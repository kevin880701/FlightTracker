package com.lhr.flighttracker.features.floorplan.presentation.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.lhr.flighttracker.R

/**
 * 位置按鈕組件
 */
@Composable
fun LocationButton(
    isLocationEnabled: Boolean,
    hasCurrentLocation: Boolean,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onLocationClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_location
            ),
            contentDescription = when {
                isLocationEnabled && hasCurrentLocation -> "移至我的位置"
                isLocationEnabled -> "獲取我的位置"
                else -> "開啟位置服務"
            },
            tint = when {
                isLocationEnabled && hasCurrentLocation -> Color.Blue
                isLocationEnabled -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
