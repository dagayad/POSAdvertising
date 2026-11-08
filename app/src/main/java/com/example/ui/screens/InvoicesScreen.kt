package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.InvoiceLineItem
import com.example.data.model.ServiceItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchTextField
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatMoney
import com.example.ui.theme.DangerRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
    invoices: List<Invoice>,
    customers: List<Customer>,
    services: List<ServiceItem>,
    settings: BusinessSettings,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onOpenCreateInvoice: () -> Unit,
    onOpenReceipt: (Invoice) -> Unit,
    onOpenPayment: (Invoice) -> Unit,
    onDeleteInvoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredInvoices = invoices.filter { inv ->
        val matchesQuery = searchQuery.isBlank() ||
                inv.number.contains(searchQuery, ignoreCase = true) ||
                inv.customerName.contains(searchQuery, ignoreCase = true) ||
                inv.description.contains(searchQuery, ignoreCase = true)

        val matchesStatus = when (statusFilter) {
            "Paid" -> inv.isPaid
            "Partial" -> inv.isPartial
            "Unpaid" -> !inv.isPaid && !inv.isPartial
            else -> true
        }
        matchesQuery && matchesStatus
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            SearchTextField(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Search invoice #, customer or project..."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("All", "Unpaid", "Partial", "Paid")
                items(filters) { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { onStatusFilterChange(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredInvoices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Invoices Found",
                    subtitle = if (searchQuery.isNotBlank()) "No records match your search." else "Create your first commercial invoice to start tracking sales.",
                    actionText = "+ Create Invoice",
                    onAction = onOpenCreateInvoice
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        InvoiceItemCard(
                            invoice = invoice,
                            currency = settings.currency,
                            onOpenReceipt = { onOpenReceipt(invoice) },
                            onOpenPayment = { onOpenPayment(invoice) },
                            onDelete = { onDeleteInvoice(invoice.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onOpenCreateInvoice,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 86.dp)
                .testTag("fab_create_invoice"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Invoice")
        }
    }
}

@Composable
private fun InvoiceItemCard(
    invoice: Invoice,
    currency: String,
    onOpenReceipt: () -> Unit,
    onOpenPayment: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Invoice: ${invoice.number}  •  ${invoice.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(isPaid = invoice.isPaid, isPartial = invoice.isPartial)
            }

            if (invoice.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ref: ${invoice.description}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMoney(invoice.total, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMoney(invoice.paidAmount, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMoney(invoice.dueAmount, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (invoice.dueAmount > 0.01) DangerRose else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (invoice.hasDollarRate) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avg USD Rate: 1 USD ≈ ${formatMoney(invoice.dollarRate, currency)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "≈ $${String.format(Locale.US, "%.2f", invoice.totalInUsd)} USD",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete invoice",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (invoice.dueAmount > 0.01) {
                    Button(
                        onClick = onOpenPayment,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Record Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                OutlinedButton(
                    onClick = onOpenReceipt,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt", fontSize = 12.sp)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Delete Invoice?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to delete invoice ${invoice.number} for ${invoice.customerName}? All associated payment records will also be removed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showDeleteConfirm = false
                                onDelete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    invoice: Invoice,
    currency: String,
    onDismiss: () -> Unit,
    onRecord: (amount: Double, method: String, date: String, ref: String, note: String) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var amountText by remember { mutableStateOf(invoice.dueAmount.toString()) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var refText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val methods = listOf("Cash", "Bank transfer", "Card", "Online payment", "Cheque", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Record Payment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Invoice ${invoice.number} • ${invoice.customerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Outstanding Due: ${formatMoney(invoice.dueAmount, currency)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DangerRose
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMsg = ""
                    },
                    label = { Text("Payment Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotBlank()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(methods) { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = refText,
                    onValueChange = { refText = it },
                    label = { Text("Reference / Txn ID (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0.0) {
                                errorMsg = "Please enter a valid amount."
                                return@Button
                            }
                            if (amount > invoice.dueAmount + 0.01) {
                                errorMsg = "Amount exceeds balance due (${formatMoney(invoice.dueAmount, currency)})."
                                return@Button
                            }
                            onRecord(amount, selectedMethod, today, refText, noteText)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Payment")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceSheet(
    customers: List<Customer>,
    services: List<ServiceItem>,
    settings: BusinessSettings,
    onDismiss: () -> Unit,
    onSubmit: (Invoice) -> Unit
) {
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
    var customerName by remember { mutableStateOf(selectedCustomer?.name ?: "") }
    var customerPhone by remember { mutableStateOf(selectedCustomer?.phone ?: "") }
    var customerEmail by remember { mutableStateOf(selectedCustomer?.email ?: "") }
    var customerAddress by remember { mutableStateOf(selectedCustomer?.address ?: "") }

    var invoiceDate by remember { mutableStateOf(today) }
    var dueDate by remember { mutableStateOf(today) }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(settings.termsText) }

    val lineItems = remember { mutableStateListOf<InvoiceLineItem>() }

    // Quick add from catalogue
    var selectedServiceId by remember { mutableStateOf(services.firstOrNull()?.id ?: "") }
    var serviceQtyText by remember { mutableStateOf("1") }

    // Custom Line item
    var customName by remember { mutableStateOf("") }
    var customPriceText by remember { mutableStateOf("") }
    var customQtyText by remember { mutableStateOf("1") }

    val currency = settings.currency
    var discountText by remember { mutableStateOf("0") }
    var extraText by remember { mutableStateOf("0") }
    var dollarRateText by remember { mutableStateOf("") }
    var paidText by remember { mutableStateOf("0") }
    var selectedMethod by remember { mutableStateOf("Cash") }

    var errorMessage by remember { mutableStateOf("") }
    var itemAddedBannerText by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun requestClose() {
        if (lineItems.isNotEmpty() || customName.isNotBlank() || description.isNotBlank()) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    // Intercept hardware and gesture back to prevent accidental loss
    BackHandler {
        requestClose()
    }

    val subtotal = lineItems.sumOf { it.lineTotal }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val extraCharges = extraText.toDoubleOrNull() ?: 0.0
    val grandTotal = ((subtotal - discount).coerceAtLeast(0.0) + extraCharges).coerceAtLeast(0.0)

    val methods = listOf("Cash", "Bank transfer", "Card", "Online payment", "Cheque", "Other")

    // Full screen dialog that NEVER dismisses on accidental outside touch/mistouch
    Dialog(
        onDismissRequest = {
            // MISTOUCH PREVENTION: Do NOT dismiss on outside clicks
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Commercial Invoice",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bill campaigns, services & materials • Protected draft",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { requestClose() },
                        modifier = Modifier.testTag("close_invoice_sheet_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close invoice window")
                    }
                }

                // Section 1: Customer Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Customer Information",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (customers.isNotEmpty()) {
                        Text(
                            text = "Select from client directory:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(customers) { c ->
                                FilterChip(
                                    selected = selectedCustomer?.id == c.id,
                                    onClick = {
                                        selectedCustomer = c
                                        customerName = c.name
                                        customerPhone = c.phone
                                        customerEmail = c.email
                                        customerAddress = c.address
                                    },
                                    label = { Text(c.name, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            if (selectedCustomer?.name != it) selectedCustomer = null
                        },
                        label = { Text("Client / Business Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone / WhatsApp") },
                        placeholder = { Text("+94 77 123 4567") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = { customerEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Billing Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section 2: Invoice Dates & Project Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. Invoice Dates & Project Reference",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = invoiceDate,
                            onValueChange = { invoiceDate = it },
                            label = { Text("Issue Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Campaign / Project Reference") },
                        placeholder = { Text("e.g. Q4 Brand Launch / Storefront Display") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section 3: Add Items / Services Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. Add Items & Services",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Option A: Rate Card Catalogue
                    if (services.isNotEmpty()) {
                        Text(
                            text = "A. Select from Rate Card Catalogue:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(services) { s ->
                                FilterChip(
                                    selected = selectedServiceId == s.id,
                                    onClick = { selectedServiceId = s.id },
                                    label = { Text("${s.name} • ${formatMoney(s.price, currency)}", fontSize = 12.sp) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = serviceQtyText,
                                onValueChange = { serviceQtyText = it },
                                label = { Text("Quantity") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val service = services.find { it.id == selectedServiceId }
                                    val qty = serviceQtyText.toDoubleOrNull() ?: 1.0
                                    if (service != null && qty > 0) {
                                        lineItems.add(
                                            InvoiceLineItem(
                                                serviceId = service.id,
                                                name = service.name,
                                                description = service.description,
                                                qty = qty,
                                                unitPrice = service.price,
                                                type = service.type
                                            )
                                        )
                                        val countDisplay = if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
                                        val notificationText = "Added '${service.name}' ($countDisplay × ${formatMoney(service.price, currency)})"
                                        itemAddedBannerText = notificationText
                                        Toast.makeText(context, "Added: ${service.name}", Toast.LENGTH_SHORT).show()
                                        serviceQtyText = "1"
                                        errorMessage = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(54.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Service")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // Option B: Custom Item
                    Text(
                        text = "B. Or Add Custom Service / Item:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Custom Service or Item Name") },
                        placeholder = { Text("e.g. 3D Acrylic Lettering Signboard") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customPriceText,
                            onValueChange = { customPriceText = it },
                            label = { Text("Unit Price ($currency)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = customQtyText,
                            onValueChange = { customQtyText = it },
                            label = { Text("Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val price = customPriceText.toDoubleOrNull() ?: 0.0
                            val qty = customQtyText.toDoubleOrNull() ?: 1.0
                            if (customName.isNotBlank() && price > 0.0 && qty > 0.0) {
                                val itemName = customName.trim()
                                lineItems.add(
                                    InvoiceLineItem(
                                        serviceId = null,
                                        name = itemName,
                                        description = "Custom service",
                                        qty = qty,
                                        unitPrice = price,
                                        type = "service"
                                    )
                                )
                                val countDisplay = if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
                                val notificationText = "Added '$itemName' ($countDisplay × ${formatMoney(price, currency)})"
                                itemAddedBannerText = notificationText
                                Toast.makeText(context, "Added: $itemName", Toast.LENGTH_SHORT).show()
                                customName = ""
                                customPriceText = ""
                                customQtyText = "1"
                                errorMessage = ""
                            } else if (customName.isBlank()) {
                                errorMessage = "Please enter custom item name."
                            } else if (price <= 0.0) {
                                errorMessage = "Please enter a valid unit price."
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Custom Line Item")
                    }
                }
            }

            // In-Sheet "Item Added" Notification Banner
            itemAddedBannerText?.let { banner ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Item Added to Bill ✓",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = banner,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = { itemAddedBannerText = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss notification",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Section 4: Added Items List
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "4. Invoice Items (${lineItems.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (lineItems.isEmpty()) {
                        Text(
                            text = "No items added yet. Please add at least one service or product item above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        lineItems.forEachIndexed { index, item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${item.qty} × ${formatMoney(item.unitPrice, currency)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        formatMoney(item.lineTotal, currency),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(onClick = { lineItems.removeAt(index) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = DangerRose)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: Discounts & Extra Charges (No Tax / VAT)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "5. Discounts & Extra Charges",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            label = { Text("Discount ($currency)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = extraText,
                            onValueChange = { extraText = it },
                            label = { Text("Extra Fee / Delivery ($currency)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Optional Dollar Rate Field
                    OutlinedTextField(
                        value = dollarRateText,
                        onValueChange = { dollarRateText = it },
                        label = { Text("Average Dollar Rate (USD) - Optional") },
                        placeholder = { Text("e.g. 305.00 (Leave blank if not needed)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = {
                            val rate = dollarRateText.toDoubleOrNull() ?: 0.0
                            if (rate > 0.0) {
                                val usdAmount = grandTotal / rate
                                Text(
                                    "1 USD ≈ $dollarRateText $currency  •  Invoice Total ≈ $${String.format(Locale.US, "%.2f", usdAmount)} USD",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text("Optional: Leave blank if you don't want to show dollar rate on the invoice.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Grand Total Summary Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Items Subtotal", style = MaterialTheme.typography.bodySmall)
                                Text(formatMoney(subtotal, currency), style = MaterialTheme.typography.bodySmall)
                            }
                            if (discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount Applied", style = MaterialTheme.typography.bodySmall)
                                    Text("- ${formatMoney(discount, currency)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (extraCharges > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Extra Charges", style = MaterialTheme.typography.bodySmall)
                                    Text(formatMoney(extraCharges, currency), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    "GRAND TOTAL",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    formatMoney(grandTotal, currency),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }

                            val parsedDollarRate = dollarRateText.toDoubleOrNull() ?: 0.0
                            if (parsedDollarRate > 0.0) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        "USD Reference (1 USD ≈ $dollarRateText $currency)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        "≈ $${String.format(Locale.US, "%.2f", grandTotal / parsedDollarRate)} USD",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: Payment & Terms
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "6. Upfront Payment & Terms",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("Advance / Paid Now ($currency)") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Payment Method:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(methods) { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Terms & Conditions / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                }
            }

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Final Submit Button
            Button(
                onClick = {
                    if (customerName.isBlank()) {
                        errorMessage = "Customer name is required."
                        return@Button
                    }
                    if (lineItems.isEmpty()) {
                        errorMessage = "Please add at least one line item to the invoice."
                        return@Button
                    }

                    val paidAmount = paidText.toDoubleOrNull() ?: 0.0
                    val invoiceNumber = "${settings.invoicePrefix}-${settings.nextInvoiceNumber.toString().padStart(5, '0')}"

                    val newInvoice = Invoice(
                        id = "",
                        number = invoiceNumber,
                        customerId = selectedCustomer?.id ?: "CUS-NEW",
                        customerName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        customerEmail = customerEmail.trim(),
                        customerAddress = customerAddress.trim(),
                        date = invoiceDate.trim(),
                        dueDate = dueDate.trim(),
                        description = description.trim(),
                        items = lineItems.toList(),
                        subtotal = subtotal,
                        discount = discount,
                        taxRate = 0.0,
                        taxAmount = 0.0,
                        taxLabel = "",
                        extraCharges = extraCharges,
                        total = grandTotal,
                        paidAmount = paidAmount.coerceAtMost(grandTotal),
                        paymentMethod = selectedMethod,
                        notes = notes.trim(),
                        status = if (grandTotal - paidAmount < 0.01) "Paid" else "Issued",
                        dollarRate = dollarRateText.toDoubleOrNull() ?: 0.0
                    )

                    onSubmit(newInvoice)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_create_invoice_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save & Generate Commercial Receipt",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

    if (showDiscardConfirm) {
        Dialog(onDismissRequest = { showDiscardConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Discard Invoice Draft?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have unsaved invoice items and information. Are you sure you want to discard this draft and exit?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDiscardConfirm = false }) {
                            Text("Continue Editing")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showDiscardConfirm = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Discard Draft")
                        }
                    }
                }
            }
        }
    }
}
