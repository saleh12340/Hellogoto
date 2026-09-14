package com.example.ui.grocery

import androidx.compose.foundation.clickable
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

@Composable fun InvoicesScreen(navController: NavController, viewModel: OmniViewModel) {
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    val filtered = invoices.filter { query.isBlank() || it.customerName.contains(query, true) || it.id.toString() == query }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { navController.navigate("add_invoice") }) { Icon(Icons.Default.Add, "فاتورة جديدة") } }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text("الفواتير", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
            OutlinedTextField(query, { query = it }, label = { Text("بحث بالعميل أو رقم الفاتورة") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            if (filtered.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text(if (invoices.isEmpty()) "لا توجد فواتير بعد" else "لا توجد نتائج") }
            else LazyColumn { items(filtered, key = { it.id }) { invoice ->
                Card(Modifier.fillMaxWidth().padding(8.dp).clickable { navController.navigate("invoice_detail/${invoice.id}") }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("فاتورة #${invoice.id}", style = MaterialTheme.typography.titleMedium); Text(invoice.customerName.ifBlank { "عميل نقدي" }); Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(invoice.date)), style = MaterialTheme.typography.bodySmall); Text("الإجمالي: ${money(invoice.totalAmount)}"); Text("المتبقي: ${money(invoice.totalAmount - invoice.paidAmount)}") }
                        IconButton(onClick = { viewModel.deleteInvoice(invoice) }) { Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            } }
        }
    }
}
private fun money(v: Double) = "%.2f".format(Locale.US, v)
