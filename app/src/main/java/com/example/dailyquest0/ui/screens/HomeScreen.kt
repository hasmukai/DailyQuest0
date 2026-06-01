package com.example.dailyquest0.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.data.entity.QuestType
import com.example.dailyquest0.data.entity.QuestIcon
import com.example.dailyquest0.ui.viewmodel.AppViewModel
import com.example.dailyquest0.utils.DateUtils
import java.time.LocalDateTime
import java.time.Duration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val quests by viewModel.quests.collectAsState()
    val completedQuestIds by viewModel.completedQuestIds.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingQuest by remember { mutableStateOf<Quest?>(null) }
    var questToDelete by remember { mutableStateOf<Quest?>(null) }
    
    var timeRemaining by remember { mutableStateOf("") }

    LaunchedEffect(selectedFilter) {
        if (selectedFilter == QuestType.DAILY.displayName || selectedFilter == QuestType.WEEKLY.displayName) {
            while (true) {
                val now = LocalDateTime.now()
                val target = if (selectedFilter == QuestType.DAILY.displayName) {
                    DateUtils.getNextDailyReset()
                } else {
                    DateUtils.getNextWeeklyReset()
                }
                val duration = Duration.between(now, target)
                val days = duration.toDays()
                val hours = duration.toHours() % 24
                val minutes = duration.toMinutes() % 60
                val seconds = duration.seconds % 60
                
                timeRemaining = if (days > 0) {
                    "${days}日 ${hours}時間 ${minutes}分 ${seconds}秒"
                } else {
                    "${hours}時間 ${minutes}分 ${seconds}秒"
                }
                delay(1000)
            }
        } else {
            timeRemaining = ""
        }
    }

    val currentEp = userStats?.currentEp ?: 0
    var previousEp by remember { mutableIntStateOf(currentEp) }
    var epDiffs by remember { mutableStateOf(listOf<EpDiff>()) }

    LaunchedEffect(currentEp) {
        if (currentEp != previousEp) {
            val diffAmount = currentEp - previousEp
            val newDiff = EpDiff(id = java.util.UUID.randomUUID().toString(), amount = diffAmount)
            epDiffs = epDiffs + newDiff
            previousEp = currentEp
            
            launch {
                delay(1500)
                epDiffs = epDiffs.filter { it.id != newDiff.id }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Quest")
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        Text(
                            text = "DailyQuest", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    },
                    title = { 
                        if (currentStreak >= 2) {
                            Text(
                                text = "$currentStreak-day streak! \uD83D\uDD25",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722) // Orange color for text
                            )
                        }
                    },
                    actions = {
                        AssistChip(
                            onClick = { },
                            label = { Text("${currentEp} EP", fontWeight = FontWeight.Bold) },
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

            val filterOptions = listOf("All") + QuestType.values().map { it.displayName }
            CustomSegmentedControl(
                options = filterOptions,
                selectedOption = selectedFilter,
                onOptionSelected = { viewModel.setFilter(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            val filteredQuests = remember(selectedFilter, quests) {
                if (selectedFilter == "All") {
                    quests
                } else {
                    quests.filter { it.type == selectedFilter }
                }
            }

            if (selectedFilter == QuestType.DAILY.displayName || selectedFilter == QuestType.WEEKLY.displayName) {
                val totalQuests = filteredQuests.size
                val completedCount = filteredQuests.count { completedQuestIds.contains(it.id) }
                val targetProgress = if (totalQuests > 0) completedCount.toFloat() / totalQuests.toFloat() else 0f
                val progress by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
                    label = "progress animation"
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "達成度: $completedCount / $totalQuests",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (timeRemaining.isNotEmpty()) {
                Text(
                    text = "リセットまで: $timeRemaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                )
            }
            
            val sortedQuests = remember(filteredQuests, completedQuestIds) {
                filteredQuests.sortedBy { quest ->
                    completedQuestIds.contains(quest.id)
                }
            }

            if (sortedQuests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quests yet. Add one to get started!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = sortedQuests, key = { it.id }) { quest ->
                        val isCompleted = completedQuestIds.contains(quest.id)
                        QuestItem(
                            quest = quest,
                            isCompleted = isCompleted,
                            modifier = Modifier.animateItem(),
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
            onAdd = { title, ep, type, iconName ->
                viewModel.addQuest(title, ep, type, iconName)
                showAddDialog = false
            }
        )
    }

    editingQuest?.let { quest ->
        EditQuestDialog(
            quest = quest,
            onDismiss = { editingQuest = null },
            onSave = { title, ep, type, iconName ->
                viewModel.updateQuest(quest, title, ep, type, iconName)
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
        
        // Floating diff text overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp, end = 32.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            epDiffs.forEach { diff ->
                key(diff.id) {
                    FloatingDiffText(diff = diff)
                }
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestItem(
    quest: Quest,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier
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
                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else QuestIcon.fromIconName(quest.iconName).outlinedIcon,
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
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(quest.type, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuestDialog(onDismiss: () -> Unit, onAdd: (String, Int, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var epReward by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(QuestType.DAILY.displayName) }
    var selectedIcon by remember { mutableStateOf(QuestIcon.CHECK_CIRCLE.iconName) }
    var epError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    val types = QuestType.values().map { it.displayName }
    val iconsList = QuestIcon.values().toList()
    
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

                Spacer(modifier = Modifier.height(8.dp))
                Text("Icon", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconsList.size) { index ->
                        val iconItem = iconsList[index]
                        val isSelected = selectedIcon == iconItem.iconName
                        IconButton(
                            onClick = { selectedIcon = iconItem.iconName },
                            modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSelected) iconItem.filledIcon else iconItem.outlinedIcon,
                                contentDescription = iconItem.iconName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
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
                    onAdd(title, ep, selectedType, selectedIcon)
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
    onSave: (String, Int, String, String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var title by remember { mutableStateOf(quest.title) }
    var epReward by remember { mutableStateOf(quest.epReward.toString()) }
    var selectedType by remember { mutableStateOf(quest.type) }
    var selectedIcon by remember { mutableStateOf(quest.iconName) }
    var epError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    val types = QuestType.values().map { it.displayName }
    val iconsList = QuestIcon.values().toList()
    
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

                Spacer(modifier = Modifier.height(8.dp))
                Text("Icon", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconsList.size) { index ->
                        val iconItem = iconsList[index]
                        val isSelected = selectedIcon == iconItem.iconName
                        IconButton(
                            onClick = { selectedIcon = iconItem.iconName },
                            modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSelected) iconItem.filledIcon else iconItem.outlinedIcon,
                                contentDescription = iconItem.iconName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
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
                    onSave(title, ep, selectedType, selectedIcon)
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



@Composable
fun CustomSegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = options.indexOf(selectedOption).takeIf { it >= 0 } ?: 0
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier.height(48.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val segmentWidth = maxWidth / options.size
            
            val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = tween(durationMillis = 300),
                label = "indicator offset"
            )
            
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .padding(4.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {}
            }
            
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { onOptionSelected(option) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

data class EpDiff(val id: String, val amount: Int)

@Composable
fun FloatingDiffText(diff: EpDiff) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
        delay(800)
        visible = false
    }
    
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }) + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200)),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(500)),
        modifier = Modifier.offset(y = (-20).dp)
    ) {
        val sign = if (diff.amount > 0) "+" else ""
        val color = if (diff.amount > 0) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        Text(
            text = "$sign${diff.amount}",
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
