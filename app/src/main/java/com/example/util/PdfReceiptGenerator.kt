package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.BusinessSettings
import com.example.data.model.Invoice
import com.example.data.model.PaymentRecord
import com.example.ui.components.formatMoney
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class PdfPaperSize(
    val id: String,
    val title: String,
    val dimensionsLabel: String,
    val widthPt: Int,
    val defaultHeightPt: Int,
    val isRoll: Boolean
) {
    A4("A4", "A4 Standard", "210 × 297 mm", 595, 842, false),
    A5("A5", "A5 Compact", "148 × 210 mm", 420, 595, false),
    LETTER("LETTER", "US Letter", "8.5 × 11 in", 612, 792, false),
    POS_80MM("80MM", "80mm Thermal POS", "80 mm roll (Standard)", 226, 0, true),
    POS_58MM("58MM", "58mm Mini POS", "58 mm roll (Mobile)", 164, 0, true)
}

object PdfReceiptGenerator {

    fun generatePdfFile(
        context: Context,
        invoice: Invoice,
        payments: List<PaymentRecord>,
        settings: BusinessSettings,
        paperSize: PdfPaperSize
    ): File {
        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val sanitizedNumber = invoice.number.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(receiptsDir, "Receipt_${sanitizedNumber}_${paperSize.id}.pdf")

        val document = PdfDocument()

        if (paperSize.isRoll) {
            renderThermalRollPdf(document, invoice, payments, settings, paperSize)
        } else {
            renderSheetPdf(document, invoice, payments, settings, paperSize)
        }

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    private fun renderSheetPdf(
        document: PdfDocument,
        invoice: Invoice,
        payments: List<PaymentRecord>,
        settings: BusinessSettings,
        paperSize: PdfPaperSize
    ) {
        val width = paperSize.widthPt
        val height = paperSize.defaultHeightPt
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val margin = if (paperSize == PdfPaperSize.A5) 24f else 36f
        val contentWidth = width - (margin * 2)

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSecondary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintBrand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(79, 70, 229)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintFill = Paint().apply {
            style = Paint.Style.FILL
        }
        val paintStroke = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        val scale = if (paperSize == PdfPaperSize.A5) 0.85f else 1.0f

        var curY = margin + (16f * scale)

        // Top Accent Banner Bar
        paintFill.color = Color.rgb(79, 70, 229)
        canvas.drawRect(margin, margin - 10f, width - margin, margin - 6f, paintFill)

        // Business Header & Invoice Meta
        val companyNameSize = (20f * scale)
        paintBold.textSize = companyNameSize
        canvas.drawText(settings.name, margin, curY, paintBold)

        // Right side: Document Type & Number
        paintBrand.textSize = 15f * scale
        val docTitle = "RECEIPT / INVOICE"
        val titleWidth = paintBrand.measureText(docTitle)
        canvas.drawText(docTitle, width - margin - titleWidth, curY, paintBrand)

        curY += (14f * scale)
        paintSecondary.textSize = 9.5f * scale
        if (settings.tagline.isNotBlank()) {
            canvas.drawText(settings.tagline, margin, curY, paintSecondary)
        }
        val invNoStr = "#${invoice.number}"
        val invNoWidth = paintSecondary.measureText(invNoStr)
        canvas.drawText(invNoStr, width - margin - invNoWidth, curY, paintSecondary)

        curY += (13f * scale)
        val contactLine = listOfNotNull(
            settings.phone.takeIf { it.isNotBlank() },
            settings.email.takeIf { it.isNotBlank() }
        ).joinToString(" | ")
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, margin, curY, paintSecondary)
        }

        val dateStr = "Date: ${invoice.date}"
        val dateWidth = paintSecondary.measureText(dateStr)
        canvas.drawText(dateStr, width - margin - dateWidth, curY, paintSecondary)

        if (settings.address.isNotBlank()) {
            curY += (12f * scale)
            canvas.drawText(settings.address, margin, curY, paintSecondary)
        }

        curY += (20f * scale)

        // Divider
        canvas.drawLine(margin, curY, width - margin, curY, paintStroke)
        curY += (14f * scale)

