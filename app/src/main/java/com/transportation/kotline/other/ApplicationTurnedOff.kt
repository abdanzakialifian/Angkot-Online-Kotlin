package com.transportation.kotline.other

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.firebase.geofire.GeoFire
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ApplicationTurnedOff : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val driverId = FirebaseAuth.getInstance().currentUser?.uid
        val driverAvailableRef = FirebaseDatabase.getInstance().getReference("DriversAvailable")
        val geoFireAvailable = GeoFire(driverAvailableRef)
        if (driverId != null) {
            geoFireAvailable.removeLocation(driverId)
        }
        val driverWorkingRef = FirebaseDatabase.getInstance().getReference("DriversWorking")
        val geoFireWorking = GeoFire(driverWorkingRef)
        if (driverId != null) {
            geoFireWorking.removeLocation(driverId)
        }

        val customerId = FirebaseAuth.getInstance().currentUser?.uid
        val customerPositionRef = FirebaseDatabase.getInstance().getReference("CustomersPosition")
        val geoFirePosition = GeoFire(customerPositionRef)
        if (customerId != null) {
            geoFirePosition.removeLocation(customerId)
        }
    }
}