package com.example.dailyquest0.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.example.dailyquest0.ui.screens.HomeScreen
import com.example.dailyquest0.ui.screens.StatsScreen
import com.example.dailyquest0.ui.screens.WalletScreen
import com.example.dailyquest0.ui.viewmodel.AppViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val items = listOf(
        Screen.Home,
        Screen.Wallet,
        Screen.Stats
    )

    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> WalletScreen(viewModel = viewModel)
                2 -> StatsScreen(viewModel = viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Quests", Icons.Filled.Home)
    object Wallet : Screen("wallet", "Wallet", Icons.Filled.ShoppingCart)
    object Stats : Screen("stats", "Stats", Icons.Filled.DateRange)
}

