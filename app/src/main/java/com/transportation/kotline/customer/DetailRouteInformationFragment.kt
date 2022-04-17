package com.transportation.kotline.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.transportation.kotline.databinding.FragmentDetailRouteInformationBinding

class DetailRouteInformationFragment : Fragment() {

    private var _binding: FragmentDetailRouteInformationBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailRouteInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    val ngetemLocation = snapshot.child("ngetemLocation").value.toString()
                    val fares = snapshot.child("fares").value.toString()
                    val description = snapshot.child("descriptionTransport").value.toString()

                    binding.apply {
                        tvTrayek.text = StringBuilder().append("Angkot ").append(trayek)
                        tvNgetem.text = ngetemLocation
                        tvTarif.text = StringBuilder().append("Rp. ").append(fares)
                        tvDescription.text = description
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    companion object {
        const val TRAYEK_TYPE = "EXTRA_TRAYEK"
    }
}