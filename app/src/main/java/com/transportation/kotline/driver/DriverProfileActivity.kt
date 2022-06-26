package com.transportation.kotline.driver

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
import com.transportation.kotline.databinding.ActivityDriverProfileBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class DriverProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDriverProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mDriverDatabase: DatabaseReference
    private lateinit var calendar: Calendar
    private var driverId: String? = null
    private var resultUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hide action bar
        supportActionBar?.hide()

        // initialize calendar
        calendar = Calendar.getInstance()

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // initialize driver id
        driverId = firebaseAuth.currentUser?.uid

        // initialize driver database
        mDriverDatabase = firebaseDatabase.reference.child("Users").child("Drivers")
            .child(driverId.toString())

        // call function to get driver information
        getDriverInformation()

        // add event click to button
        binding.apply {
            btnBack.setOnClickListener(this@DriverProfileActivity)
            edtBirthdate.setOnClickListener(this@DriverProfileActivity)
            btnSave.setOnClickListener(this@DriverProfileActivity)
            imgProfile.setOnClickListener(this@DriverProfileActivity)
            // default radio button
            rgTrayek.check(R.id.rb_a)
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
                    val vehiclePlate = edtVehiclePlate.text.toString()
                    val numberTransportation = edtTransportationNumber.text.toString()

                    if (name.isNotEmpty() && birthDate.isNotEmpty() && phone.isNotEmpty() && vehiclePlate.isNotEmpty() && numberTransportation.isNotEmpty()) {
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
            edtVehiclePlate.addTextChangedListener(registerTextWatcher)
            edtTransportationNumber.addTextChangedListener(registerTextWatcher)
        }
    }

    // function to getting data driver
    private fun getDriverInformation() {
        // view loading bar
        showLoading(true)
        if (driverId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            mDriverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        // hide loading bar
                        showLoading(false)
                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        binding.apply {
                            if (map?.get("profileImageUrl") != null) {
                                val image = map["profileImageUrl"].toString()
                                Glide.with(this@DriverProfileActivity)
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

                            if (map?.get("vehiclePlate") != null) {
                                val vehiclePlate = map["vehiclePlate"].toString()
                                edtVehiclePlate.setText(vehiclePlate)
                            }

                            if (map?.get("numberTransportation") != null) {
                                val vehiclePlate = map["numberTransportation"].toString()
                                edtTransportationNumber.setText(vehiclePlate)
                            }

                            if (map?.get("gender") != null) {
                                when (map["gender"].toString()) {
                                    "Pria" -> rgGender.check(R.id.rb_male)
                                    "Wanita" -> rgGender.check(R.id.rb_female)
                                }
                            }

                            if (map?.get("trayek") != null) {
                                when (map["trayek"].toString()) {
                                    "A" -> rgTrayek.check(R.id.rb_a)
                                    "B" -> rgTrayek.check(R.id.rb_b)
                                    "C" -> rgTrayek.check(R.id.rb_c)
                                    "D" -> rgTrayek.check(R.id.rb_d)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to saving data driver
    private fun saveDriverInformation() {
        binding.apply {
            val name = edtName.text.toString()
            val phone = edtPhone.text.toString().trim()
            val date = edtBirthdate.text.toString()
            val vehiclePlate = edtVehiclePlate.text.toString()
            val numberTransportation = edtTransportationNumber.text.toString()

            // radio button gender
            val selectIdGender = rgGender.checkedRadioButtonId
            val radioGender = findViewById<RadioButton>(selectIdGender)
            if (radioGender.text == null) {
                return
            }
            val gender = radioGender.text.toString()

            // radio button trayek
            val selectedIdTrayek = rgTrayek.checkedRadioButtonId
            val radioTrayek = findViewById<RadioButton>(selectedIdTrayek)
            if (radioTrayek.text == null) {
                return
            }
            val trayek = radioTrayek.text.toString()

            // add image profile
            if (resultUri != null && driverId != null) {
                val filePath = FirebaseStorage.getInstance().reference.child("profile_images")
                    .child(driverId.toString())
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
                        .child(driverId.toString()).downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val newImage = HashMap<String, Any>()
                                newImage["profileImageUrl"] = task.result.toString()
                                mDriverDatabase.updateChildren(newImage)
                            }
                        }
                }

                uploadTask.addOnFailureListener {
                    finish()
                }
            }

            val driverInformation = HashMap<String, Any>()
            driverInformation["name"] = name
            driverInformation["phone"] = phone
            driverInformation["birthdate"] = date
            driverInformation["vehiclePlate"] = vehiclePlate
            driverInformation["gender"] = gender
            driverInformation["trayek"] = trayek
            driverInformation["numberTransportation"] = numberTransportation

            if (driverId != null) {
                mDriverDatabase.updateChildren(driverInformation).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@DriverProfileActivity,
                            resources.getString(R.string.saving_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@DriverProfileActivity,
                            resources.getString(R.string.saving_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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

            // button save driver information
            R.id.btn_save -> {
                saveDriverInformation()
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