package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessSettings
import com.example.data.model.Invoice
import com.example.data.model.PaymentRecord
import com.example.ui.theme.BrandIndigo

@Composable
fun ThermalReceiptDialog(
    invoice: Invoice,
    payments: List<PaymentRecord>,
    settings: BusinessSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptText = buildReceiptText(invoice, payments, settings)

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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thermal Receipt Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Realistic Paper Receipt View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFCFCFD), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = settings.name.uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        if (settings.tagline.isNotBlank()) {
                            Text(
                                text = settings.tagline,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (settings.phone.isNotBlank()) {
                            Text(
                                text = "Tel / WhatsApp: ${settings.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }

                        ReceiptDashedDivider()

                        Text(
                            text = "COMMERCIAL INVOICE & RECEIPT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Invoice #: ${invoice.number}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "Date: ${invoice.date} | Due: ${invoice.dueDate}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF64748B)
                        )

                        ReceiptDashedDivider()

                        // Customer
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Client:",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = invoice.customerName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1E293B)
                            )
                        }
                        if (invoice.customerPhone.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Phone:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = invoice.customerPhone,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        ReceiptDashedDivider()

                        // Line items
                        invoice.items.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.qty} × ${formatMoney(item.unitPrice, settings.currency)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = formatMoney(item.lineTotal, settings.currency),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }

                        ReceiptDashedDivider()

                        // Financial totals
                        ReceiptRow("Subtotal", formatMoney(invoice.subtotal, settings.currency))
                        if (invoice.discount > 0) {
                            ReceiptRow("Discount", "- ${formatMoney(invoice.discount, settings.currency)}")
                        }
                        if (invoice.taxAmount > 0) {
                            ReceiptRow("${invoice.taxLabel} (${invoice.taxRate}%)", formatMoney(invoice.taxAmount, settings.currency))
                        }
                        if (invoice.extraCharges > 0) {
                            ReceiptRow("Extra charges", formatMoney(invoice.extraCharges, settings.currency))
                        }

                        Divider(color = Color(0xFF0F172A), thickness = 1.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = formatMoney(invoice.total, settings.currency),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }

                        ReceiptRow("Paid to Date", formatMoney(invoice.paidAmount, settings.currency))
                        ReceiptRow(
                            label = "BALANCE DUE",
                            value = formatMoney(invoice.dueAmount, settings.currency),
                            isBold = true,
                            textColor = if (invoice.dueAmount > 0.01) Color(0xFFBE123C) else Color(0xFF15803D)
                        )

                        ReceiptDashedDivider()

                        // Status & Payments
                        Text(
                            text = "STATUS: ${if (invoice.isPaid) "PAID IN FULL" else if (invoice.isPartial) "PARTIALLY PAID" else "UNPAID"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (invoice.isPaid) Color(0xFF15803D) else Color(0xFFB45309),
                            textAlign = TextAlign.Center
                        )

                        if (payments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            payments.forEach { pay ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${pay.date} · ${pay.method}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = formatMoney(pay.amount, settings.currency),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }

                        ReceiptDashedDivider()

                        Text(
                            text = settings.receiptFooter,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Thank you for your business!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { shareReceipt(context, receiptText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share via App")
                    }

                    OutlinedButton(
                        onClick = { shareReceipt(context, receiptText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_receipt_button"),
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

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    textColor: Color = Color(0xFF1E293B)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = Color(0xFF475569)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor
        )
    }
}

@Composable
private fun ReceiptDashedDivider() {
    Text(
        text = "------------------------------------------",
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        ),
        color = Color(0xFFCBD5E1),
        maxLines = 1,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

private fun buildReceiptText(invoice: Invoice, payments: List<PaymentRecord>, settings: BusinessSettings): String {
    val sb = StringBuilder()
    sb.appendLine("==========================================")
    sb.appendLine(settings.name.uppercase())
    if (settings.tagline.isNotBlank()) sb.appendLine(settings.tagline)
    if (settings.phone.isNotBlank()) sb.appendLine("Tel/WhatsApp: ${settings.phone}")
    sb.appendLine("==========================================")
    sb.appendLine("COMMERCIAL INVOICE & RECEIPT")
    sb.appendLine("Invoice No: ${invoice.number}")
    sb.appendLine("Date: ${invoice.date}  Due: ${invoice.dueDate}")
    sb.appendLine("Client: ${invoice.customerName}")
    if (invoice.customerPhone.isNotBlank()) sb.appendLine("Phone: ${invoice.customerPhone}")
    sb.appendLine("------------------------------------------")
    sb.appendLine("ITEMS / SERVICES:")
    invoice.items.forEach {
        sb.appendLine("${it.name}")
        sb.appendLine("  ${it.qty} x ${formatMoney(it.unitPrice, settings.currency)} = ${formatMoney(it.lineTotal, settings.currency)}")
    }
    sb.appendLine("------------------------------------------")
    sb.appendLine("Subtotal:       ${formatMoney(invoice.subtotal, settings.currency)}")
    if (invoice.discount > 0) sb.appendLine("Discount:      -${formatMoney(invoice.discount, settings.currency)}")
    if (invoice.taxAmount > 0) sb.appendLine("${invoice.taxLabel}:    ${formatMoney(invoice.taxAmount, settings.currency)}")
    if (invoice.extraCharges > 0) sb.appendLine("Extra charges:  ${formatMoney(invoice.extraCharges, settings.currency)}")
    sb.appendLine("TOTAL:          ${formatMoney(invoice.total, settings.currency)}")
    sb.appendLine("Paid to Date:   ${formatMoney(invoice.paidAmount, settings.currency)}")
    sb.appendLine("BALANCE DUE:    ${formatMoney(invoice.dueAmount, settings.currency)}")
    sb.appendLine("------------------------------------------")
    sb.appendLine("STATUS: ${if (invoice.isPaid) "PAID IN FULL" else if (invoice.isPartial) "PARTIALLY PAID" else "UNPAID"}")
    if (payments.isNotEmpty()) {
        sb.appendLine("Payment History:")
        payments.forEach {
            sb.appendLine("  ${it.date} [${it.method}]: ${formatMoney(it.amount, settings.currency)}")
        }
    }
    sb.appendLine("==========================================")
    sb.appendLine(settings.receiptFooter)
    sb.appendLine("Thank you for your business!")
    return sb.toString()
}

fun shareReceipt(context: Context, receiptText: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, receiptText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Receipt")
    context.startActivity(shareIntent)
}
