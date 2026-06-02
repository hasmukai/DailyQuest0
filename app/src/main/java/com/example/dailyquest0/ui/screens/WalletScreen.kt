package com.example.dailyquest0.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.MoreVert
import com.example.dailyquest0.data.entity.ExchangeRate
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.ui.viewmodel.AppViewModel
import com.example.dailyquest0.data.entity.QuestIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: AppViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    var showAddWalletDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddWalletDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Wallet")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Wallets", fontWeight = FontWeight.Bold) },
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
            if (wallets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No wallets yet. Create one (e.g., 'Money', 'Game Time')!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(wallets) { wallet ->
                        WalletItem(wallet = wallet, viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showAddWalletDialog) {
        var name by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("円") }
        var selectedIcon by remember { mutableStateOf("AttachMoney") }
        var nameError by remember { mutableStateOf<String?>(null) }
        var unitError by remember { mutableStateOf<String?>(null) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        AlertDialog(
            onDismissRequest = { showAddWalletDialog = false },
            title = { Text("New Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (nameError != null) nameError = null
                        },
                        label = { Text("Wallet Name (e.g. お小遣い)") },
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        modifier = Modifier.focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { 
                            unit = it
                            if (unitError != null) unitError = null
                        },
                        label = { Text("Unit (e.g. 円, 分)") },
                        singleLine = true,
                        isError = unitError != null,
                        supportingText = unitError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Icon", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(QuestIcon.values().size) { index ->
                            val iconItem = QuestIcon.values()[index]
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
                    if (name.isBlank()) { nameError = "名前を入力してください"; isValid = false }
                    if (unit.isBlank()) { unitError = "単位を入力してください"; isValid = false }
                    if (isValid) {
                        viewModel.addWallet(name, unit, selectedIcon)
                        showAddWalletDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWalletDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun WalletItem(wallet: Wallet, viewModel: AppViewModel) {
    val balance by viewModel.getWalletBalance(wallet.id).collectAsState(initial = 0)
    val rates by viewModel.getExchangeRates(wallet.id).collectAsState(initial = emptyList())
    val userStats by viewModel.userStats.collectAsState()

    var showConsumeDialog by remember { mutableStateOf(false) }
    var showAddRateDialog by remember { mutableStateOf(false) }
    var showEditWalletDialog by remember { mutableStateOf(false) }
    var showHideWalletDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var rateToDelete by remember { mutableStateOf<com.example.dailyquest0.data.entity.ExchangeRate?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = QuestIcon.fromIconName(wallet.iconName).filledIcon,
                    contentDescription = wallet.iconName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(wallet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Balance: ${balance ?: 0} ${wallet.unit}", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuExpanded = false
                                showEditWalletDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hide") },
                            onClick = {
                                menuExpanded = false
                                showHideWalletDialog = true
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Exchange EP to ${wallet.name}:", fontWeight = FontWeight.SemiBold)
            if (rates.isEmpty()) {
                Text("No rates set.", color = Color.Gray, fontSize = 12.sp)
            } else {
                val chunkedRates = rates.chunked(3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    chunkedRates.forEach { rowRates ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (rate in rowRates) {
                                @OptIn(ExperimentalFoundationApi::class)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .combinedClickable(
                                            onClick = {
                                                if ((userStats?.currentEp ?: 0) >= rate.requiredEp) {
                                                    viewModel.exchangeEp(rate)
                                                }
                                            },
                                            onLongClick = {
                                                rateToDelete = rate
                                            }
                                        ),
                                    color = if ((userStats?.currentEp ?: 0) >= rate.requiredEp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if ((userStats?.currentEp ?: 0) >= rate.requiredEp) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                        Text(
                                            text = "-${rate.requiredEp}EP\n➔+${rate.rewardedAmount}${wallet.unit}",
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 16.sp,
                                            color = if ((userStats?.currentEp ?: 0) >= rate.requiredEp) MaterialTheme.colorScheme.onPrimary else Color.Gray
                                        )
                                    }
                                }
                            }
                            val emptySlots = 3 - rowRates.size
                            for (i in 0 until emptySlots) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { showAddRateDialog = true }) {
                    Text("Add Rate")
                }
                Button(
                    onClick = { showConsumeDialog = true },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Consume", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddRateDialog) {
        var reqEp by remember { mutableStateOf("") }
        var rewAmount by remember { mutableStateOf("") }
        var reqEpError by remember { mutableStateOf<String?>(null) }
        var rewAmountError by remember { mutableStateOf<String?>(null) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        AlertDialog(
            onDismissRequest = { showAddRateDialog = false },
            title = { Text("Add Exchange Rate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reqEp,
                        onValueChange = { 
                            reqEp = it
                            if (reqEpError != null) reqEpError = null
                        },
                        label = { Text("Required EP") },
                        singleLine = true,
                        isError = reqEpError != null,
                        supportingText = reqEpError?.let { { Text(it) } },
                        modifier = Modifier.focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = rewAmount,
                        onValueChange = { 
                            rewAmount = it
                            if (rewAmountError != null) rewAmountError = null
                        },
                        label = { Text("Rewarded Amount (${wallet.unit})") },
                        singleLine = true,
                        isError = rewAmountError != null,
                        supportingText = rewAmountError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    var isValid = true
                    val ep = reqEp.toIntOrNull()
                    val am = rewAmount.toIntOrNull()
                    if (ep == null || ep <= 0) { reqEpError = "正の整数を入力してください"; isValid = false }
                    if (am == null || am <= 0) { rewAmountError = "正の整数を入力してください"; isValid = false }
                    
                    if (isValid && ep != null && am != null) {
                        viewModel.addExchangeRate(wallet.id, ep, am)
                        showAddRateDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddRateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showConsumeDialog) {
        var amount by remember { mutableStateOf("") }
        var memo by remember { mutableStateOf("") }
        var amountError by remember { mutableStateOf<String?>(null) }
        var memoError by remember { mutableStateOf<String?>(null) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        AlertDialog(
            onDismissRequest = { showConsumeDialog = false },
            title = { Text("Consume from Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            if (amountError != null) amountError = null
                        },
                        label = { Text("Amount (${wallet.unit})") },
                        singleLine = true,
                        isError = amountError != null,
                        supportingText = amountError?.let { { Text(it) } },
                        modifier = Modifier.focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { 
                            memo = it
                            if (memoError != null) memoError = null
                        },
                        label = { Text("Memo (e.g. Bought a book)") },
                        singleLine = true,
                        isError = memoError != null,
                        supportingText = memoError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    var isValid = true
                    val am = amount.toIntOrNull()
                    if (am == null || am <= 0) { amountError = "正の整数を入力してください"; isValid = false }
                    if (memo.isBlank()) { memoError = "メモを入力してください"; isValid = false }
                    
                    if (isValid && am != null) {
                        viewModel.consumeFromWallet(wallet.id, am, memo)
                        showConsumeDialog = false
                    }
                }) { Text("Consume") }
            },
            dismissButton = {
                TextButton(onClick = { showConsumeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditWalletDialog) {
        var name by remember { mutableStateOf(wallet.name) }
        var unit by remember { mutableStateOf(wallet.unit) }
        var selectedIcon by remember { mutableStateOf(wallet.iconName) }
        var nameError by remember { mutableStateOf<String?>(null) }
        var unitError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditWalletDialog = false },
            title = { Text("Edit Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (nameError != null) nameError = null
                        },
                        label = { Text("Wallet Name") },
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { 
                            unit = it
                            if (unitError != null) unitError = null
                        },
                        label = { Text("Unit") },
                        singleLine = true,
                        isError = unitError != null,
                        supportingText = unitError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Icon", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(QuestIcon.values().size) { index ->
                            val iconItem = QuestIcon.values()[index]
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
                    if (name.isBlank()) { nameError = "名前を入力してください"; isValid = false }
                    if (unit.isBlank()) { unitError = "単位を入力してください"; isValid = false }
                    if (isValid) {
                        viewModel.updateWallet(wallet, name, unit, selectedIcon)
                        showEditWalletDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditWalletDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showHideWalletDialog) {
        AlertDialog(
            onDismissRequest = { showHideWalletDialog = false },
            title = { Text("Hide Wallet") },
            text = { Text("このWalletを非表示にしますか？\n(履歴等のデータは保持されます)") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hideWallet(wallet)
                        showHideWalletDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hide") }
            },
            dismissButton = {
                TextButton(onClick = { showHideWalletDialog = false }) { Text("Cancel") }
            }
        )
    }
    
    rateToDelete?.let { rate ->
        AlertDialog(
            onDismissRequest = { rateToDelete = null },
            title = { Text("Delete Exchange Rate") },
            text = { Text("この交換レートを削除しますか？\n(-${rate.requiredEp}EP ➔ +${rate.rewardedAmount}${wallet.unit})") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExchangeRate(rate)
                        rateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { rateToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
