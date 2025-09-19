package com.example.selliaapp.viewmodel.sales

import androidx.lifecycle.ViewModel
import com.example.selliaapp.data.model.sales.InvoiceSummary
import com.example.selliaapp.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SalesInvoicesViewModel @Inject constructor(
    private val repo: InvoiceRepository
) : ViewModel() {
    val invoices: Flow<List<InvoiceSummary>> = repo.observeAll()
}
