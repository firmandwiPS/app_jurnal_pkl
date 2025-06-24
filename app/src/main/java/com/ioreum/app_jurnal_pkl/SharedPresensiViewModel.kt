package com.ioreum.app_jurnal_pkl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedPresensiViewModel : ViewModel() {
    private val _dataUpdated = MutableLiveData(false)
    val dataUpdated: LiveData<Boolean> get() = _dataUpdated

    fun notifyDataUpdated() {
        _dataUpdated.value = true
    }

    fun clearUpdateFlag() {
        _dataUpdated.value = false
    }
}
