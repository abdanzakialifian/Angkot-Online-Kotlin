package com.transportation.kotline.customer

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
import com.transportation.kotline.adapter.CustomersHistoryAdapter
import com.transportation.kotline.databinding.ActivityCustomersHistoryBinding
import java.util.*

class CustomersHistoryActivity : AppCompatActivity(), IOnItemCustomerCallback {

    private lateinit var binding: ActivityCustomersHistoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var listCustomersHistory: ArrayList<CustomersHistory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomersHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // initialize default list history customers
        listCustomersHistory = arrayListOf()

        // call function history list customers
        getHistoryListCustomers()

        // add event click to button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    // function to get list in children history customers
    private fun getHistoryListCustomers() {
        val customerId = firebaseAuth.currentUser?.uid
        if (customerId != null) {
            val customersDatabase =
                firebaseDatabase.reference.child("Users").child("Customers")
                    .child(customerId).child("history")
            customersDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        // hide animation and show recyclerview
                        binding.apply {
                            historyAnimation.visibility = View.GONE
                            tvNoOrderHistory.visibility = View.GONE
                            rvCustomersHistory.visibility = View.VISIBLE
                        }

                        // looping data
                        for (data in snapshot.children) {
                            val history = data.getValue(CustomersHistory::class.java)
                            listCustomersHistory.add(history as CustomersHistory)
                        }

                        // sort recycler view by descending
                        listCustomersHistory.sortByDescending {
                            it.time
                        }

                        binding.apply {
                            rvCustomersHistory.layoutManager =
                                LinearLayoutManager(this@CustomersHistoryActivity)
                            val historyAdapter =
                                CustomersHistoryAdapter(this@CustomersHistoryActivity)
                            historyAdapter.setListHistory(listCustomersHistory)
                            rvCustomersHistory.setHasFixedSize(true)
                            rvCustomersHistory.adapter = historyAdapter
                        }
                    } else {
                        // show animation and hide recyclerview
                        binding.apply {
                            historyAnimation.visibility = View.VISIBLE
                            tvNoOrderHistory.visibility = View.VISIBLE
                            rvCustomersHistory.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // event clicked to move detail customer history
    override fun onItemClicked(customersHistory: CustomersHistory) {
        Intent(this, DetailCustomerHistoryActivity::class.java).apply {
            putExtra(CUSTOMER_DATA_HISTORY, customersHistory)
            startActivity(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        const val CUSTOMER_DATA_HISTORY = "EXTRA_DATA_HISTORY"
    }
}