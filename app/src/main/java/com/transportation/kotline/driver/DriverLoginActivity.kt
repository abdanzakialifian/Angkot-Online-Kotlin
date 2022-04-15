package com.transportation.kotline.driver

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.transportation.kotline.databinding.ActivityDriverLoginBinding
import com.transportation.kotline.other.LoginEmailActivity
import com.transportation.kotline.other.RegisterEmailActivity
import com.transportation.kotline.other.SendEmail

@Suppress("DEPRECATION")
class DriverLoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDriverLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverLoginBinding.inflate(layoutInflater)
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
            btnLoginGoogle.setOnClickListener(this@DriverLoginActivity)
            btnLoginEmail.setOnClickListener(this@DriverLoginActivity)
            tvRegister.setOnClickListener(this@DriverLoginActivity)
        }
    }

    // signInGoogle function
    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE)
    }

    // check id for driver
    private fun checkDrivers() {
        // get value account
        val driverId = firebaseAuth.currentUser?.uid
        val fullName = firebaseAuth.currentUser?.displayName
        val email = firebaseAuth.currentUser?.email

        val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
        val driverDatabase = firebaseDatabase.reference.child("Users").child("Drivers")
        driverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map: Map<String?, Any?>? = snapshot.getValue(gti)
                // check driver id in firebase database
                if (map?.get(driverId) != null) {
                    checkDriverVerification()
                } else {
                    if (driverId != null && fullName != null && email != null) {
                        val driverData = Driver(fullName, email, false)
                        val driversDb = firebaseDatabase.reference.child("Users")
                            .child("Drivers").child(driverId)
                        // add data driver to child id drivers
                        driversDb.setValue(driverData)

                        // send email to driver
                        val sendEmail = SendEmail(application, email)
                        sendEmail.email()

                        // call function alert dialog
                        alertDialog()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // check account verification for driver
    private fun checkDriverVerification() {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val driverDatabase = firebaseDatabase.reference.child("Users").child("Drivers")
                .child(driverId)
            driverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map: Map<String?, Any?>? = snapshot.getValue(gti)
                    // check verification
                    if (map?.get("verification") == true) {
                        Intent(this@DriverLoginActivity, DriverActivity::class.java).apply {
                            startActivity(this)
                            finishAffinity()
                        }
                    } else {
                        Toast.makeText(
                            this@DriverLoginActivity,
                            resources.getString(R.string.account_is_not_verification),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun alertDialog() {
        val builder = AlertDialog.Builder(this).create()
        val view = View.inflate(this, R.layout.custom_alert_dialog_layout, null)
        val buttonOk = view.findViewById<Button>(R.id.btn_login_ok)
        val tvContent = view.findViewById<TextView>(R.id.tv_please_login)
        tvContent.text = resources.getString(R.string.wait_for_verification)
        builder.setView(view)
        buttonOk.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
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
    @Deprecated("Deprecated in Java")
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
                                checkDrivers()
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
                    putExtra(DRIVER, "Drivers")
                    startActivity(this)
                }
            }
            // go to register email page
            R.id.tv_register -> {
                Intent(this, RegisterEmailActivity::class.java).apply {
                    putExtra(DRIVER, "Drivers")
                    startActivity(this)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 100
        const val DRIVER = "EXTRA_DRIVER"
    }
}