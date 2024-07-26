package com.pinco.app.common

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.children

class Configurator(
    private val main: WebView,
    private val client: WebViewClient,
    private val chrome: WebChromeClient
) {

    private val firstHit = "; wv"
    private val secondHit = "\\sVersion/\\d+?\\.\\d+(?=\\s)"
    private val networkChrome = ConfiguratorClient()

    fun goBack(): Boolean {
        val last = main.children.lastOrNull()
        return if (last is WebView) {
            if (last.canGoBack()) {
                last.goBack()
                true
            } else {
                main.webChromeClient?.onCloseWindow(last)
                true
            }
        } else {
            val result = main.canGoBack()
            if (result) {
                main.goBack()
            }
            result
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(view: WebView) {
        with (view) {
            webChromeClient = networkChrome
            webViewClient = client

            with (settings) {
                userAgentString = userAgentString
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true // Important
                mixedContentMode = 0
                allowFileAccess = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = true
                allowContentAccess = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                displayZoomControls = false
                loadsImagesAutomatically = true
                setSupportMultipleWindows(true) // Important
            }
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(this@with, true)
            }
        }
    }

    private fun String.fix(): String {
        return substringBefore(firstHit) + with (substringAfter(firstHit)) {
            replace(Regex(secondHit), "")
        }
    }

    inner class ConfiguratorClient : WebChromeClient() {

        // Important
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            return view?.let { parent ->
                val window = WebView(parent.context)
                configure(window)
                window.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                parent.addView(window)

                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = window
                resultMsg?.sendToTarget()
                true
            } ?: false
        }

        // Important
        override fun onCloseWindow(window: WebView?) {
            super.onCloseWindow(window)
            main.removeView(window)
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return chrome.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }

    companion object {
        const val mainURL: String = ""
    }
}