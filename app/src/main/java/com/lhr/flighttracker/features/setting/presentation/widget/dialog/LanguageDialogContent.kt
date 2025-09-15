package com.lhr.flighttracker.features.setting.presentation.widget.dialog

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.setting.domain.entity.LanguageItem
import com.lhr.flighttracker.features.setting.presentation.viewmodel.SettingViewModel
import kotlinx.coroutines.launch

@Composable
fun LanguageDialogContent(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val viewModel: SettingViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val languages = listOf(
        LanguageItem("zh-rTW", stringResource(id = R.string.language_zh_tw)),
        LanguageItem("en", stringResource(id = R.string.language_en))
    )

    BoxWithConstraints {
        val maxHeight = this.maxHeight * 0.7f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.select_language),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(languages) { languageItem ->
                        LanguageItemRow(
                            languageName = languageItem.languageName,
                            onClick = {
                                scope.launch {
                                    viewModel.updateLanguage(languageItem.languageCode)
                                    onDismissRequest()
                                    activity?.recreate()
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageItemRow(
    languageName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = languageName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}