package com.pinco.app.views

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.pinco.app.MainActivity
import com.pinco.app.R
import com.pinco.app.common.Configurator
import com.pinco.app.databinding.ActivityWebViewBinding


class WebViewActivity : AppCompatActivity() {
    private var configurator: Configurator? = null
    private val TEL_PREFIX = "tel:"

    private lateinit var pathCallback: ValueCallback<Array<Uri?>>

    private var state: Boolean = false
    private var listener: ValueCallback<Array<Uri>>? = null

    private val content = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        listener?.onReceiveValue(it.toTypedArray())
    }

    val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            pathCallback.onReceiveValue(it.toTypedArray())
        }

    companion object {
        private const val IMAGE_MIME_TYPE = "image/*"

        var data: Array<String>? = arrayOf()
        var urlK: String = ""
        private var instance: WebViewActivity? = null

        fun pickMedia(listener: ValueCallback<Array<Uri>>?): Boolean {
            return instance?.let {
                pickMedia(listener)
                true
            } ?: false
        }
    }

    private val binding by lazy {
        ActivityWebViewBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        urlK = intent.extras?.getString("url").toString()
         Log.d("extras", ((intent.extras?.get("data")) as Array<String>)[0])
        data = intent.extras?.get("data") as Array<String>
        this.onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    configurator?.goBack()
                }
            }
        )


        setContentView(binding.root)
//        setSystemBarDark(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_pin_up_color)
        }

        with(binding) {
            configurator = Configurator(
                main = webView,
                client = object : WebViewClient() {

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        if (webView.isInvisible) {
                            webView.isInvisible = false
                        }
                        CookieManager.getInstance().flush()
                        Log.d("onPageLife", "onPageFinished " + url.toString())
                        if (MainActivity.sharedPrefs?.getString(
                                "link",
                                null
                            ) == null && url != "about:blank"
                        ) {
                            Log.d("dataSave", "saved url - $url")
                            MainActivity.sharedPrefs?.edit()?.putString("link", url)?.apply()
                        }

                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        Log.d("onPageLife", "onPageStarted " + url.toString())

                        if (url != null && Uri.parse(url).host?.contains("pin-up") == true && !state) {
                            state = true
                        }

                        super.onPageStarted(view, url, favicon)
                    }

//                    @Deprecated("Deprecated in Java")
//                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                        Log.d("tabs1_url", url.toString())
//
//                        if (url!!.contains("tg:")) {
//                            val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                            telegramIntent.setPackage("org.telegram.messenger")
//                            return try {
//                                startActivity(telegramIntent)
//                                true
//                            } catch (e: ActivityNotFoundException) {
//
//                                val customTabsIntent = CustomTabsIntent.Builder()
////                                    .setToolbarColor(ContextCompat.getColor(this@WebViewActivity, R.color.status_bar_pin_up_color)) // Optional: set the toolbar color
////                                    .addDefaultShareMenuItem() // Optional: add a default share button
////                                    .setShowTitle(true) // Optional: show the title in the custom tab
//                                    .build()
//                                Log.d("tabs2_url", url)
//                                // Launching the URL
//                                customTabsIntent.launchUrl(this@WebViewActivity, Uri.parse("https://stackoverflow.com/"))
//                                true
//                            }
//                        } else {
//                            return false
//                        }
//
//
//
////                        if (url != null && Uri.parse(url).host?.contains("pin-up") != true) {
////                            Log.d("onPageLife", "overridedUrl " + url.toString())
////
////                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
////                            context?.startActivity(intent)
////                            return true // Indicates that we're handling the URL loading
////                        }
////                        Log.d("onPageLife", "shouldOvveride " + url.toString())
//
//                        // Allow WebView to handle the URL
//
////
////                        val url = URL(url.toString())
////                        val host3 = url.host
////                        Log.d("tags222", url.toString())
////                        if (Uri.parse(url.toString()).host!!.contains("pin-up")) {
////
////                            Log.d("flow222", url.toString())
////
////                            // This is my web site, so do not override; let the WebView load the page.
////                            return false;
////                        }
////                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
////                        startActivity(intent)
////                        // Reject everything else.
////
////                        return true
//
//                    }


                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest
                    ): Boolean {
                        Log.d("tabs1_request", request.url.toString())
                        Log.d("tabs1_request", data?.get(0).toString())


//request.url.toString().contains("tg:") || request.url.toString().contains("t.me") || request.url.toString().contains("instagram.com") || request.url.toString().contains("twitter.com")
                        //   !data!!.contains(request.url.host) && state && !request.url.toString().contains("getTokenPage")
                        if (request.url.toString().contains("tg:") || request.url.toString()
                                .contains("t.me") || request.url.toString()
                                .contains("instagram.com") || request.url.toString()
                                .contains("twitter.com") || request.url.toString().contains("payment")
                        ) {
                            val telegramIntent =
                                Intent(Intent.ACTION_VIEW, Uri.parse(request.url.toString()))
                            telegramIntent.setPackage("org.telegram.messenger")
                            return try {
                                startActivity(telegramIntent)
                                true
                            } catch (e: ActivityNotFoundException) {
                                Log.d(
                                    "tabs1_request",
                                    request.url.toString() + " activity not found"
                                )

                                val customTabsIntent = CustomTabsIntent.Builder()
                                    .setToolbarColor(
                                        ContextCompat.getColor(
                                            this@WebViewActivity,
                                            R.color.status_bar_pin_up_color
                                        )
                                    ) // Optional: set the toolbar color
                                    .addDefaultShareMenuItem() // Optional: add a default share button
                                    .setShowTitle(true) // Optional: show the title in the custom tab
                                    .build()

                                // Launching the URL
                                customTabsIntent.launchUrl(
                                    this@WebViewActivity,
                                    Uri.parse(request.url.toString())
                                )
                                true
                            }
                        } else if(request.url.toString().startsWith(TEL_PREFIX)){
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.setData(Uri.parse(request.url.toString()))
                        startActivity(intent)
                        return true
                        }
                        else {
                            Log.d("tabs1_request", "FALSE")

                            return false
                        }
//                        if (request.url != null && Uri.parse(request.url.toString()).host?.contains("pin-up") != true) {
//                            Log.d("onPageLife", "overridedUrl "+ request.url.toString())
//
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.url.toString()))
//                            context?.startActivity(intent)
//                            return true // Indicates that we're handling the URL loading
//                        }
//                        Log.d("onPageLife", "shouldOvveride "+request.url.toString())

