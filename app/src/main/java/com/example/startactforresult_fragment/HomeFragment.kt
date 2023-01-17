package com.example.startactforresult_fragment

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.startactforresult_fragment.databinding.FragmentHomeBinding
import com.example.startactforresult_fragment.util.BiometricAuthUtil
import com.example.startactforresult_fragment.util.Permission
import com.example.startactforresult_fragment.util.PermissionManager
import com.example.startactforresult_fragment.util.SMSUtil
import com.example.startactforresult_fragment.yes_bank.FetchYesBankDeviceToken
import com.example.startactforresult_fragment.yes_bank.YesBankDataWrapper
import com.google.gson.Gson
import java.util.HashMap


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var biometricAuthUtil: BiometricAuthUtil
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    lateinit var permission: PermissionManager
    lateinit var smsUtil: SMSUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        biometricAuthUtil = BiometricAuthUtil(requireActivity())
        biometricPrompt = biometricAuthUtil.createBiometricPrompt()
        promptInfo = biometricAuthUtil.generatePromptInfo()
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

    fun fireEventForSuccessSendSms(): String? {
        val payload = createPayload("status" to "SUCCESS")
        return Gson().toJson(payload)
    }

    private fun createPayload(vararg params: Pair<String, Any>): Map<String, Any> {
        return mapOf(*params)
    }

    private fun addBiometricAuthentication() {
        val keyguardManager =
            requireActivity().getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (biometricAuthUtil.isBiometricAvailable() || keyguardManager.isDeviceSecure) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(requireActivity(), "Not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun tryScreenLock() {
        val keyguardManager =
            requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardSecure) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "Unlock",
                "Confirm your screen lock to proceed"
            )
            startActivityForResult(intent, 101)
        } else {
            // Screen lock is not enabled
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
                    smsUtil.sendSMSToGivenPhoneNumber("7042583364", "TADA Testing sms api")
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