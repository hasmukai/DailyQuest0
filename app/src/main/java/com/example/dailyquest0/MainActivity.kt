package com.example.dailyquest0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailyquest0.data.AppDatabase
import com.example.dailyquest0.data.repository.AppRepository
import com.example.dailyquest0.ui.navigation.AppNavigation
import com.example.dailyquest0.ui.theme.DailyQuest0Theme
import com.example.dailyquest0.ui.viewmodel.AppViewModel
import com.example.dailyquest0.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao())
        
        enableEdgeToEdge()
        setContent {
            DailyQuest0Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AppViewModel = viewModel(
                        factory = AppViewModelFactory(repository)
                    )
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}