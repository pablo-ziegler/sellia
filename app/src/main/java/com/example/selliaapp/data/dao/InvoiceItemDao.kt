package com.example.selliaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.selliaapp.data.model.InvoiceItem

@Dao
interface InvoiceItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Int)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getByInvoiceId(invoiceId: Int): List<InvoiceItem>
}
