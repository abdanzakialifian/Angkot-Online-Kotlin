package com.transportation.kotline.customer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomersHistory(
    var requestId: String = "",
    var driverId: String = "",
    var driverName: String = "",
    var driverImage: String = "",
    var driverPhone: String = "",
    var locationLat: Double = 0.0,
    var locationLng: Double = 0.0,
    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,
    var destination: String = "",
    var destinationAddress: String = "",
    var time: Long = 0L
) : Parcelable
