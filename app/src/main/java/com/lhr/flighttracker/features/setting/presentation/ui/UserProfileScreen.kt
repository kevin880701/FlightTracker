package com.lhr.flighttracker.features.setting.presentation.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.core.ui.theme.unverifiedRed
import com.lhr.flighttracker.core.ui.theme.verifiedGreen
import com.lhr.flighttracker.core.dialog.DialogManager.dismissDialog
import com.lhr.flighttracker.core.dialog.DialogPosition
import com.lhr.flighttracker.core.dialog.showDialog
import com.lhr.flighttracker.core.utils.ResourceProvider.Companion.getString
import com.lhr.flighttracker.core.toast.ToastManager
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.setting.domain.entity.UserProfile
import com.lhr.flighttracker.features.setting.domain.entity.UserProfileFaker
import com.lhr.flighttracker.features.setting.presentation.widget.SettingItem
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.SharedDialogContent

@Composable
fun UserProfileScreen(navController: NavController) {
    val user = remember { UserProfileFaker.create() }

    // 需要的藍牙權限
    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        // 這是使用者選擇權限後的結果回呼
        val allGranted = permissionsResult.values.all { it }
        if (allGranted) {
            // 如果所有權限都被授予，則導航到目標畫面
            navController.navigate("SearchNearbyUsersScreen")
        } else {
            // 如果有權限被拒絕，給予提示
            ToastManager.showToast(getString(R.string.permissions_required_for_nearby_share))
        }
    }

    BaseScreen(
        extendToStatusBar = false,
        extendToNavigationBar = true,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {

                MainTitleBar(
                    title = stringResource(id = R.string.user_profile),
                    onBackPress = {
                        navController.popBackStack()
                    },
                    actions = {
                        IconButton(onClick = {
                            showDialog(
                                position = DialogPosition.BOTTOM,
                                extendToNavigationBar = true,
                                content = {
                                    SharedDialogContent(
                                        onDismissRequest = {
                                            dismissDialog()
                                        },
                                        onQrShareClick = {
                                            navController.navigate("QrShareScreen")
                                        },
                                        onNearbyShareClick = {
                                            permissionLauncher.launch(blePermissions.toTypedArray())
                                        },
                                    )
                                }
                            )
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_id_card),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = stringResource(R.string.share),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    UserAvatar(avatarUrl = user.avatarUrl)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 使用者名稱
                    Text(
                        text = user.name, // 從 user 物件讀取
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // 帳號資訊 (將 user 物件傳遞下去) - 已重構
                    AccountInfoSection(user = user)
                    Spacer(modifier = Modifier.height(24.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    DeleteAccountButton {
                        // 在此處理刪除帳號的邏輯
                    }
                }
            }
        }
    )
}

@Composable
private fun UserAvatar(avatarUrl: String?) {
    if (avatarUrl.isNullOrBlank()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_account_circle),
            contentDescription = stringResource(R.string.user_avatar),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(120.dp)
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = R.drawable.ic_account_circle),
            error = painterResource(id = R.drawable.ic_account_circle),
            contentDescription = stringResource(R.string.user_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun AccountInfoSection(user: UserProfile) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingItem(
            title = stringResource(id = R.string.email),
            subtitle = user.email,
            onClick = { /* TODO: 處理點擊事件，例如修改 Email */ },
            trailingContent = {
                VerificationStatus(isVerified = user.isEmailVerified)
            },
        )
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        SettingItem(
            title = stringResource(id = R.string.phone_number),
            subtitle = user.phoneNumber,
            onClick = { /* TODO: 處理點擊事件，例如修改電話號碼 */ },
            trailingContent = {
                VerificationStatus(isVerified = user.isPhoneNumberVerified)
            },
        )
    }
}

@Composable
private fun VerificationStatus(isVerified: Boolean) {
    Text(
        text = if (isVerified) stringResource(id = R.string.verified) else stringResource(id = R.string.unverified),
        color = if (isVerified) verifiedGreen else unverifiedRed,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun DeleteAccountButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.delete_account),
            color = MaterialTheme.colorScheme.error
        )
    }
}