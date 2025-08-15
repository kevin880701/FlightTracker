package com.lhr.flighttracker.features.flightScheduled.presentation.widget.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.features.flightScheduled.presentation.viewmodel.FlightViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lhr.flighttracker.core.utils.dialog.DialogManager.dismissDialog
import androidx.compose.ui.res.stringResource
import com.lhr.flighttracker.R

@Composable
fun SearchDialogContent(
    viewModel: FlightViewModel = hiltViewModel()
) {
    var flightNumber by remember { mutableStateOf("") }
    var upAirport by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.flight_filter),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = flightNumber,
            onValueChange = { flightNumber = it },
            label = { Text(stringResource(id = R.string.flight_number)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = upAirport,
            onValueChange = { upAirport = it },
            label = { Text(stringResource(id = R.string.up_airport)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    viewModel.filterFlights(flightNumber, upAirport)
                    dismissDialog()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.filter))
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(
                onClick = {
                    flightNumber = ""
                    upAirport = ""
                    viewModel.filterFlights("", "")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.clear))
            }
        }
    }
}