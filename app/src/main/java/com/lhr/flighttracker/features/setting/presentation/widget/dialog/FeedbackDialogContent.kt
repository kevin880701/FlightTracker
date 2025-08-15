package com.lhr.flighttracker.features.setting.presentation.widget.dialog

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.R

@Composable
fun FeedbackDialogContent(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val feedbackEmail = "feedback@flighttracker.com"

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
            text = stringResource(id = R.string.feedback),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "如果您有任何建議或遇到問題，請隨時透過電子郵件與我們聯繫：",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = feedbackEmail,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // 只處理郵件
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackEmail))
                        putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                    onDismissRequest()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("發送電子郵件")
            }

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.button_close))
            }
        }
    }
}