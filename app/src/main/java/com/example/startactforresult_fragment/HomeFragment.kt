package com.example.startactforresult_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import com.example.startactforresult_fragment.databinding.FragmentHomeBinding
import com.example.startactforresult_fragment.util.BiometricAuthUtil
import com.example.startactforresult_fragment.yes_bank.FetchYesBankDeviceToken
import com.example.startactforresult_fragment.yes_bank.YesBankDataWrapper


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var biometricAuthUtil: BiometricAuthUtil
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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

        binding.btn.setOnClickListener {
//            fetchArbitraryData.launch(YesBankDataWrapper(count))
//            count++
            checkBoiMetric()
        }
    }

    fun checkBoiMetric(){
        if(biometricAuthUtil.isBiometricAvailable()){
            biometricPrompt.authenticate(promptInfo)
        }else{
            Toast.makeText(requireActivity().applicationContext,"Biometric not available",Toast.LENGTH_LONG).show()
        }
    }

    private val fetchArbitraryData = registerForActivityResult(FetchYesBankDeviceToken()) {
        binding.tv.text = it?.statusCode + it?.errorMsg

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}