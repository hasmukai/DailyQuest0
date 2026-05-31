package com.example.dailyquest0.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val quests by viewModel.quests.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Quest")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("DailyQuest", fontWeight = FontWeight.Bold) },
                actions = {
                    AssistChip(
                        onClick = { },
                        label = { Text("${userStats?.currentEp ?: 0} EP", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Today's Quests",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            val filterOptions = listOf("All", "Daily", "Weekly", "Temporary")
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                filterOptions.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == selectedFilter,
                        onClick = { viewModel.setFilter(option) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = filterOptions.size)
                    ) {
                        Text(option)
                    }
                }
            }

            val filteredQuests = if (selectedFilter == "All") {
                quests
            } else {
                quests.filter { it.type == selectedFilter }
            }

            if (filteredQuests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quests yet. Add one to get started!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredQuests) { quest ->
                        val isCompleted = todayLogs.any { it.questId == quest.id && it.isCompleted }
                        QuestItem(
                            quest = quest,
                            isCompleted = isCompleted,
                            onToggle = { completed ->
                                viewModel.toggleQuest(quest, completed)
                            },
                            onDelete = {
                                viewModel.deleteQuest(quest.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddQuestDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, ep, type ->
                viewModel.addQuest(title, ep, type)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun QuestItem(
    quest: Quest,
    isCompleted: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle(!isCompleted) },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon for 1-tap completion with animation
            IconButton(onClick = { onToggle(!isCompleted) }) {
                Icon(
                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle Complete",
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${quest.epReward} EP",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text(quest.type, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Quest", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun AddQuestDialog(onDismiss: () -> Unit, onAdd: (String, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var epReward by remember { mutableStateOf("10") }
    var selectedType by remember { mutableStateOf("Daily") }
    val types = listOf("Daily", "Weekly", "Temporary")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Quest") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Name") }
                )
                OutlinedTextField(
                    value = epReward,
                    onValueChange = { epReward = it },
                    label = { Text("EP Reward") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quest Type", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    types.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = type == selectedType,
                            onClick = { selectedType = type },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size)
                        ) {
                            Text(type, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val ep = epReward.toIntOrNull() ?: 10
                if (title.isNotBlank()) {
                    onAdd(title, ep, selectedType)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
