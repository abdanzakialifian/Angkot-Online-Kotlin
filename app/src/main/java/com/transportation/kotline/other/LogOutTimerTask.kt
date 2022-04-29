package com.transportation.kotline.other

import com.firebase.geofire.GeoFire
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class LogOutTimerTask(
    private val googleSignInClient: GoogleSignInClient,
    private val userType: String
) : TimerTask() {
    override fun run() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        googleSignInClient.revokeAccess()
            .addOnCompleteListener {
                if (userType == "CustomersPosition") {
                    val customersPosition =
                        Firebase.database.getReference("CustomersPosition")
                    val geoFirePosition = GeoFire(customersPosition)
                    geoFirePosition.removeLocation(userId)

                    val customersDb = Firebase.database.reference.child("Users").child("Customers")
                        .child(userId.toString())
                    val checkLogin = HashMap<String, Any>()
                    checkLogin["login"] = false
                    customersDb.updateChildren(checkLogin)
                }

                if (userType == "DriversAvailable") {
                    val driversPosition =
                        Firebase.database.getReference("DriversAvailable")
                    val geoFirePosition = GeoFire(driversPosition)
                    geoFirePosition.removeLocation(userId)
                }
            }
    }
}