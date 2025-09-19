package com.example.selliaapp.viewmodel.checkout


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.data.dao.CategoryDao
import com.example.selliaapp.data.dao.ProviderDao
import com.example.selliaapp.data.local.entity.CategoryEntity
import com.example.selliaapp.data.local.entity.ProviderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryProviderViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val providerDao: ProviderDao
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    private val _providers = MutableStateFlow<List<ProviderEntity>>(emptyList())
    val providers: StateFlow<List<ProviderEntity>> = _providers

    init {
        viewModelScope.launch {
            _categories.value = categoryDao.getAll()
            _providers.value = providerDao.getAll()
        }
    }
}
