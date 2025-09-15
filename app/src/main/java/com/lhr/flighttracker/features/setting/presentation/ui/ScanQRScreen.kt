package com.lhr.flighttracker.features.setting.presentation.ui

import android.content.res.Configuration
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lhr.flighttracker.LocalStatusBarHeight
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.AppConstants.APP_QR_SCHEME
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.core.utils.DeviceType
import com.lhr.flighttracker.core.utils.ResourceProvider.Companion.getString
import com.lhr.flighttracker.core.dialog.DialogManager
import com.lhr.flighttracker.core.dialog.showDialog
import com.lhr.flighttracker.core.utils.rememberDeviceType
import com.lhr.flighttracker.core.toast.ToastManager
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.setting.domain.entity.UserProfileFaker
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.UserProfileDialogContent
import java.util.concurrent.Executors
import kotlin.math.min

@Composable
fun ScanQRScreen(navController: NavController) {
    var hasScanned by remember { mutableStateOf(false) }

    BaseScreen(
        extendToStatusBar = true,
        extendToNavigationBar = true,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {

                CameraPreview(
                    onQrCodeScanned = { result ->
                        if (hasScanned) return@CameraPreview

                        if (result.startsWith(APP_QR_SCHEME)) {
                            hasScanned = true
                            val userId = result.removePrefix(APP_QR_SCHEME)
                            val scannedUser = UserProfileFaker.create(id = userId)
                            navController.popBackStack()
                            showDialog {
                                UserProfileDialogContent(
                                    user = scannedUser,
                                    onDismissRequest = { DialogManager.dismissDialog() },
                                    onAddFriendClick = { userToAdd ->
                                        ToastManager.showToast("已發送好友邀請給 ${userToAdd.name}")
                                        DialogManager.dismissDialog()
                                    }
                                )
                            }
                        } else {
                            ToastManager.showToast(getString(R.string.invalid_qr_code))
                        }
                    }
                )

                ScannerOverlay(modifier = Modifier.fillMaxSize())

                Box(
                    modifier = Modifier
                        .padding(top = LocalStatusBarHeight.current)
                ) {
                    MainTitleBar(
                        title = stringResource(id = R.string.scan_qr_code),
                        onBackPress = { navController.popBackStack() },
                        backgroundColor = Color.Transparent,
                        contentColor = Color.White,
                        dividerColor = Color.Transparent
                    )
                }
            }
        }
    )
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun CameraPreview(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        imageProxy.image?.let { mediaImage ->
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (barcodes.isNotEmpty()) {
                                        barcodes.firstOrNull()?.rawValue?.let(onQrCodeScanned)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ScanQRScreen", "ScanQR Error", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("ScanQRScreen", "ScanQR Error", exc)
            }

            previewView
        }
    )
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    // 取得裝置類型和螢幕方向
    val deviceType = rememberDeviceType()
    val orientation = LocalConfiguration.current.orientation
    val isPhoneInLandscape =
        deviceType == DeviceType.PHONE && orientation == Configuration.ORIENTATION_LANDSCAPE

    Canvas(modifier = modifier) {
        val boxSize: Float
        val boxTopLeft: Offset

        if (isPhoneInLandscape) {
            boxSize = size.height * 0.5f
            boxTopLeft = Offset(
                x = (size.width - boxSize) / 2,
                y = (size.height - boxSize) * 0.65f
            )
        } else {
            boxSize = min(size.width, size.height) * 0.7f
            boxTopLeft = Offset(
                x = (size.width - boxSize) / 2,
                y = (size.height - boxSize) / 2
            )
        }


        drawRect(color = Color.Black.copy(alpha = 0.5f))

        drawRoundRect(
            topLeft = boxTopLeft,
            size = Size(boxSize, boxSize),
            color = Color.Transparent,
            blendMode = BlendMode.Clear,
            cornerRadius = CornerRadius(24.dp.toPx())
        )

        val cornerRadius = 24.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 32.dp.toPx()

        val left = boxTopLeft.x
        val top = boxTopLeft.y
        val right = left + boxSize
        val bottom = top + boxSize


        val borderPath = Path().apply {
            // 左上角: 從垂直線段下方開始，向上畫，向右轉彎，再向右畫
            moveTo(left, top + cornerRadius + cornerLength)
            lineTo(left, top + cornerRadius)
            arcTo(
                rect = Rect(
                    left = left,
                    top = top,
                    right = left + cornerRadius * 2,
                    bottom = top + cornerRadius * 2
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left + cornerRadius + cornerLength, top)

            // 右上角: 從水平線段左方開始，向右畫，向下轉彎，再向下畫
            moveTo(right - cornerRadius - cornerLength, top)
            lineTo(right - cornerRadius, top)
            arcTo(
                rect = Rect(
                    left = right - cornerRadius * 2,
                    top = top,
                    right = right,
                    bottom = top + cornerRadius * 2
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(right, top + cornerRadius + cornerLength)

            // 右下角: 從垂直線段上方開始，向下畫，向左轉彎，再向左畫
            moveTo(right, bottom - cornerRadius - cornerLength)
            lineTo(right, bottom - cornerRadius)
            arcTo(
                rect = Rect(
                    left = right - cornerRadius * 2,
                    top = bottom - cornerRadius * 2,
                    right = right,
                    bottom = bottom
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(right - cornerRadius - cornerLength, bottom)

            // 左下角: 從水平線段右方開始，向左畫，向上轉彎，再向上畫
            moveTo(left + cornerRadius + cornerLength, bottom)
            lineTo(left + cornerRadius, bottom)
            arcTo(
                rect = Rect(
                    left = left,
                    top = bottom - cornerRadius * 2,
                    right = left + cornerRadius * 2,
                    bottom = bottom
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left, bottom - cornerRadius - cornerLength)
        }

        drawPath(
            path = borderPath,
            color = Color.White,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}
