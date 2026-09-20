package com.example.operacaopipa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.operacaopipa.data.local.AppDatabase
import com.example.operacaopipa.data.repository.PipaRepository
import com.example.operacaopipa.ui.screens.AdminScreen
import com.example.operacaopipa.ui.screens.DriverScreen
import com.example.operacaopipa.ui.screens.LoginScreen
import com.example.operacaopipa.ui.theme.OperacaoPipaTheme
import com.example.operacaopipa.ui.viewmodel.AppScreen
import com.example.operacaopipa.ui.viewmodel.PipaViewModel
import com.example.operacaopipa.ui.viewmodel.PipaViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO))
        val repository = PipaRepository(database.pipaDao())
        val factory = PipaViewModelFactory(repository)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[PipaViewModel::class.java]

        setContent {
            OperacaoPipaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: PipaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (currentScreen) {
        AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
        AppScreen.ADMIN_DASHBOARD -> AdminScreen(viewModel = viewModel)
        AppScreen.DRIVER_DASHBOARD -> DriverScreen(viewModel = viewModel)
    }
}
