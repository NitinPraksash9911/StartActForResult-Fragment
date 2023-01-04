package com.example.startactforresult_fragment.yes_bank

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class FetchYesBankDeviceToken : ActivityResultContract<YesBankDataWrapper, YesBankResultData?>() {
    override fun createIntent(context: Context, input: YesBankDataWrapper): Intent {
        val intent = Intent(context, GetTokenActivity::class.java)
        intent.putExtra("key", input.key)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): YesBankResultData? {
        return when (resultCode) {
            RESULT_OK -> {
                val ok = intent?.getIntExtra("success", 0).toString()
                YesBankResultData(ok, "Success")
            }
            RESULT_CANCELED -> {
                val fail = intent?.getIntExtra("error", 0).toString()
                YesBankResultData(fail, "error")
            }
            else -> {
                null
            }
        }

    }

    override fun getSynchronousResult(
        context: Context,
        input: YesBankDataWrapper
    ): SynchronousResult<YesBankResultData?>? {
        return super.getSynchronousResult(context, input)
    }
}