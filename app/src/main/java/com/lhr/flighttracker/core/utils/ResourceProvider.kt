package com.lhr.flighttracker.core.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceProvider @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private lateinit var instance: ResourceProvider

        fun init(provider: ResourceProvider) {
            instance = provider
        }

        fun getString(resId: Int): String {
            return instance.context.getString(resId)
        }

        fun getString(resId: Int, vararg formatArgs: Any): String {
            return instance.context.getString(resId, *formatArgs)
        }

        fun getColor(resId: Int): Color {
            return Color(instance.context.getColor(resId))
        }

        @Composable
        fun getDrawable(resId: Int): Painter {
            return painterResource(resId)
        }
    }
}
