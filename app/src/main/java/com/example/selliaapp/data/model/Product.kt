// File: Product.kt
package com.example.selliaapp.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = true), // MOD: índice + UNIQUE
        Index(value = ["code"], unique = true),    // MOD: índice + UNIQUE
        Index(value = ["name"])                    // MOD: índice búsqueda por nombre
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String? = null,
    val barcode: String? = null,
    val name: String,
    // E4:
    val basePrice: Double? = null,
    val taxRate: Double? = null,     // 0..1
    val finalPrice: Double? = null,
    // Legacy:
    val price: Double? = null,
    val quantity: Int = 0,
    val description: String? = null,
    val imageUrl: String? = null,
    // E1:
    val category: String? = null,
    val providerName: String? = null,
    val minStock: Int? = null,
    val updatedAt: LocalDate? = null
)
