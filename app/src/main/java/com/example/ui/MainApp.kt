package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.firebase.FirebaseManager
import com.example.ui.auth.AuthScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.image.ImageScreen
import com.example.ui.music.MusicScreen
import com.example.ui.voice.VoiceScreen
import com.example.ui.grocery.InvoicesScreen
import com.example.ui.grocery.AddInvoiceScreen

@Composable
fun MainApp(omniViewModel: OmniViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentUser by FirebaseManager.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (currentUser == null) {
        AuthScreen()
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        Triple("invoices", "Invoices", Icons.Default.Receipt),
                        Triple("chat", "AI Assistant", Icons.Default.Psychology),
                        Triple("image", "Design", Icons.Default.Brush),
                        Triple("music", "Music", Icons.Default.MusicNote),
                        Triple("voice", "Voice", Icons.Default.Mic)
                    )
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo("chat") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "invoices",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("invoices") { InvoicesScreen(navController, omniViewModel) }
                composable("add_invoice") { AddInvoiceScreen(navController, omniViewModel) }
                composable("chat") { ChatScreen(omniViewModel) }
                composable("image") { ImageScreen(omniViewModel) }
                composable("music") { MusicScreen(omniViewModel) }
                composable("voice") { VoiceScreen(omniViewModel) }
            }
        }
    }
}
