package com.lhr.flighttracker.features.setting.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.AppConstants.APP_QR_SCHEME
import com.lhr.flighttracker.core.permission.PermissionType
import com.lhr.flighttracker.core.permission.rememberPermissionClickHandler
import com.lhr.flighttracker.core.permission.rememberPermissionManager
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.setting.domain.entity.UserProfileFaker
import com.lhr.flighttracker.features.setting.presentation.widget.QRCodeDisplay

@Composable
fun QRShareScreen(navController: NavController) {

    val user = remember { UserProfileFaker.create() }

    // QR Code 尺寸
    val configuration = LocalConfiguration.current
    val qrCodeSize = (configuration.screenWidthDp * 0.75).dp

    val cameraPermissionManager = rememberPermissionManager(
        permission = PermissionType.CAMERA,
        rationale = stringResource(R.string.camera_permission_rationale)
    )

    val navigateToScanWithPermission = rememberPermissionClickHandler(
        manager = cameraPermissionManager,
        onGranted = {
            navController.navigate("ScanQrScreen")
        }
    )

    BaseScreen(
        extendToStatusBar = false,
        extendToNavigationBar = true,
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainTitleBar(
                    title = stringResource(id = R.string.share_user_profile),
                    onBackPress = {
                        navController.popBackStack()
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                QRCodeDisplay(
                    data = "$APP_QR_SCHEME${user.id}",
                    size = qrCodeSize,
                    cornerRadius = 36.dp,
                    qrCodeColor = MaterialTheme.colorScheme.primary,
                    bottomContent = {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(800), fontSize = 24.sp),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        // TODO: Implement "share QR code" logic
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.share_qr_code),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = navigateToScanWithPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_code_scanner),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.scan_qr_code),
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    )
}
