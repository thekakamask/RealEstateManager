package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import android.content.Context

interface StaticMapDataSource {

    suspend fun getStaticMapImage(config: StaticMapConfig): ByteArray?
    fun saveStaticMapToLocal(context: Context, fileName: String, bytes: ByteArray): String?

}