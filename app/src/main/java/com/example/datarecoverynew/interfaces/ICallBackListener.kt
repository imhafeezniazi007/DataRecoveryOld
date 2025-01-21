package com.example.datarecoverynew.interfaces

import com.example.datarecoverynew.models.ErrorDto
import okhttp3.Headers


interface ICallBackListener<T> {
    fun onSuccess(t: T, headers: Headers?)

    fun onFailure(t: ErrorDto)
}