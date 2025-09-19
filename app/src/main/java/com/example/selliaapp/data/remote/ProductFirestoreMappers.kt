package com.example.selliaapp.data.remote


import com.example.selliaapp.data.local.entity.ProductEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Mappers entre ProductEntity (Room) y Map<String, Any?> (Firestore).
 * Guardamos LocalDate como string ISO (yyyy-MM-dd) para legibilidad.
 */
object ProductFirestoreMappers {
    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toMap(p: ProductEntity): Map<String, Any?> = mapOf(
        "id"           to p.id,               // también guardamos id para depuración (docId será el id string)
        "code"         to p.code,
        "barcode"      to p.barcode,
        "name"         to p.name,
        "basePrice"    to p.basePrice,
        "taxRate"      to p.taxRate,
        "finalPrice"   to p.finalPrice,
        "price"        to p.price,
        "quantity"     to p.quantity,
        "description"  to p.description,
        "imageUrl"     to p.imageUrl,
        "categoryId"   to p.categoryId,
        "providerId"   to p.providerId,
        "providerName" to p.providerName,
        "category"     to p.category,
        "minStock"     to p.minStock,
        "updatedAt"    to p.updatedAt.format(ISO_DATE)
    )

    fun fromMap(docId: String, data: Map<String, Any?>): ProductEntity {
        val updatedAtStr = data["updatedAt"] as? String
        val updatedAt = updatedAtStr?.let { LocalDate.parse(it, ISO_DATE) } ?: LocalDate.now()
        return ProductEntity(
            id           = docId.toIntOrNull() ?: 0, // si docId es numérico, lo usamos; si no, 0 para insert local
            code         = data["code"] as? String,
            barcode      = data["barcode"] as? String,
            name         = (data["name"] as? String).orEmpty(),
            basePrice    = (data["basePrice"] as? Number)?.toDouble(),
            taxRate      = (data["taxRate"] as? Number)?.toDouble(),
            finalPrice   = (data["finalPrice"] as? Number)?.toDouble(),
            price        = (data["price"] as? Number)?.toDouble(),
            quantity     = (data["quantity"] as? Number)?.toInt() ?: 0,
            description  = data["description"] as? String,
            imageUrl     = data["imageUrl"] as? String,
            categoryId   = (data["categoryId"] as? Number)?.toInt(),
            providerId   = (data["providerId"] as? Number)?.toInt(),
            providerName = data["providerName"] as? String,
            category     = data["category"] as? String,
            minStock     = (data["minStock"] as? Number)?.toInt(),
            updatedAt    = updatedAt
        )
    }
}
