package com.lhr.flighttracker.features.setting.presentation.widget.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun TermsOfServiceDialogContent(
    onDismissRequest: () -> Unit
) {
    val terms = listOf(
        stringResource(id = R.string.terms_1),
        stringResource(id = R.string.terms_2),
        stringResource(id = R.string.terms_3),
        stringResource(id = R.string.terms_4),
        stringResource(id = R.string.terms_5),
        stringResource(id = R.string.terms_6),
        stringResource(id = R.string.terms_7),
        stringResource(id = R.string.terms_8),
        stringResource(id = R.string.terms_9)
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
            text = stringResource(id = R.string.terms_of_service),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BoxWithConstraints {
            val maxHeight = this.maxHeight * 0.8f
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
            ) {
                items(terms) { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}