//                        return false // Allow WebView to handle the URL
                    }


                    // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                    // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs

//                        return  if (request?.url.toString().contains("tg:") && isUriAvailable(requireContext(), request?.url.toString())) {
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request?.url.toString()))
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            requireActivity().startActivity(intent)
//                            false
//                        } else if (request?.url.toString().contains("t.me")) {
//                            false
//                        } else {
//                            false
//                        }
//                        val url = request?.url.toString()
//                        if (url.isEmpty()) {
//                            return true
//                        } else {
//                            if (url.contains("tg:")){
//                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
////                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                requireActivity().startActivity(intent)
//                            } else if (url.contains("instagram.com")){
//                                return false
//                            } else return !(url.contains("twitter.com") || url.contains("x.com"))
//                        }
//

//                        return request!!.url.toString().contains("instagram.com") || request.url.toString().contains("twitter.com") || request.url.toString().contains("t.me")


                },
                chrome = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView,
                        filePathCallback: ValueCallback<Array<Uri?>>,
                        fileChooserParams: FileChooserParams
                    ): Boolean {
                        pathCallback = filePathCallback
                        activityResultLauncher.launch(IMAGE_MIME_TYPE)
                        return true
                    }
                }
            )

            configurator?.configure(webView)
            Log.d("dataFlow2", urlK)
            webView.loadUrl(urlK)
        }


//        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
//            Configuration.UI_MODE_NIGHT_YES -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    window.decorView.systemUiVisibility =View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                    window.statusBarColor = Color.BLACK
//                }
//            }
//            Configuration.UI_MODE_NIGHT_NO -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    window.decorView.systemUiVisibility =View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                    window.statusBarColor = Color.WHITE
//                }
//            }
//            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
//        }


    }

    private fun pickMedia(listener: ValueCallback<Array<Uri>>?) {
        this.listener = listener
        content.launch("image/*")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()


    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

}