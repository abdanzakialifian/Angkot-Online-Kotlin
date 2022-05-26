package com.transportation.kotline.other

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.transportation.kotline.R
import com.transportation.kotline.customer.CustomerActivity
import com.transportation.kotline.customer.CustomerLoginActivity.Companion.CUSTOMER
import com.transportation.kotline.databinding.ActivityLoginEmailBinding
import com.transportation.kotline.driver.DriverActivity
import com.transportation.kotline.driver.DriverLoginActivity.Companion.DRIVER

class LoginEmailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var customer: String? = null
    private var driver: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // get intent from customer login activity
        customer = intent?.getStringExtra(CUSTOMER)

        // get intent from driver login activity
        driver = intent?.getStringExtra(DRIVER)

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // add event click to button
        binding.apply {
            btnLogin.setOnClickListener(this@LoginEmailActivity)
            signUpLayout.tvSignUp.setOnClickListener(this@LoginEmailActivity)
            customActionBar.btnBack.setOnClickListener(this@LoginEmailActivity)
        }

        // call function enabled button
        enabledButton()
    }

    // check form field to enabled button
    private fun enabledButton() {
        val registerTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.apply {
                    val email = edtEmail.text.toString()
                    val password = edtPassword.text.toString()

                    if (email.isNotEmpty() && password.isNotEmpty() && password.length >= 8) {
                        btnLogin.isEnabled = true
                        btnLogin.isClickable = true
                        btnLogin.setBackgroundResource(R.drawable.custom_button_blue)
                    } else {
                        btnLogin.isEnabled = false
                        btnLogin.isClickable = false
                        btnLogin.setBackgroundResource(R.drawable.custom_button_disabled)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        }

        binding.apply {
            edtEmail.addTextChangedListener(registerTextWatcher)
            edtPassword.addTextChangedListener(registerTextWatcher)
        }
    }

    // check id for customer
    private fun checkCustomers(customer: String?) {
        val customerId = firebaseAuth.currentUser?.uid
        val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
        val customerDatabase = firebaseDatabase.reference.child("Users").child(customer.toString())
        customerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map: Map<String?, Any?>? = snapshot.getValue(gti)
                // check driver id in firebase database
                if (map?.get(customerId) != null) {
                    val customersDb = firebaseDatabase.reference.child("Users").child("Customers")
                        .child(customerId.toString())
                    val checkLogin = HashMap<String, Any>()
                    checkLogin["login"] = true
                    customersDb.updateChildren(checkLogin)

                    Intent(this@LoginEmailActivity, CustomerActivity::class.java).apply {
                        startActivity(this)
                        finishAffinity()
                    }
                } else {
                    Toast.makeText(
                        this@LoginEmailActivity,
                        resources.getString(R.string.account_is_not_registered),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // check id for driver
    private fun checkDrivers(driver: String?) {
        val driverId = firebaseAuth.currentUser?.uid
        val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
        val driverDatabase = firebaseDatabase.reference.child("Users").child(driver.toString())
        driverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map: Map<String?, Any?>? = snapshot.getValue(gti)
                // check driver id in firebase database
                if (map?.get(driverId) != null) {
                    checkDriverVerification(driver)
                } else {
                    Toast.makeText(
                        this@LoginEmailActivity,
                        resources.getString(R.string.account_is_not_registered),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // check account verification for driver
    private fun checkDriverVerification(driver: String?) {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val driverDatabase = firebaseDatabase.reference.child("Users").child(driver.toString())
                .child(driverId)
            driverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map: Map<String?, Any?>? = snapshot.getValue(gti)
                    // check verification
                    if (map?.get("verification") == true) {
                        Intent(this@LoginEmailActivity, DriverActivity::class.java).apply {
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    FirebaseService.sharedPreferences =
                                        getSharedPreferences(
                                            "sharedPreferences",
                                            Context.MODE_PRIVATE
                                        )
                                    FirebaseService.token = token
                                    val driverInformation = HashMap<String, Any>()
                                    driverInformation["deviceToken"] = task.result
                                    driverDatabase.updateChildren(driverInformation)
                                }
                            }
                            startActivity(this)
                            finishAffinity()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginEmailActivity,
                            resources.getString(R.string.account_is_not_verification),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function show loading
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.apply {
            visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                val email = binding.edtEmail.text.toString()
                val password = binding.edtPassword.text.toString()
                // view loading bar
                showLoading(true)

                // hidden keyboard
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.btnLogin.windowToken, 0)

                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            // hidden loading bar
                            showLoading(false)
                            Toast.makeText(this, "Sign up error", Toast.LENGTH_SHORT).show()
                        } else if (task.isSuccessful && customer != null) {
                            // hidden loading bar
                            showLoading(false)
                            checkCustomers(customer)
                        } else if (task.isSuccessful && driver != null) {
                            // hidden loading bar
                            showLoading(false)
                            checkDrivers(driver)
                        }
                    }
            }

            R.id.tv_sign_up -> {
                if (customer != null) {
                    Intent(this, RegisterEmailActivity::class.java).apply {
                        // send data to register email activity
                        putExtra(CUSTOMER, "Customers")
                        startActivity(this)
                        finishAffinity()
                    }
                } else {
                    Intent(this, RegisterEmailActivity::class.java).apply {
                        // send data to register email activity
                        putExtra(DRIVER, "Drivers")
                        startActivity(this)
                        finishAffinity()
                    }
                }
            }

            R.id.btn_back -> onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}