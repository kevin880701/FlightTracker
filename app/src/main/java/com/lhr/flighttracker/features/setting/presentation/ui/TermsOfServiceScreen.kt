package com.lhr.flighttracker.features.setting.presentation.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TermsOfServiceScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    val url = "https://sudden-gallimimus-ad4.notion.site/Privacy-Policy-256df9661292806a9341c33eca6f0a3a"

    BaseScreen(
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                MainTitleBar(
                    title = stringResource(id = R.string.terms_of_service),
                    onBackPress = { navController.popBackStack() },
                    testTag = "terms_of_service_screen_title_bar"
                )

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("terms_of_service_web_view")
                        .semantics {testTagsAsResourceId = true},
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }
                            }
                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        // 可選：當狀態改變時更新 WebView
                    }
                )
            }
        }
    )
}