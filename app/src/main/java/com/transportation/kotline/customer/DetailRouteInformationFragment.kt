package com.transportation.kotline.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.R
import com.transportation.kotline.databinding.FragmentDetailRouteInformationBinding

class DetailRouteInformationFragment : Fragment() {

    private var _binding: FragmentDetailRouteInformationBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailRouteInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportMapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync { googleMap ->
            mMap = googleMap
        }

        firebaseDatabase = Firebase.database

        if (arguments != null) {
            val trayekType = arguments?.getString(TRAYEK_TYPE)
            if (trayekType != null) {
                getDataTrayek(trayekType)
            }
        }

        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun getDataTrayek(trayekType: String) {
        binding.progressBar.visibility = View.VISIBLE

        val trayekRef = firebaseDatabase.reference.child("Trayek").child(trayekType)
        trayekRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.progressBar.visibility = View.GONE
                    val trayek = snapshot.ref.key
                    val ngetemLatitude = snapshot.child("ngetemLatitude").value
                    val ngetemLongitude = snapshot.child("ngetemLongitude").value
                    val ngetemLocation = snapshot.child("ngetemLocation").value
                    val fares = snapshot.child("fares").value
                    val description = snapshot.child("descriptionTransport").value

                    binding.apply {
                        if (trayek != null) {
                            tvTrayek.text = StringBuilder().append("Angkot ").append(trayek)
                        } else {
                            tvTrayek.text = resources.getString(R.string.empty_route)
                        }

                        if (ngetemLatitude != null && ngetemLongitude != null) {
                            val ngetemLatLng = LatLng(
                                ngetemLatitude.toString().toDouble(),
                                ngetemLongitude.toString().toDouble()
                            )
                            val titleMarker =
                                StringBuilder().append("Ngetem Angkot ").append(trayek)

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(ngetemLatLng))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ngetemLatLng, 20F))
                            mMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        ngetemLatitude.toString().toDouble(),
                                        ngetemLongitude.toString().toDouble()
                                    )
                                ).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_ngetem))
                                    .title(titleMarker.toString())
                            )
                        } else {
                            mMap.addMarker(
                                MarkerOptions().position(
                                    LatLng(0.0, 0.0)
                                ).title("")
                            )
                        }

                        if (ngetemLocation != null) {
                            tvNgetem.text = ngetemLocation.toString()
                        } else {
                            tvNgetem.text = resources.getString(R.string.empty_location)
                        }

                        if (fares != null) {
                            tvTarif.text = StringBuilder().append("Rp. ").append(fares)
                        } else {
                            tvTarif.text = resources.getString(R.string.empty_fares)
                        }

                        if (description != null) {
                            tvDescription.text = description.toString()
                        } else {
                            tvDescription.text = resources.getString(R.string.empty_description)
                        }
                    }
                } else {
                    binding.apply {
                        progressBar.visibility = View.GONE
                        tvTrayek.text = resources.getString(R.string.empty_route)
                        tvNgetem.text = resources.getString(R.string.empty_location)
                        tvTarif.text = resources.getString(R.string.empty_fares)
                        tvDescription.text = resources.getString(R.string.empty_description)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    companion object {
        const val TRAYEK_TYPE = "EXTRA_TRAYEK"
    }
}