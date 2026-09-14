package com.example.ui.grocery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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

@Composable fun AddInvoiceScreen(navController: NavController, viewModel: OmniViewModel) {
    var customer by remember{mutableStateOf("")};var item by remember{mutableStateOf("")};var qty by remember{mutableStateOf("")};var price by remember{mutableStateOf("")};var paid by remember{mutableStateOf("")};val rows=remember{mutableStateListOf<InvoiceItem>()};val total=rows.sumOf{it.quantity*it.price}
    Scaffold(topBar={TopAppBar(title={Text("فاتورة مبيعات جديدة")})},floatingActionButton={FloatingActionButton({if(customer.isNotBlank()&&rows.isNotEmpty()){viewModel.saveInvoice(Invoice(customerName=customer.trim(),totalAmount=total,paidAmount=paid.toDoubleOrNull()?:0.0),rows.toList());navController.popBackStack()}}){Icon(Icons.Default.Save,"حفظ")}}){pad->Column(Modifier.fillMaxSize().padding(pad).padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(customer,{customer=it},label={Text("اسم العميل")},modifier=Modifier.fillMaxWidth());Text("الأصناف",style=MaterialTheme.typography.titleMedium);Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){OutlinedTextField(item,{item=it},label={Text("الصنف")},modifier=Modifier.weight(2f));OutlinedTextField(qty,{qty=it},label={Text("الكمية")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));OutlinedTextField(price,{price=it},label={Text("السعر")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));IconButton({val q=qty.toDoubleOrNull();val p=price.toDoubleOrNull();if(item.isNotBlank()&&q!=null&&q>0&&p!=null&&p>=0){rows.add(InvoiceItem(invoiceId=0,itemName=item.trim(),quantity=q,price=p));item="";qty="";price=""}}){Icon(Icons.Default.Add,"إضافة")}};LazyColumn(Modifier.weight(1f)){items(rows){r->ListItem(headlineContent={Text(r.itemName)},supportingContent={Text("${r.quantity} × ${r.price}")},trailingContent={Row(verticalAlignment=Alignment.CenterVertically){Text("${r.quantity*r.price}");IconButton({rows.remove(r)}){Icon(Icons.Default.Delete,"حذف")}})}}};HorizontalDivider();Text("الإجمالي: $total",style=MaterialTheme.typography.headlineSmall);OutlinedTextField(paid,{paid=it},label={Text("المبلغ المدفوع")},modifier=Modifier.fillMaxWidth(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));Text("المتبقي: ${total-(paid.toDoubleOrNull()?:0.0)}",style=MaterialTheme.typography.titleMedium)}}}
