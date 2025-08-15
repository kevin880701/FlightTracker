package com.lhr.flighttracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lhr.flighttracker.core.ui.navigation.AppNavHost
import com.lhr.flighttracker.core.ui.theme.AppTheme
import com.lhr.flighttracker.core.utils.LanguageManager
import com.lhr.flighttracker.core.utils.ResourceProvider
import com.lhr.flighttracker.core.utils.ThemeManager
import com.lhr.flighttracker.core.utils.dialog.DialogView
import com.lhr.flighttracker.core.utils.loading.LoadingType
import com.lhr.flighttracker.core.utils.loading.LoadingView
import com.lhr.flighttracker.core.utils.toast.ToastView
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.repeatOnLifecycle
import com.lhr.flighttracker.core.utils.NetworkMonitor
import kotlinx.coroutines.flow.drop
import androidx.lifecycle.Lifecycle
import com.lhr.flighttracker.core.utils.toast.ToastManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LanguageManagerEntryPoint {
    fun languageManager(): LanguageManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeManagerEntryPoint {
    fun themeManager(): ThemeManager
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}
val LocalStatusBarHeight = staticCompositionLocalOf<Dp> {
    0.dp
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var themeManager: ThemeManager

    override fun attachBaseContext(newBase: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            newBase.applicationContext,
            LanguageManagerEntryPoint::class.java
        )
        val languageManager = entryPoint.languageManager()

        val context = languageManager.applyLanguage(newBase)
        super.attachBaseContext(context)
    }


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        ResourceProvider.init(ResourceProvider(this))

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentTheme by themeManager.appTheme.collectAsState(initial = "light")
            val isDarkTheme = currentTheme == "dark"

            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(networkMonitor) {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    networkMonitor.isNetworkAvailable
                        .drop(1)
                        .onEach { isAvailable ->
                            if (!isAvailable) {
                                ToastManager.showToast(getString(R.string.network_status_disconnected))
                            } else {
                                ToastManager.showToast(getString(R.string.network_status_reconnected))
                            }
                        }
                        .launchIn(this)
                }
            }
            AppContent(isDarkTheme)
        }
    }
    companion object{

    }
}

@Composable
fun AppContent(isDarkTheme: Boolean) {
    // 計算狀態欄高度
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var toastMessage: String? by remember { mutableStateOf(null) }


    LaunchedEffect(Unit) {
        ToastManager.messages.collect { message ->
            toastMessage = message
        }
    }

    CompositionLocalProvider(
        LocalNavController provides rememberNavController(),
        LocalStatusBarHeight provides statusBarHeight
    ) {
        AppTheme(darkTheme = isDarkTheme) {
            Box {
                AppNavHost(navController = LocalNavController.current)
                DialogView()
                LoadingView(loadingType = LoadingType.RingBars(radius = 16.dp))
                ToastView(
                    message = toastMessage,
                    onToastFinished = { toastMessage = null }
                )
            }
        }
    }
}

