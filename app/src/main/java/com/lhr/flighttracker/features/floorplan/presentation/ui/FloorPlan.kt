package com.lhr.flighttracker.features.floorplan.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanCallbacks
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanConfiguration
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanController
import com.lhr.flighttracker.features.floorplan.presentation.viewmodel.FloorPlanViewModel

object FloorPlan {

    const val VERSION = "1.0.0"
    const val NAME = "FloorPlan"

    /**
     * 主要的 Composable 函數 (重構後)
     *
     * @param configuration 靜態配置
     * @param controller 外部控制器，透過 FloorPlan.rememberController() 創建
     * @param modifier Modifier
     * @param callbacks 事件回調
     */
    @Composable
    fun View(
        configuration: FloorPlanConfiguration,
        controller: FloorPlanController,
        modifier: Modifier = Modifier,
        callbacks: FloorPlanCallbacks = FloorPlanCallbacks()
    ) {

        FloorPlanView(
            configuration = configuration,
            modifier = modifier,
            callbacks = callbacks,
            controller = controller
        )
    }

    /**
     * 創建 FloorPlan Controller 的便利函數
     */
    @Composable
    fun rememberController(
        viewModel: FloorPlanViewModel = hiltViewModel()
    ): FloorPlanController {
        return remember(viewModel) {
            FloorPlanController(viewModel)
        }
    }
}