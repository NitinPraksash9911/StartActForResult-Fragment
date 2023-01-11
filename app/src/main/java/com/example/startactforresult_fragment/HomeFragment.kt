package com.example.startactforresult_fragment

import android.Manifest
import android.R
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.startactforresult_fragment.databinding.FragmentHomeBinding
import com.example.startactforresult_fragment.util.Permission
import com.example.startactforresult_fragment.util.PermissionManager
import com.example.startactforresult_fragment.util.SMSUtil
import com.example.startactforresult_fragment.yes_bank.FetchYesBankDeviceToken
import com.example.startactforresult_fragment.yes_bank.YesBankDataWrapper


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var permission: PermissionManager
    lateinit var smsUtil: SMSUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var count = 1
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        smsUtil = SMSUtil(requireActivity())
        permission = PermissionManager.from(this)
        binding.btn.setOnClickListener {
            getSmsPermission()
        }
    }


    @JavascriptInterface
    fun hasSmsPermission(): Boolean {
        return permission.hasSmsPermission()
    }

    @JavascriptInterface
    fun getSmsPermission() {
        permission
            .request(Permission.SendSMS, Permission.ReadPhoneState)
            .checkPermission { granted ->
                if (granted.not()) {
                    permission.handlePermissionDenied()
                } else {
                    smsUtil.sendSMSToGivenPhoneNumber("7042583364", "Testing sms api")
                }
            }
    }


    override fun onPause() {
        super.onPause()
    }


    private val fetchArbitraryData = registerForActivityResult(FetchYesBankDeviceToken()) {
        binding.tv.text = it?.statusCode + it?.errorMsg
    }

    private fun fetchToken(yesBankDataWrapper: YesBankDataWrapper) {
        fetchArbitraryData.launch(yesBankDataWrapper)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}