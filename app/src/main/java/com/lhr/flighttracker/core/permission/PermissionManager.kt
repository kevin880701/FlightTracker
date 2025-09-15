package com.lhr.flighttracker.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.lhr.flighttracker.core.data.repository.UserPreferencesRepository
import com.lhr.flighttracker.core.permission.dialog.RationaleDialogContent
import com.lhr.flighttracker.core.permission.dialog.SettingsDialogContent
import com.lhr.flighttracker.core.dialog.DialogManager
import com.lhr.flighttracker.core.dialog.showDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

enum class PermissionType(
    val permissions: List<String>,
    val requiredApiLevel: Int? = null
) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    NOTIFICATIONS(
        permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
        requiredApiLevel = Build.VERSION_CODES.TIRAMISU
    ),
    CAMERA(
        permissions = listOf(Manifest.permission.CAMERA)
    ),
    LOCATION(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}

/**
 * 權限管理器，負責處理權限請求的邏輯並提供統一的 [PermissionStatus]。
 */
@OptIn(ExperimentalPermissionsApi::class)
class PermissionManager(
    private val permissionsState: MultiplePermissionsState,
    private val rationale: String
) {
    internal var onGrantedAction: (() -> Unit)? = null

    // 用於追蹤是否已經請求過權限
    private var hasLaunchedRequest by mutableStateOf(false)

    val status: PermissionStatus
        get() = determineStatus()

    /**
     * 發起權限請求。
     */
    fun launchRequest(onGranted: (() -> Unit)? = null) {
        hasLaunchedRequest = true
        // 如果權限已存在，直接執行動作並返回
        if (status is PermissionStatus.Granted) {
            onGranted?.invoke()
            return
        }
        // 將動作儲存起來
        this.onGrantedAction = onGranted
        // 發起系統權限請求
        permissionsState.launchMultiplePermissionRequest()
    }

    /**
     * 根據 Accompanist 的狀態來決定我們自訂的 PermissionStatus。
     */
    private fun determineStatus(): PermissionStatus {
        return when {
            permissionsState.allPermissionsGranted -> {
                PermissionStatus.Granted
            }

            permissionsState.shouldShowRationale -> {
                PermissionStatus.Denied.NeedsRationale(rationale)
            }

            !hasLaunchedRequest -> {
                PermissionStatus.Idle
            }

            else -> {
                PermissionStatus.Denied.Permanently
            }
        }
    }
}

/**
 * 建立並記住一個 [PermissionManager] 的實例。
 *
 * @param permissions 要請求的權限列表。
 * @param rationale 當需要顯示理由時的提示文字。
 * @return 一個管理權限狀態的 PermissionManager 實例。
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionManager(
    permissions: List<PermissionType>,
    rationale: String,
): PermissionManager {
    // 篩選出當前裝置版本需要的權限
    val permissionsToRequest = remember(permissions) {
        permissions
            .filter { Build.VERSION.SDK_INT >= (it.requiredApiLevel ?: 0) }
            .flatMap { it.permissions }
    }

    // 記住 Accompanist 的狀態
    val permissionStates = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    // 建立並記住我們的 PermissionManager
    val permissionManager = remember(permissionStates, rationale) {
        PermissionManager(permissionStates, rationale)
    }


    LaunchedEffect(permissionStates.allPermissionsGranted) {
        if (permissionStates.allPermissionsGranted) {
            permissionManager.onGrantedAction?.invoke()
            permissionManager.onGrantedAction = null
        }
    }

    return permissionManager
}

/**
 * [重載函式] 處理單一權限請求。
 */
@Composable
fun rememberPermissionManager(
    permission: PermissionType,
    rationale: String,
): PermissionManager {
    return rememberPermissionManager(
        permissions = listOf(permission),
        rationale = rationale
    )
}

// openAppSettings 函式維持不變
fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

/**
 * 建立並記住一個完整的權限請求點擊處理器。
 *
 * @param manager PermissionManager 的實例。
 * @param onGranted 當權限成功獲取時要執行的動作。
 * @param onRationaleNeeded 當需要向使用者顯示權限理由時執行的動作。
 * 預設行為是彈出一個說明對話框，使用者確認後會再次發起權限請求。
 * @param onPermanentlyDenied 當權限被永久拒絕時執行的動作。
 * 如果未提供 (null)，預設行為是彈出一個引導使用者前往系統設定的對話框。
 * @return 一個可以直接給 Button 或 IconButton 使用的 () -> Unit lambda。
 */
@Composable
fun rememberPermissionClickHandler(
    manager: PermissionManager,
    onGranted: () -> Unit,
    onRationaleNeeded: (currentStatus: PermissionStatus.Denied.NeedsRationale) -> Unit = { status ->
        showDialog {
            RationaleDialogContent(
                rationaleText = status.rationale,
                onConfirm = {
                    DialogManager.dismissDialog()
                    manager.launchRequest(onGranted = onGranted)
                },
                onDismiss = { DialogManager.dismissDialog() }
            )
        }
    },
    onPermanentlyDenied: (() -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current

    return remember(manager, onGranted) {
        {
            when (val currentStatus = manager.status) {
                is PermissionStatus.Granted -> {
                    onGranted()
                }

                is PermissionStatus.Denied.NeedsRationale -> {
                    onRationaleNeeded(currentStatus)
                }

                is PermissionStatus.Denied.Permanently -> if (onPermanentlyDenied != null) {
                    onPermanentlyDenied()
                } else {
                    showDialog {
                        SettingsDialogContent(
                            onConfirm = {
                                DialogManager.dismissDialog()
                                openAppSettings(context)
                            },
                            onDismiss = { DialogManager.dismissDialog() }
                        )
                    }
                }

                is PermissionStatus.Idle -> {
                    manager.launchRequest(onGranted = onGranted)
                }
            }
        }
    }
}

/**
 * 用於處理「自動啟動權限引導」。
 */
@Singleton
class PermissionGuideManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val _shouldShowPermissionGuide = MutableStateFlow(false)
    val shouldShowPermissionGuide: StateFlow<Boolean> = _shouldShowPermissionGuide

    /**
     * 檢查是否需要觸發權限引導。
     */
    suspend fun checkAndTriggerGuideIfNeeded() {
        val hasShown = userPreferencesRepository.hasShownAutostartGuide.first()
        if (!hasShown && isProblematicManufacturer()) {
            _shouldShowPermissionGuide.value = true
        }
    }

    /**
     * 當引導對話框被使用者關閉時呼叫。
     */
    suspend fun onGuideDismissed() {
        userPreferencesRepository.setAutostartGuideShown(true)
        _shouldShowPermissionGuide.value = false
    }
}