package com.example.startactforresult_fragment

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.example.startactforresult_fragment.databinding.FragmentHomeBinding
import com.example.startactforresult_fragment.util.BiometricAuthUtil
import com.example.startactforresult_fragment.util.PermissionManager
import com.example.startactforresult_fragment.util.SMSUtil
import com.example.startactforresult_fragment.util.showDebugToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor


const val TAGSS = "asdadadas"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var biometricAuthUtil: BiometricAuthUtil
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    lateinit var permissionUtil: PermissionManager
    lateinit var smsUtil: SMSUtil



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        biometricAuthUtil = BiometricAuthUtil(requireActivity())
        return binding.root
    }

    var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    var isPageError = false
    private var count = 1
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window: Window = requireActivity().window
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.black))

        smsUtil = SMSUtil(requireActivity())


        binding.webview.isLongClickable = false
        permissionUtil = PermissionManager.from(this)

        webViewSetting(binding.webview.settings)
        WebView.setWebContentsDebuggingEnabled(true)
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(binding.webview, true)

        binding.webview.webViewClient = webClient


        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                launchImagePicker()

                // Save the file upload callback for later use
                fileUploadCallback = filePathCallback

                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
                request?.grant(request.resources)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest) {
                super.onPermissionRequestCanceled(request)
            }
        }

        binding.webview.loadUrl("https://www.workapps.com/web2/openChat/unsupported.html?source=https://www.workapps.com/openKYC/index.html?parentGrpId=4817914&guestGrpId=6374335&guestId=6319526&token=8cpWYkClRLuIUudhp9MM77GVUyXASP1nwGEztfu-4atGoTpPkf8aYVcwUqd88BcUadXBj0usZuWKqYCeMZ_c9lK5Kv6nFmjt02rFbxLJ_PyHOVTbIWqXSilAgusg9USGfY-5QHLfJwvFip_s4Bnoyldje4n3AelDEKqZkA3itko!1&orgId=6258&redirectUrl=https://webapp.alpha.stage-upswing.one/redirect/vkyc&orgId=6258&guestId=6319526&guestGrpId=6374335&cplink=1&orgId=6258&guestId=6319526&guestGrpId=6374335")
        binding.btn.setOnClickListener {

            val deeplink = "upswing://test.sdk/uti"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplink))
            startActivity(intent)
