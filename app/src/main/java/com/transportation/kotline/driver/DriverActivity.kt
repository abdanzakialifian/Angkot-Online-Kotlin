package com.transportation.kotline.driver

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.directions.route.*
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.BuildConfig
import com.transportation.kotline.R
import com.transportation.kotline.databinding.ActivityDriverBinding
import com.transportation.kotline.databinding.NavHeaderBinding
import com.transportation.kotline.other.ApplicationTurnedOff
import com.transportation.kotline.other.OptionActivity
import java.util.*


@Suppress("DEPRECATION")
class DriverActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener,
    View.OnClickListener {

    private lateinit var binding: ActivityDriverBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var polyLines: ArrayList<Polyline>
    private lateinit var navHeaderBinding: NavHeaderBinding
    private var mLastLocation: Location? = null
    private var destinationLatLng: LatLng? = null
    private var mLocationRequest: LocationRequest? = null
    private var pickUpMarker: Marker? = null
    private var assignedCustomerPickupLocationRef: DatabaseReference? = null
    private var assignedCustomerPickupLocationRefListener: ValueEventListener? = null
    private var driverName: String? = null
    private var driverImage: String? = null
    private var driverEmail: String? = null
    private var driverPhone: String? = null
    private var customerName: String? = null
    private var customerImage: String? = null
    private var customerPhone: String? = null
    private var customerDestination: String? = null
    private var destinationAddress: String? = null
    private var pickUpLatLng: LatLng? = null
    private var timer: Timer? = null
    private var customerId: String = ""
    private var trayek: String = ""
    private var isZoomUpdate = false
    private var isChecked = false
    private var isGetPositionCustomersStarted = false
    private var backPressedTime = 0L
    private var status = 0
    private var markerList: ArrayList<Marker> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val header = binding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(header)

        // custom action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // initialize navigation drawer
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                    Intent(this, DriverProfileActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                R.id.working -> {
                    val switchWorking = findViewById<SwitchMaterial>(R.id.switch_working)
                    if (isChecked) {
                        isChecked = false
                        switchWorking.isChecked = false
                        disconnectDriver()
                    } else {
                        isChecked = true
                        switchWorking.isChecked = true
                        connectDriver()
                    }
                }
                // move page driver history
                R.id.history -> {
                    Intent(this, DriversHistoryActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                // logout from application
                R.id.logout -> alertLogout()
            }
            true
        }

        // initialize google sign client and declare google sign in options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id_auth))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        // initialize the firebase database
        firebaseDatabase = Firebase.database

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        // check permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            mapFragment.getMapAsync(this)
        }

        // initialize fused location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // initialize poly lines
        polyLines = ArrayList()

        // hidden bottom sheet
        BottomSheetBehavior.from(binding.customBackgroundLayoutDriver.bottomSheet).apply {
            peekHeight = 100
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // add event click to button
        binding.customBackgroundLayoutDriver.btnRideStatus.setOnClickListener(this)

        // call function get assigned customer
        getAssignedCustomer()

        // call function get driver profile in navigation drawer
        getDriverProfile()

        // call class is application turned off
        startService(Intent(this, ApplicationTurnedOff::class.java))
    }

    // function to get driver profile
    private fun getDriverProfile() {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val driverRef = firebaseDatabase.reference.child("Users").child("Drivers")
                .child(driverId.toString())
            driverRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        if (map?.get("profileImageUrl") != null) {
                            val imageProfile = navHeaderBinding.imgProfileNavHeader
                            driverImage = map["profileImageUrl"].toString()
                            Glide.with(this@DriverActivity)
                                .load(driverImage)
                                .placeholder(R.drawable.ic_load_data)
                                .error(R.drawable.ic_error_load_data)
                                .into(imageProfile)
                        } else {
                            val imageProfile = navHeaderBinding.imgProfileNavHeader
                            imageProfile.setImageResource(R.drawable.ic_default_image)
                        }

                        if (map?.get("name") != null) {
                            val tvName = navHeaderBinding.tvName
                            driverName = map["name"].toString()
                            tvName.text = driverName
                        } else {
                            driverName = null
                            val tvName = navHeaderBinding.tvName
                            tvName.text = resources.getString(R.string.no_name)
                        }

                        if (map?.get("email") != null) {
                            val tvEmail = navHeaderBinding.tvEmail
                            driverEmail = map["email"].toString()
                            tvEmail.text = driverEmail
                        } else {
                            driverEmail = null
                            val tvEmail = navHeaderBinding.tvEmail
                            tvEmail.text = resources.getString(R.string.no_email)
                        }

                        driverPhone = if (map?.get("phone") != null) {
                            map["phone"].toString()
                        } else {
                            null
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // location callback
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                // location periodically
                mLastLocation = location

                if (!isZoomUpdate) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 16F)
                    )
                    isZoomUpdate = true
                }

                val driverId = firebaseAuth.currentUser?.uid
                val refAvailable = firebaseDatabase.getReference("DriversAvailable")
                val refWorking = firebaseDatabase.getReference("DriversWorking")
                val geoFireAvailable = GeoFire(refAvailable)
                val geoFireWorking = GeoFire(refWorking)
                // get status driver
                when (customerId) {
                    "" -> {
                        // add location to child driver id in Drivers Available firebase database
                        if (mLastLocation != null && driverId != null) {
                            geoFireWorking.removeLocation(driverId)
                            geoFireAvailable.setLocation(
                                driverId,
                                GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude)
                            )
                        }
                    }
                    else -> {
                        // add location to child driver id in Drivers Working firebase database
                        if (mLastLocation != null && driverId != null) {
                            geoFireAvailable.removeLocation(driverId)
                            geoFireWorking.setLocation(
                                driverId,
                                GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude)
                            )
                        }
                    }
                }

                if (!isGetPositionCustomersStarted) {
                    getCustomersAround()
                }
            }
        }
    }

    // get assigned customer
    private fun getAssignedCustomer() {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val assignedCustomerRef =
                firebaseDatabase.reference.child("Users").child("Drivers").child(driverId)
                    .child("customerRequest")
            assignedCustomerRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // initialization variable status
                        status = if (status < 1) {
                            1
                        } else {
                            2
                        }

                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        if (map?.get("customerRideId") != null) {
                            customerId = map["customerRideId"].toString()
                        }

                        if (map?.get("destinationLat") != null && map["destinationLng"] != null) {
                            val destinationLat = map["destinationLat"].toString()
                            val destinationLng = map["destinationLng"].toString()
                            destinationLatLng =
                                LatLng(destinationLat.toDouble(), destinationLng.toDouble())
                        }

                        // call function customer pickup location
                        getAssignedCustomerPickupLocation()

                        // call function customer information
                        getAssignedCustomerInformation()

                        // call function customer destination and message
                        getAssignedCustomerDestinationAndMessage()

                        // call function trayek driver
                        getTrayekDriver()

                        // show bottom sheet
                        BottomSheetBehavior.from(binding.customBackgroundLayoutDriver.bottomSheet)
                            .apply {
                                state = BottomSheetBehavior.STATE_EXPANDED
                            }
                    } else {
                        // call the function end ride
                        endRide()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // get customer pickup location
    private fun getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef =
            firebaseDatabase.reference.child("CustomersRequest").child(customerId)
                .child("l")
        assignedCustomerPickupLocationRefListener =
            assignedCustomerPickupLocationRef?.addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val map: List<Any?> = snapshot.value as List<Any?>
                        var locationLat = 0.0
                        var locationLng = 0.0

                        if (map[0] != null) {
                            locationLat = map[0].toString().toDouble()
                        }
                        if (map[1] != null) {
                            locationLng = map[1].toString().toDouble()
                        }

                        pickUpLatLng = LatLng(locationLat, locationLng)
                        if (pickUpLatLng != null) {

                            // call function to get route to customer
                            getRouteToMarker(pickUpLatLng)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // function to getting customer information
    private fun getAssignedCustomerInformation() {
        val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
        val mCustomerDatabase = firebaseDatabase.reference.child("Users").child("Customers")
            .child(customerId)
        mCustomerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.childrenCount > 0) {

                    val map: Map<String?, Any?>? = snapshot.getValue(gti)

                    binding.customBackgroundLayoutDriver.apply {

                        // hidden text and show content
                        imgProfileCustomer.visibility = View.VISIBLE
                        layoutName.visibility = View.VISIBLE
                        layoutPhone.visibility = View.VISIBLE
                        tvNoOrders.visibility = View.GONE

                        if (map?.get("profileImageUrl") != null) {
                            customerImage = map["profileImageUrl"].toString()
                            Glide.with(applicationContext)
                                .load(customerImage)
                                .placeholder(R.drawable.ic_load_data)
                                .error(R.drawable.ic_error_load_data)
                                .into(imgProfileCustomer)
                        } else {
                            customerImage = null
                            imgProfileCustomer.setImageResource(R.drawable.ic_person_32)
                        }

                        if (map?.get("name") != null) {
                            customerName = map["name"].toString()
                            tvNameCustomer.text = customerName
                        } else {
                            customerName = null
                            tvNameCustomer.text = resources.getString(R.string.no_name)
                        }

                        if (map?.get("phone") != null) {
                            customerPhone = map["phone"].toString()
                            tvPhoneNumberCustomer.text = customerPhone
                        } else {
                            customerPhone = null
                            tvPhoneNumberCustomer.text =
                                resources.getString(R.string.no_phone_number)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // function to get all customers position in around
    fun getCustomersAround() {
        isGetPositionCustomersStarted = true
        val customersPosition = firebaseDatabase.reference.child("CustomersPosition")

        if (mLastLocation != null) {
            val geoFire = GeoFire(customersPosition)
            val geoQuery = geoFire.queryAtLocation(
                GeoLocation(
                    mLastLocation!!.latitude,
                    mLastLocation!!.longitude
                ), RADIUS
            )

            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    for (markerIt in markerList) {
                        if (markerIt.tag == key) return
                    }
                    customerPosition(key, location)
                }

                override fun onKeyExited(key: String?) {
                    for (markerIt in markerList) {
                        if (markerIt.tag == key) {
                            markerIt.remove()
                            markerList.remove(markerIt)
                            return
                        }
                    }
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    for (marketIt in markerList) {
                        if (marketIt.tag == key) {
                            if (location != null) {
                                marketIt.position = LatLng(location.latitude, location.longitude)
                            }
                        }
                    }
                }

                override fun onGeoQueryReady() {}

                override fun onGeoQueryError(error: DatabaseError?) {}
            })
        }
    }

    // function to get customer position marker
    private fun customerPosition(key: String?, location: GeoLocation?) {
        if (location != null && key != null) {
            val customersLocation = LatLng(location.latitude, location.longitude)

            val customerRef =
                firebaseDatabase.reference.child("Users").child("Customers")
                    .child(key)
            customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value.toString()

                        val mCustomerMarker = mMap.addMarker(
                            MarkerOptions().position(customersLocation)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person))
                                .title(name)
                        )
                        mCustomerMarker?.tag = key
                        markerList.add(mCustomerMarker as Marker)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to get destination and message customer
    private fun getAssignedCustomerDestinationAndMessage() {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val assignedCustomerRef =
                firebaseDatabase.reference.child("Users").child("Drivers").child(driverId)
                    .child("customerRequest")
            assignedCustomerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        binding.customBackgroundLayoutDriver.apply {
                            layoutDestination.visibility = View.VISIBLE
                            layoutAddress.visibility = View.VISIBLE
                            btnRideStatus.visibility = View.VISIBLE

                            if (map?.get("destination") != null) {
                                customerDestination = map["destination"].toString()
                                tvDestinationCustomer.text = customerDestination
                            } else {
                                customerDestination = null
                                tvDestinationCustomer.text =
                                    resources.getString(R.string.no_destination)
                            }

                            if (map?.get("address") != null) {
                                destinationAddress = map["address"].toString()
                                tvAddressCustomer.text = destinationAddress
                            } else {
                                destinationAddress = null
                                tvAddressCustomer.text = resources.getString(R.string.no_address)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to get value in trayek driver
    private fun getTrayekDriver() {
        val driverId = firebaseAuth.currentUser?.uid
        if (driverId != null) {
            val driverRef =
                firebaseDatabase.reference.child("Users").child("Drivers").child(driverId)
                    .child("trayek")
            driverRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trayek = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to get route marker API KEY
    private fun getRouteToMarker(pickUpLatLng: LatLng?) {
        if (pickUpLatLng != null && mLastLocation != null) {
            val routing = Routing.Builder()
                .key(BuildConfig.TEMPORARY_API_KEY)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(
                    LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude),
                    pickUpLatLng
                )
                .build()
            routing.execute()
        }
    }

    // function to delete route polylines
    private fun erasePolyLines() {
        for (line in polyLines) {
            line.remove()
        }
        polyLines.clear()
    }

    // function to end ride
    private fun endRide() {
        status = 0

        binding.customBackgroundLayoutDriver.btnRideStatus.text =
            resources.getString(R.string.picked_customer)

        // call function to delete route polyLines
        erasePolyLines()

        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val driverRef = firebaseDatabase.reference.child("Users").child("Drivers").child(userId)
                .child("customerRequest")
            driverRef.removeValue()
        }

        // remove customer request
        val ref = firebaseDatabase.getReference("CustomersRequest")
        val geofire = GeoFire(ref)
        geofire.removeLocation(customerId)
        customerId = ""

        // remove pickup marker
        pickUpMarker?.remove()

        // remove listener
        assignedCustomerPickupLocationRef?.removeEventListener(
            assignedCustomerPickupLocationRefListener as ValueEventListener
        )

        binding.customBackgroundLayoutDriver.apply {
            // hidden bottom sheet
            BottomSheetBehavior.from(bottomSheet).apply {
                peekHeight = 100
                state = BottomSheetBehavior.STATE_COLLAPSED
            }

            // hidden content and show text if no request
            imgProfileCustomer.visibility = View.GONE
            layoutName.visibility = View.GONE
            layoutPhone.visibility = View.GONE
            layoutDestination.visibility = View.GONE
            layoutAddress.visibility = View.GONE
            btnRideStatus.visibility = View.GONE
            tvNoOrders.visibility = View.VISIBLE
        }
    }

    // function to record ride
    private fun recordRide() {
        val driverId = firebaseAuth.currentUser?.uid

        if (driverId != null) {
            val driverRef =
                firebaseDatabase.reference.child("Users").child("Drivers")
                    .child(driverId)
                    .child("history")
            val customerRef =
                firebaseDatabase.reference.child("Users").child("Customers")
                    .child(customerId)
                    .child("history")
            val historyRef =
                firebaseDatabase.reference.child("History").child(trayek)
            val requestId = historyRef.push().key

            if (requestId != null) {

                // put to driver history
                val driverHistory = HashMap<String, Any>()
                driverHistory["requestId"] = requestId
                driverHistory["customerId"] = customerId
                driverHistory["driverId"] = driverId
                if (customerDestination != null) {
                    driverHistory["customerDestination"] = customerDestination.toString()
                } else {
                    driverHistory["customerDestination"] =
                        resources.getString(R.string.no_destination)
                }
                if (destinationAddress != null) {
                    driverHistory["destinationAddress"] = destinationAddress.toString()
                } else {
                    driverHistory["destinationAddress"] = resources.getString(R.string.no_address)
                }
                if (customerName != null) {
                    driverHistory["customerName"] = customerName.toString()
                } else {
                    driverHistory["customerName"] = resources.getString(R.string.no_name)
                }
                if (customerImage != null) {
                    driverHistory["customerImage"] = customerImage.toString()
                }
                if (customerPhone != null) {
                    driverHistory["customerPhone"] = customerPhone.toString()
                } else {
                    driverHistory["customerPhone"] = resources.getString(R.string.no_phone_number)
                }
                if (mLastLocation != null) {
                    driverHistory["locationLat"] = mLastLocation!!.latitude
                    driverHistory["locationLng"] = mLastLocation!!.longitude
                } else {
                    driverHistory["locationLat"] = 0.0
                    driverHistory["locationLng"] = 0.0
                }
                if (destinationLatLng != null) {
                    driverHistory["destinationLat"] = destinationLatLng!!.latitude
                    driverHistory["destinationLng"] = destinationLatLng!!.longitude
                } else {
                    driverHistory["destinationLat"] = 0.0
                    driverHistory["destinationLng"] = 0.0
                }
                driverHistory["time"] = getCurrentTimestamp()
                driverHistory["rating"] = 0
                driverRef.child(requestId.toString()).updateChildren(driverHistory)

                // put to customer history
                val customerHistory = HashMap<String, Any>()
                customerHistory["requestId"] = requestId
                customerHistory["driverId"] = driverId
                if (customerDestination != null) {
                    customerHistory["destination"] = customerDestination.toString()
                } else {
                    customerHistory["destination"] =
                        resources.getString(R.string.no_destination)
                }
                if (destinationAddress != null) {
                    customerHistory["destinationAddress"] = destinationAddress.toString()
                } else {
                    customerHistory["destinationAddress"] =
                        resources.getString(R.string.no_address)
                }
                if (driverName != null) {
                    customerHistory["driverName"] = driverName.toString()
                } else {
                    customerHistory["driverName"] =
                        resources.getString(R.string.no_name)
                }
                if (driverImage != null) {
                    customerHistory["driverImage"] = driverImage.toString()
                }
                if (driverPhone != null) {
                    customerHistory["driverPhone"] = driverPhone.toString()
                } else {
                    customerHistory["driverPhone"] =
                        resources.getString(R.string.no_phone_number)
                }
                if (pickUpLatLng != null) {
                    customerHistory["locationLat"] = pickUpLatLng!!.latitude
                    customerHistory["locationLng"] = pickUpLatLng!!.longitude
                } else {
                    customerHistory["locationLat"] = 0.0
                    customerHistory["locationLng"] = 0.0
                }
                if (destinationLatLng != null) {
                    customerHistory["destinationLat"] = destinationLatLng!!.latitude
                    customerHistory["destinationLng"] = destinationLatLng!!.longitude
                } else {
                    customerHistory["destinationLat"] = 0.0
                    customerHistory["destinationLng"] = 0.0
                }
                customerHistory["time"] = getCurrentTimestamp()
                customerRef.child(requestId.toString()).updateChildren(customerHistory)

                // put to history
                val history = HashMap<String, Any>()
                history["driver"] = driverId
                history["customer"] = customerId
                historyRef.child(requestId.toString()).updateChildren(history)
            }
        }
    }

    // function to get current time
    private fun getCurrentTimestamp(): Long = System.currentTimeMillis() / 1000

    private fun alertLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.logout)
        builder.setIcon(R.drawable.ic_logout_32)
        builder.setMessage(R.string.are_you_sure)

        builder.setPositiveButton("Ya") { _, _ ->
            googleSignInClient.revokeAccess()
                .addOnCompleteListener {
                    disconnectDriver()
                    Intent(this, OptionActivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }
        }

        builder.setNegativeButton("Tidak") { _, _ ->

        }
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    // function connect driver
    private fun connectDriver() {
        mLocationRequest = LocationRequest.create().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }

        if (mLocationRequest != null) {
            fusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest as LocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )
        }
    }

    // function disconnect driver
    private fun disconnectDriver() {
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)

        val driverId = FirebaseAuth.getInstance().currentUser?.uid

        val driverAvailableRef = firebaseDatabase.getReference("DriversAvailable")
        val geoFireAvailable = GeoFire(driverAvailableRef)
        if (driverId != null) {
            geoFireAvailable.removeLocation(driverId)
        }
        val driverWorkingRef = firebaseDatabase.getReference("DriversWorking")
        val geoFireWorking = GeoFire(driverWorkingRef)
        if (driverId != null) {
            geoFireWorking.removeLocation(driverId)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }

        mMap.isMyLocationEnabled = true
    }

    override fun onRoutingFailure(e: RouteException?) {}

    override fun onRoutingStart() {}

    override fun onRoutingSuccess(route: ArrayList<Route>?, shortestRouteIndex: Int) {
        if (polyLines.isNotEmpty()) {
            for (poly in polyLines) {
                poly.remove()
            }
        }

        polyLines = ArrayList()
        //add route(s) to the map.
        //add route(s) to the map.
        if (route != null) {
            for (i in 0 until route.size) {

                //In case of more than 5 alternative routes
                val colorIndex: Int = i % COLORS.size
                val polyOptions = PolylineOptions()
                polyOptions.color(ContextCompat.getColor(applicationContext, COLORS[colorIndex]))
                polyOptions.width((10 + i * 3).toFloat())
                polyOptions.addAll(route[i].points)
                val polyline: Polyline = mMap.addPolyline(polyOptions)
                polyLines.add(polyline)
            }
        }
    }

    override fun onRoutingCancelled() {}

    // activation drawer navigation
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_ride_status) {
            // check status
            when (status) {
                1 -> {
                    val driverId = firebaseAuth.currentUser?.uid
                    val driverRef =
                        firebaseDatabase.reference.child("Users").child("Drivers")
                            .child(driverId.toString()).child("customerRequest")
                    val customerRequest = HashMap<String, Any>()
                    customerRequest["isPicked"] = true
                    driverRef.updateChildren(customerRequest)

                    binding.customBackgroundLayoutDriver.btnRideStatus.text =
                        resources.getString(R.string.drive_completed)

                    // initialization variable status
                    status = 2
                }
                2 -> {
                    // call function to record history
                    recordRide()
                    // call function to ended ride
                    endRide()
                }
            }
        }
    }

    // Get GPS location permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this)
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.please_provide_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // double click button back to exit app
    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, resources.getString(R.string.leave_the_app), Toast.LENGTH_LONG)
                .show()
        }
        backPressedTime = System.currentTimeMillis()
    }

//    override fun onPause() {
//        super.onPause()
//
//        timer = Timer()
//        val logoutTimeTaskAvailable = LogOutTimerTask(googleSignInClient, "DriversAvailable")
//        timer!!.scheduleAtFixedRate(logoutTimeTaskAvailable, 60000L, 5000L)
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        if (timer != null) {
//            timer!!.cancel()
//            timer = null
//        }
//    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private val COLORS = intArrayOf(R.color.light_blue)
        private const val RADIUS = 1.0
    }
}