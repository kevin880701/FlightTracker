package com.lhr.flighttracker.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.lhr.flighttracker.core.location.domain.model.LocationData
import com.lhr.flighttracker.core.location.domain.model.OrientationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * 位置和方向服務管理器
 */
class LocationServiceManager private constructor(
    private val context: Context
) : LocationListener, SensorEventListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 感測器
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // 狀態流
    private val _locationData = MutableStateFlow<LocationData?>(null)
    val locationData: StateFlow<LocationData?> = _locationData.asStateFlow()

    private val _orientationData = MutableStateFlow(OrientationData(0f, 0f, 0f))
    val orientationData: StateFlow<OrientationData> = _orientationData.asStateFlow()

    private val _isLocationEnabled = MutableStateFlow(false)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    // 方向計算用的變數
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var isStarted = false

    /**
     * 開始位置和方向監聽
     */
    fun startLocationTracking() {
        if (isStarted) return

        // 權限檢查
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationService", "啟動追蹤時缺少位置權限，無法繼續。")
            _isLocationEnabled.value = false
            return
        }
        try {
            // 檢查 GPS 和網絡位置提供者
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!gpsEnabled && !networkEnabled) {
                _isLocationEnabled.value = false
                return
            }

            // 請求位置更新
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L, // 最小時間間隔 (毫秒)
                    1f,    // 最小距離變化 (米)
                    this
                )
            }

            if (networkEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }

            // 註冊方向感測器
            rotationSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            // 如果沒有旋轉向量感測器，使用磁力計和加速度計
            if (rotationSensor == null) {
                magnetometer?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
                accelerometer?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
            }

            isStarted = true
            _isLocationEnabled.value = true

        } catch (ex: SecurityException) {
            _isLocationEnabled.value = false
        }
    }

    /**
     * 停止位置和方向監聽
     */
    fun stopLocationTracking() {
        if (!isStarted) return

        locationManager.removeUpdates(this)
        sensorManager.unregisterListener(this)
        isStarted = false
        _isLocationEnabled.value = false
    }

    // LocationListener 接口實現
    override fun onLocationChanged(location: Location) {
        val locationData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            azimuth = if (location.hasBearing()) location.bearing else 0f
        )
        _locationData.value = locationData
    }

    override fun onProviderEnabled(provider: String) {
        _isLocationEnabled.value = true
    }

    override fun onProviderDisabled(provider: String) {
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        _isLocationEnabled.value = gpsEnabled || networkEnabled
    }

    @Deprecated("Deprecated in API level 29")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // 保持空實現以兼容舊版本 API
    }

    // SensorEventListener 接口實現
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                // 使用旋轉向量感測器 (推薦方法)
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientationAngles = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                updateOrientation(orientationAngles)
            }

            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                updateOrientationFromGravityAndMagnetic()
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                updateOrientationFromGravityAndMagnetic()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 可以在這裡處理精度變化
    }

    private fun updateOrientationFromGravityAndMagnetic() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            updateOrientation(orientationAngles)
        }
    }

    private fun updateOrientation(angles: FloatArray) {
        val azimuthRadians = angles[0]
        val pitchRadians = angles[1]
        val rollRadians = angles[2]

        // 轉換為度數並標準化
        var azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
        if (azimuthDegrees < 0) {
            azimuthDegrees += 360f
        }

        val orientationData = OrientationData(
            azimuth = azimuthDegrees,
            pitch = Math.toDegrees(pitchRadians.toDouble()).toFloat(),
            roll = Math.toDegrees(rollRadians.toDouble()).toFloat()
        )

        _orientationData.value = orientationData
    }


    companion object {
        @Volatile
        private var INSTANCE: LocationServiceManager? = null

        fun getInstance(context: Context): LocationServiceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationServiceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
