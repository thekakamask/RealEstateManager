package com.dcac.realestatemanager.data.offlineStaticMap

import android.content.Context

interface StaticMapRepository {

    suspend fun getStaticMapImage(config: StaticMapConfig): ByteArray?
    fun saveStaticMapToLocal(context: Context, fileName: String, bytes: ByteArray): String?

}