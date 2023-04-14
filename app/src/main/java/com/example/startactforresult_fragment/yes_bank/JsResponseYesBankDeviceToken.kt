package com.example.startactforresult_fragment.yes_bank


import com.google.gson.annotations.SerializedName

data class JsResponseYesBankDeviceToken(
    @SerializedName("status")
    val status: String,
    @SerializedName("statusCode")
    val statusCode: String,
    @SerializedName("deviceToken")
    val deviceToken: String?,
    @SerializedName("yppReferenceNumber")
    val yppReferenceNumber: String?,
    @SerializedName("isBindingHappen")
    val isBindingHappen: Boolean,
    @SerializedName("msg")
    val msg: String
)