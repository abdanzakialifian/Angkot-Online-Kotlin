package com.transportation.kotline.model

data class PushNotification(
    var data: Notification = Notification("", ""),
    var to: String = ""
)