//            val intentAction = "one.upswing.partner_action_${requireActivity().packageName}_edhas"
//            startActivity(Intent(intentAction))
//            requireActivity().deleteSharedPref()

        }
        binding.btn2.setOnClickListener {
            val intentAction = "one.upswing.partner_action_${requireActivity().packageName}_edhas2"
            startActivity(Intent(intentAction))
        }

        setFragmentResultListener("requestKey") { requestKey, bundle ->

            val result = bundle.getString("bundleKey")
            binding.btn.text = result
        }

        binding.errorLayout.retryButton.setOnClickListener {


        }

        registerBackPressed()

    }

    private fun Context.deleteSharedPref() {
        val dir = File("${this.filesDir.parent}/shared_prefs/")
        dir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".xml") && file.nameWithoutExtension == "upswing_pref_storage") {
                file.delete()
            }
        }
    }


    private fun checkBiometric() {
        biometricAuthUtil.authenticViaDeviceBiometricOrPin(onSuccess = {
            showDebugToast(requireContext(), "Auth success")
        }, onError = {
            showDebugToast(requireContext(), "Auth fialed")
        })
    }

    private val webClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            isPageError = false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Timber.tag("ALLTHEURLS-Page").d(": %s", url)
            if (isPageError) {
                view?.visibility = View.GONE;
                binding.errorLayout.root.visibility = View.VISIBLE
            } else {
                view?.visibility = View.VISIBLE;
                binding.errorLayout.root.visibility = View.GONE
            }
        }

        override fun onReceivedError(
            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            isPageError = true
            binding.webview.visibility = View.GONE
            binding.errorLayout.root.visibility = View.VISIBLE
            handleError(view, error?.errorCode ?: ERROR_CONNECT, error?.description.toString())
            Log.d(TAGSS, "error:  ${error?.description.toString()}")
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?, request: WebResourceRequest?
        ): Boolean {
//                file.appendText(url+"\n")
            val url = request!!.url.toString()
            if (url.startsWith("https://sameeksha-portal-uat.axisbank.co.in")) {
                Log.d(TAGSS, "sameeksha called in url over loading")
                setProxy()
                return false
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldInterceptRequest(
            view: WebView?, request: WebResourceRequest?
        ): WebResourceResponse? {
            val url = request?.url.toString()
            if (url.startsWith("https://sameeksha-portal-uat.axisbank.co.in")) {
                Log.d(TAGSS, "sameeksha called in intercpet request")
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?
        ) {
            Log.d(TAGSS, "proxy auth for this host: ${host.toString()}")
            handler!!.proceed("acme-partner", "acmePartner456")
        }

        override fun onReceivedSslError(
            view: WebView?, handler: SslErrorHandler?, error: SslError?
        ) {
            handler!!.proceed()
            super.onReceivedSslError(view, handler, error)
        }
    }


    private fun webViewSetting(webSettings: WebSettings) {
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"
        webSettings.userAgentString = userAgent
        webSettings.javaScriptEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.allowContentAccess = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.setSupportMultipleWindows(true)
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.loadsImagesAutomatically = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }


    private val multiPhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { imageUris: List<Uri> ->
            if (imageUris.isNotEmpty()) {
                fileUploadCallback?.onReceiveValue(imageUris.toTypedArray())
                binding.image.setImageURI(imageUris.get(0))
            } else {
                fileUploadCallback?.onReceiveValue(null)
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    //TODO use as a image picker
    private fun launchImagePicker() {
        multiPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private val activityResultForPickingImage = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            fileUploadCallback?.onReceiveValue(uris.toTypedArray())
            binding.image.setImageURI(uris.get(0))
        } else {
            fileUploadCallback?.onReceiveValue(null)
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImagesUsingMediaStoreApi() {
        lifecycleScope.launch {
            val images = loadPhotosFromExternalStorage()
            Toast.makeText(requireActivity(), "ss" + images.get(0).contentUri, Toast.LENGTH_SHORT)
                .show()
            binding.image.setImageURI(images.get(0).contentUri)
        }
    }


    fun loadFromUri(photoUri: Uri?): Bitmap? {
        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(requireActivity().contentResolver, photoUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                // support older versions of Android by using getBitmap
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }


    private suspend fun loadPhotosFromExternalStorage(): List<SharedStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )
            val photos = mutableListOf<SharedStoragePhoto>()
            requireActivity().contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(SharedStoragePhoto(id, displayName, width, height, contentUri))
                }
                photos.toList()
            } ?: listOf()
        }
    }


    private fun setProxy() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val proxyConfig: ProxyConfig =
                ProxyConfig.Builder().addProxyRule("http://proxy.uat-upswing.one:3128").build()
            ProxyController.getInstance().setProxyOverride(proxyConfig, object : Executor {
                override fun execute(command: Runnable) {
                    Timber.tag(TAGSS).w("proxy start execute")
                }
            }, Runnable {
                Timber.tag(TAGSS).w("WebView proxy finish execution")
            })
        } else {
            Timber.tag(TAGSS).w("Webview is not support proxyhere")
        }

    }


    private fun handleError(view: WebView?, errorCode: Int, error: String) {
        val msg = when (errorCode) {
            WebViewClient.ERROR_TIMEOUT -> "Error: Timeout"
            WebViewClient.ERROR_HOST_LOOKUP -> {
                "No Internet, Please check your connection/wifi"
            }
            WebViewClient.ERROR_CONNECT -> "Connection failed"
            WebViewClient.ERROR_UNKNOWN -> "Unknown error"
            WebViewClient.ERROR_BAD_URL -> "Bad UR"
            WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "Failed SSL handshake"
            WebViewClient.ERROR_UNSUPPORTED_SCHEME -> "Unsupported scheme"
            WebViewClient.ERROR_FILE -> "File error"
            WebViewClient.ERROR_FILE_NOT_FOUND -> "File not found"
            WebViewClient.ERROR_TOO_MANY_REQUESTS -> "Too many requests"
            else -> "Something went wrong"
        }
        binding.errorLayout.errorMsgTv.text = msg + "error: $error"
    }


    private fun registerBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webview.canGoBack()) {
                        binding.webview.goBack()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inline fun <T> sdk29AndUp(onSdk29: () -> T): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onSdk29()
        } else null
    }

}
