package com.lhr.flighttracker.features.map.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * 自定義的地圖標記 Composable
 *
 * @param marker 標記數據
 * @param screenPosition 標記在螢幕上的位置
 * @param onMarkerClick 標記點擊事件的回調
 */
@Composable
fun CustomMarker(
    marker: MapMarker,
    screenPosition: IntOffset,
    textAlpha: Float,
    onMarkerClick: (MapMarker) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                // 將圖標的中心點對準 screenPosition
                val iconSizePx = 36.dp.toPx()
                val x = screenPosition.x - (placeable.width / 2)
                val y = screenPosition.y - (iconSizePx / 2).roundToInt()

                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(x, y)
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick ={
                    onMarkerClick(marker)
                }
            )
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
