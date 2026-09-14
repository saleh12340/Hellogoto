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
import com.example.data.local.Customer
import com.example.data.local.CustomerTransaction
import com.example.ui.OmniViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerDetailScreen(customer: Customer, viewModel: OmniViewModel, onBack: () -> Unit) {
    val transactions by viewModel.transactions(customer.id).collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    val balance = customer.openingBalance + transactions.sumOf { it.debit - it.credit }
    Scaffold(topBar = { TopAppBar(title = { Text(customer.name) }, navigationIcon = { TextButton(onClick = onBack) { Text("رجوع") } }) }, floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة حركة") } }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("كشف حساب", style = MaterialTheme.typography.titleLarge); if (customer.phone.isNotBlank()) Text("الهاتف: ${customer.phone}"); Text("الرصيد: ${money(customer.openingBalance + transactions.sumOf { it.debit - it.credit })}", style = MaterialTheme.typography.headlineSmall) } }
            Spacer(Modifier.height(12.dp)); Text("الحركات", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(transactions, key = { it.id }) { tx ->
                Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(tx.details); Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.date)), style = MaterialTheme.typography.bodySmall); if (tx.debit > 0) Text("مدين: ${money(tx.debit)}"); if (tx.credit > 0) Text("دائن: ${money(tx.credit)}") }; IconButton(onClick = { viewModel.deleteTransaction(tx) }) { Icon(Icons.Default.Delete, "حذف") } } }
            } }
        }
    }
    if (showAdd) AddTransactionDialog(customer.id, viewModel) { showAdd = false }
}

@Composable private fun AddTransactionDialog(customerId: Long, viewModel: OmniViewModel, onDismiss: () -> Unit) {
    var details by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var credit by remember { mutableStateOf(true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة حركة") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(details, { details = it }, label = { Text("البيان") }, singleLine = true)
        OutlinedTextField(amount, { amount = it }, label = { Text("المبلغ") }, singleLine = true)
        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(credit, { credit = true }); Text("دفعة"); Spacer(Modifier.width(8.dp)); RadioButton(!credit, { credit = false }); Text("مديونية") }
    } }, confirmButton = { Button(onClick = { amount.toDoubleOrNull()?.takeIf { it > 0 }?.let { value -> viewModel.saveTransaction(CustomerTransaction(customerId = customerId, details = details.ifBlank { "حركة حساب" }, debit = if (credit) 0.0 else value, credit = if (credit) value else 0.0)); onDismiss() } }) { Text("حفظ") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } })
}

private fun money(value: Double) = "%.2f".format(Locale.US, value)
