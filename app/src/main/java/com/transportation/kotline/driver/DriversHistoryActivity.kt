package com.transportation.kotline.driver

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.adapter.DriversHistoryAdapter
import com.transportation.kotline.databinding.ActivityDriversHistoryBinding

class DriversHistoryActivity : AppCompatActivity(), IOnItemDriverCallback {

    private lateinit var binding: ActivityDriversHistoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var listDriversHistory: ArrayList<DriversHistory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriversHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // initialize default list history drivers
        listDriversHistory = arrayListOf()

        // call function history list drivers
        getHistoryListDrivers()

        // add event click to button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    // function to get list in children history drivers
    private fun getHistoryListDrivers() {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val driversDatabase =
                firebaseDatabase.reference.child("Users").child("Drivers")
                    .child(driverId).child("history")
            driversDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // hide animation and show recyclerview
                        binding.apply {
                            historyAnimation.visibility = View.GONE
                            tvNoOrderHistory.visibility = View.GONE
                            rvDriversHistory.visibility = View.VISIBLE
                        }

                        for (data in snapshot.children) {
                            val history = data.getValue(DriversHistory::class.java)
                            listDriversHistory.add(history as DriversHistory)

                        }

                        // sort recycler view by descending
                        listDriversHistory.sortByDescending {
                            it.time
                        }

                        binding.apply {
                            rvDriversHistory.layoutManager =
                                LinearLayoutManager(this@DriversHistoryActivity)
                            val historyAdapter = DriversHistoryAdapter(this@DriversHistoryActivity)
                            historyAdapter.setListHistory(listDriversHistory)
                            rvDriversHistory.setHasFixedSize(true)
                            rvDriversHistory.adapter = historyAdapter
                        }
                    } else {
                        // show animation and hide recyclerview
                        binding.apply {
                            historyAnimation.visibility = View.VISIBLE
                            tvNoOrderHistory.visibility = View.VISIBLE
                            rvDriversHistory.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    override fun onItemClicked(driversHistory: DriversHistory) {
        Intent(this, DetailDriverHistoryActivity::class.java).apply {
            putExtra(DRIVER_DATA_HISTORY, driversHistory)
            startActivity(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        const val DRIVER_DATA_HISTORY = "EXTRA_DATA_HISTORY"
    }
}