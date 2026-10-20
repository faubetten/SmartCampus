package com.smartcampus.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartcampus.client.ui.navigation.AppNavGraph
import com.smartcampus.client.ui.theme.SmartCampusClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Compose builds the single screen displayed by this activity.
        setContent {
            SmartCampusClientTheme {
                AppNavGraph()
            }
        }
    }
}
