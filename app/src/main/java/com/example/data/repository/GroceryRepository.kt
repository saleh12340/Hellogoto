package com.example.data.repository

import com.example.data.local.Invoice
import com.example.data.local.InvoiceDao
import com.example.data.local.InvoiceItem
import kotlinx.coroutines.flow.Flow

class GroceryRepository(private val invoiceDao: InvoiceDao) {
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItem>> = 
        invoiceDao.getItemsForInvoice(invoiceId)

    suspend fun insertInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertItems(itemsWithId)
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.deleteInvoice(invoice)
    }
}
