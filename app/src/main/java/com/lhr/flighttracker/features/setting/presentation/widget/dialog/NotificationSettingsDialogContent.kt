package com.lhr.flighttracker.features.setting.presentation.widget.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.room.entity.TrackedFlightEntity
import com.lhr.flighttracker.features.setting.presentation.viewmodel.SettingViewModel

@Composable
fun NotificationSettingsDialogContent(
    onDismissRequest: () -> Unit
) {
    val viewModel: SettingViewModel = hiltViewModel()
    val trackedFlights by viewModel.trackedFlights.collectAsState()
    val langCode by viewModel.languageCodeFlow.collectAsState(initial = "zh-rTW")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.notifications_setting),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (trackedFlights.isEmpty()) {
            Text(
                text = stringResource(id = R.string.cancel_tracked_flights),
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.6f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trackedFlights, key = { it.flightId }) { flight ->
                    TrackedFlightItem(
                        flight = flight,
                        langCode = langCode,
                        onDelete = { viewModel.untrackFlight(flight.flightId) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TrackedFlightItem(
    flight: TrackedFlightEntity,
    langCode: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${flight.airLineName.get(langCode)} ${flight.airLineNum}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(
                    id = R.string.flight_from_time,
                    flight.upAirportName.get(langCode),
                    flight.expectTime
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.untrack_flight),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}