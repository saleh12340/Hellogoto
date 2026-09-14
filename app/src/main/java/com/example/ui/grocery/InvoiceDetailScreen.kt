package com.example.ui.grocery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.Invoice
import com.example.ui.OmniViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvoiceDetailScreen(invoice: Invoice, viewModel: OmniViewModel, onBack: () -> Unit) {
    val items by viewModel.invoiceItems(invoice.id).collectAsState(initial = emptyList())
    Scaffold(topBar = { TopAppBar(title = { Text("فاتورة #${invoice.id}") }, navigationIcon = { TextButton(onClick = onBack) { Text("رجوع") } }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text("العميل: ${invoice.customerName}", style = MaterialTheme.typography.titleMedium)
                Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(invoice.date)), style = MaterialTheme.typography.bodySmall)
                Text("الإجمالي: ${money(invoice.totalAmount)}")
                Text("المدفوع: ${money(invoice.paidAmount)}")
                Text("المتبقي: ${money(invoice.totalAmount - invoice.paidAmount)}", style = MaterialTheme.typography.titleMedium)
            } }
            Spacer(Modifier.height(12.dp)); Text("الأصناف", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) { items(items, key = { it.id }) { item ->
                ListItem(headlineContent = { Text(item.itemName) }, supportingContent = { Text("الكمية: ${item.quantity} • السعر: ${money(item.price)} • الإجمالي: ${money(item.quantity * item.price)}") })
            } }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { viewModel.deleteInvoice(invoice); onBack() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(6.dp)); Text("حذف الفاتورة") }
        }
    }
}

private fun money(value: Double) = "%.2f".format(Locale.US, value)
