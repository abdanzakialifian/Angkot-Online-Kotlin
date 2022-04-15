package com.transportation.kotline.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.transportation.kotline.R
import com.transportation.kotline.databinding.ActivityRouteInformationContainerBinding

class RouteInformationContainerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInformationContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInformationContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<RouteInformationFragment>(R.id.fragment_container_view)
        }
    }
}