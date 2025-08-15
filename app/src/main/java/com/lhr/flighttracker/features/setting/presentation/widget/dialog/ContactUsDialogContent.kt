package com.lhr.flighttracker.features.setting.presentation.widget.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.R

@Composable
fun ContactUsDialogContent(
    onDismissRequest: () -> Unit
) {
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
            text = stringResource(id = R.string.contact_us),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 聯絡資訊
        ContactInfoRow(
            label = "地址",
            value = "台灣台北市信義區松智路1號"
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        ContactInfoRow(
            label = "電話",
            value = "+886-2-1234-5678"
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        ContactInfoRow(
            label = "電子郵件",
            value = "support@flighttracker.com"
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ContactInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}