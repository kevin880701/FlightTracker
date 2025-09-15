package com.lhr.flighttracker.features.map.presentation.widget.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource

@Composable
fun MarkerDetailDialogContent(
    marker: MapMarker,
    onDismissRequest: () -> Unit,
    onNavigateToMarker: ((MapMarker) -> Unit)? = null,
    navigationButtonText: String = "導航",
    showAddToFavorites: Boolean = true
) {
    val painter = when (val source = marker.imageSource) {
        is ImageSource.FromResource -> {
            painterResource(id = source.resourceId)
        }
        is ImageSource.FromAsset -> {
            rememberAsyncImagePainter(model = "file:///android_asset/${source.assetName}")
        }
        is ImageSource.FromUri -> {
            rememberAsyncImagePainter(model = source.uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        // 標題區域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = marker.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = marker.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "關閉"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 詳細資訊卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 描述
                if (marker.description.isNotEmpty()) {
                    DetailItem(
                        iconDrawable = R.drawable.ic_info,
                        label = "描述",
                        value = marker.description
                    )
                }

                // 樓層
                DetailItem(
                    iconDrawable = R.drawable.ic_location,
                    label = "樓層",
                    value = marker.area
                )

                // 營業時間
                if (marker.operatingHours.isNotEmpty()) {
                    DetailItem(
                        iconDrawable = R.drawable.ic_time,
                        label = "營業時間",
                        value = marker.operatingHours
                    )
                }

                // 電話號碼
                if (marker.phoneNumber.isNotEmpty()) {
                    DetailItem(
                        iconDrawable = R.drawable.ic_phone,
                        label = "聯絡電話",
                        value = marker.phoneNumber
                    )
                }

                // 額外資訊
                if (marker.additionalInfo.isNotEmpty()) {
                    DetailItem(
                        iconDrawable = R.drawable.ic_notes,
                        label = "其他資訊",
                        value = marker.additionalInfo
                    )
                }

                // 導航相關資訊
                DetailItem(
                    iconDrawable = R.drawable.ic_navigation,
                    label = "導航支援",
                    value = "支援導航功能"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 操作按鈕
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 導航按鈕 - 根據傳入的參數決定顯示內容
            if (onNavigateToMarker != null) {
                Button(
                    onClick = {
                        onNavigateToMarker(marker)
                        onDismissRequest() // 關閉對話框
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_navigation),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(navigationButtonText)
                }
            } else {
                // 如果不能導航，顯示禁用的按鈕
                OutlinedButton(
                    onClick = { /* 不執行任何操作 */ },
                    enabled = false,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("無法導航")
                }
            }

            // 收藏按鈕 - 可選顯示
            if (showAddToFavorites) {
                OutlinedButton(
                    onClick = { /* TODO: 實現收藏功能 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("收藏")
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    iconDrawable: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = iconDrawable),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}