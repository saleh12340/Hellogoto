package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.firebase.FirebaseManager
import com.example.ui.auth.AuthScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.image.ImageScreen
import com.example.ui.music.MusicScreen
import com.example.ui.voice.VoiceScreen
import com.example.ui.grocery.*

@Composable fun MainApp(omniViewModel: OmniViewModel = viewModel()) {
    val navController=rememberNavController(); val currentUser by FirebaseManager.currentUser.collectAsState(); val entry by navController.currentBackStackEntryAsState(); val route=entry?.destination?.route
    if(currentUser==null){AuthScreen();return}
    val items=listOf("home" to ("الرئيسية" to Icons.Default.Home),"invoices" to ("الفواتير" to Icons.Default.Receipt),"customers" to ("العملاء" to Icons.Default.People),"products" to ("المخزون" to Icons.Default.Inventory),"reports" to ("التقارير" to Icons.Default.Assessment))
    Scaffold(bottomBar={NavigationBar{items.forEach{(r,p)->NavigationBarItem(selected=route==r,onClick={navController.navigate(r){popUpTo("home"){saveState=true};launchSingleTop=true;restoreState=true}},icon={Icon(p.second,null)},label={Text(p.first)})}}}){pad->
        NavHost(navController,"home",Modifier.padding(pad)){
            composable("home"){DashboardScreen(omniViewModel){navController.navigate(it)}}
            composable("invoices"){InvoicesScreen(navController,omniViewModel)}
            composable("add_invoice"){AddInvoiceScreen(navController,omniViewModel)}
            composable("customers"){CustomersScreen(omniViewModel){c->navController.navigate("customer_detail/${c.id}")}}
            composable("products"){ProductsScreen(omniViewModel)}
            composable("reports"){ReportsScreen(omniViewModel)}
            composable("customer_detail/{id}",arguments=listOf(navArgument("id"){type=NavType.LongType})){back->val id=back.arguments?.getLong("id")?:-1L;val c=omniViewModel.customers.collectAsState(initial=emptyList()).value.firstOrNull{it.id==id};if(c!=null)CustomerDetailScreen(c,omniViewModel){navController.popBackStack()}}
            composable("invoice_detail/{id}",arguments=listOf(navArgument("id"){type=NavType.LongType})){back->val id=back.arguments?.getLong("id")?:-1L;val invoice=omniViewModel.allInvoices.collectAsState(initial=emptyList()).value.firstOrNull{it.id==id};if(invoice!=null)InvoiceDetailScreen(invoice,omniViewModel){navController.popBackStack()}}
            composable("chat"){ChatScreen(omniViewModel)}
            composable("image"){ImageScreen(omniViewModel)}
            composable("music"){MusicScreen(omniViewModel)}
            composable("voice"){VoiceScreen(omniViewModel)}
        }
    }
}
