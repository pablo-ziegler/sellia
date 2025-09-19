package com.example.selliaapp.data.mappers

import com.example.selliaapp.data.local.entity.ProviderEntity
import com.example.selliaapp.data.model.Provider

/**
 * Conversión entre ProviderEntity (Room) y Provider (UI/dominio).
 */

fun ProviderEntity.toModel(): Provider =
    Provider(
        id = id,
        name = name,
        phone = phone,
        rubrosCsv = rubrosCsv,
        paymentTerm = paymentTerm,
        paymentMethod = paymentMethod
    )

fun Provider.toEntity(): ProviderEntity =
    ProviderEntity(
        id = id,
        name = name,
        phone = phone,
        rubrosCsv = rubrosCsv,
        paymentTerm = paymentTerm,
        paymentMethod = paymentMethod
    )
