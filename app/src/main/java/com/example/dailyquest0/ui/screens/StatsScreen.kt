package com.example.dailyquest0.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val totalCompletedQuests by viewModel.totalCompletedQuests.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val maxStreak by viewModel.maxStreak.collectAsState()
    val epSnapshots by viewModel.epSnapshots.collectAsState()
    val walletsWithTransactions by viewModel.walletsWithTransactions.collectAsState()
    
    val logicalDate = DateUtils.getLogicalDate()
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Quests", "Wallet")
    
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
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedTabIndex == 0) {
            Text("Activity (Last 10 Weeks)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contribution Graph
            val startDate = remember(logicalDate) { logicalDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY)).minusWeeks(9) }
            val days = remember(startDate) { (0..69).map { startDate.plusDays(it.toLong()) } }
            
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Lifetime Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total EP Earned: ${userStats?.totalEp ?: 0}", fontSize = 16.sp)
                    Text("Total Quests Completed: $totalCompletedQuests", fontSize = 16.sp)
                    Text("Current Streak: $currentStreak days", fontSize = 16.sp)
                    Text("Max Streak: $maxStreak days", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("EP Balance History", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            EpHistoryChart(snapshots = epSnapshots)

        } // End of selectedTabIndex == 0
                else {
                    // Wallet Stats Tab
                    if (walletsWithTransactions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No wallet transactions yet.", color = Color.Gray)
                        }
                    } else {
                        var selectedWalletForHistory by remember { mutableStateOf<com.example.dailyquest0.data.entity.WalletWithTransactions?>(null) }
                        
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(walletsWithTransactions.size) { index ->
                                val walletWithTx = walletsWithTransactions[index]
                                val wallet = walletWithTx.wallet
                                val txs = remember(walletWithTx.transactions) { walletWithTx.transactions.sortedByDescending { it.createdAt } }
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val icon = com.example.dailyquest0.data.entity.QuestIcon.fromIconName(wallet.iconName).filledIcon
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = wallet.name,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(wallet.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.weight(1f))
                                            val balance = remember(txs) { txs.sumOf { it.amount } }
                                            val balanceSign = if (balance > 0) "+" else ""
                                            Text("$balanceSign$balance ${wallet.unit}", fontWeight = FontWeight.Bold, color = if (balance > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        if (txs.isEmpty()) {
                                            Text("No transactions.", fontSize = 14.sp, color = Color.Gray)
                                        } else {
                                            val recentTxs = txs.take(4)
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { selectedWalletForHistory = walletWithTx }
                                                    .padding(8.dp)
                                            ) {
                                                recentTxs.forEach { tx ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(tx.memo.ifEmpty { "No Memo" }, fontSize = 14.sp)
                                                            Text(
                                                                tx.createdAt.substringBefore("T"), 
                                                                fontSize = 12.sp, 
                                                                color = Color.Gray
                                                            )
                                                        }
                                                        val amountSign = if (tx.amount > 0) "+" else ""
                                                        val amountColor = if (tx.amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                        Text("$amountSign${tx.amount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = amountColor)
                                                    }
                                                    if (tx != recentTxs.last()) {
                                                        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                                                    }
                                                }
                                                if (txs.size > 4) {
                                                    Text(
                                                        text = "View all ${txs.size} records...",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        selectedWalletForHistory?.let { walletWithTx ->
                            val wallet = walletWithTx.wallet
                            val allTxs = remember(walletWithTx.transactions) { walletWithTx.transactions.sortedByDescending { it.createdAt } }
                            AlertDialog(
                                onDismissRequest = { selectedWalletForHistory = null },
                                title = { Text("${wallet.name} History") },
                                text = {
                                    androidx.compose.foundation.lazy.LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                                    ) {
                                        items(allTxs.size) { i ->
                                            val tx = allTxs[i]
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(tx.memo.ifEmpty { "No Memo" }, fontSize = 14.sp)
                                                    Text(
                                                        tx.createdAt.replace("T", " ").substringBefore("."), 
                                                        fontSize = 12.sp, 
                                                        color = Color.Gray
                                                    )
                                                }
                                                val amountSign = if (tx.amount > 0) "+" else ""
                                                val amountColor = if (tx.amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                Text("$amountSign${tx.amount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = amountColor)
                                            }
                                            if (i != allTxs.lastIndex) {
                                                Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { selectedWalletForHistory = null }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EpHistoryChart(snapshots: List<com.example.dailyquest0.data.entity.DailyEpSnapshot>) {
    if (snapshots.isEmpty()) {
        Text("No history available yet", color = Color.Gray, fontSize = 14.sp)
        return
    }

    // Sort by date ascending for the chart
    val sortedSnapshots = snapshots.sortedBy { it.date }
    val maxEp = sortedSnapshots.maxOfOrNull { it.balance }?.coerceAtLeast(1) ?: 1
    val minEp = sortedSnapshots.minOfOrNull { it.balance } ?: 0

    val primaryColor = MaterialTheme.colorScheme.primary
    val axisColor = Color.LightGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp)
    ) {
        // Y-axis Labels
        Column(
            modifier = Modifier.fillMaxHeight().padding(end = 8.dp, bottom = 20.dp), // Bottom padding for X-axis space
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(maxEp.toString(), fontSize = 10.sp, color = Color.Gray)
            Text(((maxEp + minEp) / 2).toString(), fontSize = 10.sp, color = Color.Gray)
            Text(minEp.toString(), fontSize = 10.sp, color = Color.Gray)
        }

        // Chart Area
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                val width = size.width
                val height = size.height
                val pointSpacing = if (sortedSnapshots.size > 1) width / (sortedSnapshots.size - 1) else width

                // Draw Y-axis line
                drawLine(
                    color = axisColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, height),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw X-axis line
                drawLine(
                    color = axisColor,
                    start = androidx.compose.ui.geometry.Offset(0f, height),
                    end = androidx.compose.ui.geometry.Offset(width, height),
                    strokeWidth = 1.dp.toPx()
                )

                // Grid lines (middle)
                drawLine(
                    color = axisColor.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(0f, height / 2),
                    end = androidx.compose.ui.geometry.Offset(width, height / 2),
                    strokeWidth = 1.dp.toPx()
                )

                val path = androidx.compose.ui.graphics.Path()

                sortedSnapshots.forEachIndexed { index, snapshot ->
                    val x = index * pointSpacing
                    val yRatio = if (maxEp == minEp) 0.5f else (snapshot.balance - minEp).toFloat() / (maxEp - minEp).toFloat()
                    val y = height - (yRatio * height)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
                
                // Draw points
                sortedSnapshots.forEachIndexed { index, snapshot ->
                    val x = index * pointSpacing
                    val yRatio = if (maxEp == minEp) 0.5f else (snapshot.balance - minEp).toFloat() / (maxEp - minEp).toFloat()
                    val y = height - (yRatio * height)
                    
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }

            // X-axis Labels (Start and End Dates)
            Row(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val startStr = sortedSnapshots.firstOrNull()?.date?.substring(5) ?: "" // MM-DD
                val endStr = sortedSnapshots.lastOrNull()?.date?.substring(5) ?: ""
                Text(startStr, fontSize = 10.sp, color = Color.Gray)
                Text(endStr, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
