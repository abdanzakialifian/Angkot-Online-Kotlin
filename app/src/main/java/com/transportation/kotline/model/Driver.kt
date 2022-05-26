package com.transportation.kotline.model

data class Driver(
    var name: String = "",
    var email: String = "",
    var verification: Boolean = false,
    var deviceToken: String = ""
)
