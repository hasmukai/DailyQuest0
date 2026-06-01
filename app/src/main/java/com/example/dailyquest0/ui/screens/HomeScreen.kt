package com.example.dailyquest0.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    var editingQuest by remember { mutableStateOf<Quest?>(null) }
    var questToDelete by remember { mutableStateOf<Quest?>(null) }

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
            
            val sortedQuests = filteredQuests.sortedBy { quest ->
                todayLogs.any { it.questId == quest.id && it.isCompleted }
            }

            if (sortedQuests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quests yet. Add one to get started!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedQuests) { quest ->
                        val isCompleted = todayLogs.any { it.questId == quest.id && it.isCompleted }
                        QuestItem(
                            quest = quest,
                            isCompleted = isCompleted,
                            onToggle = { completed ->
                                viewModel.toggleQuest(quest, completed)
                            },
                            onLongClick = {
                                editingQuest = quest
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

    editingQuest?.let { quest ->
        EditQuestDialog(
            quest = quest,
            onDismiss = { editingQuest = null },
            onSave = { title, ep, type ->
                viewModel.updateQuest(quest, title, ep, type)
                editingQuest = null
            },
            onDeleteRequest = {
                questToDelete = quest
            }
        )
    }

    questToDelete?.let { quest ->
        AlertDialog(
            onDismissRequest = { questToDelete = null },
            title = { Text("クエストの削除") },
            text = { Text("「${quest.title}」を削除してもよろしいですか？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuest(quest.id)
                        questToDelete = null
                        editingQuest = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { questToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestItem(
    quest: Quest,
    isCompleted: Boolean,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onToggle(!isCompleted) },
                onLongClick = { onLongClick() }
            ),
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
        }
    }
}

@Composable
fun AddQuestDialog(onDismiss: () -> Unit, onAdd: (String, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var epReward by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Daily") }
    var epError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    val types = listOf("Daily", "Weekly", "Temporary")
    
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Quest") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        if (titleError != null) titleError = null
                    },
                    label = { Text("Task Name") },
                    singleLine = true,
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = epReward,
                    onValueChange = { 
                        epReward = it
                        if (epError != null) epError = null
                    },
                    label = { Text("EP Reward") },
                    singleLine = true,
                    isError = epError != null,
                    supportingText = epError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
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
                var isValid = true
                if (title.isBlank()) {
                    titleError = "名前を入力してください"
                    isValid = false
                }
                
                val ep = epReward.toIntOrNull()
                if (ep == null || ep <= 0) {
                    epError = "正の整数を入力してください"
                    isValid = false
                }
                
                if (isValid && ep != null) {
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

@Composable
fun EditQuestDialog(
    quest: Quest,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var title by remember { mutableStateOf(quest.title) }
    var epReward by remember { mutableStateOf(quest.epReward.toString()) }
    var selectedType by remember { mutableStateOf(quest.type) }
    var epError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    val types = listOf("Daily", "Weekly", "Temporary")
    
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Quest") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        if (titleError != null) titleError = null
                    },
                    label = { Text("Task Name") },
                    singleLine = true,
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = epReward,
                    onValueChange = { 
                        epReward = it
                        if (epError != null) epError = null
                    },
                    label = { Text("EP Reward") },
                    singleLine = true,
                    isError = epError != null,
                    supportingText = epError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
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
                var isValid = true
                if (title.isBlank()) {
                    titleError = "名前を入力してください"
                    isValid = false
                }
                
                val ep = epReward.toIntOrNull()
                if (ep == null || ep <= 0) {
                    epError = "正の整数を入力してください"
                    isValid = false
                }
                
                if (isValid && ep != null) {
                    onSave(title, ep, selectedType)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDeleteRequest) { 
                    Text("Delete", color = MaterialTheme.colorScheme.error) 
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
