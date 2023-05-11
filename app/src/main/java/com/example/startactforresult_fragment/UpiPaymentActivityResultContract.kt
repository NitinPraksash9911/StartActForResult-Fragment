package com.example.startactforresult_fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class UpiPaymentActivityResultContract :
    ActivityResultContract<UPIPaymentRequiredData, UPIPaymentResult>() {

    override fun createIntent(context: Context, input: UPIPaymentRequiredData): Intent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = input.upiIntentDeepLink
            setPackage(input.UpiappPackageName)
        }
        return intent.takeIf { it.resolveActivity(context.packageManager) != null }
            ?: throw ActivityNotFoundException("No UPI payment app found on the device")
    }

    override fun parseResult(resultCode: Int, intent: Intent?): UPIPaymentResult {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                val response = intent?.getStringExtra("response")
                if (response == null) {
                    UPIPaymentResult.Failed("Payment response is null")
                } else {
                    runCatching { getTransactionDetails(response) }
                        .fold(
                            onSuccess = { UPIPaymentResult.Success(it) },
                            onFailure = { UPIPaymentResult.Failed("Payment failed") }
                        )
                }
            }
            Activity.RESULT_CANCELED -> UPIPaymentResult.Failed("Payment cancelled")
            else -> UPIPaymentResult.Failed("Unknown payment status")
        }
    }
}

data class UPIPaymentRequiredData(
    val upiIntentDeepLink: Uri,
    val UpiappPackageName: String
)

sealed class UPIPaymentResult {
    data class Success(val transactionDetails: TransactionDetails) : UPIPaymentResult()
    data class Failed(val errorMessage: String) : UPIPaymentResult()
}

data class TransactionDetails(
    val transactionId: String?,
    val responseCode: String?,
    val approvalRefNo: String?,
    val transactionRefId: String?,
)

private fun getTransactionDetails(response: String): TransactionDetails {
    val queryParameters = response.split("&")
        .map { it.split("=") }
        .mapNotNull { if (it.size == 2) it[0] to it[1] else null }
        .toMap()

    return TransactionDetails(
        transactionId = queryParameters["txnId"],
        responseCode = queryParameters["responseCode"],
        approvalRefNo = queryParameters["ApprovalRefNo"],
        transactionRefId = queryParameters["txnRef"],
    )
}
