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
import com.example.data.local.Expense
import com.example.data.local.Product
import com.example.ui.OmniViewModel
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: OmniViewModel, open: (String) -> Unit) {
    val invoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val products by viewModel.products.collectAsState(initial = emptyList())
    val expenses by viewModel.expenses.collectAsState(initial = emptyList())
    val today = java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
    val todaySales = invoices.filter { it.date >= today }.sumOf { it.totalAmount }
    val todayExpenses = expenses.filter { it.date >= today }.sumOf { it.amount }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("لوحة التحكم", style = MaterialTheme.typography.headlineMedium)
        Text("إدارة المبيعات والحسابات والمخزون من مكان واحد", style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("مبيعات اليوم", money(todaySales), Modifier.weight(1f))
            StatCard("المصروفات", money(todayExpenses), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("الفواتير", invoices.size.toString(), Modifier.weight(1f))
            StatCard("العملاء", customers.size.toString(), Modifier.weight(1f))
            StatCard("الأصناف", products.size.toString(), Modifier.weight(1f))
        }
        Button(onClick = { open("add_invoice") }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("فاتورة مبيعات جديدة") }
        OutlinedButton(onClick = { open("customers") }, Modifier.fillMaxWidth()) { Text("إدارة العملاء والحسابات") }
        OutlinedButton(onClick = { open("products") }, Modifier.fillMaxWidth()) { Text("الأصناف والمخزون") }
        OutlinedButton(onClick = { open("reports") }, Modifier.fillMaxWidth()) { Text("التقارير والمصروفات") }
    }
}

@Composable private fun StatCard(title: String, value: String, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(12.dp)) { Text(title, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.titleLarge) } }
}

@Composable
fun CustomersScreen(viewModel: OmniViewModel) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; var opening by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("العملاء والحسابات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f)); FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null) } }
        Spacer(Modifier.height(8.dp))
        if (customers.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("لا يوجد عملاء بعد") } else LazyColumn { items(customers, key = { it.id }) { c -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(c.name, style = MaterialTheme.typography.titleMedium); if (c.phone.isNotBlank()) Text(c.phone); Text("الرصيد الافتتاحي: ${money(c.openingBalance)}") }; IconButton(onClick = { viewModel.deleteCustomer(c) }) { Icon(Icons.Default.Delete, "حذف") } } } } }
    }
    if (showAdd) AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("إضافة عميل") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name, { name = it }, label = { Text("اسم العميل") }); OutlinedTextField(phone, { phone = it }, label = { Text("الجوال") }); OutlinedTextField(opening, { opening = it }, label = { Text("الرصيد الافتتاحي") }) } }, confirmButton = { Button(onClick = { if (name.isNotBlank()) { viewModel.saveCustomer(Customer(name = name.trim(), phone = phone.trim(), openingBalance = opening.toDoubleOrNull() ?: 0.0)); name=""; phone=""; opening=""; showAdd=false } }) { Text("حفظ") } }, dismissButton = { TextButton(onClick = { showAdd=false }) { Text("إلغاء") } })
}

@Composable
fun ProductsScreen(viewModel: OmniViewModel) {
    val products by viewModel.products.collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var sale by remember { mutableStateOf("") }; var purchase by remember { mutableStateOf("") }; var stock by remember { mutableStateOf("") }; var min by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("الأصناف والمخزون", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f)); FloatingActionButton(onClick = { showAdd=true }) { Icon(Icons.Default.Add, null) } }
        LazyColumn { items(products, key={it.id}) { p -> Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) { Row(Modifier.padding(14.dp), verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(p.name, style=MaterialTheme.typography.titleMedium); Text("بيع: ${money(p.salePrice)} • مخزون: ${p.stock} ${p.unit}"); if (p.stock <= p.minimumStock) Text("تنبيه: المخزون منخفض", color=MaterialTheme.colorScheme.error) }; IconButton(onClick={viewModel::deleteProduct.bind(p)}) { Icon(Icons.Default.Delete, "حذف") } } } } }
    }
    if(showAdd) AlertDialog(onDismissRequest={showAdd=false}, title={Text("إضافة صنف")}, text={Column(verticalArrangement=Arrangement.spacedBy(6.dp)){OutlinedTextField(name,{name=it},label={Text("اسم الصنف")});OutlinedTextField(sale,{sale=it},label={Text("سعر البيع")});OutlinedTextField(purchase,{purchase=it},label={Text("سعر الشراء")});OutlinedTextField(stock,{stock=it},label={Text("الكمية الحالية")});OutlinedTextField(min,{min=it},label={Text("حد التنبيه")})}}, confirmButton={Button(onClick={if(name.isNotBlank()){viewModel.saveProduct(Product(name=name.trim(),salePrice=sale.toDoubleOrNull()?:0.0,purchasePrice=purchase.toDoubleOrNull()?:0.0,stock=stock.toDoubleOrNull()?:0.0,minimumStock=min.toDoubleOrNull()?:0.0));name="";showAdd=false}}){Text("حفظ")}},dismissButton={TextButton(onClick={showAdd=false}){Text("إلغاء")}})
}

@Composable
fun ReportsScreen(viewModel: OmniViewModel) {
    val invoices by viewModel.allInvoices.collectAsState(initial=emptyList()); val expenses by viewModel.expenses.collectAsState(initial=emptyList()); var showExpense by remember{mutableStateOf(false)}; var title by remember{mutableStateOf("")}; var amount by remember{mutableStateOf("")}
    val sales=invoices.sumOf{it.totalAmount}; val paid=invoices.sumOf{it.paidAmount}; val due=sales-paid; val exp=expenses.sumOf{it.amount}
    Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("التقارير",style=MaterialTheme.typography.headlineMedium);ReportRow("إجمالي المبيعات",sales);ReportRow("المبالغ المدفوعة",paid);ReportRow("المتبقي على العملاء",due);ReportRow("إجمالي المصروفات",exp);Button(onClick={showExpense=true},Modifier.fillMaxWidth()){Text("إضافة مصروف")};Text("عدد الفواتير: ${invoices.size}");Text("صافي الحركة: ${money(sales-exp)}",style=MaterialTheme.typography.titleLarge)}
    if(showExpense) AlertDialog(onDismissRequest={showExpense=false},title={Text("إضافة مصروف")},text={Column{OutlinedTextField(title,{title=it},label={Text("البيان")});OutlinedTextField(amount,{amount=it},label={Text("المبلغ")})}},confirmButton={Button(onClick={if(title.isNotBlank()){viewModel.saveExpense(Expense(title=title.trim(),amount=amount.toDoubleOrNull()?:0.0));title="";amount="";showExpense=false}}){Text("حفظ")}},dismissButton={TextButton(onClick={showExpense=false}){Text("إلغاء")}})
}

@Composable private fun ReportRow(label:String,value:Double){Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(14.dp)){Text(label,Modifier.weight(1f));Text(money(value),style=MaterialTheme.typography.titleMedium)}}}
private fun money(value:Double)=String.format(Locale.getDefault(),"%.2f",value)
private fun <T> ((T)->Unit).bind(value:T):()->Unit={return{this(value)}}
