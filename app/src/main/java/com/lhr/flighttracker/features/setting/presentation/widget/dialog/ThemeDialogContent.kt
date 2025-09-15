package com.lhr.flighttracker.features.setting.presentation.widget.dialog

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.setting.domain.entity.ThemeItem
import com.lhr.flighttracker.features.setting.presentation.viewmodel.SettingViewModel
import kotlinx.coroutines.launch

@Composable
fun ThemeDialogContent(
    onDismissRequest: () -> Unit
) {
    val viewModel: SettingViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val themeOptions = listOf(
        ThemeItem("light", stringResource(id = R.string.light_theme)),
        ThemeItem("dark", stringResource(id = R.string.dark_theme))
    )

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
            text = stringResource(id = R.string.choose_theme),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(themeOptions) { themeItem ->
                ThemeItemRow(
                    themeName = themeItem.themeName,
                    onClick = {
                        scope.launch {
                            viewModel.updateTheme(themeItem.themeCode)
                            onDismissRequest()
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ThemeItemRow(
    themeName: String,
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
            text = themeName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}