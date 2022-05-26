package com.transportation.kotline.remote

import com.transportation.kotline.BuildConfig
import com.transportation.kotline.model.PushNotification
import com.transportation.kotline.other.Global
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface INotificationApi {
    @Headers("Authorization: key=${BuildConfig.SERVER_KEY}", "Content-Type:${Global.CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}