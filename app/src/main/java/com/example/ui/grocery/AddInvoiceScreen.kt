package com.example.ui.grocery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.local.Invoice
import com.example.data.local.InvoiceItem
import com.example.ui.OmniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(navController: NavController, viewModel: OmniViewModel) {
    var customerName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    
    val currentItems = remember { mutableStateListOf<InvoiceItem>() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Invoice") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (customerName.isNotBlank() && currentItems.isNotEmpty()) {
                    val total = currentItems.sumOf { it.price * it.quantity }
                    viewModel.saveInvoice(Invoice(customerName = customerName, totalAmount = total), currentItems.toList())
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Add Items", style = MaterialTheme.typography.titleMedium)
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item") },
                    modifier = Modifier.weight(2f)
                )
                IconButton(onClick = {
                    viewModel.sendMessage("Suggest 3 common grocery items. Respond with only item names separated by commas.", modelType = "lite")
                }) {
                    Icon(Icons.Default.Star, contentDescription = "AI Suggest")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                IconButton(onClick = {
                    if (itemName.isNotBlank() && quantity.isNotBlank() && price.isNotBlank()) {
                        currentItems.add(InvoiceItem(
                            invoiceId = 0,
                            itemName = itemName,
                            quantity = quantity.toIntOrNull() ?: 1,
                            price = price.toDoubleOrNull() ?: 0.0
                        ))
                        itemName = ""
                        quantity = ""
                        price = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(currentItems) { item ->
                    ListItem(
                        headlineContent = { Text(item.itemName) },
                        supportingContent = { Text("${item.quantity} x ${item.price} SAR") },
                        trailingContent = { Text("${item.quantity * item.price} SAR") }
                    )
                }
            }
            
            Divider()
            
            val total = currentItems.sumOf { it.price * it.quantity }
            Text(
                "Total: $total SAR",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.End).padding(top = 16.dp)
            )
        }
    }
}
