package com.pinco.app.views

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.webkit.ValueCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.pinco.app.R

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PreLoaderActivity : AppCompatActivity() {
    private var listener: ValueCallback<Array<Uri>>? = null

    private val content = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        listener?.onReceiveValue(it.toTypedArray())
    }

    var remoteConfig: Any? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_loader)


//        val crashButton = Button(this)
//        crashButton.text = "Test Crash"
//        crashButton.setOnClickListener {
//            throw RuntimeException("Test Crash") // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_pin_up_color )
        }

            val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings =
                FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600)
                    .build()
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)


            lifecycleScope.launch(Dispatchers.IO) {
//                apps().collect {



                    Log.d("dataFlow", "conversion2:  " )

                    firebaseRemoteConfig.fetchAndActivate().addOnSuccessListener { aVoid: Boolean? ->
                        Log.d("tags2", FirebaseRemoteConfig.getInstance().getString("url"))
                        lifecycleScope.launch(Dispatchers.Main) {
                            Log.d("dataFlow", "conversion3:  " + FirebaseRemoteConfig.getInstance()
                                .getString("url"))

                            Log.d("dataFlow", "conversion4:  " + FirebaseRemoteConfig.getInstance()
                                .getString("data").split(",").toTypedArray().get(0))

                        startActivity(
                            Intent(this@PreLoaderActivity, WebViewActivity::class.java).putExtra(
                                "url",
                                FirebaseRemoteConfig.getInstance()
                                    .getString("url"),
                            ).putExtra("data", FirebaseRemoteConfig.getInstance().getString("data").split(",").toTypedArray()
                            ))
                            finish()
                        }

                }
            }



    }


//    private fun apps() = callbackFlow {
//
//        val firebaseRemoteConfig: FirebaseRemoteConfig
//        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        val configSettings =
//            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build()
//        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
//        firebaseRemoteConfig.fetchAndActivate().addOnSuccessListener { aVoid: Boolean? ->
//            Log.d("tags", FirebaseRemoteConfig.getInstance().getString("url"))
//            trySend(FirebaseRemoteConfig.getInstance().getString("url"))
//        }
//
//
//        awaitClose {
//
//            cancel()
//
//        }
//    }


//    private fun apps() = callbackFlow {
//        AppsFlyerLib.getInstance().init(
//            APPS_FLYER_DEV_KEY,
//            object : AppsFlyerConversionListener {
//                override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
//                    Log.d("dataFlow", "conversion:  " + data.toString())
//
//                    trySend(data)
//                }
//
//                override fun onConversionDataFail(message: String?) {
//                    Log.d("dataFlow", "conversion:  " + " null")
//
//                    trySend(null)
//
//                }
//
//                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
//
//                }
//
//                override fun onAttributionFailure(p0: String?) {
//
//                }
//            },
//            this@PreLoaderActivity
//        )
//        AppsFlyerLib.getInstance().start(this@PreLoaderActivity)
//        awaitClose {
//            cancel()
//        }
//    }


    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }


    companion object {
        private const val TAG = "PreLoaderActivity"


        private var instance: PreLoaderActivity? = null
        const val APPS_FLYER_DEV_KEY = "m5ZqwaoEQYpwAg8CXbWzKm"
    }
}