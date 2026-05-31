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
import com.example.dailyquest0.data.entity.ExchangeRate
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.ui.viewmodel.AppViewModel

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

        AlertDialog(
            onDismissRequest = { showAddWalletDialog = false },
            title = { Text("New Wallet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Wallet Name (e.g. お小遣い)") }
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. 円, 分)") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && unit.isNotBlank()) {
                        viewModel.addWallet(name, unit)
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

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(wallet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Balance: ${balance ?: 0} ${wallet.unit}", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Exchange EP to ${wallet.name}:", fontWeight = FontWeight.SemiBold)
            if (rates.isEmpty()) {
                Text("No rates set.", color = Color.Gray, fontSize = 12.sp)
            } else {
                rates.forEach { rate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("- ${rate.requiredEp} EP ➔ +${rate.rewardedAmount} ${wallet.unit}")
                        Button(
                            onClick = { viewModel.exchangeEp(rate) },
                            enabled = (userStats?.currentEp ?: 0) >= rate.requiredEp
                        ) {
                            Text("Exchange")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showAddRateDialog = true }) {
                    Text("Add Rate")
                }
                Button(onClick = { showConsumeDialog = true }) {
                    Text("Consume")
                }
            }
        }
    }

    if (showAddRateDialog) {
        var reqEp by remember { mutableStateOf("") }
        var rewAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddRateDialog = false },
            title = { Text("Add Exchange Rate") },
            text = {
                Column {
                    OutlinedTextField(value = reqEp, onValueChange = { reqEp = it }, label = { Text("Required EP") })
                    OutlinedTextField(value = rewAmount, onValueChange = { rewAmount = it }, label = { Text("Rewarded Amount (${wallet.unit})") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val ep = reqEp.toIntOrNull()
                    val am = rewAmount.toIntOrNull()
                    if (ep != null && am != null) {
                        viewModel.addExchangeRate(wallet.id, ep, am)
                        showAddRateDialog = false
                    }
                }) { Text("Add") }
            }
        )
    }

    if (showConsumeDialog) {
        var amount by remember { mutableStateOf("") }
        var memo by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showConsumeDialog = false },
            title = { Text("Consume from Wallet") },
            text = {
                Column {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (${wallet.unit})") })
                    OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("Memo (e.g. Bought a book)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val am = amount.toIntOrNull()
                    if (am != null && am > 0 && memo.isNotBlank()) {
                        viewModel.consumeFromWallet(wallet.id, am, memo)
                        showConsumeDialog = false
                    }
                }) { Text("Consume") }
            }
        )
    }
}
