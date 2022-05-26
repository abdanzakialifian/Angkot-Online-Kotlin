package com.transportation.kotline.customer

import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.directions.route.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.BuildConfig
import com.transportation.kotline.R
import com.transportation.kotline.customer.CustomersHistoryActivity.Companion.CUSTOMER_DATA_HISTORY
import com.transportation.kotline.databinding.ActivityDetailCustomerHistoryBinding
import com.transportation.kotline.model.CustomersHistory
import java.util.*

@Suppress("DEPRECATION")
class DetailCustomerHistoryActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDetailCustomerHistoryBinding
    private lateinit var polyLines: ArrayList<Polyline>
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var customerDataHistory: CustomersHistory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailCustomerHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initialize firebase database
        firebaseDatabase = Firebase.database

        // initialize poly lines
        polyLines = ArrayList()

        // get object from intent
        customerDataHistory = intent?.getParcelableExtra(CUSTOMER_DATA_HISTORY)

        // show bottom sheet
        BottomSheetBehavior.from(binding.layoutDetailCustomerHistory.bottomSheet)
            .apply { state = BottomSheetBehavior.STATE_EXPANDED }

        // call function to add data for customer layout
        addDataCustomerToLayout()

        // call function get rating driver
        getRatingDriver()

        // rating listener
        binding.layoutDetailCustomerHistory.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            // call function add rating to driver
            addRatingToDriver(rating)
        }
    }

    // function to add data for customer layout
    private fun addDataCustomerToLayout() {
        binding.layoutDetailCustomerHistory.apply {
            if (customerDataHistory != null) {
                customerDataHistory?.apply {
                    tvTime.text = getDate(time)
                    if (destination != "") {
                        tvDestination.text = customerDataHistory?.destination
                    } else {
                        tvDestination.text = resources.getString(R.string.no_destination)
                    }

                    if (destinationAddress != "") {
                        tvAddress.text = customerDataHistory?.destinationAddress
                    } else {
                        tvAddress.text = resources.getString(R.string.no_address)
                    }

                    if (driverImage != "") {
                        Glide.with(applicationContext)
                            .load(customerDataHistory?.driverImage)
                            .placeholder(R.drawable.ic_load_data)
                            .error(R.drawable.ic_error_load_data)
                            .into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.ic_person_32)
                    }

                    if (driverName != "") {
                        tvName.text = customerDataHistory?.driverName
                    } else {
                        tvName.text = resources.getString(R.string.no_name)
                    }

                    if (driverPhone != "") {
                        tvPhoneNumber.text = customerDataHistory?.driverPhone
                    } else {
                        tvPhoneNumber.text = resources.getString(R.string.no_phone_number)
                    }

                    val locationLatLng = LatLng(locationLat, locationLng)
                    val destinationLatLng = LatLng(destinationLat, destinationLng)

                    getRouteToMarker(locationLatLng, destinationLatLng)
                }
            }
        }
    }

    // function get rating driver
    private fun getRatingDriver() {
        customerDataHistory?.apply {
            val ratingRef =
                firebaseDatabase.reference.child("Users").child("Drivers").child(driverId)
                    .child("history").child(requestId).child("rating")
            ratingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val rating = snapshot.value
                        if (rating != null) {
                            binding.layoutDetailCustomerHistory.ratingBar.rating =
                                rating.toString().toFloat()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function add rating bar to driver
    private fun addRatingToDriver(rating: Float) {
        customerDataHistory?.apply {
            val ratingRef =
                firebaseDatabase.reference.child("Users").child("Drivers").child(driverId)
                    .child("history").child(requestId)
            // put to driver history
            val driverRating = HashMap<String, Any>()
            driverRating["rating"] = rating
            ratingRef.updateChildren(driverRating)
        }
    }

    // function to get route marker API KEY
    private fun getRouteToMarker(locationLatLng: LatLng, destinationLatLng: LatLng) {
        val routing = Routing.Builder()
            .key(BuildConfig.TEMPORARY_API_KEY)
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .alternativeRoutes(false)
            .waypoints(locationLatLng, destinationLatLng)
            .build()
        routing.execute()
    }

    // function to convert time
    private fun getDate(time: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = time * 1000
        return DateFormat.format("dd-MM-yyyy HH:mm", calendar).toString()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onRoutingFailure(e: RouteException?) {
        if (e != null) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRoutingStart() {}

    override fun onRoutingSuccess(route: ArrayList<Route>?, shortestRouteIndex: Int) {
        // zooming direction map
        val builder = LatLngBounds.builder()
        customerDataHistory?.apply {
            builder.include(LatLng(locationLat, locationLng))
            builder.include(LatLng(destinationLat, destinationLng))
            val bounds = builder.build()
            val width = resources.displayMetrics.widthPixels
            val padding = (width * 0.2).toInt()
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            mMap.animateCamera(cameraUpdate)
            mMap.addMarker(
                MarkerOptions().position(LatLng(locationLat, locationLng)).title("Your location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_user))
            )
            mMap.addMarker(
                MarkerOptions().position(LatLng(destinationLat, destinationLng)).title("Your destination")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_destination_pin))
            )
        }

        if (polyLines.isNotEmpty()) {
            for (poly in polyLines) {
                poly.remove()
            }
        }

        polyLines = ArrayList()
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
                Toast.makeText(
                    applicationContext,
                    "Route " + (i + 1) + ": distance - " + route[i].distanceValue + ": duration - " + route[i].durationValue,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRoutingCancelled() {}

    companion object {
        private val COLORS = intArrayOf(R.color.light_blue)
    }
}