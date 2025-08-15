package com.lhr.flighttracker.features.main.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.utils.ResourceProvider.Companion.getString

sealed class Screen(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit
) {
    data object Flight : Screen(
        route = "flight_screen",
        title = getString(R.string.flight),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_flight),
                contentDescription = stringResource(id = R.string.flight)
            )
        }
    )

    data object ExchangeRate : Screen(
        route = "exchange_rate_screen",
        title = getString(R.string.exchange_rate),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_coin),
                contentDescription = stringResource(id = R.string.exchange_rate)
            )
        }
    )

    data object Settings : Screen(
        route = "settings_screen",
        title = getString(R.string.settings),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = stringResource(id = R.string.settings)
            )
        }
    )

    data object BleRollCall : Screen(
        route = "ble_screen",
        title = getString(R.string.bluetooth),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_bluetooth),
                contentDescription = stringResource(id = R.string.bluetooth)
            )
        }
    )
}