package com.dcac.realestatemanager.network

import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.ResponseBody
import retrofit2.Response
import com.dcac.realestatemanager.BuildConfig

interface StaticMapApiService {
    @GET("staticmap")
    suspend fun getStaticMapImage(
        @Query("center") center: String,
        @Query("zoom") zoom: Int,
        @Query("size") size: String,
        @Query("maptype") mapType: String = "roadmap",
        @Query("markers") markers: List<String>,
        @Query("style") style: List<String>,
        @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
    ): Response<ResponseBody>
}