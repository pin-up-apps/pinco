package com.pinco.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.onesignal.OneSignal
import com.pinco.app.views.PreLoaderActivity

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OneSignal.initWithContext(this)
        OneSignal.setAppId("3c5c3baf-e425-4935-b308-da7dc1c3c3c6")



            startActivity(
                Intent(this@MainActivity, PreLoaderActivity::class.java)
            )
            finish()
        }




    companion object {
        var sharedPrefs: SharedPreferences? = null
        val editor: SharedPreferences.Editor by lazy {
            sharedPrefs!!.edit()
        }

    }
}