package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.Invoice
import com.example.data.model.PaymentRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SearchTextField
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatMoney
import com.example.ui.theme.DangerRose
import com.example.ui.theme.SuccessGreen

@Composable
fun CustomersScreen(
    customers: List<Customer>,
    invoices: List<Invoice>,
    settings: BusinessSettings,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenAddCustomer: (Customer?) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    onOpenStatement: (Customer) -> Unit,
    onOpenPayment: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val filteredCustomers = customers.filter { c ->
        searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true) ||
                c.email.contains(searchQuery, ignoreCase = true)
    }

    val pendingInvoices = invoices.filter { it.dueAmount > 0.01 }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SearchTextField(
                    query = searchQuery,
                    onQueryChange = onSearchChange,
                    placeholder = "Find customer by name or phone..."
                )
            }

            // Outstanding Invoices quick section
            if (pendingInvoices.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Outstanding Client Dues (${pendingInvoices.size})",
                        subtitle = "Pending invoice balances requiring collection"
                    )
                }

                items(pendingInvoices.take(3), key = { "due_${it.id}" }) { inv ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inv.customerName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${inv.number} • Due: ${formatMoney(inv.dueAmount, settings.currency)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRose
                                    )
                                )
                            }
                            Button(
                                onClick = { onOpenPayment(inv) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Customer Accounts (${filteredCustomers.size})",
                    subtitle = "Client directory & account statements"
                )
            }

            if (filteredCustomers.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.People,
                        title = "No Customers Found",
                        subtitle = if (searchQuery.isNotBlank()) "No records match your query." else "Add client businesses to track their project balances and statements.",
                        actionText = "+ Add Customer",
                        onAction = { onOpenAddCustomer(null) }
                    )
                }
            } else {
                items(filteredCustomers, key = { it.id }) { customer ->
                    val customerInvoices = invoices.filter { it.customerId == customer.id }
                    val totalBilled = customerInvoices.sumOf { it.total }
                    val totalPaid = customerInvoices.sumOf { it.paidAmount }
                    val totalDue = customerInvoices.sumOf { it.dueAmount }

                    CustomerCard(
                        customer = customer,
                        invoicesCount = customerInvoices.size,
                        totalBilled = totalBilled,
                        totalPaid = totalPaid,
                        totalDue = totalDue,
                        currency = settings.currency,
                        onCall = {
                            if (customer.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        onWhatsApp = {
                            if (customer.phone.isNotBlank()) {
                                val cleanNum = customer.phone.replace(Regex("[^0-9+]"), "")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNum"))
                                context.startActivity(intent)
                            }
                        },
                        onStatement = { onOpenStatement(customer) },
                        onEdit = { onOpenAddCustomer(customer) },
                        onDelete = { onDeleteCustomer(customer.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { onOpenAddCustomer(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 86.dp)
                .testTag("fab_add_customer"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Customer")
        }
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    invoicesCount: Int,
    totalBilled: Double,
    totalPaid: Double,
    totalDue: Double,
    currency: String,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onStatement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (customer.email.isNotBlank()) {
                        Text(
                            text = customer.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Balance Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (totalDue > 0.01) DangerRose.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (totalDue > 0.01) "Balance Due" else "Settled",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (totalDue > 0.01) DangerRose else SuccessGreen
                        )
                        Text(
                            text = formatMoney(totalDue, currency),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (totalDue > 0.01) DangerRose else SuccessGreen
                        )
                    }
                }
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = customer.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            // Invoicing Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$invoicesCount invoices",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Billed: ${formatMoney(totalBilled, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Paid: ${formatMoney(totalPaid, currency)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (customer.phone.isNotBlank()) {
                        IconButton(onClick = onCall, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    }
                }

                OutlinedButton(
                    onClick = onStatement,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Statement PDF", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditCustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }
    var errorMsg by remember { mutableStateOf("") }

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
                        text = if (customer == null) "Add Customer" else "Edit Customer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer / Company Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / WhatsApp") },
                    placeholder = { Text("+94 77 ...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Billing Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Client Reference / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMsg = "Customer name is required."
                                return@Button
                            }
                            onSave(
                                Customer(
                                    id = customer?.id ?: "",
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    address = address.trim(),
                                    notes = notes.trim(),
                                    createdAt = customer?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Customer")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerStatementDialog(
    customer: Customer,
    invoices: List<Invoice>,
    payments: List<PaymentRecord>,
    settings: BusinessSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val totalBilled = invoices.sumOf { it.total }
    val totalPaid = invoices.sumOf { it.paidAmount }
    val totalDue = invoices.sumOf { it.dueAmount }

    val statementText = buildStatementText(customer, invoices, payments, settings)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account Statement",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Statement Paper Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = settings.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customer Statement for: ${customer.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = "Contact: ${customer.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Invoiced:", style = MaterialTheme.typography.bodySmall)
                            Text(formatMoney(totalBilled, settings.currency), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid:", style = MaterialTheme.typography.bodySmall)
                            Text(formatMoney(totalPaid, settings.currency), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Outstanding:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                formatMoney(totalDue, settings.currency),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalDue > 0.01) DangerRose else SuccessGreen
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Invoice History (${invoices.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                invoices.forEach { inv ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inv.number, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text(inv.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatMoney(inv.total, settings.currency), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = if (inv.isPaid) "PAID" else "Due: ${formatMoney(inv.dueAmount, settings.currency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (inv.isPaid) SuccessGreen else DangerRose
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { shareStatement(context, statementText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share via App")
                    }

                    OutlinedButton(
                        onClick = { shareStatement(context, statementText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / PDF")
                    }
                }
            }
        }
    }
}

private fun buildStatementText(
    customer: Customer,
    invoices: List<Invoice>,
    payments: List<PaymentRecord>,
    settings: BusinessSettings
): String {
    val totalBilled = invoices.sumOf { it.total }
    val totalPaid = invoices.sumOf { it.paidAmount }
    val totalDue = invoices.sumOf { it.dueAmount }

    val sb = StringBuilder()
    sb.appendLine("==========================================")
    sb.appendLine(settings.name.uppercase())
    sb.appendLine("CUSTOMER ACCOUNT STATEMENT")
    sb.appendLine("Client: ${customer.name}")
    if (customer.phone.isNotBlank()) sb.appendLine("Phone: ${customer.phone}")
    if (customer.address.isNotBlank()) sb.appendLine("Address: ${customer.address}")
    sb.appendLine("------------------------------------------")
    sb.appendLine("SUMMARY OF ACCOUNT:")
    sb.appendLine("Total Invoiced:     ${formatMoney(totalBilled, settings.currency)}")
    sb.appendLine("Total Paid:         ${formatMoney(totalPaid, settings.currency)}")
    sb.appendLine("TOTAL OUTSTANDING:  ${formatMoney(totalDue, settings.currency)}")
    sb.appendLine("------------------------------------------")
    sb.appendLine("INVOICES:")
    invoices.forEach {
        val st = if (it.isPaid) "PAID" else "DUE: ${formatMoney(it.dueAmount, settings.currency)}"
        sb.appendLine("${it.number} (${it.date}) - Total: ${formatMoney(it.total, settings.currency)} [$st]")
    }
    sb.appendLine("==========================================")
    sb.appendLine(settings.receiptFooter)
    return sb.toString()
}

fun shareStatement(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Customer Statement"))
}
