package com.lhr.flighttracker.features.setting.presentation.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.core.utils.QrCodeGenerator

@Composable
fun QRCodeDisplay(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    topContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    qrCodeColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp
) {
    val qrCodeColorInt = qrCodeColor.toArgb()
    val backgroundColorInt = backgroundColor.toArgb()

    val sizeInPixels = with(LocalDensity.current) { size.toPx() }.toInt()

    val qrBitmap = remember(data, sizeInPixels, qrCodeColor, backgroundColor) {
        QrCodeGenerator.generateQrCodeBitmap(
            data = data,
            width = sizeInPixels,
            height = sizeInPixels,
            qrCodeColor = qrCodeColorInt,
            backgroundColor = backgroundColorInt
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        topContent?.let {
            it()
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .background(backgroundColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code"
                    )
                } else {
                    Text("無法生成 QR Code")
                }
            }

            bottomContent?.let {
                Spacer(modifier = Modifier.height(8.dp))
                it()
            }
        }
    }
}
