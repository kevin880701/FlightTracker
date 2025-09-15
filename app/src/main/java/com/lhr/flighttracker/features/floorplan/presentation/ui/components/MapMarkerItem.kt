package com.lhr.flighttracker.features.floorplan.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource
import kotlin.math.roundToInt

/**
 * 地圖標記
 */
@Composable
internal fun MapMarkerItem(
    marker: MapMarker,
    screenPosition: IntOffset,
    textAlpha: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                // 將圖片的 dp 尺寸轉換為像素
                val imageSizePx = 28.dp.toPx()
                val x = screenPosition.x - (placeable.width / 2)
                val y = screenPosition.y - (imageSizePx / 2).roundToInt()

                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(x, y)
                }
            }
    ) {
        val painter: Painter = when (val source = marker.imageSource) {
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

        Image(
            painter = painter,
            contentDescription = marker.name,
            modifier = Modifier
                .size(28.dp)
        )

        if (marker.name.isNotBlank()) {
            Text(
                text = marker.name,
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = textAlpha),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}