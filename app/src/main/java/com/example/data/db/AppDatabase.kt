package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        CustomerEntity::class,
        ServiceItemEntity::class,
        InvoiceEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        SettingEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LineItemsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "connect_studio.db"
                ).addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.appDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: AppDao) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())
            val now = System.currentTimeMillis()

            // Categories
            listOf(
                "Advertising",
                "Design",
                "Printing",
                "Digital Marketing",
                "Materials",
                "Outdoor Advertising",
                "Signage"
            ).forEach {
                dao.insertCategory(CategoryEntity(name = it))
            }

            // Services
            val services = listOf(
                ServiceItemEntity(
                    id = "SKU-ADV-01",
                    name = "Social Media Marketing Campaign",
                    type = "service",
                    category = "Advertising",
                    price = 45000.0,
                    code = "ADV-SMM",
                    stock = 0.0,
                    description = "Monthly Facebook & Instagram Ads management, copy, and creatives"
                ),
                ServiceItemEntity(
                    id = "SKU-DES-01",
                    name = "Brand Identity & Logo Package",
                    type = "service",
                    category = "Design",
                    price = 28000.0,
                    code = "DES-LOGO",
                    stock = 0.0,
                    description = "Primary logo, color scheme, typography guidelines, vector assets"
                ),
                ServiceItemEntity(
                    id = "SKU-PRT-01",
                    name = "Vinyl Flex Banner (8ft x 4ft)",
                    type = "service",
                    category = "Printing",
                    price = 7200.0,
                    code = "PRT-FLX",
                    stock = 0.0,
                    description = "High resolution exterior solvent print with metal eyelets"
                ),
                ServiceItemEntity(
                    id = "SKU-PRT-02",
                    name = "Tri-Fold Corporate Brochures (1,000 pcs)",
                    type = "service",
                    category = "Printing",
                    price = 32000.0,
                    code = "PRT-BROC",
                    stock = 0.0,
                    description = "150gsm gloss art paper, full color double-sided offset print"
                ),
                ServiceItemEntity(
                    id = "SKU-MAT-01",
                    name = "Aluminum Roll-Up Standee (33in x 78in)",
                    type = "product",
                    category = "Materials",
                    price = 14500.0,
                    code = "MAT-STAND",
                    stock = 18.0,
                    description = "Sturdy aluminum base with padded carry bag and printed satin film"
                ),
                ServiceItemEntity(
                    id = "SKU-DIG-01",
                    name = "Google Ads & PPC Search Optimization",
                    type = "service",
                    category = "Digital Marketing",
                    price = 35000.0,
                    code = "DIG-PPC",
                    stock = 0.0,
                    description = "Targeted keyword research, negative keywords, analytics tracking"
                )
            )
            services.forEach { dao.insertService(it) }

            // Customers
            val customers = listOf(
                CustomerEntity(
                    id = "CUS-APEX",
                    name = "Apex Holdings (Pvt) Ltd",
                    phone = "+94 77 123 4567",
                    email = "marketing@apexholdings.lk",
                    address = "No. 120, Union Place, Colombo 02",
                    notes = "Corporate annual retainer client",
                    createdAt = now - 86400000L * 20
                ),
                CustomerEntity(
                    id = "CUS-DILRUK",
                    name = "Dilrukshi Fashions",
                    phone = "+94 71 987 6543",
                    email = "dilrukshi.boutique@gmail.com",
                    address = "No. 450, Galle Road, Dehiwala",
                    notes = "Boutique store signage & social media",
                    createdAt = now - 86400000L * 15
                ),
                CustomerEntity(
                    id = "CUS-KANDY",
                    name = "Kandy Tea Exporters",
                    phone = "+94 81 223 3445",
                    email = "exports@kandytea.lk",
                    address = "No. 88, Peradeniya Rd, Kandy",
                    notes = "Packaging & international trade show banners",
                    createdAt = now - 86400000L * 8
                )
            )
            customers.forEach { dao.insertCustomer(it) }

            // Invoices
            val conv = LineItemsConverter()
            val inv1Items = listOf(
                com.example.data.model.InvoiceLineItem(
                    serviceId = "SKU-ADV-01",
                    name = "Social Media Marketing Campaign",
                    description = "Monthly Facebook & Instagram Ads",
                    qty = 1.0,
                    unitPrice = 45000.0
                ),
                com.example.data.model.InvoiceLineItem(
                    serviceId = "SKU-PRT-01",
                    name = "Vinyl Flex Banner (8ft x 4ft)",
                    description = "Storefront banner",
                    qty = 2.0,
                    unitPrice = 7200.0
                )
            )
            dao.insertInvoice(
                InvoiceEntity(
                    id = "INV-1001",
                    number = "CON-01001",
                    customerId = "CUS-APEX",
                    customerName = "Apex Holdings (Pvt) Ltd",
                    customerPhone = "+94 77 123 4567",
                    customerEmail = "marketing@apexholdings.lk",
                    customerAddress = "No. 120, Union Place, Colombo 02",
                    date = today,
                    dueDate = today,
                    description = "Q3 Campaign Launch & Storefront Display",
                    itemsJson = conv.fromLineItems(inv1Items),
                    subtotal = 59400.0,
                    discount = 0.0,
                    taxRate = 0.0,
                    taxAmount = 0.0,
                    taxLabel = "Tax / VAT",
                    extraCharges = 0.0,
                    total = 59400.0,
                    paidAmount = 59400.0,
                    paymentMethod = "Bank transfer",
                    notes = "Full payment settled via commercial bank transfer.",
                    status = "Issued",
                    createdAt = now - 86400000L * 5
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    id = "PAY-001",
                    invoiceId = "INV-1001",
                    amount = 59400.0,
                    method = "Bank transfer",
                    date = today,
                    reference = "TXN-COM-88392",
                    note = "Settled on issue",
                    recordedAt = now - 86400000L * 5
                )
            )

            // Invoice 2: Partially Paid
            val inv2Items = listOf(
                com.example.data.model.InvoiceLineItem(
                    serviceId = "SKU-DES-01",
                    name = "Brand Identity & Logo Package",
                    qty = 1.0,
                    unitPrice = 28000.0
                ),
                com.example.data.model.InvoiceLineItem(
                    serviceId = "SKU-MAT-01",
                    name = "Aluminum Roll-Up Standee",
                    qty = 1.0,
                    unitPrice = 14500.0
                )
            )
            dao.insertInvoice(
                InvoiceEntity(
                    id = "INV-1002",
                    number = "CON-01002",
                    customerId = "CUS-DILRUK",
                    customerName = "Dilrukshi Fashions",
                    customerPhone = "+94 71 987 6543",
                    customerEmail = "dilrukshi.boutique@gmail.com",
                    customerAddress = "No. 450, Galle Road, Dehiwala",
                    date = today,
                    dueDate = today,
                    description = "New Boutique Identity & Roll-up Standee",
                    itemsJson = conv.fromLineItems(inv2Items),
                    subtotal = 42500.0,
                    discount = 2500.0,
                    taxRate = 0.0,
                    taxAmount = 0.0,
                    taxLabel = "Tax / VAT",
                    extraCharges = 0.0,
                    total = 40000.0,
                    paidAmount = 25000.0,
                    paymentMethod = "Cash",
                    notes = "Advance paid. Remaining balance of LKR 15,000 due upon roll-up delivery.",
                    status = "Issued",
                    createdAt = now - 86400000L * 2
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    id = "PAY-002",
                    invoiceId = "INV-1002",
                    amount = 25000.0,
                    method = "Cash",
                    date = today,
                    reference = "REC-ADV-102",
                    note = "Deposit payment",
                    recordedAt = now - 86400000L * 2
                )
            )

            // Invoice 3: Unpaid
            val inv3Items = listOf(
                com.example.data.model.InvoiceLineItem(
                    serviceId = "SKU-PRT-02",
                    name = "Tri-Fold Corporate Brochures (1,000 pcs)",
                    qty = 1.0,
                    unitPrice = 32000.0
                )
            )
            dao.insertInvoice(
                InvoiceEntity(
                    id = "INV-1003",
                    number = "CON-01003",
                    customerId = "CUS-KANDY",
                    customerName = "Kandy Tea Exporters",
                    customerPhone = "+94 81 223 3445",
                    customerEmail = "exports@kandytea.lk",
                    customerAddress = "No. 88, Peradeniya Rd, Kandy",
                    date = today,
                    dueDate = today,
                    description = "Tokyo Trade Expo Product Brochures",
                    itemsJson = conv.fromLineItems(inv3Items),
                    subtotal = 32000.0,
                    discount = 0.0,
                    taxRate = 0.0,
                    taxAmount = 0.0,
                    taxLabel = "Tax / VAT",
                    extraCharges = 0.0,
                    total = 32000.0,
                    paidAmount = 0.0,
                    paymentMethod = "Bank transfer",
                    notes = "Payment due within 14 days of invoice delivery.",
                    status = "Issued",
                    createdAt = now - 86400000L * 1
                )
            )

            // Expenses
            val expenses = listOf(
                ExpenseEntity(
                    id = "EXP-001",
                    title = "Meta Ads Sponsored Campaigns",
                    category = "Advertising spend",
                    amount = 18500.0,
                    date = today,
                    note = "Client ad spend prepaid card recharge",
                    createdAt = now - 86400000L * 4
                ),
                ExpenseEntity(
                    id = "EXP-002",
                    title = "Adobe Creative Cloud Annual Subscription",
                    category = "Software & subscriptions",
                    amount = 9200.0,
                    date = today,
                    note = "Illustrator, Photoshop, InDesign team licenses",
                    createdAt = now - 86400000L * 7
                ),
                ExpenseEntity(
                    id = "EXP-003",
                    title = "Heavy Vinyl Flex Roll 3.2m x 50m",
                    category = "Printing & production",
                    amount = 14500.0,
                    date = today,
                    note = "Supplier: Lanka Media Materials",
                    createdAt = now - 86400000L * 2
                ),
                ExpenseEntity(
                    id = "EXP-004",
                    title = "Studio Fiber Internet & Electricity",
                    category = "Office & utilities",
                    amount = 8400.0,
                    date = today,
                    note = "Monthly utility billing",
                    createdAt = now - 86400000L * 3
                )
            )
            expenses.forEach { dao.insertExpense(it) }
        }
    }
}
