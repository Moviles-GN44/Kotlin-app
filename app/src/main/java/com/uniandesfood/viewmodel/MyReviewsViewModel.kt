package com.uniandesfood.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.local.ConnectivityHelper
import com.uniandesfood.data.local.MyReview
import com.uniandesfood.data.local.MyReviewsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyReviewsViewModel : ViewModel() {

    private val _reviews = MutableStateFlow<List<MyReview>>(emptyList())
    val reviews: StateFlow<List<MyReview>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            ConnectivityHelper.isOnlineFlow.collect { online ->
                if (online) {
                    delay(3000)
                    load()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            load()
        }
    }

    private suspend fun load() {
        val list = withContext(Dispatchers.IO) {
            Log.d("Threading", "My reviews loaded on: ${Thread.currentThread().name}")
            MyReviewsStore.getAll()
        }
        _reviews.value = list
        _isLoading.value = false
    }
}