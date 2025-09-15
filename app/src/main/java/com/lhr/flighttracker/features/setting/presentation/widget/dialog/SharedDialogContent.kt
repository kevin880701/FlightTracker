package com.lhr.flighttracker.features.setting.presentation.widget.dialog

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.R

@Composable
fun SharedDialogContent(
    onDismissRequest: () -> Unit,
    onQrShareClick: () -> Unit,
    onNearbyShareClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

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
            text = stringResource(id = R.string.share),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ShareOptionRow(
            optionText = stringResource(id = R.string.qr_share),
            onClick = {
                onQrShareClick()
                onDismissRequest()
            }
        )
        HorizontalDivider()

        ShareOptionRow(
            optionText = stringResource(id = R.string.share_with_nearby_users),
            onClick = {
                onNearbyShareClick()
                onDismissRequest()
            }
        )
        HorizontalDivider()
    }
}

@Composable
fun ShareOptionRow(
    optionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = optionText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
