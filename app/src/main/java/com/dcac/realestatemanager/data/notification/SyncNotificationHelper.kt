package com.dcac.realestatemanager.data.notification

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dcac.realestatemanager.R

class SyncNotificationHelper(
    private val context: Context
) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showPropertyInsertedNotification(
        title: String,
        agentName: String
    ) {
        showNotification(
            title = "New property downloaded",
            content = "$title managed by $agentName has been downloaded"
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showPropertyUpdatedNotification(
        title: String,
        agentName: String
    ) {
        showNotification(
            title = "Property updated",
            content = "$title managed by $agentName has been updated"
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        title: String,
        content: String
    ) {
        val notification = NotificationCompat.Builder(
            context,
            "sync_channel"
        )
            .setSmallIcon(R.drawable.real_estate_manager_logo)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                notification
            )
    }
}