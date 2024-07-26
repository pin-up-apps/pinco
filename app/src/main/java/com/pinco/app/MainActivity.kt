package com.pinco.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.onesignal.OneSignal
import com.pinco.app.views.PreLoaderActivity

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OneSignal.initWithContext(this)
        OneSignal.setAppId("9709c6bb-071d-455a-9d79-142dfb23ff7d")

        val handler = Handler()

        handler.postDelayed({
            startActivity(
                Intent(this@MainActivity, PreLoaderActivity::class.java)
            )
            finish()
        }, 400)

        }




    companion object {
        var sharedPrefs: SharedPreferences? = null
        val editor: SharedPreferences.Editor by lazy {
            sharedPrefs!!.edit()
        }

    }
}