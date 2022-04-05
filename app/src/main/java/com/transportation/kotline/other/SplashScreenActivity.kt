package com.transportation.kotline.other

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.transportation.kotline.R
import com.transportation.kotline.onboarding.OnBoardingActivity
import com.transportation.kotline.other.Global.SHARED_PREFS
import com.transportation.kotline.other.Global.STARTED_PREFS

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // hide action bar
        supportActionBar?.hide()

        // window fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // get shared preferences from on boarding three fragment
        val sharedPreference = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val isStarted = sharedPreference?.getBoolean(STARTED_PREFS, false)

        // check whether the starter button has been clicked or not
        if (isStarted == true) {
            Handler(mainLooper).postDelayed({
                // go to option activity
                Intent(this, OptionActivity::class.java).apply {
                    // start activity
                    startActivity(this)
                    // close activity
                    finish()
                }
            }, SPLASH_TIME_OUT)
        } else {
            Handler(mainLooper).postDelayed({
                // go to on boarding activity
                Intent(this, OnBoardingActivity::class.java).apply {
                    // start activity
                    startActivity(this)
                    // close activity
                    finish()
                }
            }, SPLASH_TIME_OUT)
        }
    }

    companion object {
        // duration splash screen 3second
        private const val SPLASH_TIME_OUT = 3000L
    }
}