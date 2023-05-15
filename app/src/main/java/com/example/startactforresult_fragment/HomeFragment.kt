package com.example.startactforresult_fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.startactforresult_fragment.databinding.FragmentHomeBinding
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btn.setOnClickListener {
            encrptionAndDecryption()
        }
        binding.btn2.setOnClickListener {

            val deepLink = Uri.Builder().scheme("upi").authority("pay")
                .appendQueryParameter("pa", "yachika.ipmedia@okicici")
                .appendQueryParameter("pn", "Yachika")
                .appendQueryParameter("tn", "testing")
                .appendQueryParameter("am", "1")
                .appendQueryParameter("cu", "INR")
                .build()




            try {
                upiRegisterForActivityResult.launch(
                    UPIPaymentRequiredData(
                        deepLink,
                        UpiAppsPackage.PHONE_PE
                    )
                )
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Not found any apps",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun encrptionAndDecryption() {
        // Generate a new Ed25519 key pair
        val keyPairGenerator = Ed25519KeyPairGenerator()
        val keyGenParams = Ed25519KeyGenerationParameters(SecureRandom())
        keyPairGenerator.init(keyGenParams)
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters

        // Convert the data to bytes
        val data = "hello world".toByteArray()

        // Sign the data using the private key
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(data, 0, data.size)
        val signature = signer.generateSignature()

        // Verify the signature using the public key
        val verifier = Ed25519Signer()
        verifier.init(false, publicKey)
        verifier.update(data, 0, data.size)
        val isVerified = verifier.verifySignature(signature)

        println("Original string: ${String(data)}")
        println("Signature: ${String(signature)}")
        println("Verified: $isVerified")
    }

    fun getAllUpiAppsOnThisDevice(context: Context): List<String> {
        val upiIntent = Intent(Intent.ACTION_VIEW)
        upiIntent.data = Uri.parse("upi://pay")
        val packageManager = context.packageManager

        val installedUpiApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                upiIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            packageManager.queryIntentActivities(
                upiIntent,
                PackageManager.GET_META_DATA
            )
        }

        return installedUpiApps.mapNotNull { it.activityInfo.packageName }
    }

    private val upiRegisterForActivityResult =
        registerForActivityResult(UpiPaymentActivityResultContract()) { upiPaymentStatus ->
            when (upiPaymentStatus) {
                is UPIPaymentResult.Success -> {
                    Toast.makeText(requireContext(), "Payment successful", Toast.LENGTH_SHORT)
                        .show()
                    val transactionDetails = upiPaymentStatus.transactionDetails

                    Log.d("UPI PAYMENET", ": $transactionDetails")
                }
                is UPIPaymentResult.Failed -> {
                    Toast.makeText(
                        requireContext(),
                        upiPaymentStatus.errorMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.webview.destroy()
        _binding = null
    }

}
