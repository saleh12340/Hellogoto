package com.example.ui.grocery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.OmniViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvoicesScreen(navController: NavController, viewModel: OmniViewModel) {
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_invoice") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Invoice")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Grocery Invoices",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (invoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No invoices yet.")
                }
            } else {
                LazyColumn {
                    items(invoices) { invoice ->
                        InvoiceItemCard(invoice, onDelete = { viewModel.deleteInvoice(invoice) })
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceItemCard(invoice: com.example.data.local.Invoice, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.customerName, style = MaterialTheme.typography.titleLarge)
                Text(dateFormat.format(Date(invoice.date)), style = MaterialTheme.typography.bodySmall)
                Text("Total: ${invoice.totalAmount} SAR", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
