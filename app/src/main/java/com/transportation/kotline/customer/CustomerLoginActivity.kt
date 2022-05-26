package com.transportation.kotline.customer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.R
import com.transportation.kotline.databinding.ActivityCustomerLoginBinding
import com.transportation.kotline.model.Customer
import com.transportation.kotline.other.LoginEmailActivity
import com.transportation.kotline.other.RegisterEmailActivity

@Suppress("DEPRECATION")
class CustomerLoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCustomerLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // configure google sign in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id_auth))
            .requestEmail()
            .build()

        // getting the value of googleSignInOptions inside the GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // button to move page and login google
        binding.apply {
            btnLoginGoogle.setOnClickListener(this@CustomerLoginActivity)
            btnLoginEmail.setOnClickListener(this@CustomerLoginActivity)
            tvRegister.setOnClickListener(this@CustomerLoginActivity)
        }
    }

    // signInGoogle function
    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE)
    }

    // check id for customer
    private fun checkCustomers() {
        // get value account
        val customerId = firebaseAuth.currentUser?.uid
        val fullName = firebaseAuth.currentUser?.displayName
        val email = firebaseAuth.currentUser?.email

        val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
        val customerDatabase = firebaseDatabase.reference.child("Users").child("Customers")
        customerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map: Map<String?, Any?>? = snapshot.getValue(gti)
                // check customer id in firebase database
                if (map?.get(customerId) != null) {
                    val customersDb = firebaseDatabase.reference.child("Users").child("Customers")
                        .child(customerId.toString())
                    val checkLogin = HashMap<String, Any>()
                    checkLogin["login"] = true
                    customersDb.updateChildren(checkLogin)

                    Intent(this@CustomerLoginActivity, CustomerActivity::class.java).apply {
                        startActivity(this)
                        finishAffinity()
                    }
                } else {
                    if (customerId != null && fullName != null && email != null) {
                        val customerData = Customer(fullName, email, true)
                        val customersDb =
                            firebaseDatabase.reference.child("Users").child("Customers")
                                .child(customerId)
                        // add data customer to child id customers
                        customersDb.setValue(customerData)

                        Intent(this@CustomerLoginActivity, CustomerActivity::class.java).apply {
                            startActivity(this)
                            finishAffinity()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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

    // this is where we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // view loading bar
        showLoading(true)
        if (requestCode == REQUEST_CODE) {
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (completedTask.isSuccessful) {
                try {
                    val account = completedTask.getResult(ApiException::class.java)
                    val authCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    firebaseAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // hidden loading bar
                                showLoading(false)
                                checkCustomers()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Authentication Failed : ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // go to google sign in
            R.id.btn_login_google -> signInGoogle()
            // go to login email page
            R.id.btn_login_email -> {
                Intent(this, LoginEmailActivity::class.java).apply {
                    // send data to login email activity
                    putExtra(CUSTOMER, "Customers")
                    startActivity(this)
                }
            }
            // go to register email page
            R.id.tv_register -> {
                Intent(this, RegisterEmailActivity::class.java).apply {
                    // send data to register email activity
                    putExtra(CUSTOMER, "Customers")
                    startActivity(this)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 100
        const val CUSTOMER = "EXTRA_CUSTOMER"
    }
}