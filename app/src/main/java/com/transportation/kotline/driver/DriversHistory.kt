package com.transportation.kotline.driver

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DriversHistory(
    var requestId: String = "",
    var customerId: String = "",
    var driverId: String = "",
    var customerName: String = "",
    var customerImage: String = "",
    var customerPhone: String = "",
    var locationLat: Double = 0.0,
    var locationLng: Double = 0.0,
    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,
    var customerDestination: String = "",
    var destinationAddress: String = "",
    var time: Long = 0L,
    var rating: Float = 0F
) : Parcelable
