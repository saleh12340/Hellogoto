package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class GroceryRepository(private val dao: InvoiceDao) {
    val allInvoices: Flow<List<Invoice>> = dao.getAllInvoices()
    val customers: Flow<List<Customer>> = dao.getCustomers()
    val products: Flow<List<Product>> = dao.getProducts()
    val expenses: Flow<List<Expense>> = dao.getExpenses()

    fun getItems(invoiceId: Long) = dao.getItemsForInvoice(invoiceId)
    fun getTransactions(customerId: Long) = dao.getTransactions(customerId)

    suspend fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        val id = dao.insertInvoice(invoice)
        dao.insertItems(items.map { it.copy(invoiceId = id) })
    }
    suspend fun deleteInvoice(invoice: Invoice) = dao.deleteInvoice(invoice)
    suspend fun saveCustomer(customer: Customer) = dao.insertCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)
    suspend fun saveProduct(product: Product) = dao.insertProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)
    suspend fun saveTransaction(tx: CustomerTransaction) = dao.insertTransaction(tx)
    suspend fun deleteTransaction(tx: CustomerTransaction) = dao.deleteTransaction(tx)
    suspend fun saveExpense(expense: Expense) = dao.insertExpense(expense)
}
