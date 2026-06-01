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
import com.example.dailyquest0.utils.DateUtils
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: AppViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    val activityStats by viewModel.activityStats.collectAsState()
    
    val logicalDate = DateUtils.getLogicalDate()
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
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
            
            Text("Activity (Last 10 Weeks)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contribution Graph
            val startDate = logicalDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY)).minusWeeks(9)
            val days = (0..69).map { startDate.plusDays(it.toLong()) }
            
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val labelWidth = 24.dp
                val spacing = 4.dp
                val availableForWeeks = maxWidth - labelWidth - (spacing * 10)
                val boxSize = availableForWeeks / 10
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // Day of Week Labels
                    Column {
                        Spacer(modifier = Modifier.height(14.dp)) // Matches date text height
                        Spacer(modifier = Modifier.height(spacing))
                        Column(
                            modifier = Modifier
                                .width(labelWidth)
                                .height(boxSize * 7 + spacing * 6)
                                .padding(end = 4.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                        Text("Sun", fontSize = 10.sp, color = Color.Gray)
                        Text("Sat", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                                val weeks = days.chunked(7)
                    for (week in weeks) {
                        Column(
                            modifier = Modifier.width(boxSize),
                            verticalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                        val firstDay = week.first()
                        Text(
                            text = "${firstDay.monthValue}/${firstDay.dayOfMonth}",
                            fontSize = 8.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.align(Alignment.CenterHorizontally).height(14.dp)
                        )
                                                Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                            for (date in week) {
                            val dateStr = date.format(dateFormatter)
                            val count = activityStats[dateStr] ?: 0
                            
                            val color = when {
                                count == 0 -> Color.LightGray.copy(alpha = 0.3f)
                                count == 1 -> Color(0xFF9BE9A8)
                                count == 2 -> Color(0xFF40C463)
                                count == 3 -> Color(0xFF30A14E)
                                else -> Color(0xFF216E39)
                            }
                            
                            val isToday = date == logicalDate
                                val boxModifier = Modifier
                                    .fillMaxWidth()
                                    .height(boxSize)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                                
                            Box(
                                modifier = if (isToday) {
                                    boxModifier.then(Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                                } else {
                                    boxModifier
                                }
                            )
                        }
                    }
                }
            }
            }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Each block represents a day. Darker green means more quests completed.", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
