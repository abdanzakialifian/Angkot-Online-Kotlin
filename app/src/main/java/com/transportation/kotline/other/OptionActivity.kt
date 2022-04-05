package com.transportation.kotline.other

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.transportation.kotline.R
import com.transportation.kotline.customer.CustomerLoginActivity
import com.transportation.kotline.databinding.ActivityOptionBinding
import com.transportation.kotline.driver.DriverLoginActivity

class OptionActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityOptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // button to move page
        binding.apply {
            btnDriver.setOnClickListener(this@OptionActivity)
            btnCustomer.setOnClickListener(this@OptionActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // move to driver login activity
            R.id.btn_driver -> {
                Intent(this, DriverLoginActivity::class.java).apply {
                    startActivity(this)
                }
            }

            // move to customer login activity
            R.id.btn_customer -> {
                Intent(this, CustomerLoginActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }
}