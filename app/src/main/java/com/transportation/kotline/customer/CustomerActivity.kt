package com.transportation.kotline.customer

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.transportation.kotline.R
import com.transportation.kotline.databinding.ActivityCustomerBinding
import com.transportation.kotline.databinding.NavHeaderBinding
import com.transportation.kotline.other.ApplicationTurnedOff
import com.transportation.kotline.other.OptionActivity

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
    private var requestTrayek: String? = null
    private var driverFound = false
    private var isZoomUpdate = false
    private var isRequestAngkot = false
    private var isGetDriversAroundStarted = false
    private var radius = 1.0
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
                R.id.route -> {
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
        Places.initialize(applicationContext, "AIzaSyDTJ2xuEs3k5e5yADs93VwiqcapyO5AT6M")
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

                // create bottom sheet
                BottomSheetBehavior.from(binding.customBackgroundLayoutCustomer.bottomSheet).apply {
                    peekHeight = 100
                    this.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onError(status: Status) {}
        })

        // create bottom sheet
        BottomSheetBehavior.from(binding.customBackgroundLayoutCustomer.bottomSheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.customBackgroundLayoutCustomer.apply {
            // add event click to button request
            btnRequestAngkot.setOnClickListener(this@CustomerActivity)
        }

        // call function get customer profile in navigation drawer
        getCustomerProfile()

        // call class is application turned off
        startService(Intent(this, ApplicationTurnedOff::class.java))

        val dstnLatLng = LatLng(-7.381635590413542, 109.74607270244847)

        getTrayekAngkotA(dstnLatLng)
    }

    private fun getTrayekAngkotA(destination: LatLng?) {
        val trayekA = "A"

        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.394799033719521
        jlVeteran.longitude = 109.700762915669

        val jlBrengkok = Location("")
        jlBrengkok.latitude = -7.396415762365111
        jlBrengkok.longitude = 109.70093516506572

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.398083949789377
        jlLetjendSuprapto.longitude = 109.69292715129609

        val jlMantrianom = Location("")
        jlMantrianom.latitude = -7.398732293120247
        jlMantrianom.longitude = 109.6283702594063

        val jlJendSoedirman = Location("")
        jlJendSoedirman.latitude = -7.405499073713895
        jlJendSoedirman.longitude = 109.60576501924116

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.3955108893575945
        jlMtHaryono.longitude = 109.69891450257482

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.392973866382533
        jlMayjendSoetojo.longitude = 109.69908275635157

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.393295474003801
        jlLetnanKarjono.longitude = 109.7006343897887

        val jlStadion = Location("")
        jlStadion.latitude = -7.39199315369452
        jlStadion.longitude = 109.70438694635027

        val terminalBus = Location("")
        terminalBus.latitude = -7.392655920315736
        terminalBus.longitude = 109.70482103417467

//        val distanceOne = jlVeteran.distanceTo(jlCampurSalam) / 1000
//        val distanceTwo = jlVeteran.distanceTo(jlBrengkok) / 1000
//        val distanceThree = jlBrengkok.distanceTo(jlCampurSalam) / 1000
//        val distanceFour = jlCampurSalam.distanceTo(jlLetjendSuprapto) / 1000
//        val distanceFive = jlLetjendSuprapto.distanceTo(jlMantrianom) / 1000
//        val distanceSix = jlMantrianom.distanceTo(jlJendSoedirman) / 1000
//        val distanceSeven = jlCampurSalam.distanceTo(jlMtHaryono) / 1000
//        val distanceEight = jlMtHaryono.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceNine = jlMayjendSoetojo.distanceTo(jlLetnanKarjono) / 1000
//        val distanceTen = jlLetnanKarjono.distanceTo(jlStadion) / 1000
//        val distanceEleven = jlStadion.distanceTo(terminalBus) / 1000

//        Log.d("CEK", distanceOne.toString())
//        Log.d("CEK", distanceTwo.toString())
//        Log.d("CEK", distanceThree.toString())
//        Log.d("CEK", distanceFour.toString())
//        Log.d("CEK", distanceFive.toString())
//        Log.d("CEK", distanceSix.toString())
//        Log.d("CEK", distanceSeven.toString())
//        Log.d("CEK", distanceEight.toString())
//        Log.d("CEK", distanceNine.toString())
//        Log.d("CEK", distanceTen.toString())
//        Log.d("CEK", distanceEleven.toString())

        val distanceToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val distanceToJlBrengkok = destinationCustomer.distanceTo(jlBrengkok) / 1000
        val distanceToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val distanceToJlLetjendSuprapto = destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val distanceToJlMantrianom = destinationCustomer.distanceTo(jlMantrianom) / 1000
        val distanceToJlJendSoedirman = destinationCustomer.distanceTo(jlJendSoedirman) / 1000
        val distanceToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val distanceToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val distanceToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val distanceToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val distanceToTerminal = destinationCustomer.distanceTo(terminalBus) / 1000

        if (distanceToJlVeteran in 0F.rangeTo(0.17980675F) && distanceToJlBrengkok in 0F.rangeTo(
                0.17980675F
            ) || distanceToJlBrengkok in 0F.rangeTo(0.22749068F) && distanceToJlCampurSalam in 0F.rangeTo(
                0.22749068F
            ) || distanceToJlCampurSalam in 0F.rangeTo(0.67659587F) && distanceToJlLetjendSuprapto in 0F.rangeTo(
                0.67659587F
            ) || distanceToJlLetjendSuprapto in 0F.rangeTo(7.1273675F) && distanceToJlMantrianom in 0F.rangeTo(
                7.1273675F
            ) || distanceToJlMantrianom in 0F.rangeTo(2.6053631F) && distanceToJlJendSoedirman in 0F.rangeTo(
                2.6053631F
            ) || distanceToJlCampurSalam in 0F.rangeTo(0.12869798F) && distanceToJlMtHaryono in 0F.rangeTo(
                0.12869798F
            ) || distanceToJlMtHaryono in 0F.rangeTo(0.28119034F) && distanceToJlMayjendSoetojo in 0F.rangeTo(
                0.28119034F
            ) || distanceToJlMayjendSoetojo in 0F.rangeTo(0.17495409F) && distanceToJlLetnanKarjono in 0F.rangeTo(
                0.17495409F
            ) || distanceToJlLetnanKarjono in 0F.rangeTo(0.43860513F) && distanceToJlStadion in 0F.rangeTo(
                0.43860513F
            ) || distanceToJlStadion in 0F.rangeTo(0.08757355F) && distanceToTerminal in 0F.rangeTo(
                0.08757355F
            )
        ) {
            requestTrayek = trayekA
            getTrayekAngkotB(destination)
            binding.customBackgroundLayoutCustomer.apply {
                cvAngkotA.visibility = View.VISIBLE
                tvRecommendationNotFound.visibility = View.GONE
            }
        } else {
            getTrayekAngkotB(destination)
        }
    }

    private fun getTrayekAngkotB(destination: LatLng?) {
        val trayekB = "B"

        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

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

//        val distanceOne = jlVeteran.distanceTo(jlLetnanKarjono) / 1000
//        val distanceTwo = jlLetnanKarjono.distanceTo(jlStadion) / 1000
//        val distanceThree = jlStadion.distanceTo(jlAjibarangSecang) / 1000
//        val distanceFour = jlAjibarangSecang.distanceTo(jlCampurSalam) / 1000
//        val distanceFive = jlCampurSalam.distanceTo(jlMtHaryono) / 1000
//        val distanceSix = jlMtHaryono.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceSeven = jlMayjendSoetojo.distanceTo(jlGotongRoyong) / 1000
//        val distanceEight = jlGotongRoyong.distanceTo(jlAlMunawwaroh) / 1000
//        val distanceNine = jlAlMunawwaroh.distanceTo(jlPasarWage) / 1000
//        val distanceTen = jlPasarWage.distanceTo(jlBrengkok) / 1000
//        val distanceEleven = jlBrengkok.distanceTo(jlSingamerta) / 1000
//        val distanceTwelve = jlSingamerta.distanceTo(jlKenteng) / 1000
//        val distanceThirteen = jlKenteng.distanceTo(jlRejasa) / 1000
//        val distanceFourteen = jlRejasa.distanceTo(jlSunanGripit) / 1000
//        val distanceFifteen = jlSunanGripit.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceSixteen = jlMayjendSoetojo.distanceTo(jlVeteran) / 1000
        val distanceOne = jlSingamerta.distanceTo(jlMadukara) / 1000
        val distanceTwo = jlMadukara.distanceTo(jlRejasa) / 1000
//
//        Log.d("CEK", distanceOne.toString())
//        Log.d("CEK", distanceTwo.toString())
//        Log.d("CEK", distanceThree.toString())
//        Log.d("CEK", distanceFour.toString())
//        Log.d("CEK", distanceFive.toString())
//        Log.d("CEK", distanceSix.toString())
//        Log.d("CEK", distanceSeven.toString())
//        Log.d("CEK", distanceEight.toString())
//        Log.d("CEK", distanceNine.toString())
//        Log.d("CEK", distanceTen.toString())
//        Log.d("CEK", distanceEleven.toString())
//        Log.d("CEK", distanceTwelve.toString())
//        Log.d("CEK", distanceThirteen.toString())
//        Log.d("CEK", distanceFourteen.toString())
//        Log.d("CEK", distanceFifteen.toString())
//        Log.d("CEK", distanceSixteen.toString())

        Log.d("CEK", distanceOne.toString())
        Log.d("CEK", distanceTwo.toString())

        val distanceToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val distanceToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val distanceToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val distanceToJlAjibarangSecang = destinationCustomer.distanceTo(jlAjibarangSecang) / 1000
        val distanceToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val distanceToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val distanceToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val distanceToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000
        val distanceToJlAlMunawwaroh = destinationCustomer.distanceTo(jlAlMunawwaroh) / 1000
        val distanceToJlPasarWage = destinationCustomer.distanceTo(jlPasarWage) / 1000
        val distanceToJlBrengkok = destinationCustomer.distanceTo(jlBrengkok) / 1000
        val distanceToJlSingamerta = destinationCustomer.distanceTo(jlSingamerta) / 1000
        val distanceToJlMadukara = destinationCustomer.distanceTo(jlMadukara) / 1000
        val distanceToJlRejasa = destinationCustomer.distanceTo(jlRejasa) / 1000
        val distanceToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000

        if (distanceToJlVeteran in 0F.rangeTo(0.43942437F) && distanceToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || distanceToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && distanceToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || distanceToJlStadion in 0F.rangeTo(0.17309159F) && distanceToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || distanceToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && distanceToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || distanceToJlCampurSalam in 0F.rangeTo(0.15971817F) && distanceToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || distanceToJlMtHaryono in 0F.rangeTo(0.25077602F) && distanceToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || distanceToJlMayjendSoetojo in 0F.rangeTo(0.72462785F) && distanceToJlGotongRoyong in 0F.rangeTo(
                0.72462785F
            ) || distanceToJlGotongRoyong in 0F.rangeTo(0.2683005F) && distanceToJlAlMunawwaroh in 0F.rangeTo(
                0.2683005F
            ) || distanceToJlAlMunawwaroh in 0F.rangeTo(0.89462245F) && distanceToJlPasarWage in 0F.rangeTo(
                0.89462245F
            ) || distanceToJlPasarWage in 0F.rangeTo(0.8200417F) && distanceToJlBrengkok in 0F.rangeTo(
                0.8200417F
            ) || distanceToJlBrengkok in 0F.rangeTo(5.5378833F) && distanceToJlSingamerta in 0F.rangeTo(
                5.5378833F
            ) || distanceToJlSingamerta in 0F.rangeTo(1.4055679F) && distanceToJlMadukara in 0F.rangeTo(
                1.4055679F
            ) || distanceToJlMadukara in 0F.rangeTo(6.17852F) && distanceToJlRejasa in 0F.rangeTo(
                6.17852F
            ) || distanceToJlRejasa in 0F.rangeTo(0.78612006F) && distanceToJlSunanGripit in 0F.rangeTo(
                0.78612006F
            )
        ) {
            requestTrayek = trayekB
            getTrayekAngkotC(destination)
            binding.customBackgroundLayoutCustomer.apply {
                cvAngkotB.visibility = View.VISIBLE
                tvRecommendationNotFound.visibility = View.GONE
            }
        } else {
            getTrayekAngkotC(destination)
        }
    }

    private fun getTrayekAngkotC(destination: LatLng?) {
        val trayekC = "C"

        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

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

//        val distanceOne = jlVeteran.distanceTo(jlCampurSalam) / 1000
//        val distanceTwo = jlCampurSalam.distanceTo(jlLetjendSuprapto) / 1000
//        val distanceThree = jlLetjendSuprapto.distanceTo(jlAlmunawwaroh) / 1000
//        val distanceFour = jlAlmunawwaroh.distanceTo(jlGotongRoyong) / 1000
//        val distanceFive = jlGotongRoyong.distanceTo(jlSunanGripit) / 1000
//        val distanceSix = jlSunanGripit.distanceTo(jlPetambakan) / 1000
//        val distanceSeven = jlPetambakan.distanceTo(jlBanjarmangu) / 1000
//        val distanceEight = jlBanjarmangu.distanceTo(polsekBanjarmangu) / 1000
//        val distanceNine = polsekBanjarmangu.distanceTo(jlKiJagapati) / 1000
//        val distanceTen = jlKiJagapati.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceEleven = jlMayjendSoetojo.distanceTo(jlGotongRoyong) / 1000
//        val distanceTwelve = jlGotongRoyong.distanceTo(jlAlmunawwaroh) / 1000
//        val distanceThirteen = jlAlmunawwaroh.distanceTo(jlLetjendSuprapto) / 1000
//        val distanceFourteen = jlLetjendSuprapto.distanceTo(jlCampurSalam) / 1000
//        val distanceFifteen = jlCampurSalam.distanceTo(jlMtHaryono) / 1000
//        val distanceSixteen = jlMtHaryono.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceSeventeen = jlMayjendSoetojo.distanceTo(jlVeteran) / 1000
//
//        Log.d("CEK", distanceOne.toString())
//        Log.d("CEK", distanceTwo.toString())
//        Log.d("CEK", distanceThree.toString())
//        Log.d("CEK", distanceFour.toString())
//        Log.d("CEK", distanceFive.toString())
//        Log.d("CEK", distanceSix.toString())
//        Log.d("CEK", distanceSeven.toString())
//        Log.d("CEK", distanceEight.toString())
//        Log.d("CEK", distanceNine.toString())
//        Log.d("CEK", distanceTen.toString())
//        Log.d("CEK", distanceEleven.toString())
//        Log.d("CEK", distanceTwelve.toString())
//        Log.d("CEK", distanceThirteen.toString())
//        Log.d("CEK", distanceFourteen.toString())
//        Log.d("CEK", distanceFifteen.toString())
//        Log.d("CEK", distanceSixteen.toString())
//        Log.d("CEK", distanceSeventeen.toString())

        val distanceToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val distanceToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val distanceToJlLetjendSupraptop = destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val distanceToJlAlmunawwaroh = destinationCustomer.distanceTo(jlAlmunawwaroh) / 1000
        val distanceToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000
        val distanceToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000
        val distanceToJlPetambakan = destinationCustomer.distanceTo(jlPetambakan) / 1000
        val distanceToJlBanjarmangu = destinationCustomer.distanceTo(jlBanjarmangu) / 1000
        val distanceToPolsekBanjarmangu = destinationCustomer.distanceTo(polsekBanjarmangu) / 1000
        val distanceToJlKiJagapati = destinationCustomer.distanceTo(jlKiJagapati) / 1000
        val distanceToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val distanceToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000

        if (distanceToJlVeteran in 0F.rangeTo(0.28372997F) && distanceToJlCampurSalam in 0F.rangeTo(
                0.28372997F
            ) || distanceToJlCampurSalam in 0F.rangeTo(1.4537477F) && distanceToJlLetjendSupraptop in 0F.rangeTo(
                1.4537477F
            ) || distanceToJlLetjendSupraptop in 0F.rangeTo(0.67350984F) && distanceToJlAlmunawwaroh in 0F.rangeTo(
                0.67350984F
            ) || distanceToJlAlmunawwaroh in 0F.rangeTo(0.27516207F) && distanceToJlGotongRoyong in 0F.rangeTo(
                0.27516207F
            ) || distanceToJlGotongRoyong in 0F.rangeTo(0.5417019F) && distanceToJlSunanGripit in 0F.rangeTo(
                0.5417019F
            ) || distanceToJlSunanGripit in 0F.rangeTo(0.84319293F) && distanceToJlPetambakan in 0F.rangeTo(
                0.84319293F
            ) || distanceToJlPetambakan in 0F.rangeTo(2.9471366F) && distanceToJlBanjarmangu in 0F.rangeTo(
                2.9471366F
            ) || distanceToJlBanjarmangu in 0F.rangeTo(0.4481622F) && distanceToPolsekBanjarmangu in 0F.rangeTo(
                0.4481622F
            ) || distanceToPolsekBanjarmangu in 0F.rangeTo(3.4831262F) && distanceToJlKiJagapati in 0F.rangeTo(
                3.4831262F
            ) || distanceToJlKiJagapati in 0F.rangeTo(0.6210776F) && distanceToJlMayjendSoetojo in 0F.rangeTo(
                0.6210776F
            ) || distanceToJlMayjendSoetojo in 0F.rangeTo(1.1454027F) && distanceToJlGotongRoyong in 0F.rangeTo(
                1.1454027F
            ) || distanceToJlGotongRoyong in 0F.rangeTo(0.27516207F) && distanceToJlAlmunawwaroh in 0F.rangeTo(
                0.27516207F
            ) || distanceToJlAlmunawwaroh in 0F.rangeTo(0.67350984F) && distanceToJlLetjendSupraptop in 0F.rangeTo(
                0.67350984F
            ) || distanceToJlLetjendSupraptop in 0F.rangeTo(1.4537477F) && distanceToJlCampurSalam in 0F.rangeTo(
                1.4537477F
            ) || distanceToJlCampurSalam in 0F.rangeTo(0.15971817F) && distanceToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || distanceToJlMtHaryono in 0F.rangeTo(0.302071F) && distanceToJlMayjendSoetojo in 0F.rangeTo(
                0.302071F
            ) || distanceToJlMayjendSoetojo in 0F.rangeTo(0.2071424F) && distanceToJlVeteran in 0F.rangeTo(
                0.2071424F
            )
        ) {
            requestTrayek = trayekC
            getTrayekAngkotD(destination)
            binding.customBackgroundLayoutCustomer.apply {
                cvAngkotC.visibility = View.VISIBLE
                tvRecommendationNotFound.visibility = View.GONE
            }
        } else {
            getTrayekAngkotD(destination)
        }
    }

    private fun getTrayekAngkotD(destination: LatLng?) {
        val trayekD = "D"

        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

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

//        val distanceOne = jlVeteran.distanceTo(jlLetnanKarjono) / 1000
//        val distanceTwo = jlLetnanKarjono.distanceTo(jlStadion) / 1000
//        val distanceThree = jlStadion.distanceTo(jlAjibarangSecang) / 1000
//        val distanceFour = jlAjibarangSecang.distanceTo(jlCampurSalam) / 1000
//        val distanceFive = jlCampurSalam.distanceTo(jlMtHaryono) / 1000
//        val distanceSix = jlMtHaryono.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceSeven = jlMayjendSoetojo.distanceTo(jlSunanGripit) / 1000
//        val distanceEight = jlSunanGripit.distanceTo(jlRejasa) / 1000
//        val distanceNine = jlRejasa.distanceTo(jlMadukara) / 1000
//        val distanceTen = jlMadukara.distanceTo(jlSingamerta) / 1000
//        val distanceEleven = jlSingamerta.distanceTo(jlLetjendSuprapto) / 1000
//        val distanceTwelve = jlLetjendSuprapto.distanceTo(jlAlMunawwaroh) / 1000
//        val distanceThirteen = jlAlMunawwaroh.distanceTo(jlGotongRoyong) / 1000
//        val distanceFourteen = jlGotongRoyong.distanceTo(jlMayjendSoetojo) / 1000
//        val distanceFifteen = jlMayjendSoetojo.distanceTo(jlVeteran) / 1000
//
//        Log.d("CEK", distanceOne.toString())
//        Log.d("CEK", distanceTwo.toString())
//        Log.d("CEK", distanceThree.toString())
//        Log.d("CEK", distanceFour.toString())
//        Log.d("CEK", distanceFive.toString())
//        Log.d("CEK", distanceSix.toString())
//        Log.d("CEK", distanceSeven.toString())
//        Log.d("CEK", distanceEight.toString())
//        Log.d("CEK", distanceNine.toString())
//        Log.d("CEK", distanceTen.toString())
//        Log.d("CEK", distanceEleven.toString())
//        Log.d("CEK", distanceTwelve.toString())
//        Log.d("CEK", distanceThirteen.toString())
//        Log.d("CEK", distanceFourteen.toString())
//        Log.d("CEK", distanceFifteen.toString())

        val distanceToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val distanceToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val distanceToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val distanceToJlAjibarangSecang = destinationCustomer.distanceTo(jlAjibarangSecang) / 1000
        val distanceToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val distanceToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val distanceToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val distanceToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000
        val distanceToJlRejasa = destinationCustomer.distanceTo(jlRejasa) / 1000
        val distanceToJlMadukara = destinationCustomer.distanceTo(jlMadukara) / 1000
        val distanceToJlSingamerta = destinationCustomer.distanceTo(jlSingamerta) / 1000
        val distanceToJlLetjendSuprapto = destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val distanceToJlAlMunawwaroh = destinationCustomer.distanceTo(jlAlMunawwaroh) / 1000
        val distanceToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000

        if (distanceToJlVeteran in 0F.rangeTo(0.43942437F) && distanceToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || distanceToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && distanceToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || distanceToJlStadion in 0F.rangeTo(0.17309159F) && distanceToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || distanceToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && distanceToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || distanceToJlCampurSalam in 0F.rangeTo(0.15971817F) && distanceToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || distanceToJlMtHaryono in 0F.rangeTo(0.25077602F) && distanceToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || distanceToJlMayjendSoetojo in 0F.rangeTo(0.44883206F) && distanceToJlSunanGripit in 0F.rangeTo(
                0.44883206F
            ) || distanceToJlSunanGripit in 0F.rangeTo(0.77481407F) && distanceToJlRejasa in 0F.rangeTo(
                0.77481407F
            ) || distanceToJlRejasa in 0F.rangeTo(6.178497F) && distanceToJlMadukara in 0F.rangeTo(
                6.178497F
            ) || distanceToJlMadukara in 0F.rangeTo(1.3967254F) && distanceToJlSingamerta in 0F.rangeTo(
                1.3967254F
            ) || distanceToJlSingamerta in 0F.rangeTo(6.34115F) && distanceToJlLetjendSuprapto in 0F.rangeTo(
                6.34115F
            ) || distanceToJlLetjendSuprapto in 0F.rangeTo(0.6694348F) && distanceToJlAlMunawwaroh in 0F.rangeTo(
                0.6694348F
            ) || distanceToJlAlMunawwaroh in 0F.rangeTo(0.28578788F) && distanceToJlGotongRoyong in 0F.rangeTo(
                0.28578788F
            ) || distanceToJlGotongRoyong in 0F.rangeTo(0.9904535F) && distanceToJlMayjendSoetojo in 0F.rangeTo(
                0.9904535F
            ) || distanceToJlMayjendSoetojo in 0F.rangeTo(0.1729356F) && distanceToJlVeteran in 0F.rangeTo(
                0.1729356F
            )
        ) {
            requestTrayek = trayekD
            binding.customBackgroundLayoutCustomer.apply {
                cvAngkotD.visibility = View.VISIBLE
                tvRecommendationNotFound.visibility = View.GONE
            }
        } else {
            binding.customBackgroundLayoutCustomer.apply {
                tvRecommendationNotFound.visibility = View.VISIBLE
            }
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

                        Log.i("TAG", "onDataChange: ${map?.get("profileImageUrl")}")
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
        val driverLocation = firebaseDatabase.reference.child("DriversAvailable")

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
                        val gti = object : GenericTypeIndicator<Map<String?, Any?>?>() {}
                        val mDriverDatabase =
                            firebaseDatabase.reference.child("Users").child("Drivers").child(key)
                        mDriverDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists() && snapshot.childrenCount > 0 && requestTrayek != null) {

                                    val driverMap: Map<String?, Any?>? = snapshot.getValue(gti)

                                    if (driverFound) {
                                        return
                                    }

                                    if (driverMap?.get("trayek") == requestTrayek) {
                                        driverFound = true
                                        driverFoundId = snapshot.key
                                        if (driverFoundId != null) {
                                            val driverRef =
                                                firebaseDatabase.reference.child("Users")
                                                    .child("Drivers")
                                                    .child(driverFoundId.toString())
                                                    .child("customerRequest")

                                            val customerId = firebaseAuth.currentUser?.uid
                                            val message =
                                                binding.customBackgroundLayoutCustomer.edtMessage.text.toString()

                                            val map = HashMap<String, Any>()
                                            // put customer ride id inside customer request
                                            if (customerId != null) {
                                                map["customerRideId"] = customerId
                                            }

                                            // put destination inside customer request
                                            if (destination != null) {
                                                map["destination"] = destination.toString()
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
                                                map["destinationLat"] = destinationLatLng!!.latitude
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

                                            // put message inside customer request
                                            if (message.isNotEmpty() && message != "") {
                                                map["message"] = message
                                            } else {
                                                map["message"] =
                                                    resources.getString(R.string.no_message)
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
                                    edtMessage.setText("")
                                } else {
                                    btnRequestAngkot.text =
                                        resources.getString(R.string.angkot_found)
                                    edtMessage.setText("")

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
                                                    binding.customBackgroundLayoutCustomer.btnRequestAngkot.apply {
                                                        isEnabled = false
                                                        text =
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
            tvNoOrders.visibility = View.VISIBLE
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

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val RADIUS = 10000.0
    }
}