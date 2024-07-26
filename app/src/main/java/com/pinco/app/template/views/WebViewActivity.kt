package com.pinco.app.template.views

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.pinco.app.template.MainActivity
import com.pinco.app.R
import com.pinco.app.template.common.Configurator
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

                            return handleDeepLink(request.url.toString()) || super.shouldOverrideUrlLoading(view, request)
                        }

                    }



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

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun isAppInstalledForUPI(): Boolean {
        val upiApps = listOf("net.one97.paytm", "com.phonepe.app", "com.google.android.apps.nbu.paisa.user") // Add more UPI app package names if needed
        for (app in upiApps) {
            if (isAppInstalled(app)) {
                return true
            }
        }
        return false
    }

    private fun openInPlayStore(packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        startActivity(intent)
    }

    private fun openInExternalBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun showNoSupportedAppsDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Supported Apps Installed")
            .setMessage("Your phone doesn't have any supported UPI apps installed.")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }


    private fun handleDeepLink(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        return try {
            when {
                url.startsWith("paytmmp://") -> {
                    if (isAppInstalled("net.one97.paytm")) {
                        startActivity(intent)
                    } else {
                        openInPlayStore("net.one97.paytm")
                    }
                }
                url.startsWith("phonepe://") -> {
                    if (isAppInstalled("com.phonepe.app")) {
                        startActivity(intent)
                    } else {
                        openInPlayStore("com.phonepe.app")
                    }
                }
                url.startsWith("upi://") -> {

                    // For other UPI links, you can add additional app checks if needed
                    if (isAppInstalledForUPI()) {
                        startActivity(intent)
                    } else {
                        showNoSupportedAppsDialog()
                    }
                }

            }
            Log.d("tabs1_request", "other true")

            false
        } catch (e: ActivityNotFoundException) {
            Log.d("tabs1_request", "other openInExternal")

            openInExternalBrowser(url)
            false
        }
    }


}