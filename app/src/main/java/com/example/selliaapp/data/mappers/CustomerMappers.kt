package com.example.selliaapp.data.mappers

import com.example.selliaapp.data.local.entity.CustomerEntity
import com.example.selliaapp.data.model.Customer

fun CustomerEntity.toModel(): Customer =
    Customer(
        id = id,
        name = name,
        phone = phone,
        email = email,
        address = address,
        nickname = nickname,
        rubrosCsv = rubrosCsv,
        paymentTerm = paymentTerm,
        paymentMethod = paymentMethod,
        createdAt = createdAt
    )

fun Customer.toEntity(): CustomerEntity =
    CustomerEntity(
        id = id,
        name = name,
        phone = phone,
        email = email,
        address = address,
        nickname = nickname,
        rubrosCsv = rubrosCsv,
        paymentTerm = paymentTerm,
        paymentMethod = paymentMethod,
        createdAt = createdAt
    )
