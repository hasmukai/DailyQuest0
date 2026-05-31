package com.example.dailyquest0.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyquest0.ui.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: AppViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    
    // For MVP, we will fake a simple 30-day contribution graph
    // A full Github-style graph requires tracking dates rigorously and displaying them in a 7xN grid.
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statistics", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lifetime Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total EP Earned: ${userStats?.totalEp ?: 0}", fontSize = 16.sp)
                    Text("Current EP Balance: ${userStats?.currentEp ?: 0}", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Activity (Last 30 Days)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contribution Graph (Simplified MVP)
            // Ideally we fetch daily quest logs, group by date, and map them to a grid.
            // Since we need to wait for the repository to support complex date range queries,
            // we will render a placeholder grid for the UI demo.
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Generate 35 blocks
                items(35) { index ->
                    // For demo, just make some random cells darker
                    val intensity = (index * 7 % 5) // fake intensity 0-4
                    val color = when (intensity) {
                        0 -> Color.LightGray.copy(alpha = 0.3f)
                        1 -> Color(0xFF9BE9A8)
                        2 -> Color(0xFF40C463)
                        3 -> Color(0xFF30A14E)
                        else -> Color(0xFF216E39)
                    }
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Note: Graph currently shows demo data. Connect to real DailyQuestLog table for full MVP.", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
