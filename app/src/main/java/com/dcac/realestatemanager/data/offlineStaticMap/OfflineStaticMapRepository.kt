package com.dcac.realestatemanager.data.offlineStaticMap

import android.content.Context
import android.util.Log
import com.dcac.realestatemanager.network.StaticMapApiService
import java.io.File
import java.io.IOException

class OfflineStaticMapRepository(
    private val staticMapApiService: StaticMapApiService
): StaticMapRepository {

    // Call API retrofit to get map image
    override suspend fun getStaticMapImage(config: StaticMapConfig): ByteArray? {
        return try {
            val response = staticMapApiService.getStaticMapImage(
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
                    "StaticMapRepository",
                    "Failed response from API: code=${response.code()}, message=${response.message()}"
                )
                null
            }
        } catch (e: IOException) {
            Log.e("StaticMapRepository", "Network error while calling Static Maps API", e)
            null
        }
    }

    // Save all bytes received into a PNG file in the internal storage
    override fun saveStaticMapToLocal(context: Context, fileName: String, bytes: ByteArray): String? {
        return try {
            val mapsDir = File(context.filesDir, "maps")
            if (!mapsDir.exists()) mapsDir.mkdirs()

            val file = File(mapsDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: IOException) {
            Log.e("StaticMapRepository", "Error saving static map locally", e)
            null
        }
    }

}