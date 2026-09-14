package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products", indices = [Index(value = ["name"], unique = true)])
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val salePrice: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val stock: Double = 0.0,
    val minimumStock: Double = 0.0,
    val unit: String = "قطعة"
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["customerId"])]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val type: String = "sale"
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [ForeignKey(entity = Invoice::class, parentColumns = ["id"], childColumns = ["invoiceId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val itemName: String,
    val quantity: Double,
    val price: Double
)

@Entity(tableName = "transactions", indices = [Index(value = ["customerId"])])
data class CustomerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long = System.currentTimeMillis(),
    val details: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
