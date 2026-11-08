package com.example.data.model

data class BusinessSettings(
    val name: String = "Con-nect Advertising",
    val tagline: String = "Con-nect POS by Sio Creations",
    val regNumber: String = "",
    val taxId: String = "",
    val address: String = "No. 45, High Level Rd, Colombo, Sri Lanka",
    val phone: String = "+94 75 510 3772",
    val email: String = "billing@connectstudio.lk",
    val website: String = "https://connectstudio.lk",
    val currency: String = "LKR",
    val termsDays: Int = 14,
    val taxLabel: String = "Tax / VAT",
    val defaultTaxRate: Double = 0.0,
    val invoicePrefix: String = "CON",
    val nextInvoiceNumber: Int = 1001,
    val termsText: String = "Please make payment by the due date shown. Contact us if you have any questions about this invoice.",
    val receiptFooter: String = "Con-nect POS by Sio Creations (WhatsApp) +94 75 510 3772",
    val bankName: String = "",
    val accountName: String = "",
    val accountNumber: String = "",
    val branchName: String = "",
    val paymentInstructions: String = ""
)

data class Customer(
    val id: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ServiceItem(
    val id: String,
    val name: String,
    val type: String = "service", // "service" or "product"
    val category: String = "Advertising",
    val price: Double = 0.0,
    val code: String = "",
    val stock: Double = 0.0,
    val description: String = ""
)

data class InvoiceLineItem(
    val serviceId: String? = null,
    val name: String,
    val description: String = "",
    val qty: Double = 1.0,
    val unitPrice: Double = 0.0,
    val type: String = "service"
) {
    val lineTotal: Double get() = qty * unitPrice
}

data class Invoice(
    val id: String,
    val number: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String = "",
    val customerEmail: String = "",
    val customerAddress: String = "",
    val date: String,
    val dueDate: String,
    val description: String = "",
    val items: List<InvoiceLineItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val taxLabel: String = "Tax / VAT",
    val extraCharges: Double = 0.0,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val status: String = "Issued",
    val createdAt: Long = System.currentTimeMillis(),
    val dollarRate: Double = 0.0
) {
    val dueAmount: Double get() = (total - paidAmount).coerceAtLeast(0.0)
    val isPaid: Boolean get() = dueAmount < 0.01
    val isPartial: Boolean get() = paidAmount > 0.01 && !isPaid
    val hasDollarRate: Boolean get() = dollarRate > 0.0
    val totalInUsd: Double get() = if (hasDollarRate) total / dollarRate else 0.0
}

data class PaymentRecord(
    val id: String,
    val invoiceId: String,
    val amount: Double,
    val method: String = "Cash",
    val date: String,
    val reference: String = "",
    val note: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

data class Expense(
    val id: String,
    val title: String,
    val category: String = "Advertising spend",
    val amount: Double = 0.0,
    val date: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DashboardMetrics(
    val invoicedRevenue: Double = 0.0,
    val cashReceived: Double = 0.0,
    val outstandingDues: Double = 0.0,
    val operatingExpenses: Double = 0.0,
    val netCash: Double = 0.0,
    val totalInvoicesCount: Int = 0,
    val pendingDuesCount: Int = 0,
    val methodBreakdown: Map<String, Double> = emptyMap()
)

enum class NavigationTab(val label: String) {
    OVERVIEW("Home"),
    INVOICES("Bills"),
    SERVICES("Rates"),
    CUSTOMERS("Clients"),
    EXPENSES("Costs"),
    SETTINGS("Setup")
}

