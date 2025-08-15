package com.lhr.flighttracker.features.flightScheduled.presentation.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.lhr.flighttracker.R

@Composable
fun FilterInfoBar(
    flightNumber: String?,
    upAirport: String?,
    onClearFlightNumber: () -> Unit,
    onClearUpAirport: () -> Unit,
    onClearAllFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!flightNumber.isNullOrBlank()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(stringResource(id = R.string.filter_label_flight_number, flightNumber)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.clear_flight_number_filter),
                                modifier = Modifier
                                    .size(AssistChipDefaults.IconSize)
                                    .clickable(onClick = onClearFlightNumber)
                            )
                        }
                    )
                }

                // 起飛機場篩選標籤
                if (!upAirport.isNullOrBlank()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(stringResource(id = R.string.filter_label_up_airport, upAirport)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.clear_up_airport_filter),
                                modifier = Modifier
                                    .size(AssistChipDefaults.IconSize)
                                    .clickable(onClick = onClearUpAirport)
                            )
                        }
                    )
                }
            }

            IconButton(onClick = onClearAllFilters) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.clear_all_filters),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}