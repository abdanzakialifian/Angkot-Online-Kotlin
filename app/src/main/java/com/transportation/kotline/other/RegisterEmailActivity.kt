package com.transportation.kotline.other

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.R
import com.transportation.kotline.customer.Customer
import com.transportation.kotline.customer.CustomerLoginActivity.Companion.CUSTOMER
import com.transportation.kotline.databinding.ActivityRegisterEmailBinding
import com.transportation.kotline.driver.Driver
import com.transportation.kotline.driver.DriverLoginActivity
import com.transportation.kotline.driver.DriverLoginActivity.Companion.DRIVER

class RegisterEmailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityRegisterEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var customer: String? = null
    private var driver: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
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
            btnRegistration.setOnClickListener(this@RegisterEmailActivity)
            signInLayout.tvSignIn.setOnClickListener(this@RegisterEmailActivity)
            customActionBar.btnBack.setOnClickListener(this@RegisterEmailActivity)
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
                    val firstName = edtFirstName.text.toString()
                    val lastName = edtLastName.text.toString()
                    val email = edtEmail.text.toString()
                    val password = edtPassword.text.toString()

                    if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password.length >= 8) {
                        btnRegistration.isEnabled = true
                        btnRegistration.isClickable = true
                        btnRegistration.setBackgroundResource(R.drawable.custom_button_blue)
                    } else {
                        btnRegistration.isEnabled = false
                        btnRegistration.isClickable = false
                        btnRegistration.setBackgroundResource(R.drawable.custom_button_disabled)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        }

        binding.apply {
            edtFirstName.addTextChangedListener(registerTextWatcher)
            edtLastName.addTextChangedListener(registerTextWatcher)
            edtEmail.addTextChangedListener(registerTextWatcher)
            edtPassword.addTextChangedListener(registerTextWatcher)
        }
    }

    private fun alertDialog(target: String?) {
        // alert dialog for customers
        if (target == "Customers") {
            val builder = AlertDialog.Builder(this).create()
            val view = View.inflate(this, R.layout.custom_alert_dialog_layout, null)
            val buttonOk = view.findViewById<Button>(R.id.btn_login_ok)
            builder.setView(view)
            buttonOk.setOnClickListener {
                Intent(this, LoginEmailActivity::class.java).apply {
                    putExtra(CUSTOMER, "Customers")
                    startActivity(this)
                    finishAffinity()
                }
            }
            builder.setCanceledOnTouchOutside(false)
            builder.show()

            // alert dialog for drivers
        } else if (target == "Drivers") {
            val builder = AlertDialog.Builder(this).create()
            val view = View.inflate(this, R.layout.custom_alert_dialog_layout, null)
            val buttonOk = view.findViewById<Button>(R.id.btn_login_ok)
            val tvContent = view.findViewById<TextView>(R.id.tv_please_login)
            tvContent.text = resources.getString(R.string.wait_for_verification)
            builder.setView(view)
            buttonOk.setOnClickListener {
                Intent(this, DriverLoginActivity::class.java).apply {
                    putExtra(DRIVER, "Drivers")
                    startActivity(this)
                    finishAffinity()
                }
            }
            builder.setCanceledOnTouchOutside(false)
            builder.show()
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
            R.id.btn_registration -> {
                // get value in edit text
                val firstName = binding.edtFirstName.text.toString().trim()
                val lastName = binding.edtLastName.text.toString().trim()
                val email = binding.edtEmail.text.toString().trim()
                val password = binding.edtPassword.text.toString().trim()
                val fullName = "$firstName $lastName"
                // view loading bar
                showLoading(true)

                // hidden keyboard
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.btnRegistration.windowToken, 0)

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // hidden loading bar
                            showLoading(false)
                            // get user id
                            val userId = firebaseAuth.currentUser?.uid
                            // add data to firebase database
                            if (customer != null && userId != null) {
                                val customerData = Customer(fullName, email)
                                val customersDb = firebaseDatabase.reference.child("Users")
                                    .child(customer.toString()).child(userId)
                                // add data customer to child id customers
                                customersDb.setValue(customerData)
                                // call function alert dialog
                                alertDialog(customer)
                            } else if (driver != null && userId != null) {
                                val driverData = Driver(fullName, email, false)
                                val driversDb = firebaseDatabase.reference.child("Users")
                                    .child(driver.toString()).child(userId)
                                // add data driver to child id drivers
                                driversDb.setValue(driverData)

                                // send email to driver
                                val sendEmail = SendEmail(application, email)
                                sendEmail.email()

                                // call function alert dialog
                                alertDialog(driver)
                            }
                        } else {
                            // hidden loading bar
                            showLoading(false)
                            Toast.makeText(this, "Sign up error", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            R.id.tv_sign_in -> {
                if (customer != null) {
                    Intent(this, LoginEmailActivity::class.java).apply {
                        // send data to login email activity
                        putExtra(CUSTOMER, "Customers")
                        startActivity(this)
                        finishAffinity()
                    }
                } else {
                    Intent(this, LoginEmailActivity::class.java).apply {
                        // send data to login email activity
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