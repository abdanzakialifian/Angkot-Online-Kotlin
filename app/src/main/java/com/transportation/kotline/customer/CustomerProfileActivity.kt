package com.transportation.kotline.customer

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.transportation.kotline.R
import com.transportation.kotline.databinding.ActivityCustomerProfileBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class CustomerProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCustomerProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mCustomerDatabase: DatabaseReference
    private lateinit var calendar: Calendar
    private var customerId: String? = null
    private var resultUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // initialize calendar
        calendar = Calendar.getInstance()

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // initialize customer id
        customerId = firebaseAuth.currentUser?.uid

        // initialize customer database
        mCustomerDatabase = firebaseDatabase.reference.child("Users").child("Customers")
            .child(customerId.toString())

        // call function to get customer information
        getCustomerInformation()

        // add event click to button
        binding.apply {
            customActionBar.btnBack.setOnClickListener(this@CustomerProfileActivity)
            edtBirthdate.setOnClickListener(this@CustomerProfileActivity)
            btnSave.setOnClickListener(this@CustomerProfileActivity)
            imgProfile.setOnClickListener(this@CustomerProfileActivity)
            // default radio button
            rgGender.check(R.id.rb_male)
        }

        // check enabled button
        enabledButton()
    }

    // check form field to enabled button
    private fun enabledButton() {
        val registerTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.apply {
                    val name = edtName.text.toString()
                    val birthDate = edtBirthdate.text.toString()
                    val phone = edtPhone.text.toString()

                    if (name.isNotEmpty() && birthDate.isNotEmpty() && phone.isNotEmpty()) {
                        btnSave.isEnabled = true
                        btnSave.isClickable = true
                        btnSave.setBackgroundResource(R.drawable.custom_button_blue)
                    } else {
                        btnSave.isEnabled = false
                        btnSave.isClickable = false
                        btnSave.setBackgroundResource(R.drawable.custom_button_disabled)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        }

        binding.apply {
            edtName.addTextChangedListener(registerTextWatcher)
            edtBirthdate.addTextChangedListener(registerTextWatcher)
            edtPhone.addTextChangedListener(registerTextWatcher)
        }
    }

    // function to getting data customer
    private fun getCustomerInformation() {
        // view loading bar
        showLoading(true)
        if (customerId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            mCustomerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        // hide loading bar
                        showLoading(false)
                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        binding.apply {
                            if (map?.get("profileImageUrl") != null) {
                                val image = map["profileImageUrl"].toString()
                                Glide.with(this@CustomerProfileActivity)
                                    .load(image)
                                    .placeholder(R.drawable.ic_load_data)
                                    .error(R.drawable.ic_error_load_data)
                                    .into(imgProfile)
                            }

                            if (map?.get("name") != null) {
                                val name = map["name"].toString()
                                edtName.setText(name)
                            }

                            if (map?.get("phone") != null) {
                                val phone = map["phone"].toString()
                                edtPhone.setText(phone)
                            }

                            if (map?.get("birthdate") != null) {
                                val birthdate = map["birthdate"].toString()
                                edtBirthdate.setText(birthdate)
                            }

                            if (map?.get("gender") != null) {
                                when (map["gender"].toString()) {
                                    "Pria" -> rgGender.check(R.id.rb_male)
                                    "Wanita" -> rgGender.check(R.id.rb_female)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to saving data customer
    private fun saveCustomerInformation() {
        binding.apply {
            val name = edtName.text.toString()
            val phone = edtPhone.text.toString().trim()
            val date = edtBirthdate.text.toString()

            val selectIdGender = rgGender.checkedRadioButtonId
            val radioGender = findViewById<RadioButton>(selectIdGender)
            if (radioGender.text == null) {
                return
            }
            val gender = radioGender.text.toString()

            // add image profile
            if (resultUri != null && customerId != null) {
                val filePath = FirebaseStorage.getInstance().reference.child("profile_images")
                    .child(customerId.toString())
                var bitmap: Bitmap? = null
                try {
                    bitmap =
                        MediaStore.Images.Media.getBitmap(application.contentResolver, resultUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, QUALITY, baos)
                val dataByte = baos.toByteArray()
                val uploadTask = filePath.putBytes(dataByte)

                uploadTask.addOnSuccessListener {
                    FirebaseStorage.getInstance().reference.child("profile_images")
                        .child(customerId.toString()).downloadUrl.addOnCompleteListener { task ->
                            val newImage = HashMap<String, Any>()
                            newImage["profileImageUrl"] = task.result.toString()
                            mCustomerDatabase.updateChildren(newImage)
                        }
                }

                uploadTask.addOnFailureListener {
                    finish()
                }
            } else {
                finish()
            }

            val customerInformation = HashMap<String, Any>()
            customerInformation["name"] = name
            customerInformation["phone"] = phone
            customerInformation["birthdate"] = date
            customerInformation["gender"] = gender

            if (customerId != null) {
                mCustomerDatabase.updateChildren(customerInformation)
                finish()
            }
        }
    }

    // function to show calendar
    private fun showDatePickerDialog() {
        val date =
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val format = "dd-MM-yyyy"
                val simpleDateFormat = SimpleDateFormat(format, Locale.US)
                binding.edtBirthdate.setText(simpleDateFormat.format(calendar.time))
            }


        DatePickerDialog(
            this,
            date,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val imageUri = data?.data
            resultUri = imageUri
            if (resultUri != null) {
                binding.imgProfile.setImageURI(resultUri)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // button back
            R.id.btn_back -> onBackPressed()

            // button to show calendar
            R.id.edt_birthdate -> showDatePickerDialog()

            // button to move gallery in phone
            R.id.img_profile -> {
                Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                    startActivityForResult(this, REQUEST_CODE)
                }
            }

            // button save customer information
            R.id.btn_save -> {
                saveCustomerInformation()
                Toast.makeText(
                    this,
                    resources.getString(R.string.saving_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        private const val REQUEST_CODE = 100
        private const val QUALITY = 20
    }
}