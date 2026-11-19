package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CalculatorDatabase
import com.example.data.repository.CalculatorRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.BongCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = CalculatorDatabase.getInstance(applicationContext)
        val calculatorRepository = CalculatorRepository(database.calculationHistoryDao())
        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModel.Factory(
                    calculatorRepository = calculatorRepository,
                    preferencesRepository = preferencesRepository
                )
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val effectiveDark = uiState.isDarkTheme || uiState.usePremiumTheme
            LaunchedEffect(effectiveDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (effectiveDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (effectiveDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }

            BongCalculatorTheme(
                darkTheme = uiState.isDarkTheme,
                usePremiumTheme = uiState.usePremiumTheme
            ) {
                CalculatorScreen(viewModel = viewModel)
            }
        }
    }
}
