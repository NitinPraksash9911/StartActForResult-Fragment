package com.example.startactforresult_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.startactforresult_fragment.databinding.FragmentHomeBinding
import com.example.startactforresult_fragment.yes_bank.FetchYesBankDeviceToken
import com.example.startactforresult_fragment.yes_bank.YesBankDataWrapper


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


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

        binding.btn.setOnClickListener {
            fetchArbitraryData.launch(YesBankDataWrapper(count))
            count++
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