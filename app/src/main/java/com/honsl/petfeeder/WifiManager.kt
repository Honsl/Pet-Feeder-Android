package com.honsl.petfeeder

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class FeederInfo(val foodLevel1: String, val foodLevel2: String)
data class FeederLevel(val leftFeeder: Float, val rightFeeder: Float)

interface ApiService {
    @GET("feed")
    suspend fun feedFeeder(
        @Query("portion") portion: String,
    ): FeederInfo

    @GET("status")
    suspend fun getFeeder(): FeederLevel

    @POST("setSchedule")
    suspend fun setSchedule(
        @Body schedules: MutableList<Schedule>?
    ): Boolean
}

class WifiManager(val context: Context, val ip_URL: String) {
    var ipAddress: String = ""

    // Ensure base URL ends with a slash
    private val baseUrl = if (ip_URL.endsWith("/")) ip_URL else "$ip_URL/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }


    //  suspend fun UpdateFeeder(feeders: List<Feeder>): Boolean {
    //       apiService.getFeeder("test","123")
    //  return false;
    // }
}