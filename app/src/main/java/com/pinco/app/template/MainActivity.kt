package com.pinco.app.template

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.onesignal.OneSignal
import com.pinco.app.template.views.PreLoaderActivity

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OneSignal.initWithContext(this)
        OneSignal.setAppId("af2a0c91-2610-4e54-a1fa-d0d58d0995d9")

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