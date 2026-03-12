package com.dcac.realestatemanager.fakeData.fakeApiService

import com.dcac.realestatemanager.network.StaticMapApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Response

class FakeStaticMapApiService : StaticMapApiService {

    var shouldSucceed: Boolean = true
    var returnedBody: String = "fake_static_map_image_binary"

    override suspend fun getStaticMapImage(
        center: String,
        zoom: Int,
        size: String,
        mapType: String,
        markers: List<String>,
        style: List<String>,
        apiKey: String
    ): Response<ResponseBody> {

        return if (shouldSucceed) {

            val body = ResponseBody.create(
                "image/png".toMediaType(),
                returnedBody
            )

            Response.success(body)

        } else {

            Response.error(
                500,
                ResponseBody.create(
                    "text/plain".toMediaType(),
                    "Fake API error"
                )
            )
        }
    }
}