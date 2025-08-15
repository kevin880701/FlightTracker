package com.lhr.flighttracker.features.flightScheduled.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.features.flightScheduled.presentation.viewmodel.FlightViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.lhr.flighttracker.features.flightScheduled.presentation.widget.FlightTitleBar
import com.lhr.flighttracker.core.utils.dialog.DialogPosition
import com.lhr.flighttracker.core.utils.dialog.showDialog
import com.lhr.flighttracker.features.flightScheduled.presentation.widget.FilterInfoBar
import com.lhr.flighttracker.features.flightScheduled.presentation.widget.dialog.SearchDialogContent
import androidx.compose.ui.res.stringResource
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.permission.PermissionType
import com.lhr.flighttracker.core.permission.dialog.PermissionGuideDialog
import com.lhr.flighttracker.core.permission.openAutostartSettings
import com.lhr.flighttracker.core.permission.rememberPermissionClickHandler
import com.lhr.flighttracker.core.permission.rememberPermissionManager

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FlightScreen(
    viewModel: FlightViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val flights by viewModel.flights.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastUpdateTime by viewModel.lastUpdateTime.collectAsState()
    val filterFlightNumber by viewModel.filterFlightNumber.collectAsState()
    val filterUpAirport by viewModel.filterUpAirport.collectAsState()

    val isFiltered = !filterFlightNumber.isNullOrBlank() || !filterUpAirport.isNullOrBlank()

    // 被追蹤航班的 ID
    val trackedIds by viewModel.trackedFlightIds.collectAsState()

    val langCode by viewModel.languageCodeFlow.collectAsState(initial = "zh-rTW")

    val showPermissionGuide by viewModel.shouldShowPermissionGuide.collectAsState()

    val notificationManager = rememberPermissionManager(
        permission = PermissionType.NOTIFICATIONS,
        rationale = stringResource(id = R.string.notification_permission_rationale)
    )

    if (showPermissionGuide) {
            PermissionGuideDialog(
                onDismissRequest = { viewModel.onPermissionGuideDismissed() },
                onConfirm = {
                    viewModel.onPermissionGuideDismissed()
                    openAutostartSettings(context)
                }
            )
    }

    Scaffold(
        topBar = {
            FlightTitleBar(
                title = stringResource(id = R.string.screen_title_flight_info),
                lastUpdateTime = lastUpdateTime,
                isLoading = isLoading,
                onSearchClick = {
                    showDialog(
                        position = DialogPosition.BOTTOM,
                        extendToNavigationBar = true,
                        content = {
                            SearchDialogContent(viewModel)
                        }
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    if (flights.isNotEmpty() || isFiltered) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 在篩選狀態下，顯示篩選資訊列
                            if (isFiltered) {
                                FilterInfoBar(
                                    flightNumber = filterFlightNumber,
                                    upAirport = filterUpAirport,
                                    onClearFlightNumber = {
                                        // 清除航班號碼時，保留機場篩選
                                        viewModel.filterFlights("", filterUpAirport ?: "")
                                    },
                                    onClearUpAirport = {
                                        // 清除機場時，保留航班號碼篩選
                                        viewModel.filterFlights(filterFlightNumber ?: "", "")
                                    },
                                    onClearAllFilters = {
                                        // 清除所有篩選
                                        viewModel.filterFlights("", "")
                                    }
                                )
                            }
                            // 航班列表
                            if (flights.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(items = flights, key = { flight ->
                                        "${flight.airLineNum}-${flight.upAirportName.get(langCode)}-${flight.expectTime}"
                                    }) { flight ->
                                        val isTracked = trackedIds.contains(viewModel.generateFlightId(flight))

                                        val airlineName = flight.airLineName.get(langCode)
                                        val upAirportName = flight.upAirportName.get(langCode)

                                        FlightInfoItem(
                                            flightNumberText = "$airlineName ${flight.airLineNum}",
                                            airlineLogoUrl = flight.airLineLogo,
                                            airlineLogoDesc = stringResource(id = R.string.content_description_airline_logo, airlineName),
                                            flightFromText = stringResource(id = R.string.flight_from, upAirportName),
                                            boardingGateText = stringResource(id = R.string.boarding_gate, flight.airBoardingGate),
                                            statusText = stringResource(id = flight.getStatusStringResId()),
                                            statusColor = flight.getStatusColor(),
                                            delayCauseText = flight.airFlyDelayCause,
                                            expectedTimeText = stringResource(id = R.string.expected_time, flight.expectTime),
                                            actualTimeText = stringResource(id = R.string.actual_time, flight.realTime),
                                            isTracked = isTracked,
                                            onTrackClick = rememberPermissionClickHandler(
                                                manager = notificationManager,
                                                onGranted = {
                                                    viewModel.onTrackFlightClicked(flight)
                                                    viewModel.onNotificationPermissionGranted()
                                                }
                                            )
                                        )
                                    }
                                }
                            } else if (!isLoading && isFiltered) {
                                Text(
                                    text = stringResource(id = R.string.no_filtered_flights),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (!isLoading) {
                        Text(
                            text = stringResource(id = R.string.no_flight_info),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun FlightInfoItem(
    flightNumberText: String,
    airlineLogoUrl: String,
    airlineLogoDesc: String,
    flightFromText: String,
    boardingGateText: String,
    statusText: String,
    statusColor: Color,
    delayCauseText: String,
    expectedTimeText: String,
    actualTimeText: String,
    isTracked: Boolean,
    onTrackClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = airlineLogoUrl,
                    contentDescription = airlineLogoDesc,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = flightNumberText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onTrackClick) {
                    Icon(
                        painter = painterResource(id = if (isTracked) R.drawable.ic_notifications_active else R.drawable.ic_notification_add),
                        contentDescription = stringResource(id = if (isTracked) R.string.untrack_flight else R.string.track_flight),
                        tint = if (isTracked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // 航班狀態
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = flightFromText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = boardingGateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (delayCauseText.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(id = R.string.delay_info),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.delay_cause, delayCauseText),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // 時間資訊
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = expectedTimeText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = actualTimeText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}