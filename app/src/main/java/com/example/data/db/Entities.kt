package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.InvoiceLineItem
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val notes: String,
    val createdAt: Long
)

@Entity(tableName = "services")
data class ServiceItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "service" or "product"
    val category: String,
    val price: Double,
    val code: String,
    val stock: Double,
    val description: String
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val number: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val customerAddress: String,
    val date: String,
    val dueDate: String,
    val description: String,
    val itemsJson: String,
    val subtotal: Double,
    val discount: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val taxLabel: String,
    val extraCharges: Double,
    val total: Double,
    val paidAmount: Double,
    val paymentMethod: String,
    val notes: String,
    val status: String,
    val createdAt: Long
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val amount: Double,
    val method: String,
    val date: String,
    val reference: String,
    val note: String,
    val recordedAt: Long
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val note: String,
    val createdAt: Long
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String
)

class LineItemsConverter {
    @TypeConverter
    fun fromLineItems(items: List<InvoiceLineItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("serviceId", item.serviceId ?: "")
                put("name", item.name)
                put("description", item.description)
                put("qty", item.qty)
                put("unitPrice", item.unitPrice)
                put("type", item.type)
            }
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toLineItems(json: String?): List<InvoiceLineItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InvoiceLineItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val sId = obj.optString("serviceId")
                list.add(
                    InvoiceLineItem(
                        serviceId = if (sId.isBlank()) null else sId,
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        qty = obj.optDouble("qty", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        type = obj.optString("type", "service")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }
}