        // Customer Info Card Box
        paintFill.color = Color.rgb(248, 250, 252)
        val custBoxHeight = 44f * scale
        canvas.drawRoundRect(margin, curY, width - margin, curY + custBoxHeight, 6f, 6f, paintFill)
        canvas.drawRoundRect(margin, curY, width - margin, curY + custBoxHeight, 6f, 6f, paintStroke)

        paintBold.textSize = 10f * scale
        canvas.drawText("BILLED TO:", margin + 12f, curY + (16f * scale), paintBold)

        paintText.textSize = 11f * scale
        canvas.drawText(invoice.customerName, margin + 12f, curY + (32f * scale), paintText)

        val metaX = width - margin - (150f * scale)
        paintSecondary.textSize = 9.5f * scale
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText("Phone: ${invoice.customerPhone}", metaX, curY + (18f * scale), paintSecondary)
        }
        val statusText = if (invoice.isPaid) "STATUS: PAID IN FULL" else if (invoice.isPartial) "STATUS: PARTIAL" else "STATUS: UNPAID"
        paintBold.textSize = 9.5f * scale
        paintBold.color = if (invoice.isPaid) Color.rgb(16, 185, 129) else Color.rgb(239, 68, 68)
        canvas.drawText(statusText, metaX, curY + (34f * scale), paintBold)
        paintBold.color = Color.rgb(15, 23, 42) // reset

        curY += custBoxHeight + (18f * scale)

        // Table Header
        val colItemX = margin + 8f
        val colQtyX = margin + contentWidth * 0.55f
        val colRateX = margin + contentWidth * 0.72f
        val colTotalX = width - margin - 8f

        paintFill.color = Color.rgb(241, 245, 249)
        canvas.drawRect(margin, curY, width - margin, curY + (22f * scale), paintFill)

        paintBold.textSize = 9f * scale
        canvas.drawText("DESCRIPTION / SERVICE", colItemX, curY + (15f * scale), paintBold)
        canvas.drawText("QTY", colQtyX, curY + (15f * scale), paintBold)
        canvas.drawText("RATE (${settings.currency})", colRateX, curY + (15f * scale), paintBold)
        val totalHeaderStr = "AMOUNT (${settings.currency})"
        val totalHeaderWidth = paintBold.measureText(totalHeaderStr)
        canvas.drawText(totalHeaderStr, colTotalX - totalHeaderWidth, curY + (15f * scale), paintBold)

        curY += (22f * scale)

        // Table Rows
        paintText.textSize = 9.5f * scale
        invoice.items.forEachIndexed { index, item ->
            val rowY = curY + (16f * scale)
            if (index % 2 == 1) {
                paintFill.color = Color.rgb(248, 250, 252)
                canvas.drawRect(margin, curY, width - margin, curY + (22f * scale), paintFill)
            }

            canvas.drawText(item.name, colItemX, rowY, paintText)
            canvas.drawText("${item.qty}", colQtyX, rowY, paintText)

            val priceStr = formatMoney(item.unitPrice, "")
            canvas.drawText(priceStr, colRateX, rowY, paintText)

            val lineTotalStr = formatMoney(item.lineTotal, "")
            val lineTotalWidth = paintText.measureText(lineTotalStr)
            canvas.drawText(lineTotalStr, colTotalX - lineTotalWidth, rowY, paintText)

            curY += (22f * scale)
            canvas.drawLine(margin, curY, width - margin, curY, paintStroke)
        }

        curY += (12f * scale)

        // Summary Section (Right aligned)
        val sumBoxWidth = 210f * scale
        val sumLeft = width - margin - sumBoxWidth

        fun drawSummaryLine(label: String, amountStr: String, isBold: Boolean = false, isAccent: Boolean = false) {
            val p = if (isBold || isAccent) paintBold else paintText
            p.textSize = (if (isAccent) 12f else 10f) * scale
            if (isAccent) p.color = Color.rgb(79, 70, 229) else p.color = Color.rgb(15, 23, 42)

            canvas.drawText(label, sumLeft, curY, p)
            val valWidth = p.measureText(amountStr)
            canvas.drawText(amountStr, width - margin - 8f - valWidth, curY, p)

            p.color = Color.rgb(15, 23, 42) // reset
            curY += (16f * scale)
        }

        drawSummaryLine("Subtotal:", formatMoney(invoice.subtotal, settings.currency))
        if (invoice.discount > 0) {
            drawSummaryLine("Discount:", "-${formatMoney(invoice.discount, settings.currency)}")
        }
        if (invoice.extraCharges > 0) {
            drawSummaryLine("Extra charges:", formatMoney(invoice.extraCharges, settings.currency))
        }

        canvas.drawLine(sumLeft, curY - (6f * scale), width - margin, curY - (6f * scale), paintStroke)
        curY += (4f * scale)

        drawSummaryLine("TOTAL AMOUNT:", formatMoney(invoice.total, settings.currency), isBold = true, isAccent = true)
        drawSummaryLine("Amount Paid:", formatMoney(invoice.paidAmount, settings.currency))
        drawSummaryLine("Balance Due:", formatMoney(invoice.dueAmount, settings.currency), isBold = true)

        if (invoice.hasDollarRate) {
            paintSecondary.textSize = 7.5f * scale
            val usdText = "USD Ref: 1 USD ≈ ${formatMoney(invoice.dollarRate, settings.currency)}   (Total ≈ $${String.format(Locale.US, "%.2f", invoice.totalInUsd)} USD)"
            canvas.drawText(usdText, sumLeft, curY, paintSecondary)
            curY += (12f * scale)
        }

        curY += (10f * scale)

        // Payment History if present
        if (payments.isNotEmpty()) {
            paintBold.textSize = 9.5f * scale
            canvas.drawText("PAYMENT TRANSACTIONS", margin, curY, paintBold)
            curY += (12f * scale)

            paintSecondary.textSize = 8.5f * scale
            payments.forEach { pay ->
                val line = "• ${pay.date}  |  ${pay.method.uppercase()}  |  Paid: ${formatMoney(pay.amount, settings.currency)}"
                canvas.drawText(line, margin + 4f, curY, paintSecondary)
                curY += (12f * scale)
            }
            curY += (8f * scale)
        }

        // Company Payment / Bank Transfer Receiving Details Box
        if (settings.bankName.isNotBlank() || settings.accountNumber.isNotBlank()) {
            val bankBoxHeight = (54f * scale)
            paintFill.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(margin, curY, width - margin, curY + bankBoxHeight, 6f, 6f, paintFill)
            canvas.drawRoundRect(margin, curY, width - margin, curY + bankBoxHeight, 6f, 6f, paintStroke)

            paintBold.textSize = 9f * scale
            canvas.drawText("COMPANY PAYMENT / BANK TRANSFER DETAILS:", margin + 10f, curY + (14f * scale), paintBold)

            paintText.textSize = 8.5f * scale
            val line1 = "Bank: ${settings.bankName.ifBlank { "Bank Transfer" }}   |   Account No: ${settings.accountNumber}"
            canvas.drawText(line1, margin + 10f, curY + (28f * scale), paintText)

            val details = listOfNotNull(
                "Name: ${settings.accountName}".takeIf { settings.accountName.isNotBlank() },
                "Branch: ${settings.branchName}".takeIf { settings.branchName.isNotBlank() }
            ).joinToString("   |   ")
            if (details.isNotBlank()) {
                paintSecondary.textSize = 8f * scale
                canvas.drawText(details, margin + 10f, curY + (40f * scale), paintSecondary)
            }

            if (settings.paymentInstructions.isNotBlank()) {
                paintSecondary.textSize = 7.5f * scale
                canvas.drawText(settings.paymentInstructions.take(90), margin + 10f, curY + (50f * scale), paintSecondary)
            }

            curY += bankBoxHeight + (10f * scale)
        }

        // Terms & Footer
        val footerY = height - margin - 20f
        canvas.drawLine(margin, footerY - 14f, width - margin, footerY - 14f, paintStroke)

        paintSecondary.textSize = 8f * scale
        val terms = if (invoice.notes.isNotBlank()) invoice.notes else settings.termsText
        if (terms.isNotBlank()) {
            canvas.drawText(terms.take(100), margin, footerY - 4f, paintSecondary)
        }

        paintBold.textSize = 8.5f * scale
        val thankYou = settings.receiptFooter.ifBlank { "Thank you for your business!" }
        val tyWidth = paintBold.measureText(thankYou)
        canvas.drawText(thankYou, (width - tyWidth) / 2f, footerY + 10f, paintBold)

        document.finishPage(page)
    }

    private fun renderThermalRollPdf(
        document: PdfDocument,
        invoice: Invoice,
        payments: List<PaymentRecord>,
        settings: BusinessSettings,
        paperSize: PdfPaperSize
    ) {
        val width = paperSize.widthPt
        val is58 = paperSize == PdfPaperSize.POS_58MM

        // Calculate dynamic height
        val baseHeight = if (is58) 380 else 420
        val itemsHeight = invoice.items.size * (if (is58) 28 else 22)
        val paymentsHeight = payments.size * (if (is58) 16 else 14)
        val bankHeight = if (settings.bankName.isNotBlank() || settings.accountNumber.isNotBlank()) (if (is58) 75 else 65) else 0
        val usdRateHeight = if (invoice.hasDollarRate) 20 else 0
        val calculatedHeight = baseHeight + itemsHeight + paymentsHeight + bankHeight + usdRateHeight

        val pageInfo = PdfDocument.PageInfo.Builder(width, calculatedHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val margin = if (is58) 8f else 12f
        val contentWidth = width - (margin * 2)

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.MONOSPACE
            textSize = if (is58) 8.5f else 10f
        }
        val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = if (is58) 9.5f else 11f
        }
        val paintHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = if (is58) 11.5f else 13.5f
        }

        var curY = margin + 14f

        fun drawCentered(text: String, paint: Paint) {
            val textWidth = paint.measureText(text)
            val x = (width - textWidth) / 2f
            canvas.drawText(text, x.coerceAtLeast(margin), curY, paint)
            curY += paint.textSize + 3f
        }

        fun drawDivider(char: String = "-") {
            val count = (contentWidth / paintText.measureText(char)).toInt()
            val divStr = char.repeat(count.coerceAtLeast(10))
            drawCentered(divStr, paintText)
        }

        fun drawRow(label: String, value: String, isBold: Boolean = false) {
            val p = if (isBold) paintBold else paintText
            canvas.drawText(label, margin, curY, p)
            val valWidth = p.measureText(value)
            canvas.drawText(value, width - margin - valWidth, curY, p)
            curY += p.textSize + 3.5f
        }

        // Header
        drawCentered(settings.name.uppercase(), paintHeader)
        if (settings.tagline.isNotBlank()) {
            drawCentered(settings.tagline, paintText)
        }
        if (settings.phone.isNotBlank()) {
            drawCentered("Tel: ${settings.phone}", paintText)
        }
        if (settings.address.isNotBlank()) {
            drawCentered(settings.address, paintText)
        }

        drawDivider("=")
        drawCentered("OFFICIAL RECEIPT", paintBold)
        drawDivider("=")

        // Meta
        drawRow("Invoice #:", invoice.number, isBold = true)
        drawRow("Date:", invoice.date)
        drawRow("Due Date:", invoice.dueDate)
        drawRow("Customer:", invoice.customerName.take(if (is58) 16 else 24))
        if (invoice.customerPhone.isNotBlank()) {
            drawRow("Phone:", invoice.customerPhone)
        }

        drawDivider("-")
        drawCentered("ITEM DETAILS", paintBold)
        drawDivider("-")

        // Items
        invoice.items.forEach { item ->
            if (is58) {
                canvas.drawText(item.name.take(22), margin, curY, paintBold)
                curY += paintBold.textSize + 2f
                val subline = "${item.qty} x ${formatMoney(item.unitPrice, "")}"
                canvas.drawText(subline, margin + 4f, curY, paintText)
                val totalStr = formatMoney(item.lineTotal, settings.currency)
                val totalWidth = paintText.measureText(totalStr)
                canvas.drawText(totalStr, width - margin - totalWidth, curY, paintText)
                curY += paintText.textSize + 4f
            } else {
                canvas.drawText(item.name.take(18), margin, curY, paintBold)
                val subline = "${item.qty} x ${formatMoney(item.unitPrice, "")}"
                canvas.drawText(subline, margin + 95f, curY, paintText)
                val totalStr = formatMoney(item.lineTotal, settings.currency)
                val totalWidth = paintText.measureText(totalStr)
                canvas.drawText(totalStr, width - margin - totalWidth, curY, paintText)
                curY += paintBold.textSize + 4f
            }
        }

        drawDivider("-")

        // Totals
        drawRow("Subtotal:", formatMoney(invoice.subtotal, settings.currency))
        if (invoice.discount > 0) {
            drawRow("Discount:", "-${formatMoney(invoice.discount, settings.currency)}")
        }
        if (invoice.extraCharges > 0) {
            drawRow("Extra:", formatMoney(invoice.extraCharges, settings.currency))
        }

        drawDivider("=")
        drawRow("TOTAL:", formatMoney(invoice.total, settings.currency), isBold = true)
        drawRow("Paid:", formatMoney(invoice.paidAmount, settings.currency))
        drawRow("Balance Due:", formatMoney(invoice.dueAmount, settings.currency), isBold = true)
        if (invoice.hasDollarRate) {
            drawRow("Avg USD Rate:", "1$ ≈ ${formatMoney(invoice.dollarRate, "")} ($${String.format(Locale.US, "%.2f", invoice.totalInUsd)})")
        }
        drawDivider("=")

        val status = if (invoice.isPaid) "*** PAID IN FULL ***" else if (invoice.isPartial) "*** PARTIAL PAYMENT ***" else "*** UNPAID ***"
        drawCentered(status, paintBold)

        if (payments.isNotEmpty()) {
            drawDivider("-")
            drawCentered("PAYMENT HISTORY", paintBold)
            payments.forEach { p ->
                val pLine = "${p.date} (${p.method})"
                val pAmt = formatMoney(p.amount, settings.currency)
                drawRow(pLine, pAmt)
            }
        }

        // Company Bank Details
        if (settings.bankName.isNotBlank() || settings.accountNumber.isNotBlank()) {
            drawDivider("-")
            drawCentered("BANK TRANSFER DETAILS", paintBold)
            if (settings.bankName.isNotBlank()) drawRow("Bank:", settings.bankName)
            if (settings.accountNumber.isNotBlank()) drawRow("A/C No:", settings.accountNumber)
            if (settings.accountName.isNotBlank()) drawRow("A/C Name:", settings.accountName)
            if (settings.branchName.isNotBlank()) drawRow("Branch:", settings.branchName)
            if (settings.paymentInstructions.isNotBlank()) {
                drawCentered(settings.paymentInstructions.take(50), paintText)
            }
        }

        drawDivider("-")
        val footer = settings.receiptFooter.ifBlank { "Thank you for your business!" }
        drawCentered(footer, paintText)
        drawCentered("Powered by Connect Studio POS", paintText)

        document.finishPage(page)
    }

    /**
     * Saves the PDF to the user's standard Downloads folder.
     */
    fun downloadToDownloadsFolder(
        context: Context,
        pdfFile: File,
        invoiceNumber: String,
        paperSize: PdfPaperSize
    ): Uri? {
        val fileName = "ConnectStudio_Receipt_${invoiceNumber}_${paperSize.id}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        pdfFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    return uri
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, fileName)
                pdfFile.copyTo(destFile, overwrite = true)
                return Uri.fromFile(destFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Share the PDF file via Android Share Sheet
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF Receipt"))
    }

    /**
     * Open the PDF directly in a PDF viewer app
     */
    fun openPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found on device", Toast.LENGTH_SHORT).show()
        }
    }
}
