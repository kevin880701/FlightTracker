package com.lhr.flighttracker.features.floorplan.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Looper
import com.lhr.flighttracker.R
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.lhr.flighttracker.features.floorplan.domain.entity.GpsCoordinate
import com.lhr.flighttracker.features.floorplan.domain.entity.MapBounds
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.MarkerType
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * 位置數據源實作 (修正最終版)
 *
 * 封裝了與 Android Location Services 和 SensorManager 的所有互動。
 * 使用 callbackFlow 將傳統的 callback-based API 轉換為現代的 Kotlin Flow。
 * 完全獨立於 Presentation 層。
 */
@Singleton
class LocationProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    // 用於方位角計算
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    /**
     * 監聽位置更新的 Flow
     */
    @SuppressLint("MissingPermission") // 權限檢查由外部 UI 層負責，這裡假設權限已授予
    override fun listenToLocation(): Flow<MapMarker?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close(SecurityException("Location permission not granted."))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(2000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val mapCoordinates = LocationConverter.convertGpsToMapCoordinates(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        mapBounds = FloorPlanConstants.AIRPORT_MAP_BOUNDS
                    )

                    val marker = mapCoordinates?.let { coords ->
                        MapMarker(
                            id = -1,
                            name = "我的位置",
                            coordinates = coords,
                            imageSource = ImageSource.FromResource(R.drawable.ic_navigation),
                            type = MarkerType.CURRENT_LOCATION,
                            description = "GPS 精度: ${location.accuracy.roundToInt()}m",
                            area = "機場內",
                            isAccessible = true,
                            searchKeywords = listOf("我的位置", "current location", "GPS"),
                            category = "位置服務",
                            customProperties = mapOf(
                                "accuracy" to "${location.accuracy.roundToInt()}",
                                "latitude" to location.latitude.toString(),
                                "longitude" to location.longitude.toString(),
                                "provider" to (location.provider ?: "unknown"),
                                "timestamp" to location.time.toString()
                            )
                        )
                    }
                    trySend(marker)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * 監聽方位角的 Flow
     */
    override fun listenToAzimuth(): Flow<Float> = callbackFlow {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }

                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val azimuthInRadians = orientationAngles[0]
                val azimuthInDegrees = (Math.toDegrees(azimuthInRadians.toDouble()) + 360) % 360
                trySend(azimuthInDegrees.toFloat())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    /**
     * 監聽 GPS 服務開關狀態的 Flow
     */
    override fun listenToLocationEnabled(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    trySend(hasLocationPermission() && isGpsEnabled())
                }
            }
        }

        // 立即發送一次當前狀態（權限 + GPS）
        trySend(hasLocationPermission() && isGpsEnabled())

        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    /**
     * 請求一次性的位置更新
     */
    @SuppressLint("MissingPermission")
    override fun requestLocationUpdate() {
        if (!hasLocationPermission()) return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener {
                // 成功獲取後，結果會透過 listenToLocation() 的 Flow 自動發送出去
            }
            .addOnFailureListener {
                // 可選：處理獲取失敗的情況
            }
    }

    private fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


}

/**
 * 地圖邊界常數
 * 為了保持 LocationProviderImpl 的獨立性，常數定義在此處或相關的 data 層檔案中
 */
private object FloorPlanConstants {
    val AIRPORT_MAP_BOUNDS = MapBounds(
        northEast = GpsCoordinate(24.1501847, 120.648122),
        southWest = GpsCoordinate(24.1458847, 120.643822),
        mapWidth = 2288f,
        mapHeight = 1231f
    )
}

/**
 * 座標轉換工具
 * 封裝 GPS 座標到地圖像素座標的轉換邏輯
 */
private object LocationConverter {
    fun convertGpsToMapCoordinates(latitude: Double, longitude: Double, mapBounds: MapBounds): Offset? {
        val isLatInBounds = latitude in mapBounds.southWest.latitude..mapBounds.northEast.latitude
        val isLonInBounds = longitude in mapBounds.southWest.longitude..mapBounds.northEast.longitude
        if (!isLatInBounds || !isLonInBounds) {
            return null
        }

        val latFraction = (latitude - mapBounds.southWest.latitude) / (mapBounds.northEast.latitude - mapBounds.southWest.latitude)
        val lonFraction = (longitude - mapBounds.southWest.longitude) / (mapBounds.northEast.longitude - mapBounds.southWest.longitude)

        val mapX = (lonFraction * mapBounds.mapWidth).toFloat()
        val mapY = ((1 - latFraction) * mapBounds.mapHeight).toFloat()

        return Offset(mapX, mapY)
    }
}