package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import android.content.Context
import android.util.Log
import com.dcac.realestatemanager.network.StaticMapApiService
import com.dcac.realestatemanager.utils.Utils
import java.io.IOException

class StaticMapRemoteDataSource(
    private val apiService: StaticMapApiService
) {

    suspend fun getStaticMapImage(config: StaticMapConfig): ByteArray? {
        return try {
            val response = apiService.getStaticMapImage(
                center = config.center,
                zoom = config.zoom,
                size = config.size,
                mapType = config.mapType,
                markers = config.markers,
                style = config.styles
            )
            if (response.isSuccessful) {
                response.body()?.bytes()
            } else {
                Log.e(
                    "StaticMapRemoteDataSour",
                    "Failed response from API: code=${response.code()}, message=${response.message()}"
                )
                null
            }
        } catch (e: IOException) {
            Log.e("StaticMapRemoteDataSour", "Network error while calling Static Maps API", e)
            null
        }
    }

    // Save all bytes received into a PNG file in the internal storage
    fun saveStaticMapToLocal(context: Context, fileName: String, bytes: ByteArray): String? {
        return try {
            // Save bytes to internal storage and return a file:// Uri string
            Utils.saveBytesToAppStorage(
                context = context,
                bytes = bytes,
                subDir = "maps",
                fileName = fileName
            )?.toString()

        } catch (e: IOException) {
            Log.e("StaticMapRemoteDataSour", "Error saving static map locally", e)
            null
        } catch (e: Exception) {
            Log.e("StaticMapRemoteDataSour", "Unexpected error saving static map locally", e)
            null
        }
    }
}