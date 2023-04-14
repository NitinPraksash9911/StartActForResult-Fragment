package com.example.startactforresult_fragment.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat

class SMSUtil(
    private val activity: Activity,
) {

    private val SENT_SMS_INTENT_FILTER by lazy { "SMS_SENT" }
    fun sendSMSToGivenPhoneNumber(recipientPhoneNumber: String, content: String) {
        // NOTE: The following permission check should always evaluate to false (make sure we have
        // the READ_PHONE_STATE permission before this method is called)
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SMSUtil", "READ_PHONE_STATE permission not found.")
            return
        }
        registerSmsBroadcast()

        val subscriptionManager: SubscriptionManager =
            activity.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        this.showSimSelectionDialog(
            activity,
            subscriptionManager.activeSubscriptionInfoList,
            fun(selectedSIMIndex) {
                val subscriptionId =
                    subscriptionManager.activeSubscriptionInfoList[selectedSIMIndex].subscriptionId
                sendSMS(
                    recipientPhoneNumber,
                    content,
                    subscriptionId
                )
                val payload =
                    """{ "simIndex": $selectedSIMIndex, "simSubscriptionId": $subscriptionId }"""

            }
        )
    }

    private fun registerSmsBroadcast() {
        activity.registerReceiver(smsSentReceiver, IntentFilter(SENT_SMS_INTENT_FILTER))
    }

    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            activity.unregisterReceiver(this)
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(activity, "Sent SMS", Toast.LENGTH_SHORT).show()

                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    Toast.makeText(activity, "Failed try again", Toast.LENGTH_SHORT).show()
                    // handle failure
                    // message failed to send due to a generic failure
                    // it could be network failure or any other problem, you can show a message to the user to retry sending the message
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    Toast.makeText(activity, "Failed try again, No Service", Toast.LENGTH_SHORT)
                        .show()

                    // handle failure
                    // message failed to send because service is currently unavailable
                    // it could be network is down or any other problem, you can show a message to the user to retry sending the message
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    Toast.makeText(activity, "Failed try again", Toast.LENGTH_SHORT).show()

                    // handle failure
                    // message failed to send because PDU (protocol data unit) is null
                    // it could be because of invalid message content, you can show a message to the user to check the message content
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    Toast.makeText(activity, "Please turn off the flight mode", Toast.LENGTH_SHORT)
                        .show()

                    // handle failure
                    // message failed to send because radio was explicitly turned off
                    // it could be because of device settings or battery saving mode, you can show a message to the user to turn on radio and retry sending the message
                }
                else -> {
                    Toast.makeText(activity, "Something went wrong try again", Toast.LENGTH_SHORT)
                        .show()
                    // handle other cases
                }
            }
        }
    }

    private fun sendSMS(recipientPhoneNumber: String, content: String, subscriptionId: Int) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.getSystemService(SmsManager::class.java)
                .createForSubscriptionId(subscriptionId)
        } else {
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        }
        val sentIntent = PendingIntent.getBroadcast(activity, 0, Intent(SENT_SMS_INTENT_FILTER), PendingIntent.FLAG_IMMUTABLE)
        smsManager.sendTextMessage(
            recipientPhoneNumber, null,
            content, sentIntent, null
        )
    }

    private fun showSimSelectionDialog(
        activity: Activity,
        activeSubscriptionInfoList: MutableList<SubscriptionInfo>,
        callbackFn: (selectedSIMIndex: Int) -> Unit
    ) {
        val simSelectionDialog = AlertDialog.Builder(activity)
        val simListArrayAdapter =
            ArrayAdapter(
                activity,
                android.R.layout.simple_list_item_1,
                activeSubscriptionInfoList.mapIndexed { index, subscriptionInfo ->
                    //https://developer.android.com/reference/android/telephony/SubscriptionInfo#getNumber()
                    "SIM ${index + 1} (${subscriptionInfo.carrierName}), slot: ${subscriptionInfo.simSlotIndex} number ${subscriptionInfo.number}"
                }
            )
        simSelectionDialog.setTitle("Select a SIM Card")
            .setAdapter(
                simListArrayAdapter
            ) { _, index ->
                callbackFn(index)
            }
            .setCancelable(false)
            .create()
            .show()
    }
}