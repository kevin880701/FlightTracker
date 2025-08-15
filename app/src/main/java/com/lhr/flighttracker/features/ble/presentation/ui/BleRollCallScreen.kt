package com.lhr.flighttracker.features.ble.presentation.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.lhr.flighttracker.features.ble.presentation.viewmodel.AppRole
import com.lhr.flighttracker.features.ble.presentation.viewmodel.BleDevice
import com.lhr.flighttracker.features.ble.presentation.viewmodel.BleRollCallViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BleRollCallScreen(
    viewModel: BleRollCallViewModel = hiltViewModel()
) {
    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(permissions = listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT))
    } else {
        rememberMultiplePermissionsState(permissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION))
    }
    LaunchedEffect(Unit) {
        if (!blePermissions.allPermissionsGranted) {
            blePermissions.launchMultiplePermissionRequest()
        }
    }

    val role by viewModel.currentRole.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val foundDevices by viewModel.foundDevices.collectAsState()
    val receivedPingInfo by viewModel.receivedPingInfo.collectAsState()
    val myReplyInfo by viewModel.myReplyInfo.collectAsState() // 收集新狀態

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!blePermissions.allPermissionsGranted) {
            Text("請先授予所有藍牙權限才能使用此功能", color = MaterialTheme.colorScheme.error)
            return
        }

        Text("選擇此手機的角色", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { viewModel.startMasterRole() },
                enabled = role == AppRole.IDLE,
                modifier = Modifier.weight(1f)
            ) { Text("我當主控端 (發送點名)") }
            Button(
                onClick = { viewModel.startResponderRole() },
                enabled = role == AppRole.IDLE,
                modifier = Modifier.weight(1f)
            ) { Text("我當回應端 (等待點名)") }
        }

        if (role != AppRole.IDLE) {
            Button(
                onClick = { viewModel.stopAllBleActivities() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("停止當前活動") }
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 顯示收到的點名廣播
        if (receivedPingInfo != null) {
            Text("收到的點名廣播資訊:", style = MaterialTheme.typography.titleMedium)
            DeviceCard(device = receivedPingInfo!!)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 顯示我方回覆的廣播內容
        if (myReplyInfo != null) {
            Text("我回覆的廣播內容:", style = MaterialTheme.typography.titleMedium)
            DeviceCard(device = myReplyInfo!!)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 顯示收到的回應列表
        if (foundDevices.isNotEmpty()) {
            Text("收到的回應列表:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foundDevices.toList()) { device ->
                    DeviceCard(device = device)
                }
            }
        }
    }
}

@Composable
fun DeviceCard(device: BleDevice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "地址: ${device.address}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "廣播的 Service UUIDs:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            device.uuids.forEach { uuid ->
                Text(
                    text = "- ${uuid.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}