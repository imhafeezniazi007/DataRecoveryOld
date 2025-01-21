package com.example.datarecoverynew.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppOpenAdViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _isAdDisplayed = MutableLiveData<Boolean>()
    val isAdDisplayed: LiveData<Boolean> get() = _isAdDisplayed

    init {
        _isAdDisplayed.value = false
    }

    fun updateAdStatus(isDisplayed: Boolean, from: String) {
        Log.d("checkTest", "updateAdStatus: $isDisplayed from $from")
        _isAdDisplayed.postValue(isDisplayed)
    }
}