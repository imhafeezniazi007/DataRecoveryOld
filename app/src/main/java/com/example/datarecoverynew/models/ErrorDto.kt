package com.example.datarecoverynew.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class ErrorDto(@SerializedName("httpStatus") var httpStatus: Int,
                    @SerializedName("serverCode") var serverCode: Int,
                    @SerializedName("message") var message: String)
