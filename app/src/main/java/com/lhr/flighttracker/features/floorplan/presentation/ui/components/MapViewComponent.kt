package com.lhr.flighttracker.features.floorplan.presentation.ui.components

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.lhr.flighttracker.core.utils.RotationGestureDetector
import com.lhr.flighttracker.features.floorplan.domain.entity.MapImageSource
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanCallbacks
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanConfiguration
import com.lhr.flighttracker.features.floorplan.domain.entity.MapInitialScaleType
import com.lhr.flighttracker.features.floorplan.presentation.viewmodel.FloorPlanViewModel

/**
 * 地圖視圖組件
 *
 * 負責 SubsamplingScaleImageView 的創建和配置，
 * 處理所有地圖相關的手勢和事件
 */
@Composable
fun MapViewComponent(
    configuration: FloorPlanConfiguration,
    viewModel: FloorPlanViewModel,
    callbacks: FloorPlanCallbacks
) {
    val uiState by viewModel.uiState.collectAsState()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            SubsamplingScaleImageView(context).apply {
                val scaleTypeInt = when (configuration.initialScaleType) {
                    MapInitialScaleType.CENTER_INSIDE -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                    MapInitialScaleType.CENTER_CROP -> SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
                    MapInitialScaleType.CUSTOM -> SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
                    MapInitialScaleType.START -> SubsamplingScaleImageView.SCALE_TYPE_START
                }
                // 載入平面圖圖片資源
                val source = when (val imageSource = configuration.imageSource) {
                    is MapImageSource.FromAsset -> ImageSource.asset(imageSource.assetName)
                    is MapImageSource.FromResource -> ImageSource.resource(imageSource.resourceId)
                    is MapImageSource.FromUri -> ImageSource.uri(imageSource.uri)
                }
                setImage(source)
                // 啟用/禁用平移功能
                setPanEnabled(configuration.panEnabled)
                // 啟用/禁用縮放功能
                isZoomEnabled = configuration.zoomEnabled
                isQuickScaleEnabled = configuration.zoomEnabled
                // 設定初始縮放類型（如置中顯示）
                setMinimumScaleType(scaleTypeInt)

                // 旋轉手勢處理
                val rotationDetector = RotationGestureDetector(object : RotationGestureDetector.OnRotationGestureListener {
                    override fun onRotation(detector: RotationGestureDetector?) {
                        if (configuration.rotationEnabled) {
                            detector?.let { gestureDetector ->
                                // 計算旋轉角度，rotationSensitivity為靈敏度
                                val deltaAngle = gestureDetector.angle * configuration.rotationSensitivity
                                val newRotation = uiState.rotation + deltaAngle
                                viewModel.updateMapState(newRotation = newRotation)
                                callbacks.onRotationChanged(newRotation)
                            }
                        }
                    }
                })

                setOnTouchListener(View.OnTouchListener { _, motionEvent ->
                    if (configuration.rotationEnabled) {
                        rotationDetector.onTouchEvent(motionEvent)
                        if (motionEvent.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                            // 保存當前旋轉角度作為基準
                        }
                    }
                    onTouchEvent(motionEvent)
                    true
                })

                setOnStateChangedListener(object : SubsamplingScaleImageView.OnStateChangedListener {
                    override fun onScaleChanged(newScale: Float, origin: Int) {
                        viewModel.updateMapState(newScale = newScale)
                        callbacks.onScaleChanged(newScale)
                    }

                    override fun onCenterChanged(newCenter: PointF?, origin: Int) {
                        newCenter?.let { centerPoint ->
                            viewModel.updateMapState(newCenter = centerPoint)
                            callbacks.onCenterChanged(centerPoint)
                        }
                    }
                })

                setOnImageEventListener(object: SubsamplingScaleImageView.DefaultOnImageEventListener() {
                    override fun onReady() {
                        // 檢查圖片是否正確載入
                        if (sWidth <= 0 || sHeight <= 0) {
                            return
                        }

                        viewModel.setMapView(this@apply)

                        viewModel.updateMapState(newScale = scale)
                        val fitToScreenScale = minScale
                        setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
                        minScale = fitToScreenScale * configuration.minScaleFactor
                        viewModel.updateAllPositions()
                    }
                })
            }
        },
        update = { view ->
            view.rotation = uiState.rotation
        }
    )
}