package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Invoice::class, InvoiceItem::class, Customer::class, Product::class, CustomerTransaction::class, Expense::class],
    version = 2,
    exportSchema = false
)
abstract class GroceryDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile private var INSTANCE: GroceryDatabase? = null
        fun getDatabase(context: Context): GroceryDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, GroceryDatabase::class.java, "grocery_database")
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, phone TEXT NOT NULL, address TEXT NOT NULL, openingBalance REAL NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, salePrice REAL NOT NULL, purchasePrice REAL NOT NULL, stock REAL NOT NULL, minimumStock REAL NOT NULL, unit TEXT NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_products_name ON products(name)")
                db.execSQL("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, customerId INTEGER NOT NULL, date INTEGER NOT NULL, details TEXT NOT NULL, debit REAL NOT NULL, credit REAL NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_customerId ON transactions(customerId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, amount REAL NOT NULL, date INTEGER NOT NULL, note TEXT NOT NULL)")
                db.execSQL("ALTER TABLE invoices ADD COLUMN customerId INTEGER")
                db.execSQL("ALTER TABLE invoices ADD COLUMN paidAmount REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE invoices ADD COLUMN type TEXT NOT NULL DEFAULT 'sale'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_invoices_customerId ON invoices(customerId)")
            }
        }
    }
}

@androidx.room.Dao
interface InvoiceDao {
    @androidx.room.Query("SELECT * FROM invoices ORDER BY date DESC") fun getAllInvoices(): kotlinx.coroutines.flow.Flow<List<Invoice>>
    @androidx.room.Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId") fun getItemsForInvoice(invoiceId: Long): kotlinx.coroutines.flow.Flow<List<InvoiceItem>>
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun insertInvoice(invoice: Invoice): Long
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun insertItems(items: List<InvoiceItem>)
    @androidx.room.Delete suspend fun deleteInvoice(invoice: Invoice)
    @androidx.room.Query("SELECT * FROM invoices WHERE id = :id") suspend fun getInvoiceById(id: Long): Invoice?
    @androidx.room.Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE") fun getCustomers(): kotlinx.coroutines.flow.Flow<List<Customer>>
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun insertCustomer(customer: Customer): Long
    @androidx.room.Delete suspend fun deleteCustomer(customer: Customer)
    @androidx.room.Query("SELECT * FROM products ORDER BY name COLLATE NOCASE") fun getProducts(): kotlinx.coroutines.flow.Flow<List<Product>>
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun insertProduct(product: Product): Long
    @androidx.room.Delete suspend fun deleteProduct(product: Product)
    @androidx.room.Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC") fun getTransactions(customerId: Long): kotlinx.coroutines.flow.Flow<List<CustomerTransaction>>
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun insertTransaction(transaction: CustomerTransaction): Long
    @androidx.room.Delete suspend fun deleteTransaction(transaction: CustomerTransaction)
    @androidx.room.Query("SELECT * FROM expenses ORDER BY date DESC") fun getExpenses(): kotlinx.coroutines.flow.Flow<List<Expense>>
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun insertExpense(expense: Expense): Long
}
