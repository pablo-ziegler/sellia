package com.example.selliaapp.ui.navigation


import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.example.selliaapp.repository.ProviderRepository
import com.example.selliaapp.repository.ProviderInvoiceRepository
import com.example.selliaapp.repository.ExpenseRepository

@HiltViewModel
class ProvidersEntryPoint @Inject constructor(
    val repo: ProviderRepository
): ViewModel()

@HiltViewModel
class ProviderInvoicesEntryPoint @Inject constructor(
    val repo: ProviderInvoiceRepository
): ViewModel()

@HiltViewModel
class ExpensesEntryPoint @Inject constructor(
    val repo: ExpenseRepository
): ViewModel()
