package com.transportation.kotline.customer

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.BuildConfig
import com.transportation.kotline.R
import com.transportation.kotline.databinding.ActivityCustomerBinding
import com.transportation.kotline.databinding.NavHeaderBinding
import com.transportation.kotline.other.ApplicationTurnedOff
import com.transportation.kotline.other.DummyTrayek
import com.transportation.kotline.other.LogOutTimerTask
import com.transportation.kotline.other.OptionActivity
import java.util.*

class CustomerActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var binding: ActivityCustomerBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var navHeaderBinding: NavHeaderBinding
    private var mDriverMarker: Marker? = null
    private var mLastLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private var pickUpLocation: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var driverFoundId: String? = null
    private var geoQuery: GeoQuery? = null
    private var driverLocationRef: DatabaseReference? = null
    private var driverLocationRefListener: ValueEventListener? = null
    private var driveHasEndedRef: DatabaseReference? = null
    private var driveHasEndedRefListener: ValueEventListener? = null
    private var pickUpMarker: Marker? = null
    private var destination: String? = null
    private var address: String? = null
    private var requestTrayekA: String? = null
    private var requestTrayekB: String? = null
    private var requestTrayekC: String? = null
    private var requestTrayekD: String? = null
    private var timer: Timer? = null
    private var isCurrentPosition = false
    private var isDestinationTrayekA = false
    private var isDestinationTrayekB = false
    private var isDestinationTrayekC = false
    private var isDestinationTrayekD = false
    private var isTransitInPasarWage = false
    private var isTransitInGayam = false
    private var isTransitInRejasa = false
    private var isTransitInKantorPos = false
    private var driverFound = false
    private var isZoomUpdate = false
    private var isRequestAngkot = false
    private var isGetDriversAroundStarted = false
    private var radius = 0.0
    private var backPressedTime = 0L
    private var markerList: ArrayList<Marker> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
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
                // move page customer profile activity
                R.id.profile -> {
                    Intent(this, CustomerProfileActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                // move page customer history
                R.id.history -> {
                    Intent(this, CustomersHistoryActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                R.id.trayek -> {
                    Intent(this, RouteInformationContainerActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                // show alert dialog logout
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

        // initialize places API KEY
        Places.initialize(applicationContext, BuildConfig.TEMPORARY_API_KEY)
        Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destination = place.name
                address = place.address
                destinationLatLng = place.latLng
            }

            override fun onError(status: Status) {}
        })

        // create bottom sheet
        BottomSheetBehavior.from(binding.customBackgroundLayoutCustomer.bottomSheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // add event click to button request
        binding.customBackgroundLayoutCustomer.btnRequestAngkot.setOnClickListener(this@CustomerActivity)

        // call function get customer profile in navigation drawer
        getCustomerProfile()

        // call class is application turned off
        startService(Intent(this, ApplicationTurnedOff::class.java))

        // dummy
        binding.btnTrayek.setOnClickListener {
            val dstnLatLng = LatLng(-7.378072416486406, 109.74332625986958)

            val getTrayekA = DummyTrayek.getTrayekAngkotA(dstnLatLng, mLastLocation)
            requestTrayekA = getTrayekA
            val getDestinationA = DummyTrayek.getTrayekDestinationA(dstnLatLng)
            isDestinationTrayekA = getDestinationA
//            getTrayekAngkotA(dstnLatLng)
            getTrayekAngkotB(dstnLatLng)
            getTrayekAngkotC(dstnLatLng)
            getTrayekAngkotD(dstnLatLng)

            checkTrayekAgkot()

            binding.customBackgroundLayoutCustomer.apply {
                tvCurrentRecommendation.visibility = View.VISIBLE
                layoutCurrentTrayek.visibility = View.VISIBLE
                layoutTransit.visibility = View.VISIBLE
                layoutNextTrayek.visibility = View.VISIBLE
            }

            isCurrentPosition = false

            // create bottom sheet
            BottomSheetBehavior.from(binding.customBackgroundLayoutCustomer.bottomSheet).apply {
                peekHeight = 100
                this.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        createNotificationChannel()
    }

//    private fun getTrayekAngkotA(destination: LatLng?) {
//        val destinationCustomer = Location("")
//        destinationCustomer.latitude = destination?.latitude ?: 0.0
//        destinationCustomer.longitude = destination?.longitude ?: 0.0
//
//        val customerLocation = Location("")
//        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
//        customerLocation.longitude = mLastLocation?.longitude ?: 0.0
//
//        val jlVeteran = Location("")
//        jlVeteran.latitude = -7.394799033719521
//        jlVeteran.longitude = 109.700762915669
//
//        val jlBrengkok = Location("")
//        jlBrengkok.latitude = -7.396415762365111
//        jlBrengkok.longitude = 109.70093516506572
//
//        val jlCampurSalam = Location("")
//        jlCampurSalam.latitude = -7.3966743624237745
//        jlCampurSalam.longitude = 109.69889089680727
//
//        val jlLetjendSuprapto = Location("")
//        jlLetjendSuprapto.latitude = -7.398083949789377
//        jlLetjendSuprapto.longitude = 109.69292715129609
//
//        val jlMantrianom = Location("")
//        jlMantrianom.latitude = -7.398732293120247
//        jlMantrianom.longitude = 109.6283702594063
//
//        val jlJendSoedirman = Location("")
//        jlJendSoedirman.latitude = -7.405499073713895
//        jlJendSoedirman.longitude = 109.60576501924116
//
//        val jlMtHaryono = Location("")
//        jlMtHaryono.latitude = -7.3955108893575945
//        jlMtHaryono.longitude = 109.69891450257482
//
//        val jlMayjendSoetojo = Location("")
//        jlMayjendSoetojo.latitude = -7.392973866382533
//        jlMayjendSoetojo.longitude = 109.69908275635157
//
//        val jlLetnanKarjono = Location("")
//        jlLetnanKarjono.latitude = -7.393295474003801
//        jlLetnanKarjono.longitude = 109.7006343897887
//
//        val jlStadion = Location("")
//        jlStadion.latitude = -7.39199315369452
//        jlStadion.longitude = 109.70438694635027
//
//        val terminalBus = Location("")
//        terminalBus.latitude = -7.392655920315736
//        terminalBus.longitude = 109.70482103417467
//
//        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
//        val customerToJlBrengkok = customerLocation.distanceTo(jlBrengkok) / 1000
//        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
//        val customerToJlLetjendSuprapto = customerLocation.distanceTo(jlLetjendSuprapto) / 1000
//        val customerToJlMantrianom = customerLocation.distanceTo(jlMantrianom) / 1000
//        val customerToJlJendSoedirman = customerLocation.distanceTo(jlJendSoedirman) / 1000
//        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000
//        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
//        val customerToJlLetnanKarjono = customerLocation.distanceTo(jlLetnanKarjono) / 1000
//        val customerToJlStadion = customerLocation.distanceTo(jlStadion) / 1000
//        val customerToTerminal = customerLocation.distanceTo(terminalBus) / 1000
//
//        if (customerToJlVeteran in 0F.rangeTo(0.17980675F) && customerToJlBrengkok in 0F.rangeTo(
//                0.17980675F
//            ) || customerToJlBrengkok in 0F.rangeTo(0.22749068F) && customerToJlCampurSalam in 0F.rangeTo(
//                0.22749068F
//            ) || customerToJlCampurSalam in 0F.rangeTo(0.67659587F) && customerToJlLetjendSuprapto in 0F.rangeTo(
//                0.67659587F
//            ) || customerToJlLetjendSuprapto in 0F.rangeTo(7.1273675F) && customerToJlMantrianom in 0F.rangeTo(
//                7.1273675F
//            ) || customerToJlMantrianom in 0F.rangeTo(2.6053631F) && customerToJlJendSoedirman in 0F.rangeTo(
//                2.6053631F
//            ) || customerToJlCampurSalam in 0F.rangeTo(0.12869798F) && customerToJlMtHaryono in 0F.rangeTo(
//                0.12869798F
//            ) || customerToJlMtHaryono in 0F.rangeTo(0.28119034F) && customerToJlMayjendSoetojo in 0F.rangeTo(
//                0.28119034F
//            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.17495409F) && customerToJlLetnanKarjono in 0F.rangeTo(
//                0.17495409F
//            ) || customerToJlLetnanKarjono in 0F.rangeTo(0.43860513F) && customerToJlStadion in 0F.rangeTo(
//                0.43860513F
//            ) || customerToJlStadion in 0F.rangeTo(0.08757355F) && customerToTerminal in 0F.rangeTo(
//                0.08757355F
//            )
//        ) {
//            requestTrayekA = "A"
//        } else {
//            requestTrayekA = null
//        }
//
//        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
//        val destinationToJlBrengkok = destinationCustomer.distanceTo(jlBrengkok) / 1000
//        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
//        val destinationToJlLetjendSuprapto =
//            destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
//        val destinationToJlMantrianom = destinationCustomer.distanceTo(jlMantrianom) / 1000
//        val destinationToJlJendSoedirman = destinationCustomer.distanceTo(jlJendSoedirman) / 1000
//        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
//        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
//        val destinationToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
//        val destinationToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
//        val destinationToTerminal = destinationCustomer.distanceTo(terminalBus) / 1000
//
//        isDestinationTrayekA =
//            destinationToJlVeteran in 0F.rangeTo(0.17980675F) && destinationToJlBrengkok in 0F.rangeTo(
//                0.17980675F
//            ) || destinationToJlBrengkok in 0F.rangeTo(0.22749068F) && destinationToJlCampurSalam in 0F.rangeTo(
//                0.22749068F
//            ) || destinationToJlCampurSalam in 0F.rangeTo(0.67659587F) && destinationToJlLetjendSuprapto in 0F.rangeTo(
//                0.67659587F
//            ) || destinationToJlLetjendSuprapto in 0F.rangeTo(7.1273675F) && destinationToJlMantrianom in 0F.rangeTo(
//                7.1273675F
//            ) || destinationToJlMantrianom in 0F.rangeTo(2.6053631F) && destinationToJlJendSoedirman in 0F.rangeTo(
//                2.6053631F
//            ) || destinationToJlCampurSalam in 0F.rangeTo(0.12869798F) && destinationToJlMtHaryono in 0F.rangeTo(
//                0.12869798F
//            ) || destinationToJlMtHaryono in 0F.rangeTo(0.28119034F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
//                0.28119034F
//            ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.17495409F) && destinationToJlLetnanKarjono in 0F.rangeTo(
//                0.17495409F
//            ) || destinationToJlLetnanKarjono in 0F.rangeTo(0.43860513F) && destinationToJlStadion in 0F.rangeTo(
//                0.43860513F
//            ) || destinationToJlStadion in 0F.rangeTo(0.08757355F) && destinationToTerminal in 0F.rangeTo(
//                0.08757355F
//            )
//    }

    private fun getTrayekAngkotB(destination: LatLng?) {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.393286634547339
        jlVeteran.longitude = 109.70061167423222

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.391957814455376
        jlLetnanKarjono.longitude = 109.70436277113605

        val jlStadion = Location("")
        jlStadion.latitude = -7.393062008311444
        jlStadion.longitude = 109.70493209729725

        val jlAjibarangSecang = Location("")
        jlAjibarangSecang.latitude = -7.3944717509148825
        jlAjibarangSecang.longitude = 109.70561317241973

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929677430276355
        jlMayjendSoetojo.longitude = 109.69907814958101

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392679466782409
        jlGotongRoyong.longitude = 109.69252086138916

        val jlAlMunawwaroh = Location("")
        jlAlMunawwaroh.latitude = -7.392585836782173
        jlAlMunawwaroh.longitude = 109.6900924200438

        val jlPasarWage = Location("")
        jlPasarWage.latitude = -7.399586906299058
        jlPasarWage.longitude = 109.68603285296724

        val jlBrengkok = Location("")
        jlBrengkok.latitude = -7.397107412864937
        jlBrengkok.longitude = 109.69303324895357

        val jlSingamerta = Location("")
        jlSingamerta.latitude = -7.39133610612604
        jlSingamerta.longitude = 109.74286100424477

        val jlMadukara = Location("")
        jlMadukara.latitude = -7.379043887211778
        jlMadukara.longitude = 109.74609622769049

        val jlRejasa = Location("")
        jlRejasa.latitude = -7.387036588738758
        jlRejasa.longitude = 109.690708412322

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.392681363604244
        jlSunanGripit.longitude = 109.69503606555965

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlLetnanKarjono = customerLocation.distanceTo(jlLetnanKarjono) / 1000
        val customerToJlStadion = customerLocation.distanceTo(jlStadion) / 1000
        val customerToJlAjibarangSecang = customerLocation.distanceTo(jlAjibarangSecang) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlGotongRoyong = customerLocation.distanceTo(jlGotongRoyong) / 1000
        val customerToJlAlMunawwaroh = customerLocation.distanceTo(jlAlMunawwaroh) / 1000
        val customerToJlPasarWage = customerLocation.distanceTo(jlPasarWage) / 1000
        val customerToJlBrengkok = customerLocation.distanceTo(jlBrengkok) / 1000
        val customerToJlSingamerta = customerLocation.distanceTo(jlSingamerta) / 1000
        val customerToJlMadukara = customerLocation.distanceTo(jlMadukara) / 1000
        val customerToJlRejasa = customerLocation.distanceTo(jlRejasa) / 1000
        val customerToJlSunanGripit = customerLocation.distanceTo(jlSunanGripit) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.43942437F) && customerToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || customerToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && customerToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || customerToJlStadion in 0F.rangeTo(0.17309159F) && customerToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || customerToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && customerToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.15971817F) && customerToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.25077602F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.72462785F) && customerToJlGotongRoyong in 0F.rangeTo(
                0.72462785F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.2683005F) && customerToJlAlMunawwaroh in 0F.rangeTo(
                0.2683005F
            ) || customerToJlAlMunawwaroh in 0F.rangeTo(0.89462245F) && customerToJlPasarWage in 0F.rangeTo(
                0.89462245F
            ) || customerToJlPasarWage in 0F.rangeTo(0.8200417F) && customerToJlBrengkok in 0F.rangeTo(
                0.8200417F
            ) || customerToJlBrengkok in 0F.rangeTo(5.5378833F) && customerToJlSingamerta in 0F.rangeTo(
                5.5378833F
            ) || customerToJlSingamerta in 0F.rangeTo(1.4055679F) && customerToJlMadukara in 0F.rangeTo(
                1.4055679F
            ) || customerToJlMadukara in 0F.rangeTo(6.17852F) && customerToJlRejasa in 0F.rangeTo(
                6.17852F
            ) || customerToJlRejasa in 0F.rangeTo(0.78612006F) && customerToJlSunanGripit in 0F.rangeTo(
                0.78612006F
            )
        ) {
            requestTrayekB = "B"
        } else {
            requestTrayekB = null
        }

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val destinationToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val destinationToJlAjibarangSecang =
            destinationCustomer.distanceTo(jlAjibarangSecang) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000
        val destinationToJlAlMunawwaroh = destinationCustomer.distanceTo(jlAlMunawwaroh) / 1000
        val destinationToJlPasarWage = destinationCustomer.distanceTo(jlPasarWage) / 1000
        val destinationToJlBrengkok = destinationCustomer.distanceTo(jlBrengkok) / 1000
        val destinationToJlSingamerta = destinationCustomer.distanceTo(jlSingamerta) / 1000
        val destinationToJlMadukara = destinationCustomer.distanceTo(jlMadukara) / 1000
        val destinationToJlRejasa = destinationCustomer.distanceTo(jlRejasa) / 1000
        val destinationToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000

        isDestinationTrayekB =
            destinationToJlVeteran in 0F.rangeTo(0.43942437F) && destinationToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || destinationToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && destinationToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || destinationToJlStadion in 0F.rangeTo(0.17309159F) && destinationToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || destinationToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && destinationToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || destinationToJlCampurSalam in 0F.rangeTo(0.15971817F) && destinationToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || destinationToJlMtHaryono in 0F.rangeTo(0.25077602F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.72462785F) && destinationToJlGotongRoyong in 0F.rangeTo(
                0.72462785F
            ) || destinationToJlGotongRoyong in 0F.rangeTo(0.2683005F) && destinationToJlAlMunawwaroh in 0F.rangeTo(
                0.2683005F
            ) || destinationToJlAlMunawwaroh in 0F.rangeTo(0.89462245F) && destinationToJlPasarWage in 0F.rangeTo(
                0.89462245F
            ) || destinationToJlPasarWage in 0F.rangeTo(0.8200417F) && destinationToJlBrengkok in 0F.rangeTo(
                0.8200417F
            ) || destinationToJlBrengkok in 0F.rangeTo(5.5378833F) && destinationToJlSingamerta in 0F.rangeTo(
                5.5378833F
            ) || destinationToJlSingamerta in 0F.rangeTo(1.4055679F) && destinationToJlMadukara in 0F.rangeTo(
                1.4055679F
            ) || destinationToJlMadukara in 0F.rangeTo(6.17852F) && destinationToJlRejasa in 0F.rangeTo(
                6.17852F
            ) || destinationToJlRejasa in 0F.rangeTo(0.78612006F) && destinationToJlSunanGripit in 0F.rangeTo(
                0.78612006F
            )
    }

    private fun getTrayekAngkotC(destination: LatLng?) {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.394840142095557
        jlVeteran.longitude = 109.70068781164427

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.399562570467833
        jlLetjendSuprapto.longitude = 109.68604454812801

        val jlAlmunawwaroh = Location("")
        jlAlmunawwaroh.latitude = -7.395031113044718
        jlAlmunawwaroh.longitude = 109.69012034651269

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392543046575512
        jlGotongRoyong.longitude = 109.69011758939891

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.392493680039571
        jlSunanGripit.longitude = 109.69502404608019

        val jlPetambakan = Location("")
        jlPetambakan.latitude = -7.385932436549343
        jlPetambakan.longitude = 109.69113398260461

        val jlBanjarmangu = Location("")
        jlBanjarmangu.latitude = -7.359293959744121
        jlBanjarmangu.longitude = 109.6918696216978

        val polsekBanjarmangu = Location("")
        polsekBanjarmangu.latitude = -7.36054271148776
        polsekBanjarmangu.longitude = 109.68800802062427

        val jlKiJagapati = Location("")
        jlKiJagapati.latitude = -7.391222574387803
        jlKiJagapati.longitude = 109.69513972955063

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929782667728965
        jlMayjendSoetojo.longitude = 109.70048342573183

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlLetjendSupraptop = customerLocation.distanceTo(jlLetjendSuprapto) / 1000
        val customerToJlAlmunawwaroh = customerLocation.distanceTo(jlAlmunawwaroh) / 1000
        val customerToJlGotongRoyong = customerLocation.distanceTo(jlGotongRoyong) / 1000
        val customerToJlSunanGripit = customerLocation.distanceTo(jlSunanGripit) / 1000
        val customerToJlPetambakan = customerLocation.distanceTo(jlPetambakan) / 1000
        val customerToJlBanjarmangu = customerLocation.distanceTo(jlBanjarmangu) / 1000
        val customerToPolsekBanjarmangu = customerLocation.distanceTo(polsekBanjarmangu) / 1000
        val customerToJlKiJagapati = customerLocation.distanceTo(jlKiJagapati) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.28372997F) && customerToJlCampurSalam in 0F.rangeTo(
                0.28372997F
            ) || customerToJlCampurSalam in 0F.rangeTo(1.4537477F) && customerToJlLetjendSupraptop in 0F.rangeTo(
                1.4537477F
            ) || customerToJlLetjendSupraptop in 0F.rangeTo(0.67350984F) && customerToJlAlmunawwaroh in 0F.rangeTo(
                0.67350984F
            ) || customerToJlAlmunawwaroh in 0F.rangeTo(0.27516207F) && customerToJlGotongRoyong in 0F.rangeTo(
                0.27516207F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.5417019F) && customerToJlSunanGripit in 0F.rangeTo(
                0.5417019F
            ) || customerToJlSunanGripit in 0F.rangeTo(0.84319293F) && customerToJlPetambakan in 0F.rangeTo(
                0.84319293F
            ) || customerToJlPetambakan in 0F.rangeTo(2.9471366F) && customerToJlBanjarmangu in 0F.rangeTo(
                2.9471366F
            ) || customerToJlBanjarmangu in 0F.rangeTo(0.4481622F) && customerToPolsekBanjarmangu in 0F.rangeTo(
                0.4481622F
            ) || customerToPolsekBanjarmangu in 0F.rangeTo(3.4831262F) && customerToJlKiJagapati in 0F.rangeTo(
                3.4831262F
            ) || customerToJlKiJagapati in 0F.rangeTo(0.6210776F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.6210776F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(1.1454027F) && customerToJlGotongRoyong in 0F.rangeTo(
                1.1454027F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.27516207F) && customerToJlAlmunawwaroh in 0F.rangeTo(
                0.27516207F
            ) || customerToJlAlmunawwaroh in 0F.rangeTo(0.67350984F) && customerToJlLetjendSupraptop in 0F.rangeTo(
                0.67350984F
            ) || customerToJlLetjendSupraptop in 0F.rangeTo(1.4537477F) && customerToJlCampurSalam in 0F.rangeTo(
                1.4537477F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.15971817F) && customerToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.302071F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.302071F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.2071424F) && customerToJlVeteran in 0F.rangeTo(
                0.2071424F
            )
        ) {
            requestTrayekC = "C"
        } else {
            requestTrayekC = null
        }

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlLetjendSupraptop =
            destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val destinationToJlAlmunawwaroh = destinationCustomer.distanceTo(jlAlmunawwaroh) / 1000
        val destinationToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000
        val destinationToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000
        val destinationToJlPetambakan = destinationCustomer.distanceTo(jlPetambakan) / 1000
        val destinationToJlBanjarmangu = destinationCustomer.distanceTo(jlBanjarmangu) / 1000
        val destinationToPolsekBanjarmangu =
            destinationCustomer.distanceTo(polsekBanjarmangu) / 1000
        val destinationToJlKiJagapati = destinationCustomer.distanceTo(jlKiJagapati) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000

        isDestinationTrayekC =
            destinationToJlVeteran in 0F.rangeTo(0.28372997F) && destinationToJlCampurSalam in 0F.rangeTo(
                0.28372997F
            ) || destinationToJlCampurSalam in 0F.rangeTo(1.4537477F) && destinationToJlLetjendSupraptop in 0F.rangeTo(
                1.4537477F
            ) || destinationToJlLetjendSupraptop in 0F.rangeTo(0.67350984F) && destinationToJlAlmunawwaroh in 0F.rangeTo(
                0.67350984F
            ) || destinationToJlAlmunawwaroh in 0F.rangeTo(0.27516207F) && destinationToJlGotongRoyong in 0F.rangeTo(
                0.27516207F
            ) || destinationToJlGotongRoyong in 0F.rangeTo(0.5417019F) && destinationToJlSunanGripit in 0F.rangeTo(
                0.5417019F
            ) || destinationToJlSunanGripit in 0F.rangeTo(0.84319293F) && destinationToJlPetambakan in 0F.rangeTo(
                0.84319293F
            ) || destinationToJlPetambakan in 0F.rangeTo(2.9471366F) && destinationToJlBanjarmangu in 0F.rangeTo(
                2.9471366F
            ) || destinationToJlBanjarmangu in 0F.rangeTo(0.4481622F) && destinationToPolsekBanjarmangu in 0F.rangeTo(
                0.4481622F
            ) || destinationToPolsekBanjarmangu in 0F.rangeTo(3.4831262F) && destinationToJlKiJagapati in 0F.rangeTo(
                3.4831262F
            ) || destinationToJlKiJagapati in 0F.rangeTo(0.6210776F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
                0.6210776F
            ) || destinationToJlMayjendSoetojo in 0F.rangeTo(1.1454027F) && destinationToJlGotongRoyong in 0F.rangeTo(
                1.1454027F
            ) || destinationToJlGotongRoyong in 0F.rangeTo(0.27516207F) && destinationToJlAlmunawwaroh in 0F.rangeTo(
                0.27516207F
            ) || destinationToJlAlmunawwaroh in 0F.rangeTo(0.67350984F) && destinationToJlLetjendSupraptop in 0F.rangeTo(
                0.67350984F
            ) || destinationToJlLetjendSupraptop in 0F.rangeTo(1.4537477F) && destinationToJlCampurSalam in 0F.rangeTo(
                1.4537477F
            ) || destinationToJlCampurSalam in 0F.rangeTo(0.15971817F) && destinationToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || destinationToJlMtHaryono in 0F.rangeTo(0.302071F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
                0.302071F
            ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.2071424F) && destinationToJlVeteran in 0F.rangeTo(
                0.2071424F
            )
    }

    private fun getTrayekAngkotD(destination: LatLng?) {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.393286634547339
        jlVeteran.longitude = 109.70061167423222

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.391957814455376
        jlLetnanKarjono.longitude = 109.70436277113605

        val jlStadion = Location("")
        jlStadion.latitude = -7.393062008311444
        jlStadion.longitude = 109.70493209729725

        val jlAjibarangSecang = Location("")
        jlAjibarangSecang.latitude = -7.3944717509148825
        jlAjibarangSecang.longitude = 109.70561317241973

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929677430276355
        jlMayjendSoetojo.longitude = 109.69907814958101

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.3925535821418125
        jlSunanGripit.longitude = 109.6950338758701

        val jlRejasa = Location("")
        jlRejasa.latitude = -7.387036260108403
        jlRejasa.longitude = 109.69070857427552

        val jlMadukara = Location("")
        jlMadukara.latitude = -7.379043887211778
        jlMadukara.longitude = 109.74609622769049

        val jlSingamerta = Location("")
        jlSingamerta.latitude = -7.391250422958347
        jlSingamerta.longitude = 109.74284967730786

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.399571113858571
        jlLetjendSuprapto.longitude = 109.68601952525529

        val jlAlMunawwaroh = Location("")
        jlAlMunawwaroh.latitude = -7.395129593968356
        jlAlMunawwaroh.longitude = 109.69013935139593

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392545545002634
        jlGotongRoyong.longitude = 109.69011664584171

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlLetnanKarjono = customerLocation.distanceTo(jlLetnanKarjono) / 1000
        val customerToJlStadion = customerLocation.distanceTo(jlStadion) / 1000
        val customerToJlAjibarangSecang = customerLocation.distanceTo(jlAjibarangSecang) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlSunanGripit = customerLocation.distanceTo(jlSunanGripit) / 1000
        val customerToJlRejasa = customerLocation.distanceTo(jlRejasa) / 1000
        val customerToJlMadukara = customerLocation.distanceTo(jlMadukara) / 1000
        val customerToJlSingamerta = customerLocation.distanceTo(jlSingamerta) / 1000
        val customerToJlLetjendSuprapto = customerLocation.distanceTo(jlLetjendSuprapto) / 1000
        val customerToJlAlMunawwaroh = customerLocation.distanceTo(jlAlMunawwaroh) / 1000
        val customerToJlGotongRoyong = customerLocation.distanceTo(jlGotongRoyong) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.43942437F) && customerToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || customerToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && customerToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || customerToJlStadion in 0F.rangeTo(0.17309159F) && customerToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || customerToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && customerToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.15971817F) && customerToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.25077602F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.44883206F) && customerToJlSunanGripit in 0F.rangeTo(
                0.44883206F
            ) || customerToJlSunanGripit in 0F.rangeTo(0.77481407F) && customerToJlRejasa in 0F.rangeTo(
                0.77481407F
            ) || customerToJlRejasa in 0F.rangeTo(6.178497F) && customerToJlMadukara in 0F.rangeTo(
                6.178497F
            ) || customerToJlMadukara in 0F.rangeTo(1.3967254F) && customerToJlSingamerta in 0F.rangeTo(
                1.3967254F
            ) || customerToJlSingamerta in 0F.rangeTo(6.34115F) && customerToJlLetjendSuprapto in 0F.rangeTo(
                6.34115F
            ) || customerToJlLetjendSuprapto in 0F.rangeTo(0.6694348F) && customerToJlAlMunawwaroh in 0F.rangeTo(
                0.6694348F
            ) || customerToJlAlMunawwaroh in 0F.rangeTo(0.28578788F) && customerToJlGotongRoyong in 0F.rangeTo(
                0.28578788F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.9904535F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.9904535F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.1729356F) && customerToJlVeteran in 0F.rangeTo(
                0.1729356F
            )
        ) {
            requestTrayekD = "D"
        } else {
            requestTrayekD = null
        }

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val destinationToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val destinationToJlAjibarangSecang =
            destinationCustomer.distanceTo(jlAjibarangSecang) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000
        val destinationToJlRejasa = destinationCustomer.distanceTo(jlRejasa) / 1000
        val destinationToJlMadukara = destinationCustomer.distanceTo(jlMadukara) / 1000
        val destinationToJlSingamerta = destinationCustomer.distanceTo(jlSingamerta) / 1000
        val destinationToJlLetjendSuprapto =
            destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val destinationToJlAlMunawwaroh = destinationCustomer.distanceTo(jlAlMunawwaroh) / 1000
        val destinationToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000

        isDestinationTrayekD =
            destinationToJlVeteran in 0F.rangeTo(0.43942437F) && destinationToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || destinationToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && destinationToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || destinationToJlStadion in 0F.rangeTo(0.17309159F) && destinationToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || destinationToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && destinationToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || destinationToJlCampurSalam in 0F.rangeTo(0.15971817F) && destinationToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || destinationToJlMtHaryono in 0F.rangeTo(0.25077602F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.44883206F) && destinationToJlSunanGripit in 0F.rangeTo(
                0.44883206F
            ) || destinationToJlSunanGripit in 0F.rangeTo(0.77481407F) && destinationToJlRejasa in 0F.rangeTo(
                0.77481407F
            ) || destinationToJlRejasa in 0F.rangeTo(6.178497F) && destinationToJlMadukara in 0F.rangeTo(
                6.178497F
            ) || destinationToJlMadukara in 0F.rangeTo(1.3967254F) && destinationToJlSingamerta in 0F.rangeTo(
                1.3967254F
            ) || destinationToJlSingamerta in 0F.rangeTo(6.34115F) && destinationToJlLetjendSuprapto in 0F.rangeTo(
                6.34115F
            ) || destinationToJlLetjendSuprapto in 0F.rangeTo(0.6694348F) && destinationToJlAlMunawwaroh in 0F.rangeTo(
                0.6694348F
            ) || destinationToJlAlMunawwaroh in 0F.rangeTo(0.28578788F) && destinationToJlGotongRoyong in 0F.rangeTo(
                0.28578788F
            ) || destinationToJlGotongRoyong in 0F.rangeTo(0.9904535F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
                0.9904535F
            ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.1729356F) && destinationToJlVeteran in 0F.rangeTo(
                0.1729356F
            )
    }

    private fun checkTrayekAgkot() {
        if (requestTrayekA == "A") {
            binding.customBackgroundLayoutCustomer.apply {
                if (isDestinationTrayekA) {
                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotA.visibility = View.VISIBLE
                    cvNextAngkotA.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekB) {
                    tvTransitLocation.text =
                        StringBuilder().append("Pasar Wage Banjarnegara, Kantor Pos Banjarnegara")

                    isTransitInPasarWage = true
                    isTransitInKantorPos = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotA.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
                if (isDestinationTrayekC) {
                    tvTransitLocation.text = StringBuilder().append("Pasar Wage Banjarnegara")

                    isTransitInPasarWage = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotA.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotC.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekD) {
                    tvTransitLocation.text =
                        StringBuilder().append("Pasar Wage Banjarnegara, Kantor Pos Banjarnegara")

                    isTransitInPasarWage = true
                    isTransitInKantorPos = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotA.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
            }
        }

        if (requestTrayekB == "B") {
            binding.customBackgroundLayoutCustomer.apply {
                if (isDestinationTrayekA) {
                    tvTransitLocation.text =
                        StringBuilder().append("Pasar Wage Banjarnegara, Kantor Pos Banjarnegara")

                    isTransitInPasarWage = true
                    isTransitInKantorPos = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotA.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
                if (isDestinationTrayekB) {
                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekC) {
                    tvTransitLocation.text =
                        StringBuilder().append("Gayam Banjarnegara, Rejasa Banjarnegara")

                    isTransitInGayam = true
                    isTransitInRejasa = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotC.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekD) {
                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
            }
        }

        if (requestTrayekC == "C") {
            binding.customBackgroundLayoutCustomer.apply {
                if (isDestinationTrayekA) {
                    tvTransitLocation.text = StringBuilder().append("Pasar Wage Banjarnegara")

                    isTransitInPasarWage = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotC.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotA.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
                if (isDestinationTrayekB) {
                    tvTransitLocation.text =
                        StringBuilder().append("Rejasa Banjarnegara, Kantor Pos Banjarnegara")

                    isTransitInRejasa = true
                    isTransitInKantorPos = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotC.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekC) {
                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotC.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotC.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekD) {
                    tvTransitLocation.text =
                        StringBuilder().append("Rejasa Banjarnegara, Kantor Pos Banjarnegara")

                    isTransitInRejasa = false
                    isTransitInKantorPos = false

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
            }
        }

        if (requestTrayekD == "D") {
            binding.customBackgroundLayoutCustomer.apply {
                if (isDestinationTrayekA) {
                    tvTransitLocation.text =
                        StringBuilder().append("Pasar Wage Banjarnegara, Kantor Pos Banjarnegara")

                    isTransitInPasarWage = true
                    isTransitInKantorPos = false

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotA.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
                if (isDestinationTrayekB) {
                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekC) {
                    tvTransitLocation.text =
                        StringBuilder().append("Gayam Banjarnegara, Rejasa Banjarnegara")

                    isTransitInGayam = true
                    isTransitInRejasa = true

                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotC.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }

                if (isDestinationTrayekD) {
                    tvCurrentRecommendation.visibility = View.VISIBLE
                    cvCurrentAngkotB.visibility = View.VISIBLE
                    cvCurrentAngkotD.visibility = View.VISIBLE
                    tvNextRecommendation.visibility = View.VISIBLE
                    cvNextAngkotB.visibility = View.VISIBLE
                    cvNextAngkotD.visibility = View.VISIBLE
                    tvCurrentRecommendationNotFound.visibility = View.GONE
                    tvNextRecommendationNotFound.visibility = View.GONE
                }
            }
        }
    }

    private fun customerCurrentPosition() {

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val pasarWageLocation = Location("")
        pasarWageLocation.latitude = -7.39951339467552
        pasarWageLocation.longitude = 109.68603198579679

        val kantorPosLocation = Location("")
        kantorPosLocation.latitude = -7.396629871578979
        kantorPosLocation.longitude = 109.69888815889803

        val rejasaLocation = Location("")
        rejasaLocation.latitude = -7.387051765548091
        rejasaLocation.longitude = 109.69077224025459

        val gayamLocation = Location("")
        gayamLocation.latitude = -7.3926715894281925
        gayamLocation.longitude = 109.69504681851232

        val customerToPasarWage = customerLocation.distanceTo(pasarWageLocation)
        val customerToKantorPos = customerLocation.distanceTo(kantorPosLocation)
        val customerToGayam = customerLocation.distanceTo(gayamLocation)
        val customerToRejasa = customerLocation.distanceTo(rejasaLocation)

        if (isTransitInPasarWage || isTransitInKantorPos || isTransitInGayam || isTransitInRejasa) {
            if (customerToPasarWage < 500) {
                sendNotification(
                    "Sebentar lagi anda sampai di Pasar Wage Banjarnegara"
                )
                val customerId = firebaseAuth.currentUser?.uid
                val historyTrayek =
                    firebaseDatabase.reference.child("PasarWage").child(customerId.toString())
                val history = HashMap<String, Any>()
                history["latitude"] = mLastLocation?.latitude.toString()
                history["longitude"] = mLastLocation?.longitude.toString()
                historyTrayek.updateChildren(history)
            }
            if (customerToKantorPos < 500) {
                sendNotification(
                    "Sebentar lagi anda sampai di Kantor Pos Banjarnegara"
                )
            }
            if (customerToGayam < 500) {
                sendNotification(
                    "Sebentar lagi anda sampai di Gayam Banjarnegara"
                )
            }
            if (customerToRejasa < 500) {
                sendNotification(
                    "Sebentar lagi anda sampai di Rejasa Banjarnegara"
                )
            }
            isCurrentPosition = true
        }
    }

    // function to get customer profile
    private fun getCustomerProfile() {
        val customerId = firebaseAuth.currentUser?.uid
        if (customerId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val customerRef = firebaseDatabase.reference.child("Users").child("Customers")
                .child(customerId.toString())
            customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        if (map?.get("profileImageUrl") != null) {

                            val image = map["profileImageUrl"].toString()
                            Glide.with(this@CustomerActivity)
                                .load(image)
                                .placeholder(R.drawable.ic_load_data)
                                .error(R.drawable.ic_error_load_data)
                                .into(navHeaderBinding.imgProfileNavHeader)
                        } else {
                            val imageProfile = navHeaderBinding.imgProfileNavHeader
                            imageProfile.setImageResource(R.drawable.ic_default_image)
                        }

                        if (map?.get("name") != null) {
                            val tvName = navHeaderBinding.tvName
                            val name = map["name"].toString()
                            tvName.text = name
                        } else {
                            val tvName = navHeaderBinding.tvName
                            tvName.text = resources.getString(R.string.no_name)
                        }

                        if (map?.get("email") != null) {
                            val tvEmail = navHeaderBinding.tvEmail
                            val email = map["email"].toString()
                            tvEmail.text = email
                        } else {
                            val tvEmail = navHeaderBinding.tvEmail
                            tvEmail.text = resources.getString(R.string.no_email)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                // location periodically
                mLastLocation = location

                if (!isZoomUpdate) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16F))
                    mMap.addMarker(
                        MarkerOptions().position(currentLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin))
                    )
                    isZoomUpdate = true
                }

                // call function customers position
                customersPosition()

                if (!isCurrentPosition) {
                    customerCurrentPosition()
                }

                // call function to get driver arround
                if (!isGetDriversAroundStarted) {
                    getDriversAround()

                }
            }
        }
    }

    // add customers position
    private fun customersPosition() {
        val customerId = firebaseAuth.currentUser?.uid
        val customerRef = firebaseDatabase.reference.child("Users").child("Customers")
            .child(customerId.toString())
        customerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val checkLogin = snapshot.child("login").value

                    val customersPosition =
                        firebaseDatabase.reference.child("CustomersPosition")
                    val geoFirePosition = GeoFire(customersPosition)

                    if (checkLogin == true) {
                        geoFirePosition.setLocation(
                            customerId,
                            GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude)
                        )
                    } else {
                        geoFirePosition.removeLocation(customerId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // get current location
    private fun getNewLocation() {
        mLocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // check location permission
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

    // get closest driver in customer location
    private fun getClosestDriver() {
        if (requestTrayekA != null || requestTrayekB != null || requestTrayekC != null || requestTrayekD != null) {
            val driverLocation = firebaseDatabase.reference.child("DriversAvailable")

            driverLocation.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val geoFire = GeoFire(driverLocation)
                        if (pickUpLocation != null) {
                            geoQuery = geoFire.queryAtLocation(
                                GeoLocation(
                                    pickUpLocation!!.latitude,
                                    pickUpLocation!!.longitude
                                ), radius
                            )
                            geoQuery?.removeAllListeners()

                            geoQuery?.addGeoQueryEventListener(object : GeoQueryEventListener {
                                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                                    if (!driverFound && isRequestAngkot && key != null) {
                                        // check request customer
                                        val gti =
                                            object : GenericTypeIndicator<Map<String?, Any?>?>() {}
                                        val mDriverDatabase =
                                            firebaseDatabase.reference.child("Users")
                                                .child("Drivers")
                                                .child(key)
                                        mDriverDatabase.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    val driverMap: Map<String?, Any?>? =
                                                        snapshot.getValue(gti)

                                                    if (driverFound) {
                                                        return
                                                    }

                                                    if (driverMap?.get("trayek") == requestTrayekA || driverMap?.get(
                                                            "trayek"
                                                        ) == requestTrayekB || driverMap?.get("trayek") == requestTrayekC || driverMap?.get(
                                                            "trayek"
                                                        ) == requestTrayekD
                                                    ) {
                                                        driverFound = true
                                                        driverFoundId = snapshot.key
                                                        if (driverFoundId != null) {
                                                            val driverRef =
                                                                firebaseDatabase.reference.child("Users")
                                                                    .child("Drivers")
                                                                    .child(driverFoundId.toString())
                                                                    .child("customerRequest")

                                                            val customerId =
                                                                firebaseAuth.currentUser?.uid

                                                            val map = HashMap<String, Any>()
                                                            // put customer ride id inside customer request
                                                            if (customerId != null) {
                                                                map["customerRideId"] = customerId
                                                            }

                                                            // put destination inside customer request
                                                            if (destination != null) {
                                                                map["destination"] =
                                                                    destination.toString()
                                                            } else {
                                                                map["destination"] =
                                                                    resources.getString(R.string.no_destination)
                                                            }

                                                            // put address inside customer request
                                                            if (address != null) {
                                                                map["address"] = address.toString()
                                                            } else {
                                                                map["address"] =
                                                                    resources.getString(R.string.no_address)
                                                            }

                                                            // put destination latitude inside customer request
                                                            if (destinationLatLng != null) {
                                                                map["destinationLat"] =
                                                                    destinationLatLng!!.latitude
                                                            } else {
                                                                map["destinationLat"] = 0.0
                                                            }

                                                            // put destination longitude inside customer request
                                                            if (destinationLatLng != null) {
                                                                map["destinationLng"] =
                                                                    destinationLatLng!!.longitude
                                                            } else {
                                                                map["destinationLng"] = 0.0
                                                            }

                                                            map["isPicked"] = false

                                                            driverRef.updateChildren(map)

                                                            // call the function get driver location
                                                            getDriverLocation()

                                                            // call the function to get driver information
                                                            getDriverInformation()

                                                            // call the function to ended ride
                                                            getHasRideEnded()

                                                            binding.customBackgroundLayoutCustomer.btnRequestAngkot.text =
                                                                resources.getString(R.string.looking_for_angkot_location)
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                    }
                                }

                                override fun onKeyExited(key: String?) {}

                                override fun onKeyMoved(key: String?, location: GeoLocation?) {}

                                override fun onGeoQueryReady() {
                                    if (!driverFound) {
                                        radius++
                                        getClosestDriver()
                                    }
                                }

                                override fun onGeoQueryError(error: DatabaseError?) {}
                            })
                        }
                    } else {
                        Handler(mainLooper).postDelayed({
                            driverNotFound()
                        }, 2000L)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            Handler(mainLooper).postDelayed({
                driverNotFound()
            }, 2000L)
        }
    }

    private fun getClosestDriverTransitLocation() {
        val driverLocation = firebaseDatabase.reference.child("DriversAvailable")
        driverLocation.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val geoFire = GeoFire(driverLocation)
                    if (mLastLocation != null) {
                        geoQuery = geoFire.queryAtLocation(
                            GeoLocation(
                                mLastLocation!!.latitude,
                                mLastLocation!!.longitude
                            ), radius
                        )
                        geoQuery?.removeAllListeners()

                        geoQuery?.addGeoQueryEventListener(object : GeoQueryEventListener {
                            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                                if (key != null) {

                                }
                            }

                            override fun onKeyExited(key: String?) {}

                            override fun onKeyMoved(key: String?, location: GeoLocation?) {}

                            override fun onGeoQueryReady() {}

                            override fun onGeoQueryError(error: DatabaseError?) {}
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // function if driver not found
    private fun driverNotFound() {
        isRequestAngkot = false
        Toast.makeText(
            this@CustomerActivity,
            resources.getString(R.string.driver_not_found),
            Toast.LENGTH_SHORT
        )
            .show()
        // remove customer request
        val customerId = firebaseAuth.currentUser?.uid
        val ref = firebaseDatabase.getReference("CustomersRequest")
        val geofire = GeoFire(ref)
        geofire.removeLocation(customerId)

        // remove pickup marker
        pickUpMarker?.remove()

        requestTrayekA = null
        requestTrayekB = null
        requestTrayekC = null
        requestTrayekD = null

        binding.customBackgroundLayoutCustomer.apply {
            btnRequestAngkot.isEnabled = true
            btnRequestAngkot.text = resources.getString(R.string.search_angkot)
            tvCurrentRecommendation.visibility = View.GONE
            layoutCurrentTrayek.visibility = View.GONE
            cvCurrentAngkotA.visibility = View.GONE
            cvCurrentAngkotB.visibility = View.GONE
            cvCurrentAngkotC.visibility = View.GONE
            cvCurrentAngkotD.visibility = View.GONE
            layoutTransit.visibility = View.GONE
            tvNextRecommendation.visibility = View.GONE
            layoutNextTrayek.visibility = View.GONE
            cvNextAngkotA.visibility = View.GONE
            cvNextAngkotB.visibility = View.GONE
            cvNextAngkotC.visibility = View.GONE
            cvNextAngkotD.visibility = View.GONE
        }
    }

    // show driver location
    private fun getDriverLocation() {
        if (driverFoundId != null) {
            driverLocationRef = firebaseDatabase.reference.child("DriversWorking")
                .child(driverFoundId.toString()).child("l")
            driverLocationRefListener =
                driverLocationRef?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && isRequestAngkot) {
                            val map: List<Any?> = snapshot.value as List<Any?>
                            var locationLat = 0.0
                            var locationLng = 0.0

                            if (map[0] != null) {
                                locationLat = map[0].toString().toDouble()
                            }
                            if (map[1] != null) {
                                locationLng = map[1].toString().toDouble()
                            }

                            val driverLatLng = LatLng(locationLat, locationLng)

                            mDriverMarker?.remove()

                            val location1 = Location("")
                            location1.latitude = pickUpLocation?.latitude as Double
                            location1.longitude = pickUpLocation?.longitude as Double

                            val location2 = Location("")
                            location2.latitude = driverLatLng.latitude
                            location2.longitude = driverLatLng.longitude

                            val distance = location1.distanceTo(location2)

                            binding.customBackgroundLayoutCustomer.apply {
                                // if distance location less then 100
                                if (distance < 100) {
                                    btnRequestAngkot.text =
                                        resources.getString(R.string.angkot_here)
                                } else {
                                    btnRequestAngkot.text =
                                        resources.getString(R.string.angkot_found)

                                    val customerReq =
                                        firebaseDatabase.reference.child("Users").child("Drivers")
                                            .child(driverFoundId.toString())
                                            .child("customerRequest")
                                    val gti =
                                        object : GenericTypeIndicator<Map<String?, Any?>?>() {}
                                    customerReq.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val mapReq: Map<String?, Any?>? =
                                                    snapshot.getValue(gti)

                                                if (mapReq?.get("isPicked") == true) {
                                                    binding.customBackgroundLayoutCustomer.apply {
                                                        tvCurrentRecommendation.visibility =
                                                            View.GONE
                                                        layoutCurrentTrayek.visibility = View.GONE
                                                        layoutTransit.visibility = View.GONE
                                                        tvNextRecommendation.visibility = View.GONE
                                                        layoutNextTrayek.visibility = View.GONE
                                                        btnRequestAngkot.isEnabled = false
                                                        btnRequestAngkot.text =
                                                            resources.getString(R.string.angkot_to_your_location)
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                            }

                            mDriverMarker = mMap.addMarker(
                                MarkerOptions().position(driverLatLng).title("Your Angkot")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    // function to getting driver information
    private fun getDriverInformation() {
        if (driverFoundId != null) {
            val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
            val mDriverDatabase = firebaseDatabase.reference.child("Users").child("Drivers")
                .child(driverFoundId.toString())
            mDriverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.childrenCount > 0) {

                        val map: Map<String?, Any?>? = snapshot.getValue(gti)

                        binding.customBackgroundLayoutCustomer.apply {

                            // hidden text and show content
                            imgProfileDriver.visibility = View.VISIBLE
                            layoutName.visibility = View.VISIBLE
                            layoutPhone.visibility = View.VISIBLE
                            layoutVehiclePlate.visibility = View.VISIBLE
                            tvNoOrders.visibility = View.GONE
                            tvCurrentRecommendation.visibility = View.GONE
                            layoutCurrentTrayek.visibility = View.GONE
                            layoutTransit.visibility = View.GONE
                            tvNextRecommendation.visibility = View.GONE
                            layoutNextTrayek.visibility = View.GONE

                            if (map?.get("profileImageUrl") != null) {
                                val image = map["profileImageUrl"].toString()
                                Glide.with(applicationContext)
                                    .load(image)
                                    .placeholder(R.drawable.ic_load_data)
                                    .error(R.drawable.ic_error_load_data)
                                    .into(imgProfileDriver)
                            } else {
                                imgProfileDriver.setImageResource(R.drawable.ic_person_32)
                            }

                            if (map?.get("name") != null) {
                                val name = map["name"].toString()
                                tvNameDriver.text = name
                            } else {
                                tvNameDriver.text = resources.getString(R.string.no_name)
                            }

                            if (map?.get("phone") != null) {
                                val phone = map["phone"].toString()
                                tvPhoneNumberDriver.text = phone
                            } else {
                                tvPhoneNumberDriver.text =
                                    resources.getString(R.string.no_phone_number)
                            }

                            if (map?.get("vehiclePlate") != null) {
                                val vehiclePlate = map["vehiclePlate"].toString()
                                tvVehiclePlateDriver.text = vehiclePlate
                            } else {
                                tvVehiclePlateDriver.text =
                                    resources.getString(R.string.no_vehicle_plate)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to get all driver available in around
    private fun getDriversAround() {
        isGetDriversAroundStarted = true
        val driversLocation = firebaseDatabase.reference.child("DriversAvailable")

        if (mLastLocation != null) {
            val geoFire = GeoFire(driversLocation)
            val geoQuery = geoFire.queryAtLocation(
                GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude),
                RADIUS
            )

            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    for (markerIt in markerList) {
                        if (markerIt.tag == key) return
                    }

                    driverPosition(key, location)
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
                    for (markerIt in markerList) {
                        if (markerIt.tag == key) {
                            if (location != null) {
                                markerIt.position = LatLng(location.latitude, location.longitude)
                            }
                        }
                    }
                }

                override fun onGeoQueryReady() {}

                override fun onGeoQueryError(error: DatabaseError?) {}
            })
        }
    }

    // function to get driver position marker
    private fun driverPosition(key: String?, location: GeoLocation?) {
        if (location != null && key != null) {
            val driversLocation = LatLng(location.latitude, location.longitude)

            val driverRef = firebaseDatabase.reference.child("Users").child("Drivers").child(key)
            driverRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val transportationNumber =
                            snapshot.child("numberTransportation").value.toString()

                        val mDriverMarker =
                            mMap.addMarker(
                                MarkerOptions().position(driversLocation)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car))
                                    .title(transportationNumber)
                            )
                        mDriverMarker?.tag = key
                        markerList.add(mDriverMarker as Marker)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // function to end ride
    private fun getHasRideEnded() {
        if (driverFoundId != null) {
            driveHasEndedRef = firebaseDatabase.reference.child("Users").child("Drivers")
                .child(driverFoundId.toString()).child("customerRequest").child("customerRideId")
            driveHasEndedRefListener =
                driveHasEndedRef?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            endRide()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun endRide() {
        requestTrayekA = null
        requestTrayekB = null
        requestTrayekC = null
        requestTrayekD = null

        isRequestAngkot = false
        // cancelling request angkot
        geoQuery?.removeAllListeners()

        driverLocationRef?.removeEventListener(driverLocationRefListener as ValueEventListener)

        driveHasEndedRef?.removeEventListener(driveHasEndedRefListener as ValueEventListener)

        // remove customer request for driver id
        if (driverFoundId != null) {
            val driverRef =
                firebaseDatabase.reference.child("Users").child("Drivers")
                    .child(driverFoundId.toString()).child("customerRequest")
            driverRef.removeValue()
            driverFoundId = null
        }

        driverFound = false
        radius = 1.0

        // remove driver marker
        mDriverMarker?.remove()

        // remove customer request
        val customerId = firebaseAuth.currentUser?.uid
        val ref = firebaseDatabase.getReference("CustomersRequest")
        val geofire = GeoFire(ref)
        geofire.removeLocation(customerId)

        // remove pickup marker
        pickUpMarker?.remove()

        binding.customBackgroundLayoutCustomer.btnRequestAngkot.apply {
            isEnabled = true
            text = resources.getString(R.string.search_angkot)
        }

        // hidden content and show text if cancel request
        binding.customBackgroundLayoutCustomer.apply {
            imgProfileDriver.visibility = View.GONE
            layoutName.visibility = View.GONE
            layoutPhone.visibility = View.GONE
            layoutVehiclePlate.visibility = View.GONE
            tvCurrentRecommendation.visibility = View.GONE
            layoutCurrentTrayek.visibility = View.GONE
            layoutTransit.visibility = View.GONE
            layoutNextTrayek.visibility = View.GONE
            tvNoOrders.visibility = View.VISIBLE
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(contentText: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_kotline)
            .setColor(ContextCompat.getColor(this, R.color.light_blue))
            .setContentTitle("Pemberitahuan")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun alertLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.logout)
        builder.setIcon(R.drawable.ic_logout_32)
        builder.setMessage(R.string.are_you_sure)

        builder.setPositiveButton("Ya") { _, _ ->
            val customerId = firebaseAuth.currentUser?.uid
            googleSignInClient.revokeAccess()
                .addOnCompleteListener {
                    val customersPosition =
                        firebaseDatabase.getReference("CustomersPosition")
                    val geoFirePosition = GeoFire(customersPosition)
                    geoFirePosition.removeLocation(customerId)

                    val customersDb = firebaseDatabase.reference.child("Users").child("Customers")
                        .child(customerId.toString())
                    val checkLogin = HashMap<String, Any>()
                    checkLogin["login"] = false
                    customersDb.updateChildren(checkLogin)

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

    // showing map
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // check location permission
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
        getNewLocation()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_request_angkot) {

            if (isRequestAngkot) {
                endRide()
            } else {
                isRequestAngkot = true
                // add current location customer request
                val customerId = firebaseAuth.currentUser?.uid
                val ref = firebaseDatabase.getReference("CustomersRequest")
                if (mLastLocation != null) {
                    val geofire = GeoFire(ref)
                    geofire.setLocation(
                        customerId,
                        GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude)
                    )

                    // add pickup marker
                    pickUpLocation = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
                    pickUpMarker = mMap.addMarker(
                        MarkerOptions().position(pickUpLocation as LatLng).title("Pickup Here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin))
                    )

                    binding.customBackgroundLayoutCustomer.btnRequestAngkot.text =
                        resources.getString(R.string.getting_your_angkot)

                    // call function to get closest driver
                    getClosestDriver()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
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


    override fun onPause() {
        super.onPause()

        timer = Timer()
        val logoutTimeTask = LogOutTimerTask(googleSignInClient, "CustomersPosition")
        timer!!.scheduleAtFixedRate(logoutTimeTask, 60000L, 5000L)
    }

    override fun onResume() {
        super.onResume()

        if (timer != null) {
            timer!!.cancel()
            timer = null
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val customersDb = Firebase.database.reference.child("Users").child("Customers")
                .child(userId.toString())
            val checkLogin = HashMap<String, Any>()
            checkLogin["login"] = true
            customersDb.updateChildren(checkLogin)
        }
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val RADIUS = 10000.0
        private const val CHANNEL_ID = "CHANNEL_ID_KOTLINE"
        private const val NOTIFICATION_ID = 101
    }
}