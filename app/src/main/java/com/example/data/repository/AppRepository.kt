package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.db.CategoryEntity
import com.example.data.db.CustomerEntity
import com.example.data.db.ExpenseEntity
import com.example.data.db.InvoiceEntity
import com.example.data.db.LineItemsConverter
import com.example.data.db.PaymentEntity
import com.example.data.db.ServiceItemEntity
import com.example.data.db.SettingEntity
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Invoice
import com.example.data.model.PaymentRecord
import com.example.data.model.ServiceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppRepository(private val dao: AppDao) {
    private val converter = LineItemsConverter()

    val customers: Flow<List<Customer>> = dao.getAllCustomers().map { list ->
        list.map {
            Customer(
                id = it.id,
                name = it.name,
                phone = it.phone,
                email = it.email,
                address = it.address,
                notes = it.notes,
                createdAt = it.createdAt
            )
        }
    }

    val services: Flow<List<ServiceItem>> = dao.getAllServices().map { list ->
        list.map {
            ServiceItem(
                id = it.id,
                name = it.name,
                type = it.type,
                category = it.category,
                price = it.price,
                code = it.code,
                stock = it.stock,
                description = it.description
            )
        }
    }

    val invoices: Flow<List<Invoice>> = dao.getAllInvoices().map { list ->
        list.map {
            Invoice(
                id = it.id,
                number = it.number,
                customerId = it.customerId,
                customerName = it.customerName,
                customerPhone = it.customerPhone,
                customerEmail = it.customerEmail,
                customerAddress = it.customerAddress,
                date = it.date,
                dueDate = it.dueDate,
                description = it.description,
                items = converter.toLineItems(it.itemsJson),
                subtotal = it.subtotal,
                discount = it.discount,
                taxRate = it.taxRate,
                taxAmount = it.taxAmount,
                taxLabel = it.taxLabel,
                extraCharges = it.extraCharges,
                total = it.total,
                paidAmount = it.paidAmount,
                paymentMethod = it.paymentMethod,
                notes = it.notes,
                status = it.status,
                createdAt = it.createdAt
            )
        }
    }

    val payments: Flow<List<PaymentRecord>> = dao.getAllPayments().map { list ->
        list.map {
            PaymentRecord(
                id = it.id,
                invoiceId = it.invoiceId,
                amount = it.amount,
                method = it.method,
                date = it.date,
                reference = it.reference,
                note = it.note,
                recordedAt = it.recordedAt
            )
        }
    }

    val expenses: Flow<List<Expense>> = dao.getAllExpenses().map { list ->
        list.map {
            Expense(
                id = it.id,
                title = it.title,
                category = it.category,
                amount = it.amount,
                date = it.date,
                note = it.note,
                createdAt = it.createdAt
            )
        }
    }

    val categories: Flow<List<String>> = dao.getAllCategories().map { list ->
        list.map { it.name }
    }

    val settings: Flow<BusinessSettings> = dao.getAllSettings().map { list ->
        val map = list.associate { it.key to it.value }
        BusinessSettings(
            name = map["name"] ?: "Con-nect Advertising",
            tagline = map["tagline"] ?: "Con-nect POS by Sio Creations",
            regNumber = map["regNumber"] ?: "",
            taxId = map["taxId"] ?: "",
            address = map["address"] ?: "No. 45, High Level Rd, Colombo, Sri Lanka",
            phone = map["phone"] ?: "+94 75 510 3772",
            email = map["email"] ?: "billing@connectstudio.lk",
            website = map["website"] ?: "https://connectstudio.lk",
            currency = map["currency"] ?: "LKR",
            termsDays = map["termsDays"]?.toIntOrNull() ?: 14,
            taxLabel = map["taxLabel"] ?: "Tax / VAT",
            defaultTaxRate = map["defaultTaxRate"]?.toDoubleOrNull() ?: 0.0,
            invoicePrefix = map["invoicePrefix"] ?: "CON",
            nextInvoiceNumber = map["nextInvoiceNumber"]?.toIntOrNull() ?: 1004,
            termsText = map["termsText"]
                ?: "Please make payment by the due date shown. Contact us if you have any questions about this invoice.",
            receiptFooter = map["receiptFooter"]
                ?: "Con-nect POS by Sio Creations (WhatsApp) +94 75 510 3772"
        )
    }

    fun getPaymentsForInvoice(invoiceId: String): Flow<List<PaymentRecord>> {
        return dao.getPaymentsForInvoice(invoiceId).map { list ->
            list.map {
                PaymentRecord(
                    id = it.id,
                    invoiceId = it.invoiceId,
                    amount = it.amount,
                    method = it.method,
                    date = it.date,
                    reference = it.reference,
                    note = it.note,
                    recordedAt = it.recordedAt
                )
            }
        }
    }

    suspend fun saveCustomer(customer: Customer) {
        val id = if (customer.id.isBlank()) "CUS-" + UUID.randomUUID().toString().take(6).uppercase() else customer.id
        dao.insertCustomer(
            CustomerEntity(
                id = id,
                name = customer.name.trim(),
                phone = customer.phone.trim(),
                email = customer.email.trim(),
                address = customer.address.trim(),
                notes = customer.notes.trim(),
                createdAt = customer.createdAt
            )
        )
    }

    suspend fun deleteCustomer(id: String) {
        dao.deleteCustomer(id)
    }

    suspend fun saveService(service: ServiceItem) {
        val id = if (service.id.isBlank()) "SKU-" + UUID.randomUUID().toString().take(6).uppercase() else service.id
        dao.insertService(
            ServiceItemEntity(
                id = id,
                name = service.name.trim(),
                type = service.type,
                category = service.category.trim(),
                price = service.price,
                code = service.code.trim(),
                stock = service.stock,
                description = service.description.trim()
            )
        )
    }

    suspend fun deleteService(id: String) {
        dao.deleteService(id)
    }

    suspend fun saveCategory(category: String) {
        if (category.isNotBlank()) {
            dao.insertCategory(CategoryEntity(name = category.trim()))
        }
    }

    suspend fun deleteCategory(category: String) {
        dao.deleteCategory(category)
    }

    suspend fun createInvoice(invoice: Invoice, nextNumber: Int): Invoice {
        val id = if (invoice.id.isBlank()) "INV-" + UUID.randomUUID().toString().take(8).uppercase() else invoice.id
        val finalInvoice = invoice.copy(id = id)

        dao.insertInvoice(
            InvoiceEntity(
                id = finalInvoice.id,
                number = finalInvoice.number,
                customerId = finalInvoice.customerId,
                customerName = finalInvoice.customerName,
                customerPhone = finalInvoice.customerPhone,
                customerEmail = finalInvoice.customerEmail,
                customerAddress = finalInvoice.customerAddress,
                date = finalInvoice.date,
                dueDate = finalInvoice.dueDate,
                description = finalInvoice.description,
                itemsJson = converter.fromLineItems(finalInvoice.items),
                subtotal = finalInvoice.subtotal,
                discount = finalInvoice.discount,
                taxRate = finalInvoice.taxRate,
                taxAmount = finalInvoice.taxAmount,
                taxLabel = finalInvoice.taxLabel,
                extraCharges = finalInvoice.extraCharges,
                total = finalInvoice.total,
                paidAmount = finalInvoice.paidAmount,
                paymentMethod = finalInvoice.paymentMethod,
                notes = finalInvoice.notes,
                status = finalInvoice.status,
                createdAt = finalInvoice.createdAt
            )
        )

        // Deduct inventory stock for product items
        for (item in finalInvoice.items) {
            if (item.serviceId != null && item.type == "product") {
                dao.deductStock(item.serviceId, item.qty)
            }
        }

        // If upfront payment was made, record payment
        if (finalInvoice.paidAmount > 0.0) {
            val payId = "PAY-" + UUID.randomUUID().toString().take(6).uppercase()
            dao.insertPayment(
                PaymentEntity(
                    id = payId,
                    invoiceId = finalInvoice.id,
                    amount = finalInvoice.paidAmount,
                    method = finalInvoice.paymentMethod,
                    date = finalInvoice.date,
                    reference = "Upfront payment",
                    note = "Recorded at invoice creation",
                    recordedAt = System.currentTimeMillis()
                )
            )
        }

        // Increment next invoice number
        dao.setSetting(SettingEntity(key = "nextInvoiceNumber", value = (nextNumber + 1).toString()))

        return finalInvoice
    }

    suspend fun recordPayment(
        invoice: Invoice,
        amount: Double,
        method: String,
        date: String,
        reference: String,
        note: String
    ) {
        val payId = "PAY-" + UUID.randomUUID().toString().take(6).uppercase()
        val now = System.currentTimeMillis()
        dao.insertPayment(
            PaymentEntity(
                id = payId,
                invoiceId = invoice.id,
                amount = amount,
                method = method,
                date = date,
                reference = reference.trim(),
                note = note.trim(),
                recordedAt = now
            )
        )

        val newPaid = (invoice.paidAmount + amount).coerceAtMost(invoice.total)
        dao.updateInvoice(
            InvoiceEntity(
                id = invoice.id,
                number = invoice.number,
                customerId = invoice.customerId,
                customerName = invoice.customerName,
                customerPhone = invoice.customerPhone,
                customerEmail = invoice.customerEmail,
                customerAddress = invoice.customerAddress,
                date = invoice.date,
                dueDate = invoice.dueDate,
                description = invoice.description,
                itemsJson = converter.fromLineItems(invoice.items),
                subtotal = invoice.subtotal,
                discount = invoice.discount,
                taxRate = invoice.taxRate,
                taxAmount = invoice.taxAmount,
                taxLabel = invoice.taxLabel,
                extraCharges = invoice.extraCharges,
                total = invoice.total,
                paidAmount = newPaid,
                paymentMethod = method,
                notes = invoice.notes,
                status = if (invoice.total - newPaid < 0.01) "Paid" else invoice.status,
                createdAt = invoice.createdAt
            )
        )
    }

    suspend fun deleteInvoice(id: String) {
        dao.deletePaymentsForInvoice(id)
        dao.deleteInvoice(id)
    }

    suspend fun saveExpense(expense: Expense) {
        val id = if (expense.id.isBlank()) "EXP-" + UUID.randomUUID().toString().take(6).uppercase() else expense.id
        dao.insertExpense(
            ExpenseEntity(
                id = id,
                title = expense.title.trim(),
                category = expense.category,
                amount = expense.amount,
                date = expense.date,
                note = expense.note.trim(),
                createdAt = expense.createdAt
            )
        )
    }

    suspend fun deleteExpense(id: String) {
        dao.deleteExpense(id)
    }

    suspend fun updateSettings(settings: BusinessSettings) {
        dao.setSetting(SettingEntity("name", settings.name))
        dao.setSetting(SettingEntity("tagline", settings.tagline))
        dao.setSetting(SettingEntity("regNumber", settings.regNumber))
        dao.setSetting(SettingEntity("taxId", settings.taxId))
        dao.setSetting(SettingEntity("address", settings.address))
        dao.setSetting(SettingEntity("phone", settings.phone))
        dao.setSetting(SettingEntity("email", settings.email))
        dao.setSetting(SettingEntity("website", settings.website))
        dao.setSetting(SettingEntity("currency", settings.currency))
        dao.setSetting(SettingEntity("termsDays", settings.termsDays.toString()))
        dao.setSetting(SettingEntity("taxLabel", settings.taxLabel))
        dao.setSetting(SettingEntity("defaultTaxRate", settings.defaultTaxRate.toString()))
        dao.setSetting(SettingEntity("invoicePrefix", settings.invoicePrefix))
        dao.setSetting(SettingEntity("nextInvoiceNumber", settings.nextInvoiceNumber.toString()))
        dao.setSetting(SettingEntity("termsText", settings.termsText))
        dao.setSetting(SettingEntity("receiptFooter", settings.receiptFooter))
    }

    suspend fun clearTransactions() {
        dao.clearPayments()
        dao.clearInvoices()
        dao.clearExpenses()
    }
}
